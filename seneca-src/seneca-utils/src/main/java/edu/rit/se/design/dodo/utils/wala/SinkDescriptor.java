package edu.rit.se.design.dodo.utils.wala;

import com.ibm.wala.types.MethodReference;

import java.util.Arrays;

/**
 * This class represents a sink method (loaded from a configuration CSV).
 *
 * @author Joanna C. S. Santos <jds5109@rit.edu>
 */
public class SinkDescriptor {

    // it is used to indicate what are the parameter(s) who are dangerous 
    // (an index = 0, means the receiver object is the dangerous one - i.e., obj.sink() --> obj is the dangerous)
    private final int[] dangerousIndexRange;

    // the specific field in the object that is problematic
    private final String[] dangerousFields;

    // the method reference of this object
    private final MethodReference methodRef;
    
    public SinkDescriptor(MethodReference methodRef, int[] dangerousIndexRange, String[] dangerousFields) {
        this.methodRef = methodRef;
        this.dangerousIndexRange = dangerousIndexRange;
        this.dangerousFields = dangerousFields;
    }
    
    public int[] getDangerousIndexRange() {
        return dangerousIndexRange;
    }
    
    public MethodReference getMethodRef() {
        return methodRef;
    }
    
    @Override
    public String toString() {
        return methodRef.getSignature() + " // dangerousRange " + Arrays.toString(dangerousIndexRange);
    }

//    public boolean equalsTo(MethodReference method) {
//        Descriptor descriptor = methodRef.getDescriptor();
//        System.out.println(methodRef.getSignature());
//        return false;
//    }
    public String[] getDangerousFields() {
        return dangerousFields;
    }
    
}
