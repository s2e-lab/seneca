package edu.rit.se.design.salsa.testcases.utils;

import java.io.*;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public abstract class AbstractTestCase {
    static final String SER_SUFFIX = ".ser";

    protected abstract String getTestName();

    protected abstract Object getObject() throws Exception;

    public void runTest() throws Exception {
        String filepath = getTestName() + SER_SUFFIX;
        Object obj = getObject();

        FileOutputStream fOut = new FileOutputStream(filepath);
        ObjectOutputStream objOut = new ObjectOutputStream(fOut);
        objOut.writeObject(obj);

        // Logger logger = Logger.getLogger(getClass().getName());

        System.out.println("Serialized the object " + obj.toString());

        FileInputStream fs = new FileInputStream(filepath);
        ObjectInputStream objIn = new ObjectInputStream(fs);
        Object deserializedObj = objIn.readObject();

        System.out.println("Deserialized the object " + deserializedObj.toString());

        new File(filepath).delete();
    }

}
