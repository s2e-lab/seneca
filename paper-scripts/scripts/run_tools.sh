#!/bin/bash

## Checks whether the JAVA_HOME is set
[[ -z "${JAVA_HOME}" ]] && echo "JAVA_HOME is not set" && exit 

## Compute full paths resolved with respect to the current directory 
## (it assumes the user is in the same folder as this script)
JAR_FOLDER=$(cd "../dataset/build/"; pwd)"/"
RESULTS_FOLDER=$(cd "../results/rq1"; pwd)"/"
PROJECTS=("batik-1.7" "castor-1.3.1" "james-2.2.0" "jgraph-5.13.0.0" "jpf-1.5.1" "log4j-1.2.16" "openjms-0.7.7-beta-1" "pooka-3.0-080505" "xalan-2.7.1" "xerces-2.10.0") 
PROJECT_NAMES=("batik" "castor" "james" "jgraph" "jpf" "log4j" "openjms" "pooka" "xalan" "xerces") 
SENECA_JAR=$(cd "../../"; pwd)"/seneca.jar"
STATIC_CGS_FOLDER=$(cd "../static-cgs/"; pwd)


## Step 1: Run Salsa & Seneca on CATS benchmark
echo "################################################"
echo "Running *Salsa* on CATS benchmark"
java -cp $SENECA_JAR \
	-Dtestcase_folder=$JAR_FOLDER \
	-Dstatic_cgs_folder=$STATIC_CGS_FOLDER \
	oopsla.evaluation.SalsaCatsEval
echo "Done! Call graphs saved on $STATIC_CGS_FOLDER"
echo "################################################"

echo "################################################"
echo "Running *Seneca* on CATS benchmark"
java -cp $SENECA_JAR \
	-Dtestcase_folder=$JAR_FOLDER \
	-Dstatic_cgs_folder=$STATIC_CGS_FOLDER \
	oopsla.evaluation.SenecaCatsEval
echo "Done! Call graphs saved on $STATIC_CGS_FOLDER"
echo "################################################"


## Step 2: Run Salsa & Seneca on XCorpus benchmark
echo "################################################"
echo "Running *Salsa* and *Seneca* on XCorpus benchmark"
java -cp $SENECA_JAR \
	-Dtestcase_folder=$JAR_FOLDER \
	-Dstatic_cgs_folder=$STATIC_CGS_FOLDER \
	oopsla.evaluation.XCorpusEval
echo "Done! Call graphs saved on $STATIC_CGS_FOLDER"
echo "################################################"

## Step 3: Compute dynamic call graphs
echo "################################################"
echo "Computing dynamic call graphs"
idx=0 # iteration counter
for project in "${PROJECTS[@]}"
do
    project_name=${PROJECT_NAMES[$idx]}
    idx=$((idx+1))

    ## Compute Dynamic Call Graph
    echo "Computing dynamic call graph for ${project_name}"
    ./computeDynamicCGs.sh ${project_name} ../dataset/testcases/${project} ../dataset/testcases/${project}/target/${project_name}-testcases-1.0-jar-with-dependencies.jar
done
echo "Done!"


