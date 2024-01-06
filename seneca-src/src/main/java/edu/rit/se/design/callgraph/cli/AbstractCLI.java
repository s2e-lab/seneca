package edu.rit.se.design.callgraph.cli;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.MonitorUtil;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaCallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.seneca.SenecaCallGraphBuilder;
import edu.rit.se.design.callgraph.model.MethodModel;
import edu.rit.se.design.callgraph.serializer.DotCallGraphSerializer;
import edu.rit.se.design.callgraph.serializer.JDynCallGraphSerializer;
import edu.rit.se.design.callgraph.serializer.JsonJcgSerializer;
import edu.rit.se.design.dodo.utils.debug.DodoLogger;
import edu.rit.se.design.dodo.utils.viz.ProjectAnalysisViewer;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.*;
import static java.lang.String.format;

/**
 * Abstract class for creating a command line interface.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public abstract class AbstractCLI {

    // argument names
    public static final String PRINT_MODELS = "print-models";
    public static final String VIEW_UI = "view-ui";
    public static final String EXCLUSIONS = "exclusions";
    public static final String FORMAT = "format";
    public static final String OUTPUT = "output";
    public static final String JAR = "jar";
    public static final String MAIN_PA_POLICY = "main-policy";
    public static final String SECONDARY_POLICY = "secondary-policy";

    // default values for args
    public static final String DEFAULT_EXCLUSIONS_FILE = "exclusions.txt";
    // regexes for parsing the PA algorithms
    private static final Pattern N_CFA_PATTERN = Pattern.compile("^(\\d+)-CFA$");
    private static final Pattern ZERO_N_CFA_PATTERN = Pattern.compile("^0-(\\d+)-CFA$");
    private static final Pattern ZERO_N_CTN_CFA_PATTERN = Pattern.compile("^0-(\\d+)-Container-CFA$");


    public void runAnalysis(String[] args) throws IOException, ClassHierarchyException, CallGraphBuilderCancelException {
        Logger logger = DodoLogger.getLogger(getClass(), false);

        // required arguments
        CommandLine cmd = setUpCommandLine(getClass(), args);
        String jarFilePath = cmd.getOptionValue(JAR);
        String format = cmd.getOptionValue(FORMAT);
        File outputFile = new File(cmd.getOptionValue(OUTPUT));
        PointerAnalysisPolicy primaryPaPolicy = parsePointerAnalysisPolicy(cmd.getOptionValue(MAIN_PA_POLICY));
        PointerAnalysisPolicy secondaryPaPolicy = new PointerAnalysisPolicy(PointerAnalysisPolicy.PolicyType.nCFA, 1);

        // optional arguments
        String exclusions = cmd.hasOption(EXCLUSIONS) ? cmd.getOptionValue(EXCLUSIONS) : getClass().getClassLoader().getResource(DEFAULT_EXCLUSIONS_FILE).toString();
        boolean showUi = cmd.hasOption(VIEW_UI);
        boolean printMethodModels = cmd.hasOption(PRINT_MODELS);


        logger.info(format("[%s] Starting analysis", getClass().getSimpleName()));
        logger.info("\tJar: " + jarFilePath);
        logger.info("\tOutput: " + outputFile);
        logger.info("\tFormat: " + format);
        logger.info("\tPointer Analysis Policy: " + primaryPaPolicy);



        // call graph construction
        long start = System.currentTimeMillis();
        Pair<CallGraph, CallGraphBuilder> results = computeCallGraph(jarFilePath, new File(exclusions), primaryPaPolicy, secondaryPaPolicy, new CustomMonitor(logger));
        long end = System.currentTimeMillis();
        logger.info("Call graph computed in " + ((end - start) / 1000L) + " seconds");

        CallGraph cg = results.getLeft();
        CallGraphBuilder builder = results.getRight();
        // Visualize call graph in Java Swing
        if (showUi) {
            Set<PointerKey> deserializedObjects = builder instanceof SenecaCallGraphBuilder ?
                    ((SenecaCallGraphBuilder) builder).getTaintedPointers() :
                    ((SalsaCallGraphBuilder) builder).getDeserializedObjects();
            new ProjectAnalysisViewer(cg, deserializedObjects, builder.getPointerAnalysis(), false)
                    .setTitle(format("[%s] %s", getClass().getSimpleName(), jarFilePath));
        }

        // Prints synthetic methods created by edu.rit.se.design.callgraph.cli.Salsa
        if (printMethodModels) {
            for (CGNode cgNode : cg) {
                if (cgNode.getMethod() instanceof MethodModel) {
                    System.out.println("ID: " + cgNode.getGraphNodeId() + " " + cgNode);
                    String[] lines = cgNode.getIR().toString().split("Instructions:\n")[1].split("\n");
                    for (String line : lines) {
                        if (line.startsWith("BB")) continue;
                        System.out.println("\t" + line);
                    }
                }
            }
        }


        // saving results
        switch (OutputFormat.valueOf(format.toUpperCase())) {
            case DOT:
                new DotCallGraphSerializer().save(cg, outputFile);
                break;
            case JSON:
                throw new UnsupportedOperationException("JSON serialization is not supported yet");
                //new JsonJcgSerializer().save(cg, outputFile);
                //break;
            case JDYN:
                new JDynCallGraphSerializer().save(cg, outputFile);
                break;
        }


    }

    protected abstract Pair<CallGraph, CallGraphBuilder> computeCallGraph(String jarFilePath, File exclusionsFile, PointerAnalysisPolicy primaryPaPolicy, PointerAnalysisPolicy policy, MonitorUtil.IProgressMonitor monitor) throws IOException, ClassHierarchyException, CallGraphBuilderCancelException;

    /**
     * Computes the PointerAnalysisPolicy object for a pointer analysis policy given as string.
     *
     * @param analysis pointer analysis policy given as string.
     * @return a {@link PointerAnalysisPolicy} instance that matches the user-specified policy.
     */
    private static PointerAnalysisPolicy parsePointerAnalysisPolicy(String analysis) {
        analysis = analysis.trim();

        Matcher matcher = N_CFA_PATTERN.matcher(analysis);
        if (matcher.find())
            return new PointerAnalysisPolicy(nCFA, Integer.valueOf(matcher.group(1)));

        matcher = ZERO_N_CFA_PATTERN.matcher(analysis);
        if (matcher.find())
            return new PointerAnalysisPolicy(ZeroXCFA, Integer.valueOf(matcher.group(1)));

        matcher = ZERO_N_CTN_CFA_PATTERN.matcher(analysis);
        if (matcher.find())
            return new PointerAnalysisPolicy(ZeroXContainerCFA, Integer.valueOf(matcher.group(1)));

        throw new IllegalArgumentException("Unknown pointer analysis policy " + analysis);
    }

    /**
     * Set ups the options for the CLI
     *
     * @param args program arguments
     * @return a {@link CommandLine} instance for retrieving the program args
     */
    private static CommandLine setUpCommandLine(Class cliClass, String[] args) {


        Option jar = new Option("j", JAR, true, "Path to the project's JAR file");
        jar.setRequired(true);

        Option output = new Option(OUTPUT.substring(0, 1), OUTPUT, true, "Path to the output file with the serialized call graph");
        output.setRequired(true);

        Option formatOpt = new Option(FORMAT.substring(0, 1), FORMAT, true, "Output format (possible values: dot, jdyn). JDyn is a custom format that saves the call graph as tuples (caller, callee)");
        formatOpt.setType(OutputFormat.class);
        formatOpt.setRequired(true);


        Option exclusionFile = new Option(EXCLUSIONS.substring(0, 1), EXCLUSIONS, true, "Path to the exclusions file");
        exclusionFile.setRequired(false);


        Option viewUi = new Option(null, VIEW_UI, false, "Shows call graph in a Java Swing UI");
        viewUi.setRequired(false);

        Option printMethodModels = new Option(null, PRINT_MODELS, false, "Prints to the console all the synthetic methods created");
        printMethodModels.setRequired(false);


        Option mainPaPolicy = new Option("pta", MAIN_PA_POLICY, true, "Pointer analysis choice (n-CFA, 0-n-CFA, 0-n-Container-CFA)");
        mainPaPolicy.setRequired(true);





        DefaultParser parser = new DefaultParser();
        Options options = new Options();

        // Required CLI arguments
        options.addOption(jar);
        options.addOption(output);
        options.addOption(formatOpt);
        options.addOption(mainPaPolicy);


        // Optional CLI arguments
        options.addOption(viewUi);
        options.addOption(printMethodModels);
        options.addOption(exclusionFile);


        try {
            if (args.length == 0) showUsageAndExit(cliClass, options);
            return parser.parse(options, args);
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
            showUsageAndExit(cliClass, options);
            return null;
        }
    }

    /**
     * Simply prints a help menu for using this command line interface.
     *
     * @param options the options used to set up the command line
     */
    private static void showUsageAndExit(Class cliClass, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(cliClass.getSimpleName(), options);
        System.exit(-1);
    }

    public enum OutputFormat {
        /**
         * Same format as described by Reif et al ISSTA'18 and ISSTA'19
         */
        JSON,
        /**
         * Dot format that can be visualized
         */
        DOT,
        /**
         * Our custom format that matches the JDynCallGraph
         */
        JDYN
    }

    /**
     * Simple monitor that prints out in the console what is going on.
     */
    private static class CustomMonitor implements MonitorUtil.IProgressMonitor {
        private final Logger logger;
        private boolean isCanceled = false;
        private int taskNo;

        private CustomMonitor(Logger logger) {
            this.logger = logger;
        }


        @Override
        public void beginTask(String s, int i) {
            logger.info("BEGIN TASK #" + i + ": " + s);
            this.taskNo = i;
        }

        @Override
        public void subTask(String s) {
            logger.info("SUBTASK " + s);
        }

        @Override
        public void cancel() {
            logger.info("CANCEL");
            isCanceled = true;
        }

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }

        @Override
        public void done() {
            logger.info("DONE");
        }

        @Override
        public void worked(int i) {
            logger.info(format("\tWORKED %d.%d", taskNo, i));
        }

        @Override
        public String getCancelMessage() {
            return "Error happened";
        }
    }

}
