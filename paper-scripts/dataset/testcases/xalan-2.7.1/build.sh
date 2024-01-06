#!/bin/bash

cd ../test-cases-utils
mvn --quiet install

cd ../xalan-2.7.1


mvn --quiet install:install-file -Dfile=lib/xalan-2.7.1.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=xalan \
                         -Dversion=2.7.1 \
                         -Dpackaging=jar

mvn --quiet package

# move the normal version and copy the dependencies
mv target/xalan-testcases-1.0.jar ../../build/xalan-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/xalan-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/xalan-testcases-1.0.jar ../../build/lib/xalan-testcases/test-cases-utils-1.0.jar ../../build/xalan-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/xalan-testcases/test-cases-utils-1.0.jar
rm ../../build/xalan-testcases-1.0.jar

#VERSION 1 - WITH DEPENDENCIES
#mv target/xalan-testcases-1.0-jar-with-dependencies.jar ../../build/xalan-testcases.jar


################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/xalan-2.7.1.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=0
total_in_jar=$(jar tf ../../build/xalan-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "xalan-2.7.1\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################