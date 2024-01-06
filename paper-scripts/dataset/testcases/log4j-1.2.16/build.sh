#!/bin/bash
cd ../test-cases-utils
mvn --quiet install

cd ../log4j-1.2.16

mvn --quiet install:install-file -Dfile=lib/log4j-1.2.16.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=log4j \
                         -Dversion=1.2.16 \
                         -Dpackaging=jar
mvn --quiet package

# move the normal version and copy the dependencies
mv target/log4j-testcases-1.0.jar ../../build/log4j-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/log4j-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/log4j-testcases-1.0.jar ../../build/lib/log4j-testcases/test-cases-utils-1.0.jar ../../build/log4j-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/log4j-testcases/test-cases-utils-1.0.jar
rm ../../build/log4j-testcases-1.0.jar

#VERSION 1 - WITH DEPENDENCIES
#mv target/log4j-testcases-1.0-jar-with-dependencies.jar ../../build/log4j-testcases.jar


################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/log4j-1.2.16.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=0
total_in_jar=$(jar tf ../../build/log4j-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "log4j-1.2.16.jar\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################