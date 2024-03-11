#!/bin/bash


# Checks whether the JAVA_HOME is set
[[ -z "${JAVA_HOME}" ]] && echo "JAVA_HOME is not set" && exit


JAR_FOLDER="../dataset/build"
RESULTS_FOLDER="../results/rq2"
PROJECTS=("batik-1.7" "castor-1.3.1" "james-2.2.0" "jgraph-5.13.0.0" "jpf-1.5.1" "log4j-1.2.16" "openjms-0.7.7-beta-1" "pooka-3.0-080505" "xalan-2.7.1" "xerces-2.10.0")
PROJECT_NAMES=("batik" "castor" "james" "jgraph" "jpf" "log4j" "openjms" "pooka" "xalan" "xerces")






# Compares the call graphs
idx=0 # iteration counter
for project in "${PROJECTS[@]}"
do
    project_name=${PROJECT_NAMES[$idx]}
    idx=$((idx+1))

    # output files
    comparison_results=$RESULTS_FOLDER/${project_name}-results.csv
    comparison_differences=$RESULTS_FOLDER/${project_name}-differences.txt

    # Compute differences
    echo "Computing differences for ${project_name}"
    python3.9 compute_xcorpus_precision_new.py ${project_name} $RESULTS_FOLDER $JAR_FOLDER

    echo "========================"
done

# Combines all results
sample_csv=$RESULTS_FOLDER/NEW_${PROJECT_NAMES[0]}-precision.csv
all_results=$RESULTS_FOLDER/NEW_all_results_combined.csv
head -n 1 ${sample_csv} > ${all_results} && tail -n+2 -q $RESULTS_FOLDER/*-precision.csv >> ${all_results}

# clean up redundant stuff
rm $RESULTS_FOLDER/*-precision.csv
echo "Save it all on ${all_results}"



