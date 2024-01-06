package edu.rit.se.design.callgraph.analysis;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.MonitorUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for the concrete implementations for how serialization should be handled in terms of points-to analysis.
 *
 * @author Joanna C. S. Santos
 */
public abstract class AbstractSerializationHandler {
    protected final SSAPropagationCallGraphBuilder builder;
    //    protected final Set<IClass> serializableClasses;
    protected final Map<ClassLoaderReference, List<IClass>> serializableClasses;

    public AbstractSerializationHandler(SSAPropagationCallGraphBuilder builder) {
        this.builder = builder;
        this.serializableClasses = computeSerializableClasses();
    }

    public abstract void handleSerializationRelatedFeatures(MonitorUtil.IProgressMonitor monitor);

    /**
     * Computes the set of serializable classes in the class hierarchy.
     *
     * @return a set of classes that implements the serializable interface (directly or indirectly)
     */

    private Map<ClassLoaderReference, List<IClass>> computeSerializableClasses() {
        IClassHierarchy cha = builder.getClassHierarchy();
        Map<ClassLoaderReference, List<IClass>> classes = new HashMap<>();
        // empty list for each classloader
        for (IClassLoader loader : cha.getLoaders()) classes.put(loader.getReference(), new ArrayList<>());


        // iterates all classes in the classpath to compute what is serializable or not
        IClass serialInterface = cha.lookupClass(TypeReference.JavaIoSerializable);
        for (IClass c : cha) {
            if (cha.implementsInterface(c, serialInterface))
                classes.get(c.getClassLoader().getReference()).add(c);
        }
        return classes;
    }
}
