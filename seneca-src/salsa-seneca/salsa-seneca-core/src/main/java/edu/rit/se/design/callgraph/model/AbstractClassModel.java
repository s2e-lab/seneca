package edu.rit.se.design.callgraph.model;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.SyntheticClass;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.strings.Atom;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.ibm.wala.types.ClassLoaderReference.Primordial;

/**
 * Abstraction to create classes' models (synthetic classes).
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public abstract class AbstractClassModel extends SyntheticClass {

    protected static final String PREFIX = "Lsalsa/model/";

    /**
     * Original class being modeled. Used to delegate methods (isPublic, modifiers, etc)
     */
    protected final IClass originalClass;

    /**
     * synthetic (model) methods
     */
    protected Set<IMethod> methods;
    /**
     * To be used by models for caching / performing analysis as expected
     */
    protected AnalysisOptions options;
    protected IAnalysisCacheView cache;


    public AbstractClassModel(IClassHierarchy cha, AnalysisOptions options, IAnalysisCacheView cache, String originalClassName) {
        super(getSyntheticTypeRef(originalClassName), cha);
        this.options = options;
        this.cache = cache;
        this.originalClass = cha.lookupClass(TypeReference.findOrCreate(Primordial, "Ljava/io/" + originalClassName));
        if (this.originalClass == null)
            throw new IllegalStateException(String.format("Could not find Ljava/io/%s in the class hierarchy", originalClassName));
        initSyntheticMethods();

        // add to the class hierarchy to prevent "class not found" errors when adding edges to synthetic methods
        cha.addClass(this);
    }

    protected static TypeReference getSyntheticTypeRef(String originalClassName) {
        return TypeReference.findOrCreate(Primordial, PREFIX + originalClassName);
    }

    protected abstract void initSyntheticMethods();

    /**
     * @return true iff this class is public
     */
    @Override
    public boolean isPublic() {
        return this.originalClass.isPublic();
    }

    /**
     * @return true iff this class is private
     */
    @Override
    public boolean isPrivate() {
        return this.originalClass.isPrivate();
    }

    /**
     * Return the integer that encodes the class's modifiers, as defined by the JVM specification
     *
     * @return the integer that encodes the class's modifiers, as defined by the JVM specification
     */
    @Override
    public int getModifiers() throws UnsupportedOperationException {
        return this.originalClass.getModifiers();
    }

    /**
     * @return the superclass, or null if java.lang.Object
     * @throws IllegalStateException if there's some problem determining the superclass
     */
    @Override
    public IClass getSuperclass() {
        return this.originalClass.getSuperclass();
    }

    /**
     * @return Collection of (IClass) interfaces this class directly implements. If this class is an
     * interface, returns the interfaces it immediately extends.
     */
    @Override
    public Collection<? extends IClass> getDirectInterfaces() {
        return this.originalClass.getDirectInterfaces();
    }

    /**
     * @return Collection of (IClass) interfaces this class implements, including all ancestors of
     * interfaces immediately implemented. If this class is an interface, it returns all
     * super-interfaces.
     */
    @Override
    public Collection<IClass> getAllImplementedInterfaces() {
        return this.originalClass.getAllImplementedInterfaces();
    }

    /**
     * Finds method matching signature. Delegates to superclass if not found.
     *
     * @param selector a method signature
     * @return IMethod from this class matching the signature; null if not found in this class or any
     * superclass.
     */
    @Override
    public IMethod getMethod(Selector selector) {
        return methods.stream()
                .filter(m -> m.getSelector().equals(selector))
                .findFirst()
                .orElse(null);
    }
//<editor-fold desc="Fields information">

    /**
     * Finds a field.
     *
     * @param name
     * @throws IllegalStateException if the class contains multiple fields with name {@code name}.
     */
    @Override
    public IField getField(Atom name) {
        return null; // this model has no fields TODO maybe it will have some fields for the sake of modeling
    }

//</editor-fold>

    /**
     * @return the method that is this class's initializer, or null if none
     */
    @Override
    public IMethod getClassInitializer() {
        return null; // no class initializer TODO maybe add something if needed
    }

    /**
     * @return an Iterator of the IMethods declared by this class.
     */
    @Override
    public Collection<? extends IMethod> getDeclaredMethods() {
        return Collections.unmodifiableCollection(methods);
    }

    /**
     * Compute the instance fields declared by this class or any of its superclasses.
     */
    @Override
    public Collection<IField> getAllInstanceFields() {
        return Collections.emptySet(); // model has no non-static (instance) fields
    }

    /**
     * Compute the static fields declared by this class or any of its superclasses.
     */
    @Override
    public Collection<IField> getAllStaticFields() {
        return Collections.emptySet(); // model has no static fields
    }

    /**
     * Compute the instance and static fields declared by this class or any of its superclasses.
     */
    @Override
    public Collection<IField> getAllFields() {
        return Collections.emptySet(); // model has no fields, and we assume no fields on superclasses
    }

    /**
     * Compute the methods declared by this class or any of its superclasses.
     */
    @Override
    public Collection<? extends IMethod> getAllMethods() {
        return getDeclaredMethods(); //FIXME? should this be delegated?
    }

    /**
     * Compute the instance fields declared by this class.
     *
     * @return Collection of IFields
     */
    @Override
    public Collection<IField> getDeclaredInstanceFields() {
        return Collections.emptySet();
    }

    /**
     * @return Collection of IField
     */
    @Override
    public Collection<IField> getDeclaredStaticFields() {
        return Collections.emptySet();
    }

    /**
     * Does 'this' refer to a reference type? If not, then it refers to a primitive type.
     */
    @Override
    public boolean isReferenceType() {
        return getReference().isReferenceType();
    }

}
