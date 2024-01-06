#!/bin/bash

cd ../test-cases-utils
mvn --quiet install

cd ../batik-1.7
mvn --quiet install:install-file -Dfile=lib/batik-1.7.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=batik \
                         -Dversion=1.7 \
                         -Dpackaging=jar


mvn --quiet install:install-file -Dfile=lib/default-lib/xerces_2_5_0.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=xerces \
                         -Dversion=2.5.0 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/default-lib/xml-apis.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=xml-apis \
                         -Dversion=1.0 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/default-lib/xml-apis-ext.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=xml-apis-ext \
                         -Dversion=1.0 \
                         -Dpackaging=jar

mvn --quiet package

# move the normal version and copy the dependencies
mv target/batik-testcases-1.0.jar ../../build/batik-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/batik-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/batik-testcases-1.0.jar ../../build/lib/batik-testcases/test-cases-utils-1.0.jar ../../build/batik-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/batik-testcases/test-cases-utils-1.0.jar
rm ../../build/batik-testcases-1.0.jar


# VERSION 1: JAR with all dependencies
# move the version with all the dependencies included
#mv target/batik-testcases-1.0-jar-with-dependencies.jar ../../build/batik-testcases.jar



################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/batik-1.7.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=$(($(jar tf lib/default-lib/xerces_2_5_0.jar | grep '.*.class' | wc -l) + \
    $(jar tf lib/default-lib/xml-apis.jar | grep '.*.class' | wc -l) + \
    $(jar tf lib/default-lib/xml-apis-ext.jar | grep '.*.class' | wc -l)))
total_in_jar=$(jar tf ../../build/batik-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "batik-1.7\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################