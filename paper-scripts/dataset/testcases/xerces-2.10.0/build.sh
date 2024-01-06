#!/bin/bash

cd ../test-cases-utils
mvn --quiet install

cd ../xerces-2.10.0


mvn --quiet install:install-file -Dfile=lib/xerces-2.10.0.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=xerces \
                         -Dversion=2.10.0 \
                         -Dpackaging=jar

mvn --quiet package 

# move the normal version and copy the dependencies
mv target/xerces-testcases-1.0.jar ../../build/xerces-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/xerces-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/xerces-testcases-1.0.jar ../../build/lib/xerces-testcases/test-cases-utils-1.0.jar ../../build/xerces-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/xerces-testcases/test-cases-utils-1.0.jar
rm ../../build/xerces-testcases-1.0.jar

#VERSION 1 - WITH DEPENDENCIES
#mv target/xerces-testcases-1.0-jar-with-dependencies.jar ../../build/xerces-testcases.jar

################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/xerces-2.10.0.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=0
total_in_jar=$(jar tf ../../build/xerces-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "xerces-2.10.0\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################