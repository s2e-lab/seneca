package edu.rit.se.design.salsa.testcases.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class TestCaseGenerator {

    static String PATH_PREFIX = "PATH_TO_OUTPUT";

    public static void main(String[] args) {
        List<String> classNames = new ArrayList<>();
//        classNames.add( "xalan.org.apache.xalan.templates.Stylesheet" );
//        classNames.add( "xalan.org.apache.xalan.templates.ElemForEach" );
//        classNames.add( "xalan.org.apache.xalan.client.XSLTProcessorApplet" );
//        classNames.add( "xalan.org.apache.xalan.xsltc.trax.TemplatesImpl" );

        classNames.add("com.ivata.groupware.admin.security.server.AbstractSecuritySession");
        classNames.add("com.ivata.groupware.business.addressbook.person.group.right.UserRightFilter");
        classNames.add("com.ivata.groupware.business.addressbook.person.group.tree.PersonTreeModel");
        classNames.add("com.ivata.groupware.business.drive.file.FileContentDO");
        classNames.add("com.ivata.groupware.business.drive.file.FileRevisionDO");
        classNames.add("com.ivata.groupware.business.library.comment.tree.CommentTreeModel");
        classNames.add("com.ivata.groupware.business.mail.session.MailAuthenticator");
        classNames.add("com.ivata.groupware.container.persistence.BaseDO");

        classNames.forEach(fullClassName -> {
            String simpleClassName = getSimpleClassName(fullClassName);
            createFile(PATH_PREFIX + "TC_" + simpleClassName + "_Simple", generateSimpleClass(fullClassName));
            createFile(PATH_PREFIX + "TC_" + simpleClassName + "_List", generateCompositeListClass(fullClassName));
            createFile(PATH_PREFIX + "TC_" + simpleClassName + "_Set", generateCompositeSetClass(fullClassName));
            createFile(PATH_PREFIX + "TC_" + simpleClassName + "_Map", generateCompositeMapClass(fullClassName));
            createFile(PATH_PREFIX + "TC_" + simpleClassName + "_Array", generateCompositeArrayClass(fullClassName));
        });
    }

    static void createFile(String fileName, String content) {
        try {
            File myObj = new File(fileName + ".java");
            if (myObj.createNewFile()) {
                FileWriter myWriter = new FileWriter(fileName + ".java");
                myWriter.write(content);
                myWriter.close();
                System.out.println("File created: " + myObj.getName());

            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    static String generateSimpleClass(String fullClassName) {
        String simpleClassName = getSimpleClassName(fullClassName);
        String generatedClass = "import " + fullClassName + ";\n" +
                "import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;\n" +
                "/**\n" +
                " * This is a test case for a simple object serialization\n" +
                " */\n" +
                "\n" +
                "public class TC_" + simpleClassName + "_Simple extends AbstractSimpleTestCase {\n" +
                "\n" +
                "    public static void main(String[] args) throws Exception {\n" +
                "        TC_" + simpleClassName + "_Simple tc = new TC_" + simpleClassName + "_Simple();\n" +
                "        tc.runTest();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    protected Object getObject() throws Exception{\n" +
                "        " + simpleClassName + " object = new " + simpleClassName + "();\n" +
                "        return object;\n" +
                "    }\n" +
                "\n" +
                "}";
        return generatedClass;
    }

    static String generateCompositeListClass(String fullClassName) {
        String simpleClassName = getSimpleClassName(fullClassName);
        String generatedClass = "import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;\n" +
                "import edu.rit.se.design.salsa.testcases.utils.CompositListTestCase;\n" +
                "/**\n" +
                " * This is a test case for a complex object serialization as a List\n" +
                " */\n" +
                "\n" +
                "public class TC_" + simpleClassName + "_List extends CompositListTestCase {\n" +
                "\n" +
                "    public static void main(String[] args) throws Exception {\n" +
                "        TC_" + simpleClassName + "_List tc = new\n" +
                "                TC_" + simpleClassName + "_List(\n" +
                "                        new TC_" + simpleClassName + "_Simple()\n" +
                "        );\n" +
                "        tc.runTest();\n" +
                "    }\n" +
                "\n" +
                "    public TC_" + simpleClassName + "_List(AbstractSimpleTestCase simpleTestCase) {\n" +
                "        super(simpleTestCase);\n" +
                "    }\n" +
                "\n" +
                "}";
        return generatedClass;
    }

    static String generateCompositeSetClass(String fullClassName) {
        String simpleClassName = getSimpleClassName(fullClassName);
        String generatedClass = "import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;\n" +
                "import edu.rit.se.design.salsa.testcases.utils.CompositSetTestCase;\n" +
                "/**\n" +
                " * This is a test case for a complex object serialization as a Set\n" +
                " */\n" +
                "\n" +
                "public class TC_" + simpleClassName + "_Set extends CompositSetTestCase {\n" +
                "\n" +
                "    public static void main(String[] args) throws Exception {\n" +
                "        TC_" + simpleClassName + "_Set tc = new\n" +
                "                TC_" + simpleClassName + "_Set(\n" +
                "                        new TC_" + simpleClassName + "_Simple()\n" +
                "        );\n" +
                "        tc.runTest();\n" +
                "    }\n" +
                "\n" +
                "    public TC_" + simpleClassName + "_Set(AbstractSimpleTestCase simpleTestCase) {\n" +
                "        super(simpleTestCase);\n" +
                "    }\n" +
                "\n" +
                "}";
        return generatedClass;
    }

    static String generateCompositeMapClass(String fullClassName) {
        String simpleClassName = getSimpleClassName(fullClassName);
        String generatedClass = "import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;\n" +
                "import edu.rit.se.design.salsa.testcases.utils.CompositMapTestCase;\n" +
                "/**\n" +
                " * This is a test case for a complex object serialization as a Map\n" +
                " */\n" +
                "\n" +
                "public class TC_" + simpleClassName + "_Map extends CompositMapTestCase {\n" +
                "\n" +
                "    public static void main(String[] args) throws Exception {\n" +
                "        TC_" + simpleClassName + "_Map tc = new\n" +
                "                TC_" + simpleClassName + "_Map(\n" +
                "                        new TC_" + simpleClassName + "_Simple()\n" +
                "        );\n" +
                "        tc.runTest();\n" +
                "    }\n" +
                "\n" +
                "    public TC_" + simpleClassName + "_Map(AbstractSimpleTestCase simpleTestCase) {\n" +
                "        super(simpleTestCase);\n" +
                "    }\n" +
                "\n" +
                "}";
        return generatedClass;
    }

    static String generateCompositeArrayClass(String fullClassName) {
        String simpleClassName = getSimpleClassName(fullClassName);
        String generatedClass = "import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;\n" +
                "import edu.rit.se.design.salsa.testcases.utils.CompositArrayTestCase;\n" +
                "/**\n" +
                " * This is a test case for a complex object serialization as an Array\n" +
                " */\n" +
                "\n" +
                "public class TC_" + simpleClassName + "_Array extends CompositArrayTestCase {\n" +
                "\n" +
                "    public static void main(String[] args) throws Exception {\n" +
                "        TC_" + simpleClassName + "_Array tc = new\n" +
                "                TC_" + simpleClassName + "_Array(\n" +
                "                        new TC_" + simpleClassName + "_Simple()\n" +
                "        );\n" +
                "        tc.runTest();\n" +
                "    }\n" +
                "\n" +
                "    public TC_" + simpleClassName + "_Array(AbstractSimpleTestCase simpleTestCase) {\n" +
                "        super(simpleTestCase);\n" +
                "    }\n" +
                "\n" +
                "}";
        return generatedClass;
    }

    static String getSimpleClassName(String fullClassName) {
        return fullClassName.split("\\.")[fullClassName.split("\\.").length - 1];
    }


}
