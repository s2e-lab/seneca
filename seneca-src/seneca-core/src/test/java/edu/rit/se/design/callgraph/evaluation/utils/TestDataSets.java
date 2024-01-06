package edu.rit.se.design.callgraph.evaluation.utils;

/**
 * Simply acts as an aggregator for paths to testing programs.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class TestDataSets {


    public static String ROOT_FOLDER = "/Users/joanna/Documents/edu.rit.se.design.callgraph.cli.Salsa/TestProjects/";

    public static String RUNNING_EXAMPLE = ROOT_FOLDER + "RunningExample-JRE1.8.jar"; // Example within the FTfJP paper
    public static String PLDI_RUNNING_EXAMPLE = ROOT_FOLDER + "PLDIRunningExample-JRE1.8.jar"; // Example within the PLDI paper
    public static String SERIALIZATION_EXAMPLE1 = ROOT_FOLDER + "SerializationExample1-JRE1.8.jar"; // More sophisticated within the paper
    public static String SERIALIZATION_EXAMPLE2 = ROOT_FOLDER + "SerializationExample2-JRE1.8.jar"; // Example within the paper
    public static String REFLECTION_EXAMPLE = ROOT_FOLDER + "ReflectionSample-JRE1.8.jar"; // Used for getting inspiration from the WALA's implementation of reflection features
    public static String SOAP_RUNNING_EXAMPLE = ROOT_FOLDER + "SOAPPaperExample-JRE1.8.jar"; // Example within the SOAP paper





    // Used for demonstrating multiple scenarios where different field types can be used in an exploit
    public static String PROPOSAL_EXAMPLE = System.getProperty("user.home") + "/Google Drive/Research Assistant/Projects/Weaknesses/DODO-TestData/sample-code/vulnerable-samples/build/ProposalExample-JRE1.7.jar";


    // CVEs
    public static String COMMONS_FILE_UPLOAD_VULN = System.getProperty("user.home") + "/Documents/Portfolio/GitHub/pldi-2021-paper/cves/commons-fileupload/commons-fileupload-1.3.2.jar";


    // DRIVERS
    public static String CLIENT_COMMONS_FILE_UPLOAD = System.getProperty("user.home") + "/Documents/Portfolio/GitHub/pldi-2021-paper/cves/Driver-commons-fileupload-1.3.2.jar";


    public static String[] SOAP_RUNNING_EXAMPLE_ARGS = new String[]{
            "-j",SOAP_RUNNING_EXAMPLE,
            "-o", "./target/SOAPPaperExample-JRE1.8.cg.dot",
            "-f","dot",
            "--analysis","1-CFA",
            "--view-ui",
            "--taint",
            "--print-models",
    };


    public static String[] PROPOSAL_EXAMPLE_ARGS = new String[]{
            "-j",PROPOSAL_EXAMPLE,
            "-o", "./target/ProposalExample-JRE1.7.cg.dot",
            "-f","dot",
            "--analysis","1-CFA",
            "--view-ui",
            "--taint",
            "--print-models",
    };


    public static String[] CLIENT_COMMONS_FILE_UPLOAD_ARGS = new String[]{
            "-j",CLIENT_COMMONS_FILE_UPLOAD,
            "-o", "./target/Driver-commons-fileupload-1.3.2.jar",
            "-f","dot",
            "--analysis","1-CFA",
            "--view-ui",
            "--taint",
            "--print-models",
    };

    public static String[] COMMONS_FILE_UPLOAD_VULN_ARGS = new String[]{
            "-j",COMMONS_FILE_UPLOAD_VULN,
            "-o", "./target/commons-fileupload-1.3.2.jar",
            "-f","dot",
            "--analysis","1-CFA",
            "--view-ui",
            "--taint",
            "--print-models",
    };

}
