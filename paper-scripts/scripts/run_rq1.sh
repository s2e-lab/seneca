#!/bin/bash


# Checks whether the JAVA_HOME is set
[[ -z "${JAVA_HOME}" ]] && echo "JAVA_HOME is not set" && exit 


JAR_FOLDER="../dataset/build"
RESULTS_FOLDER="../results/rq1"
PROJECTS=("batik-1.7" "castor-1.3.1" "james-2.2.0" "jgraph-5.13.0.0" "jpf-1.5.1" "log4j-1.2.16" "openjms-0.7.7-beta-1" "pooka-3.0-080505" "xalan-2.7.1" "xerces-2.10.0") 
PROJECT_NAMES=("batik" "castor" "james" "jgraph" "jpf" "log4j" "openjms" "pooka" "xalan" "xerces") 




mkdir -p $RESULTS_FOLDER # creates folder if needed
# Cleans up prior results
echo "Cleaning up $RESULTS_FOLDER"
rm -f $RESULTS_FOLDER/*.txt
rm -f $RESULTS_FOLDER/*.csv


# Compares the call graphs
idx=0 # iteration counter
for project in "${PROJECTS[@]}"
do
    project_name=${PROJECT_NAMES[$idx]}
    idx=$((idx+1))

    # output files
    comparison_results=$RESULTS_FOLDER/${project_name}-results.csv
    comparison_differences=$RESULTS_FOLDER/${project_name}-differences.txt

    # Compute Dynamic Call Graph
    echo "Computing dynamic call graph for ${project_name}"
#    ./computeDynamicCGs.sh ../dataset/testcases/${project} ../dataset/build/${project_name}-testcases.jar
    ./computeDynamicCGs.sh ${project_name} ../dataset/testcases/${project} ../dataset/testcases/${project}/target/${project_name}-testcases-1.0-jar-with-dependencies.jar

    # Compute differences
    echo "Computing differences for ${project_name}"
    python3.9 compare_callgraphs.py ${project_name} $RESULTS_FOLDER $JAR_FOLDER

    echo "========================"
done

# Combines all results
sample_csv=$RESULTS_FOLDER/${PROJECT_NAMES[0]}-results.csv
all_results=$RESULTS_FOLDER/all_results_combined.csv
head -n 1 ${sample_csv} > ${all_results} && tail -n+2 -q $RESULTS_FOLDER/*-results.csv >> ${all_results}

# clean up redundant stuff
rm $RESULTS_FOLDER/*-results.csv
echo "Save it all on ${all_results}"


