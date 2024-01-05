package edu.rit.se.design.dodo.utils.wala;

import com.ibm.wala.types.Descriptor;
import edu.rit.se.design.dodo.utils.wala.MethodDescriptorsLoader.ClassType;

/**
 * It has the metadata for the list of project-specific entrypoints.
 * @author Omar Dajani
 */
public class EntrypointDescriptor {

    // class that the method belongs to
    private final String memberOf;
    // method's name
    private final String name;
    // descriptor has the parameter's types and method's return type
    private final Descriptor descriptor;
    // whether the method is from an Interface or an actual class
    private final ClassType classType;

    /**
     *
     * @param memberOf the class/interface that the method belongs to
     * @param methodName method's name
     * @param descriptor a descriptor that has the parameter's types and method's return type
     * @param classType whether the method is from an Interface or an actual class
     */
    public EntrypointDescriptor(String memberOf, String methodName, Descriptor descriptor, ClassType classType) {
        this.descriptor = descriptor;
        this.memberOf = memberOf;
        this.name = methodName;
        this.classType = classType;

    }

    public String getName() {
        return this.name;
    }

    public String getMemberOf() {
        return this.memberOf;
    }

    public Descriptor getDescriptor() {
        return this.descriptor;
    }

    public ClassType getClassType() {
        return this.classType;
    }

    @Override
    public String toString() {
        return String.format("<(%s) %s,%s%s>", classType, memberOf, name, descriptor);
    }

}
