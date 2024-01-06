package edu.rit.se.design.callgraph.util;


/**
 * Enumerates all possible categories for a static type.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public enum TypeCategory {
    PRIMITIVE, ARRAY, OBJECT, LIST, SET, MAP, // common data types
    IGNORED // special flag to indicate that the field's static type is not in the CHA due to the exclusion file
}
