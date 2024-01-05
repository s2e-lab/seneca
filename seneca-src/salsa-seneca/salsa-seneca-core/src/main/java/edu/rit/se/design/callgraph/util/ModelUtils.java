package edu.rit.se.design.callgraph.util;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;

import java.util.LinkedList;
import java.util.List;

import static edu.rit.se.design.callgraph.util.NameUtils.readObjectNoDataCallbackSelector;
import static edu.rit.se.design.callgraph.util.NameUtils.validateObjectCallbackSelector;

/**
 * Utilities for abstracting {@link java.io.ObjectInputStream} and {@link java.io.ObjectOutputStream} classes.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class ModelUtils {


    public static List<IMethod> getDeserializationCallbacks(IClass klass) {
        List<IMethod> cbMethods = new LinkedList<>();

        // if class implements the readObject() callback
        IMethod readObjectCallbackMethod = klass.getMethod(NameUtils.readObjectCallbackSelector);
        if (readObjectCallbackMethod != null && readObjectCallbackMethod.getDeclaringClass().equals(klass))
            cbMethods.add(readObjectCallbackMethod);

        // if class implements the readObjectNoData() callback
        IMethod readObjectNoDataCallbackMethod = klass.getMethod(readObjectNoDataCallbackSelector);
        if (readObjectNoDataCallbackMethod != null && readObjectNoDataCallbackMethod.getDeclaringClass().equals(klass))
            cbMethods.add(readObjectNoDataCallbackMethod);

        // if class implements the readResolve() callback
        IMethod readResolveCallbackMethod = klass.getMethod(NameUtils.readResolveCallbackSelector);
        if (readResolveCallbackMethod != null && readResolveCallbackMethod.getDeclaringClass().equals(klass))
            cbMethods.add(readResolveCallbackMethod);

        // if class implements the validateObject() callback
        IMethod validateObjectCallbackMethod = klass.getMethod(validateObjectCallbackSelector);
        if (validateObjectCallbackMethod != null && validateObjectCallbackMethod.getDeclaringClass().equals(klass))
            cbMethods.add(validateObjectCallbackMethod);

        return cbMethods;
    }
}
