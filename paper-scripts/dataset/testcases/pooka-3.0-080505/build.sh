#!/bin/bash
cd ../test-cases-utils
mvn --quiet install

cd ../pooka-3.0-080505

mvn --quiet install:install-file -Dfile=lib/pooka-3.0-080505.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=pooka \
                         -Dversion=3.0-080505 \
                         -Dpackaging=jar

mvn --quiet package

# move the normal version and copy the dependencies
mv target/pooka-testcases-1.0.jar ../../build/pooka-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/pooka-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/pooka-testcases-1.0.jar ../../build/lib/pooka-testcases/test-cases-utils-1.0.jar ../../build/pooka-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/pooka-testcases/test-cases-utils-1.0.jar
rm ../../build/pooka-testcases-1.0.jar

#VERSION 1 - WITH DEPENDENCIES
#mv target/pooka-testcases-1.0-jar-with-dependencies.jar ../../build/pooka-testcases.jar


################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/pooka-3.0-080505.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=0
total_in_jar=$(jar tf ../../build/pooka-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "pooka-3.0-080505\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################