package edu.rit.se.design.callgraph.util;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.FileOfClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;

/**
 * Utility class for creating required data structures for call graph construction, that is:
 * {@link IClassHierarchy}, {@link AnalysisScope}, and {@link AnalysisOptions}.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class AnalysisUtils {

    /**
     * Create an analysis scope (uses JRE 1.8)
     *
     * @param jarFilePath        relative/absolute path to jar file
     * @param exclusionsFile     exclusions file
     * @param dependenciesFolder folder containing dependencies
     * @return an AnalysisScope
     * @throws IOException
     */
    public static AnalysisScope makeAnalysisScope(String jarFilePath, File exclusionsFile, File dependenciesFolder) throws IOException {
        AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();

        // add project to scope
        scope.addToScope(ClassLoaderReference.Application, new JarFile(jarFilePath));

        // add standard libraries to scope (JavaSE)
        for (String stdlib : new String[]{"rt.jar", "jsse.jar", "jce.jar"}) {
            scope.addToScope(ClassLoaderReference.Primordial, new JarFile("../../config/JREs/jre1.8_mini/lib/" + stdlib));
        }

        // find dependencies to be added as Extension scope
        if (dependenciesFolder != null) {
            if (!dependenciesFolder.exists())
                throw new RuntimeException("Dependencies folder does not exist: " + dependenciesFolder.getAbsolutePath());
            for (File dependency : dependenciesFolder.listFiles((dir, name) -> name.endsWith(".jar"))) {
                scope.addToScope(ClassLoaderReference.Extension, new JarFile(dependency));
            }
        }

        // set exclusions file
        if (exclusionsFile != null) {
            try (InputStream fs =
                         exclusionsFile.exists()
                                 ? new FileInputStream(exclusionsFile)
                                 : AnalysisUtils.class
                                 .getClassLoader()
                                 .getResourceAsStream(exclusionsFile.getName())) {
                scope.setExclusions(new FileOfClasses(fs));
            }
        }
        return scope;
    }


    public static IClassHierarchy makeIClassHierarchy(AnalysisScope scope) throws ClassHierarchyException {
        return ClassHierarchyFactory.make(scope);
    }

    public static AnalysisOptions makeAnalysisOptions(AnalysisScope scope, IClassHierarchy cha) {
        return makeAnalysisOptions(scope, cha, false);
    }


    public static AnalysisOptions makeAnalysisOptions(AnalysisScope scope, IClassHierarchy cha, boolean enableReflection) {
        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, cha);

        AnalysisOptions options = new AnalysisOptions();
        options.setEntrypoints(entrypoints);
        if (enableReflection)
            options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);
        else
            options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);
        return options;
    }


    public static AnalysisCache makeAnalysisCache() {
        return new AnalysisCacheImpl(AstIRFactory.makeDefaultFactory());
    }
}
