#!/bin/bash

cd ../test-cases-utils
mvn --quiet install

cd ../james-2.2.0


mvn --quiet install:install-file -Dfile=lib/james-2.2.0.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=james \
                         -Dversion=2.2.0 \
                         -Dpackaging=jar


mvn --quiet install:install-file -Dfile=lib/avalon-framework-api-4.3.1.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=avalon-framework-api \
                         -Dversion=4.3.1 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/default-lib/mail-1.3.1.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=mail \
                         -Dversion=1.3.1 \
                         -Dpackaging=jar


mvn --quiet package

# move the normal version and copy the dependencies
mv target/james-testcases-1.0.jar ../../build/james-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/james-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/james-testcases-1.0.jar ../../build/lib/james-testcases/test-cases-utils-1.0.jar ../../build/james-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/james-testcases/test-cases-utils-1.0.jar
rm ../../build/james-testcases-1.0.jar
#VERSION 1 - WITH DEPENDENCIES
#mv target/james-testcases-1.0-jar-with-dependencies.jar ../../build/james-testcases.jar

################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/james-2.2.0.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=$(( $(jar tf lib/avalon-framework-api-4.3.1.jar | grep '.*.class' | wc -l)
+ $(jar tf lib/default-lib/mail-1.3.1.jar | grep '.*.class' | wc -l)
))
total_in_jar=$(jar tf ../../build/james-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "james-2.2.0\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################