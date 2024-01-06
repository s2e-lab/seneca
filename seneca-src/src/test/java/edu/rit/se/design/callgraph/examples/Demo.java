package edu.rit.se.design.callgraph.examples;

import java.io.*;

public class Demo extends Superclass implements Serializable, ObjectInputValidation {

    static final long serialVersionUID = 42L;

    public void validateObject() throws InvalidObjectException {
        System.out.println("Demo.validateObject()");
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.registerValidation(this, 0);
        in.defaultReadObject();
        System.out.println("Demo.readObject()");
    }
    private void readObjectNoData() throws ObjectStreamException{
        System.out.println("Demo.readObjectNoData()");
    }

    private Object readResolve() throws ObjectStreamException {
        System.out.println("Demo.readResolve()");
        return this;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        System.out.println("Demo.writeObject()");
    }
    private Object writeReplace() throws ObjectStreamException {
        System.out.println("Demo.writeReplace()");
        return this;
    }

    public static void main(String[] args) throws Exception {

        System.out.println("SERIALIZATION");
        Demo serialize = new Demo();
        FileOutputStream fos = new FileOutputStream("test.ser");
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(serialize);
        out.close();

        System.out.println("DESERIALIZATION");

        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Demo obj = (Demo) in.readObject();
        in.close();


    }
}

class Superclass {
    public void callback() {
        System.out.println("Superclass.callback()");
    }

    public Superclass() {
        callback();
    }
}
