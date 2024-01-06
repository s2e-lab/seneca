#!/bin/bash
cd ../test-cases-utils
mvn --quiet install

cd ../jgraph-5.13.0.0


mvn --quiet install:install-file -Dfile=lib/jgraph-5.13.0.0.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=jgraph \
                         -Dversion=5.13.0.0 \
                         -Dpackaging=jar
mvn --quiet package

# move the normal version and copy the dependencies
mv target/jgraph-testcases-1.0.jar ../../build/jgraph-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/jgraph-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/jgraph-testcases-1.0.jar ../../build/lib/jgraph-testcases/test-cases-utils-1.0.jar ../../build/jgraph-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/jgraph-testcases/test-cases-utils-1.0.jar
rm ../../build/jgraph-testcases-1.0.jar

#VERSION 1 - WITH DEPENDENCIES
#mv target/jgraph-testcases-1.0-jar-with-dependencies.jar ../../build/jgraph-testcases.jar


################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/jgraph-5.13.0.0.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=0
total_in_jar=$(jar tf ../../build/jgraph-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "jgraph-5.13.0.0.jar\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################