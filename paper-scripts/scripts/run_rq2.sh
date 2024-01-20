#!/bin/bash



# Checks whether the JAVA_HOME is set
[[ -z "${JAVA_HOME}" ]] && echo "JAVA_HOME is not set" && exit



JAR_FOLDER=$(cd "../dataset/build/"; pwd)"/"
RESULTS_FOLDER=$(cd "../results/rq2"; pwd)"/"
PROJECTS=("batik-1.7" "castor-1.3.1" "james-2.2.0" "jgraph-5.13.0.0" "jpf-1.5.1" "log4j-1.2.16" "openjms-0.7.7-beta-1" "pooka-3.0-080505" "xalan-2.7.1" "xerces-2.10.0")
PROJECT_NAMES=("batik" "castor" "james" "jgraph" "jpf" "log4j" "openjms" "pooka" "xalan" "xerces")




mkdir -p $RESULTS_FOLDER # creates folder if needed
# Cleans up prior results
echo "Cleaning up $RESULTS_FOLDER"
rm -f $RESULTS_FOLDER/*.txt
rm -f $RESULTS_FOLDER/*.csv



## Step 1: CATS precision calculation (by comparing the call graphs)
echo "CATS precision calculation"
python3.9 compute_cats_precision.py > ../results/rq2/cats_precision.tsv


## Step 2: XCorpus precision calculation (by comparing the call graphs)
echo "XCorpus precision calculation"
idx=0 # iteration counter
for project in "${PROJECTS[@]}"
do
    project_name=${PROJECT_NAMES[$idx]}
    idx=$((idx+1))

    ## Output files
    comparison_results=$RESULTS_FOLDER/${project_name}-results.csv
    comparison_differences=$RESULTS_FOLDER/${project_name}-differences.txt

    ## Compute differences
    echo "Computing differences for ${project_name}"
    python3.9 compute_xcorpus_precision.py ${project_name} $RESULTS_FOLDER $JAR_FOLDER

    echo "========================"
done

## Combines all results
sample_csv=$RESULTS_FOLDER/${PROJECT_NAMES[0]}-results.csv
all_results=$RESULTS_FOLDER/all_results_combined.csv
head -n 1 ${sample_csv} > ${all_results} && tail -n+2 -q $RESULTS_FOLDER/*-results.csv >> ${all_results}

## Clean up redundant stuff
rm $RESULTS_FOLDER/*-results.csv
echo "Save it all on ${all_results}"



