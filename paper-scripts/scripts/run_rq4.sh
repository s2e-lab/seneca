#!/bin/bash

## Checks whether the JAVA_HOME is set
[[ -z "${JAVA_HOME}" ]] && echo "JAVA_HOME is not set" && exit 

## Compute full paths resolved with respect to the current directory 
## (it assumes the user is in the same folder as this script)
JAR_FOLDER=$(cd "../dataset/build/"; pwd)"/"
RESULTS_FOLDER=$(cd "../results/rq4"; pwd)"/"
SENECA_JAR=$(cd "../../"; pwd)"/seneca.jar"
STATIC_CGS_FOLDER=$(cd "../static-cgs/"; pwd)


## Run Seneca on YSoSerial benchmark
echo "################################################"
echo "Running *Seneca* on YSoSerial benchmark"
java -cp $SENECA_JAR \
    -Dtestcase_folder=$JAR_FOLDER \
    -Dstatic_cgs_folder=$STATIC_CGS_FOLDER \
    -Dvuln_paths_folder=$RESULTS_FOLDER \
    oopsla.evaluation.VulnerabilityFindingEval
echo "Done!"
echo "################################################"


echo "################################################"
echo "Parsing vulnerable paths"
python3 parse_vuln_paths.py
echo "Done!"
echo "################################################"
