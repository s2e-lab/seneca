#!/bin/bash

cd ../test-cases-utils
mvn --quiet install

cd ../ivatagroupware-0.11.3

mvn --quiet install:install-file -Dfile=lib/ivatagroupware-0.11.3.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=ivatagroupware \
                         -Dversion=0.11.13 \
                         -Dpackaging=jar

mvn --quiet package

mv target/ivatagroupware-testcases-1.0-jar-with-dependencies.jar ../../build/ivatagroupware-testcases.jar

################# Prints out Project Stats ######################### 
# total_classes=$(jar tf lib/batik-1.7.jar | grep '.*.class' | wc -l | xargs)
# total_classes_dependencies=$(($(jar tf lib/default-lib/xerces_2_5_0.jar | grep '.*.class' | wc -l) + \
#     $(jar tf lib/default-lib/xml-apis.jar | grep '.*.class' | wc -l) + \
#     $(jar tf lib/default-lib/xml-apis-ext.jar | grep '.*.class' | wc -l)))
# total_in_jar=$(jar tf ../../build/batik-testcases.jar | grep '.*.class' | wc -l)
# total_testcases=$(($total_in_jar - $total_classes_dependencies - $total_classes - 2))
# echo  -e "batik-1.7\t#Classes=${total_classes}\t#Classes in Dependencies=${total_classes_dependencies}\t#Test Cases=${total_testcases}"
####################################################################