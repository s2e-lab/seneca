#!/bin/bash
cd ../test-cases-utils
mvn --quiet install

cd ../htmlunit-2.8

mvn --quiet install:install-file -Dfile=lib/htmlunit-2.8.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=htmlunit \
                         -Dversion=2.8 \
                         -Dpackaging=jar
mvn --quiet package

# move the normal version and copy the dependencies
mv target/htmlunit-testcases-1.0.jar ../../build/htmlunit-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/htmlunit-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/htmlunit-testcases-1.0.jar ../../build/lib/htmlunit-testcases/test-cases-utils-1.0.jar ../../build/htmlunit-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/htmlunit-testcases/test-cases-utils-1.0.jar
rm ../../build/htmlunit-testcases-1.0.jar

#VERSION 1 - WITH DEPENDENCIES
#mv target/htmlunit-testcases-1.0-jar-with-dependencies.jar ../../build/htmlunit-testcases.jar


################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/htmlunit-2.8.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=0
total_in_jar=$(jar tf ../../build/htmlunit-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "htmlunit-2.8\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################