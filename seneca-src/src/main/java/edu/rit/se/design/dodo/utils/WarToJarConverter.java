package edu.rit.se.design.dodo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Utility class for repackaging a WAR file into a JAR file.
 *
 * @author Joanna C. S. Santos <jds5109@rit.edu>
 */
public class WarToJarConverter {

    private static final String WEB_INF = "WEB-INF/classes/";

    /**
     * Repackage a WAR file into a JAR file. This method re-structure the WAR file contents into a JAR-like structure.
     *
     * @param warFile path to the WAR file
     * @param jarFile path to the JAR file
     * @throws IOException
     */
    public static void convert(File warFile, File jarFile) throws IOException {
        try (JarOutputStream jarOutStream = new JarOutputStream(new FileOutputStream(jarFile))) {
            JarFile warInputFile = new JarFile(warFile);
            Enumeration<JarEntry> entries = warInputFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                try (InputStream inputStream = warInputFile.getInputStream(entry)) {
                    JarEntry newEntry = new JarEntry(entry.getName().replace(WEB_INF, "")); // fixes path
                    jarOutStream.putNextEntry(newEntry);
                    while (inputStream.available() > 0) {
                        jarOutStream.write(inputStream.read()); // re-adds the entry to the JAR file
                    }
                    jarOutStream.closeEntry();
                }
            }
            jarOutStream.finish();
        }
    }

}
