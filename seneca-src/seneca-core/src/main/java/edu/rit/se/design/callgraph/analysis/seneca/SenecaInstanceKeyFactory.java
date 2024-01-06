package edu.rit.se.design.callgraph.analysis.seneca;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.classLoader.ProgramCounter;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKeyFactory;
import com.ibm.wala.types.TypeReference;
import edu.rit.se.design.callgraph.model.MethodModel;


public class SenecaInstanceKeyFactory implements InstanceKeyFactory {

    private ITaintedCallGraphBuilder builder;
    private InstanceKeyFactory A;
    private InstanceKeyFactory B;

    public SenecaInstanceKeyFactory(
            ITaintedCallGraphBuilder builder,
            InstanceKeyFactory A,
            InstanceKeyFactory B) {

        if (A == null)
            throw new IllegalArgumentException("Untainted ContextInterpreter for ContextInterpreter delegation is null");
        if (B == null)
            throw new IllegalArgumentException("Tainted ContextInterpreter for ContextInterpreter delegation is null");
        if (builder == null)
            throw new IllegalArgumentException("Builder for ContextInterpreter delegation is null");

        this.builder = builder;
        this.A = A;
        this.B = B;
    }

    @Override
    public InstanceKey getInstanceKeyForAllocation(CGNode node, NewSiteReference allocation) {
        if (!builder.isTainted(node))
            return A.getInstanceKeyForAllocation(node, allocation);

        // delegates to taint policy
        InstanceKey instanceKeyForAllocation = B.getInstanceKeyForAllocation(node, allocation);
        // handles an exceptional scenario: we are going to invoke the callback from an abstract class
        if(instanceKeyForAllocation == null && node.getMethod() instanceof MethodModel){
            IClass abstractClass = node.getClassHierarchy().lookupClass( allocation.getDeclaredType());
            if (abstractClass.isAbstract() && !abstractClass.isInterface()) {
                instanceKeyForAllocation = new ConcreteTypeKey(abstractClass);
            }
        }
        return instanceKeyForAllocation;
    }

    @Override
    public InstanceKey getInstanceKeyForMultiNewArray(CGNode node, NewSiteReference allocation, int dim) {
        if (!builder.isTainted(node))
            return A.getInstanceKeyForMultiNewArray(node, allocation, dim);

        return B.getInstanceKeyForMultiNewArray(node, allocation, dim);
    }

    @Override
    public <T> InstanceKey getInstanceKeyForConstant(TypeReference type, T S) {
        // Currently the primary InstanceKeyFactory is used,
        // since there is no way to determine which one to use
        return A.getInstanceKeyForConstant(type, S);
    }

    @Override
    public InstanceKey getInstanceKeyForPEI(CGNode node, ProgramCounter instr, TypeReference type) {
        if (!builder.isTainted(node))
            return A.getInstanceKeyForPEI(node, instr, type);

        return B.getInstanceKeyForPEI(node, instr, type);
    }

    @Override
    public InstanceKey getInstanceKeyForMetadataObject(Object obj, TypeReference objType) {
        // Currently the primary InstanceKeyFactory is used,
        // since there is no way to determine which one to use
        return A.getInstanceKeyForMetadataObject(obj, objType);
    }

    /**
     * This method is needed for making ZeroXContainer ContextSelector. It only accepts ZeroXCFA,
     * so this class cannot be used and one of its InstanceKeyFactorys must directly be used.
     *
     * @return
     */
    public InstanceKeyFactory getPrimaryInstanceKeyFactory() {
        return A;
    }

    /**
     * This method is needed for making ZeroXContainer ContextSelector. It only accepts ZeroXCFA,
     * so this class cannot be used and one of its InstanceKeyFactorys must directly be used.
     *
     * @return
     */
    public InstanceKeyFactory getSecondaryInstanceKeyFactory() {
        return B;
    }
}
