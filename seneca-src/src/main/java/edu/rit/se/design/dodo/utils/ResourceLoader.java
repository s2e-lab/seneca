package edu.rit.se.design.dodo.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to load resources from the classpath.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class ResourceLoader {

    /**
     * Returns the absolute path of the resource (file) in the classpath.
     *
     * @param resource the resource to be loaded from the classpath (relative path to the 'resources' folder).
     * @return the absolute path of the resource.
     */
    public static String getResourcePath(String resource) {
//        return ResourceLoader.class.getResource(resource).getPath();

        return getResourceFile(resource).getAbsolutePath();
    }

    /**
     * Returns the absolute path of the resource (file) in the classpath
     *
     * @param resource the resource to be loaded from the classpath (relative path to the resources folder)
     * @return a file pointer to the resource.
     */
    public static File getResourceFile(String resource) {
//        return new File(getResourcePath(resource));

        try {
            InputStream in = ResourceLoader.class.getResourceAsStream(resource);
            File tempFile = File.createTempFile("temp", ".tmp");
            tempFile.deleteOnExit();
            FileOutputStream out = new FileOutputStream(tempFile);
            IOUtils.copy(in, out);
            return tempFile;
        } catch (IOException e) {

        }
        return null;
    }


}
