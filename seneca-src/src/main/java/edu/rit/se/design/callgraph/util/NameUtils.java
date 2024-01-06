package edu.rit.se.design.callgraph.util;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.strings.Atom;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ibm.wala.types.ClassLoaderReference.Primordial;

/**
 * This class has selectors and type references useful for finding callback methods and creating synthetic methods.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class NameUtils {
    // used for finding serialization and deserialization points
    public static TypeReference JavaIoObjectInputStream = TypeReference.findOrCreate(Primordial, "Ljava/io/ObjectInputStream");
    public static TypeReference JavaIoObjectOutputStream = TypeReference.findOrCreate(Primordial, "Ljava/io/ObjectOutputStream");
    public static Selector readObjectSelector = new Selector(Atom.findOrCreateAsciiAtom("readObject"), Descriptor.findOrCreateUTF8("()Ljava/lang/Object;"));
    public static Selector writeObjectSelector = new Selector(Atom.findOrCreateAsciiAtom("writeObject"), Descriptor.findOrCreateUTF8("(Ljava/lang/Object;)V"));
    // used for finding callback methods
    public static Selector writeObjectCallbackSelector = new Selector(Atom.findOrCreateAsciiAtom("writeObject"), Descriptor.findOrCreateUTF8("(Ljava/io/ObjectOutputStream;)V"));
    public static Selector writeReplaceCallbackSelector = new Selector(Atom.findOrCreateAsciiAtom("writeReplace"), Descriptor.findOrCreateUTF8("()Ljava/lang/Object;"));
    // deserialization callback methods
    public static Selector readObjectCallbackSelector = new Selector(Atom.findOrCreateAsciiAtom("readObject"), Descriptor.findOrCreateUTF8("(Ljava/io/ObjectInputStream;)V"));
    public static Selector readObjectNoDataCallbackSelector = new Selector(Atom.findOrCreateAsciiAtom("readObjectNoData"), Descriptor.findOrCreateUTF8("()V"));
    public static Selector readResolveCallbackSelector = new Selector(Atom.findOrCreateAsciiAtom("readResolve"), Descriptor.findOrCreateUTF8("()Ljava/lang/Object;"));
    public static Selector validateObjectCallbackSelector = new Selector(Atom.findOrCreateAsciiAtom("validateObject"), Descriptor.findOrCreateUTF8("()V"));

    // methods from the java.lang.reflect.Proxy interface
    public static Selector proxyInvokeSelector = new Selector(Atom.findOrCreateAsciiAtom("invoke"), Descriptor.findOrCreateUTF8("(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;"));


    // non-magic methods from ObjectInputStream
    public static Selector defaultReadObjectSelector = new Selector(Atom.findOrCreateAsciiAtom("defaultReadObject"), Descriptor.findOrCreateUTF8("()V"));

    /**
     * List of magic methods that are involved in untrusted object deserialization vulnerabilities.
     * These are meant to be used as entrypoints during exploit generation.
     */
    public static final Set<Selector> MAGIC_METHOD_ENTRYPOINTS = new HashSet<>(Arrays.asList(
            // methods from the java.io.Serializable interface
            readObjectCallbackSelector,
            readObjectNoDataCallbackSelector,
            readResolveCallbackSelector,
            validateObjectCallbackSelector,
            // methods from the java.lang.reflect.Proxy interface
            proxyInvokeSelector
    ));

    /**
     * List of magic methods that are involved in untrusted object deserialization vulnerabilities.
     * These are meant to be used as entrypoints during exploit generation.
     */
    public static final Set<Selector> ALL_MAGIC_METHODS = new HashSet<>(Arrays.asList(
            // methods from the java.io.Serializable interface (Serialization call backs)
            writeObjectCallbackSelector,
            writeReplaceCallbackSelector,

            // methods from the java.io.Serializable interface (Deserialization call backs)
            readObjectCallbackSelector,
            readObjectNoDataCallbackSelector,
            readResolveCallbackSelector,
            validateObjectCallbackSelector,

            // methods from the java.lang.reflect.Proxy interface
            proxyInvokeSelector
    ));


    /**
     * True iff the method m is a magic method.
     *
     * @param m method to be verified
     * @return true if a magic method; false otherwise.
     */
    public static boolean isMagicMethodEntrypoint(IMethod m) {
        return MAGIC_METHOD_ENTRYPOINTS.contains(m.getSelector());
    }
}
