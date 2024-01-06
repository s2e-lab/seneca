package edu.rit.se.design.callgraph.evaluation.utils;




public class XCorpusTestCases {

    public static final String BATIK_TC = TestUtilities.TC_ROOT_FOLDER + "batik-testcases.jar";
    public static final String CASTOR_TC = TestUtilities.TC_ROOT_FOLDER + "castor-testcases.jar";
    public static final String COMMONS_COLLECTION_TC = TestUtilities.TC_ROOT_FOLDER + "commons-collections-testcases.jar";
    public static final String HTMLUNIT_TC = TestUtilities.TC_ROOT_FOLDER + "htmlunit-testcases.jar";
    public static final String JAMES_TC = TestUtilities.TC_ROOT_FOLDER + "james-testcases.jar";
    public static final String JGRAPH_TC = TestUtilities.TC_ROOT_FOLDER + "jgraph-testcases.jar";
    public static final String JPF_TC = TestUtilities.TC_ROOT_FOLDER + "jpf-testcases.jar";
    public static final String LOG4J_TC = TestUtilities.TC_ROOT_FOLDER + "log4j-testcases.jar";
    public static final String MEGAMEK_TC = TestUtilities.TC_ROOT_FOLDER + "megamek-testcases.jar";
    public static final String OPENJMS_TC = TestUtilities.TC_ROOT_FOLDER + "openjms-testcases.jar";
    public static final String POOKA_TC = TestUtilities.TC_ROOT_FOLDER + "pooka-testcases.jar";
    public static final String TOMCAT_TC = TestUtilities.TC_ROOT_FOLDER + "tomcat-testcases.jar";
    public static final String WEKA_TC = TestUtilities.TC_ROOT_FOLDER + "weka-testcases.jar";
    public static final String XALAN_TC = TestUtilities.TC_ROOT_FOLDER + "xalan-testcases.jar";
    public static final String XERCES_TC = TestUtilities.TC_ROOT_FOLDER + "xerces-testcases.jar";

    // Args

    public static String[] LOG4J_TC_ARGS = new String[]{
            "-j", LOG4J_TC,
            "-o", "./target/log4j-testcases.dot",
            "-f", "dot",
            "--main-policy","0-1-CFA",
            "--secondary-policy", "1-CFA",
            "--view-ui",
            "--print-models",
    };


    public static String[] JPF_TC_ARGS = new String[]{
            "-j", JPF_TC,
            "-o", "./target/jpf-testcases.dot",
            "-f", "dot",
            "--analysis", "1-CFA",
            "--view-ui",
            "--taint",
            "--print-models",
    };


}
