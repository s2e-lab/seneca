package edu.rit.se.design.callgraph.model;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.collections.HashSetFactory;

import static edu.rit.se.design.callgraph.util.NameUtils.defaultReadObjectSelector;
import static edu.rit.se.design.callgraph.util.NameUtils.readObjectSelector;

public class ObjectInputStreamModel extends AbstractClassModel {


    public ObjectInputStreamModel(IClassHierarchy cha, AnalysisOptions options, IAnalysisCacheView cache) {
        super(cha, options, cache, "ObjectInputStream");
    }

    @Override
    protected void initSyntheticMethods() {
        try {
            IMethod actualReadObjectMethod = this.originalClass.getMethod(readObjectSelector);

            this.methods = HashSetFactory.make();
            this.methods.add(
                    new MethodModel(
                            readObjectSelector,
                            actualReadObjectMethod.getDeclaredExceptions(),
                            this,
                            this.originalClass.getClassHierarchy(),
                            options,
                            cache)
            );
            this.methods.add(
                    new MethodModel(
                            defaultReadObjectSelector,
                            this.originalClass.getMethod(defaultReadObjectSelector).getDeclaredExceptions(),
                            this,
                            this.originalClass.getClassHierarchy(),
                            options,
                            cache)
            );
        } catch (InvalidClassFileException e) {
            throw new IllegalStateException(e);
        }
    }
}