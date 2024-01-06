package edu.rit.se.design.callgraph.analysis.salsa;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.classLoader.ProgramCounter;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKeyFactory;
import com.ibm.wala.types.TypeReference;
import edu.rit.se.design.callgraph.model.MethodModel;


/**
 * @author Reese A. Jones (raj8065@rit.edu)
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class SalsaDelegatingInstanceKeyFactory implements InstanceKeyFactory {

    private SalsaCallGraphBuilder builder;
    private InstanceKeyFactory normalFactory;
    private InstanceKeyFactory modelNodesFactory;

    public SalsaDelegatingInstanceKeyFactory(SalsaCallGraphBuilder builder, InstanceKeyFactory normalFactory, InstanceKeyFactory modelNodesFactory) {

        if (normalFactory == null)
            throw new IllegalArgumentException("normalFactory for InstanceKeyFactory delegation is null");
        if (modelNodesFactory == null)
            throw new IllegalArgumentException("modelNodesFactory for InstanceKeyFactory delegation is null");
        if (builder == null)
            throw new IllegalArgumentException("Builder for InstanceKeyFactory delegation is null");

        this.builder = builder;
        this.normalFactory = normalFactory;
        this.modelNodesFactory = modelNodesFactory;
    }

    @Override
    public InstanceKey getInstanceKeyForAllocation(CGNode node, NewSiteReference allocation) {
        if (!builder.isSyntheticModel(node))
            return normalFactory.getInstanceKeyForAllocation(node, allocation);


        // delegates to taint policy
        InstanceKey instanceKeyForAllocation = modelNodesFactory.getInstanceKeyForAllocation(node, allocation);
        // handles an exceptional scenario: we are going to invoke the callback from an abstract class
        if (instanceKeyForAllocation == null && node.getMethod() instanceof MethodModel) {
            IClass abstractClass = node.getClassHierarchy().lookupClass(allocation.getDeclaredType());
            if (abstractClass.isAbstract() && !abstractClass.isInterface()) {
                instanceKeyForAllocation = new ConcreteTypeKey(abstractClass);
            }
        }
        return instanceKeyForAllocation;
    }

    @Override
    public InstanceKey getInstanceKeyForMultiNewArray(CGNode node, NewSiteReference allocation, int dim) {
        if (!builder.isSyntheticModel(node))
            return normalFactory.getInstanceKeyForMultiNewArray(node, allocation, dim);

        return modelNodesFactory.getInstanceKeyForMultiNewArray(node, allocation, dim);
    }

    @Override
    public <T> InstanceKey getInstanceKeyForConstant(TypeReference type, T S) {
        // Currently the primary InstanceKeyFactory is used,
        // since there is no way to determine which one to use
        return normalFactory.getInstanceKeyForConstant(type, S);
    }

    @Override
    public InstanceKey getInstanceKeyForPEI(CGNode node, ProgramCounter instr, TypeReference type) {
        if (!builder.isSyntheticModel(node))
            return normalFactory.getInstanceKeyForPEI(node, instr, type);

        return modelNodesFactory.getInstanceKeyForPEI(node, instr, type);
    }

    @Override
    public InstanceKey getInstanceKeyForMetadataObject(Object obj, TypeReference objType) {
        // Currently the primary InstanceKeyFactory is used,
        // since there is no way to determine which one to use
        return normalFactory.getInstanceKeyForMetadataObject(obj, objType);
    }

    /**
     * This method is needed for making ZeroXContainer ContextSelector. It only accepts ZeroXCFA,
     * so this class cannot be used and one of its InstanceKeyFactories must directly be used.
     *
     * @return
     */
    public InstanceKeyFactory getPrimaryInstanceKeyFactory() {
        return normalFactory;
    }

    /**
     * This method is needed for making ZeroXContainer ContextSelector. It only accepts ZeroXCFA,
     * so this class cannot be used and one of its InstanceKeyFactories must directly be used.
     *
     * @return
     */
    public InstanceKeyFactory getSecondaryInstanceKeyFactory() {
        return modelNodesFactory;
    }
}
