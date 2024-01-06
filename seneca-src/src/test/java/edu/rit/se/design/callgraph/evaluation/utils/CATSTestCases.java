package edu.rit.se.design.callgraph.evaluation.utils;

import edu.rit.se.design.callgraph.evaluation.utils.TestUtilities;

/**
 * Simply acts as an aggregator for paths to case studies (actually used in the paper).
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class CATSTestCases {
    // Projects from CATS dataset
    public static String CASE_STUDY_SER1 = TestUtilities.TC_ROOT_FOLDER + "Ser1-JRE1.8.jar";
    public static String CASE_STUDY_SER2 = TestUtilities.TC_ROOT_FOLDER + "Ser2-JRE1.8.jar";
    public static String CASE_STUDY_SER3 = TestUtilities.TC_ROOT_FOLDER + "Ser3-JRE1.8.jar";
    public static String CASE_STUDY_SER4 = TestUtilities.TC_ROOT_FOLDER + "Ser4-JRE1.8.jar";
    public static String CASE_STUDY_SER5 = TestUtilities.TC_ROOT_FOLDER + "Ser5-JRE1.8.jar";
    public static String CASE_STUDY_SER6 = TestUtilities.TC_ROOT_FOLDER + "Ser6-JRE1.8.jar";
    public static String CASE_STUDY_SER7 = TestUtilities.TC_ROOT_FOLDER + "Ser7-JRE1.8.jar";
    public static String CASE_STUDY_SER8 = TestUtilities.TC_ROOT_FOLDER + "Ser8-JRE1.8.jar";
    public static String CASE_STUDY_SER9 = TestUtilities.TC_ROOT_FOLDER + "Ser9-JRE1.8.jar";


    public static String[] SER1_ARGS = new String[]{
            "-j",CASE_STUDY_SER1,
            "-o", "./target/ser1.cg.dot",
            "-f","dot",
            "--analysis","0-1-CFA",
            "--view-ui",
            "--print-models",
    };


}
