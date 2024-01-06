package edu.rit.se.design.callgraph.model;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.collections.HashSetFactory;

import static edu.rit.se.design.callgraph.util.NameUtils.writeObjectSelector;

public class ObjectOutputStreamModel extends AbstractClassModel {
    public ObjectOutputStreamModel(IClassHierarchy cha, AnalysisOptions options, IAnalysisCacheView cache) {
        super(cha, options, cache, "ObjectOutputStream");
    }

    @Override
    protected void initSyntheticMethods() {
        // inits synthetic method models
        try {
            this.methods = HashSetFactory.make();
            IMethod actualWriteObjectMethod = this.originalClass.getMethod(writeObjectSelector);
            IClassHierarchy cha = originalClass.getClassHierarchy();
            this.methods.add(new MethodModel(writeObjectSelector, actualWriteObjectMethod.getDeclaredExceptions(), this, cha, options, cache));
        } catch (InvalidClassFileException e) {
            throw new IllegalStateException(e);
        }
    }
}
