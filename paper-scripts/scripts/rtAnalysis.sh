#!/bin/bash

# Checks whether the JAVA_HOME is set
[[ -z "${JAVA_HOME}" ]] && echo "JAVA_HOME is not set" && exit 

# INCLUDE_REGEX="TC.*,java.io.*"
# INCLUDE_REGEX="TC.*,testgeneratortemplates.*,templates.*,java.lang.reflect.*,java.io.Object.*,org.*,java.util.[A-Z]"
# EXCLUDE_REGEX="gr.gousiosg.*,java.io.File.*,sun.*" 
# INCLUDE_REGEX="TC.*,testgeneratortemplates.*,templates.*,java.io.*,org.*"

INCLUDE_REGEX="TC.*,testgeneratortemplates.*,templates.*,java.io.*,org.*"
EXCLUDE_REGEX="gr.gousiosg.*,java.io.FileWriter" 



JAVA_CALLGRAPH="libs/javacg-0.1-SNAPSHOT-dycg-agent.jar"
JRE_LIB="/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/jre/lib"
JAVA_LIB="/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/lib"


JRE_LIB_JARS=
for i in `ls ${JRE_LIB}/*.jar`
do
  JRE_LIB_JARS=${JRE_LIB_JARS}:${i}
done

JRE_EXT_LIB_JARS=
for i in `ls ${JRE_LIB}/ext/*.jar`
do
  JRE_EXT_LIB_JARS=${JRE_EXT_LIB_JARS}:${i}
done

JAVA_LIB_JARS=
for i in `ls ${JAVA_LIB}/*.jar`
do
  JAVA_LIB_JARS=${JAVA_LIB_JARS}:${i}
done



# XBOOT_CP="${JAVA_LIB}/charsets.jar:${JAVA_LIB}/deploy.jar:${JAVA_LIB}/ext/cldrdata.jar:${JAVA_LIB}/ext/dnsns.jar:${JAVA_LIB}/ext/jaccess.jar:${JAVA_LIB}/ext/jfxrt.jar:${JAVA_LIB}/ext/localedata.jar:${JAVA_LIB}/ext/nashorn.jar:${JAVA_LIB}/ext/sunec.jar:${JAVA_LIB}/ext/sunjce_provider.jar:${JAVA_LIB}/ext/sunpkcs11.jar:${JAVA_LIB}/ext/zipfs.jar:${JAVA_LIB}/javaws.jar:${JAVA_LIB}/jce.jar:${JAVA_LIB}/jfr.jar:${JAVA_LIB}/jfxswt.jar:${JAVA_LIB}/jsse.jar:${JAVA_LIB}/management-agent.jar:${JAVA_LIB}/plugin.jar:${JAVA_LIB}/resources.jar:${JAVA_LIB}/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/lib/ant-javafx.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/lib/dt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/lib/javafx-mx.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/lib/jconsole.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/lib/packager.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/lib/sa-jdi.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/lib/tools.jar:${JAVA_CALLGRAPH}"
XBOOT_CP="${JAVA_CALLGRAPH}${JRE_LIB_JARS}${JRE_EXT_LIB_JARS}${JAVA_LIB_JARS}"
JAVA_AGENT=${JAVA_CALLGRAPH}="incl=${INCLUDE_REGEX};excl=${EXCLUDE_REGEX}"





# Checks the number of parameters
[[ $# != 2 ]] && echo "Usage: <TC-Folder> <File.jar>" && exit


# Parses the parameters
tc_folder=$1
jar_path=$2
jar_name="${jar_path##*/}"
jar_name=${jar_name%.*}
output_folder="../runtime-cgs/new_cats/${jar_name}"
output_folder=${output_folder%-*}




echo "Creating output folder ${output_folder}"
mkdir -p ${output_folder}

echo "Removing old results from ${output_folder}/*.txt"
rm ${output_folder}/*.txt



DEPENDENCIES=
for i in `ls ${tc_folder}/lib/default-lib/*.jar`
do
  DEPENDENCIES=${DEPENDENCIES}:${i}
done



printf "Analyzing Jar File: ${jar_path}\n"
printf "Searching for test classes: ${tc_folder}\n"
for test_case_class in `grep -l -r "main(" $tc_folder` ; do
    class_name="${test_case_class##*/}"
    main_class="${class_name%.*}"
    # if main_class equals to "DEmo"
    if  [[ $main_class == TC* ]] ;
    then
    
        echo "Creating dynamic call graph with main class: ${class_name}"

        java -Djava.ext.dirs=${JAVA_LIB}/ext \
            -Xbootclasspath:${XBOOT_CP}:${jar_path} \
            -javaagent:${JAVA_AGENT} \
            -classpath ${JAVA_CALLGRAPH}:${DEPENDENCIES}:${jar_path} testcases.${main_class} \
            > ${output_folder}/${main_class}-output.txt 2> ${output_folder}/${main_class}-stderr.txt

        
        
        mv calltrace.txt ${output_folder}/${main_class}-trace.txt
    fi
done
########################################################################################################
# Example on how to run it
########################################################################################################
# ./rtAnalysis.sh ../dataset/testcases/batik-1.7 ../dataset/build/batik-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/castor-1.3.1 ../dataset/build/castor-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/commons-collections-3.2.1 ../dataset/build/commons-collections-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/htmlunit-2.8 ../dataset/build/htmlunit-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/james-2.2.0 ../dataset/build/james-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/jgraph-5.13.0.0 ../dataset/build/jgraph-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/jpf-1.5.1 ../dataset/build/jpf-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/log4j-1.2.16 ../dataset/build/log4j-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/megamek-0.35.18 ../dataset/build/megamek-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/openjms-0.7.7-beta-1 ../dataset/build/openjms-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/pooka-3.0-080505/ ../dataset/build/pooka-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/tomcat-7.0.2 ../dataset/build/tomcat-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/weka-3.7.9 ../dataset/build/weka-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/xalan-2.7.1 ../dataset/build/xalan-testcases.jar
# ./rtAnalysis.sh ../dataset/testcases/xerces-2.10.0 ../dataset/build/xerces-testcases.jar
# ./rtAnalysis.sh ../dataset/cats/ser1 ../dataset/build/Ser1-JRE1.8.jar
########################################################################################################






