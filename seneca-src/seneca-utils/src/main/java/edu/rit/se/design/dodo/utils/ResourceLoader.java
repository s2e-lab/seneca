package edu.rit.se.design.dodo.utils;

import java.io.File;

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
        return ResourceLoader.class.getResource(resource).getPath();
    }

    /**
     * Returns the absolute path of the resource (file) in the classpath
     *
     * @param resource the resource to be loaded from the classpath (relative path to the resources folder)
     * @return a file pointer to the resource.
     */
    public static File getResourceFile(String resource) {
        return new File(getResourcePath(resource));
    }


}
