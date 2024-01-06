package edu.rit.se.design.callgraph.util;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.Constants;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;

import java.util.*;
import java.util.stream.Collectors;

import static com.ibm.wala.types.ClassLoaderReference.Primordial;
import static com.ibm.wala.types.TypeName.string2TypeName;
import static com.ibm.wala.types.TypeReference.JavaUtilMap;
import static com.ibm.wala.types.TypeReference.JavaUtilSet;
import static edu.rit.se.design.dodo.utils.wala.WalaUtils.isApplicationScope;


/**
 * Utilities for aiding the inference of possible allocations for fields within deserialized objects.
 *
 * @author Joanna C. S. Santos - jds5109@rit.edu
 */
public class SerializationUtils {


    public static final TypeReference JavaUtilList = TypeReference.findOrCreate(Primordial, string2TypeName("Ljava/util/List"));
    public static final TypeReference JavaUtilArrayList = TypeReference.findOrCreate(Primordial, string2TypeName("Ljava/util/ArrayList"));
    public static final TypeReference JavaUtilHashMap = TypeReference.findOrCreate(Primordial, string2TypeName("Ljava/util/HashMap"));


    /**
     * Determines the category of a {@link TypeReference} as listed in {@link TypeCategory}.
     *
     * @param cha           class hierarchy
     * @param typeReference the type under analysis
     * @return the type of the field as indicated in {@link TypeCategory}.
     */
    public static TypeCategory getTypeCategory(IClassHierarchy cha, TypeReference typeReference) {
        // field is primitive
        if (typeReference.isPrimitiveType())
            return TypeCategory.PRIMITIVE;

        // field is an array
        if (typeReference.isArrayType())
            return TypeCategory.ARRAY;

        IClass fieldClass = cha.lookupClass(typeReference);

        if (fieldClass != null) {
            // field is a java.util.List
            IClass listClass = cha.lookupClass(JavaUtilList);
            if (cha.isAssignableFrom(listClass, fieldClass))
                return TypeCategory.LIST;

            // field is a java.util.Set
            IClass setClass = cha.lookupClass(JavaUtilSet);
            if (cha.isAssignableFrom(setClass, fieldClass))
                return TypeCategory.SET;

            // field is a java.util.Map
            IClass mapClass = cha.lookupClass(JavaUtilMap);
            if (cha.isAssignableFrom(mapClass, fieldClass))
                return TypeCategory.MAP;

            // if we reach this point, it was not any of the collection types above, so we just return
            return TypeCategory.OBJECT;
        }

        return TypeCategory.IGNORED;
    }


    /**
     * Compute the set of possible types for a given <pre>object</pre>.
     *
     * @param cha                 class hierarchy.
     * @param declaringClass      the class where the object was declared.
     * @param type                the static type of the object.
     * @param serializableClasses set of classes in the project that are serializable
     * @param prune               whether we should prune or not upon large sets of possible types
     * @param pruningThreshold    the size tolerated; above this threshold the set is trimmed to include application-only classes
     * @return
     */
    public static Set<IClass> computePossibleTypes(IClassHierarchy cha, IClass declaringClass, IClass type, Map<ClassLoaderReference, List<IClass>> serializableClasses, boolean prune, int pruningThreshold) {
        if (type == null) return Collections.emptySet();
        TypeCategory typeCategory = getTypeCategory(cha, type.getReference());

        switch (typeCategory) {
            // three cases below are collection types
            case LIST:
//                return new HashSet<>(Arrays.asList(cha.lookupClass(JavaUtilArrayList)));
            case SET:
//                return new HashSet<>(Arrays.asList(cha.lookupClass(JavaUtilHashSet)));
            case MAP:
//                return new HashSet<>(Arrays.asList(cha.lookupClass(JavaUtilHashMap)));
                // for all other cases that the type is not a collection
            case OBJECT:
            case ARRAY:

                IClass fieldClassType = typeCategory == TypeCategory.ARRAY ?
                        cha.lookupClass(type.getReference().getInnermostElementType()) : // if is array, the concrete type is based on T[]
                        type;


                Set<IClass> concreteTypes = new HashSet<>();
                // compute all concrete types that are accessible  to the declaring class and type safe to the field
                for (List<IClass> classes : serializableClasses.values()) {
                    classes.stream().filter(s -> type != null &&
                                    !s.isInterface() && !s.isAbstract() &&
                                    isAccessible(declaringClass, s) &&
                                    isTypeSafe(cha, fieldClassType, s))
                            .forEach(concreteTypes::add);
                }


                // prune away primordial classes if the computed sets are fairly large
                if (prune && concreteTypes.size() > pruningThreshold) {
                    concreteTypes = serializableClasses.get(ClassLoaderReference.Application).stream().collect(Collectors.toSet());
                }
                return concreteTypes;
        }

        throw new UnsupportedOperationException("We only support the computation of possible set for " + Arrays.toString(TypeCategory.values()));

    }


    /**
     * Is the sClass accessible to fieldClass?
     *
     * @param fieldClass the class that declares the field
     * @param sClass     possible concrete type (a class to be tested upon)
     * @return true if the sClass is accessible to fieldClass (false otherwise).
     */
    public static boolean isAccessible(IClass fieldClass, IClass sClass) {

        // one of the three conditions below has to hold

        // (1) class is public
        if (sClass.isPublic()) return true;

        // (2) class is package accessible and they are within the same package
        boolean isPackageAccessible = ((sClass.getModifiers() & Constants.ACC_PUBLIC) == 0) && ((sClass.getModifiers() & Constants.ACC_PRIVATE) == 0) && ((sClass.getModifiers() & Constants.ACC_PROTECTED) == 0);
        boolean isInSamePackage = Objects.equals(sClass.getName().getPackage(), fieldClass.getName().getPackage());
        if (isPackageAccessible && isInSamePackage) return true;


        // (3) class is protected, wrapped in another class T, and the fieldClass extends T
        boolean isProtected = ((sClass.getModifiers() & Constants.ACC_PROTECTED) != 0);
        // TODO
        // IClass wrapperClass = ?;
        // boolean isChildren = cha.isSubclassOf(fieldClass, wrapperClass);

        return false;

    }

    /**
     * Does an expression fieldClass x := sClass y typecheck?
     *
     * @param cha        class hierarchy
     * @param fieldClass the static type of the field under analysis
     * @param sClass     possible concrete type
     * @return true if sClass a subtype of fieldClass; false otherwise.
     */
    public static boolean isTypeSafe(IClassHierarchy cha, IClass fieldClass, IClass sClass) {
        return fieldClass != null && sClass != null && cha.isAssignableFrom(fieldClass, sClass);
    }


}
