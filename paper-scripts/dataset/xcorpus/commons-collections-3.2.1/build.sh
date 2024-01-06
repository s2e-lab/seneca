#!/bin/bash

cd ../test-cases-utils
mvn --quiet install

cd ../commons-collections-3.2.1


mvn --quiet install:install-file -Dfile=lib/commons-collections-3.2.1.jar \
                         -DgroupId=commons-collections \
                         -DartifactId=commons-collections \
                         -Dversion=3.2.1 \
                         -Dpackaging=jar

mvn --quiet package


# move the normal version and copy the dependencies
mv target/commons-collections-testcases-1.0.jar ../../build/commons-collections-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/commons-collections-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/commons-collections-testcases-1.0.jar ../../build/lib/commons-collections-testcases/test-cases-utils-1.0.jar ../../build/commons-collections-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/commons-collections-testcases/test-cases-utils-1.0.jar
rm ../../build/commons-collections-testcases-1.0.jar

#VERSION 1: JAR with all dependencies
#mv target/commons-collections-testcases-1.0-jar-with-dependencies.jar ../../build/commons-collections-testcases.jar


################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/commons-collections-3.2.1.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=0
total_in_jar=$(jar tf ../../build/commons-collections-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "commons-collections-3.2.1\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################