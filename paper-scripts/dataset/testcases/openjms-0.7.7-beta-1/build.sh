#!/bin/bash
cd ../test-cases-utils
mvn --quiet install

cd ../openjms-0.7.7-beta-1

mvn --quiet install:install-file -Dfile=lib/openjms-0.7.7-beta-1.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=jms \
                         -Dversion=0.7.7-beta-1 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/commons-logging-1.2.jar \
                         -DgroupId=commons-logging \
                         -DartifactId=commons-logging \
                         -Dversion=1.2 \
                         -Dpackaging=jar

mvn --quiet package

# move the normal version and copy the dependencies
mv target/openjms-testcases-1.0.jar ../../build/openjms-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/openjms-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/openjms-testcases-1.0.jar ../../build/lib/openjms-testcases/test-cases-utils-1.0.jar ../../build/openjms-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/openjms-testcases/test-cases-utils-1.0.jar
rm ../../build/openjms-testcases-1.0.jar

#VERSION 1 - WITH DEPENDENCIES
#mv target/openjms-testcases-1.0-jar-with-dependencies.jar ../../build/openjms-testcases.jar



################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/openjms-0.7.7-beta-1.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=$(($(jar tf lib/commons-logging-1.2.jar | grep '.*.class' | wc -l)))
total_in_jar=$(jar tf ../../build/openjms-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "openjms-0.7.7-beta-1\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################