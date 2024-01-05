package edu.rit.se.design.callgraph.serializer;

import com.ibm.wala.ipa.callgraph.CallGraph;

import java.io.File;

public interface ICallGraphSerializer {

    /**
     * Saves a file in a given format.
     *
     * @param cg         call graph
     * @param outputFile where to save the file
     */
    void save(CallGraph cg, File outputFile);
}
