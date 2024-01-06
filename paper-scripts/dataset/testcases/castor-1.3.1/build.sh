#!/bin/bash


cd ../test-cases-utils
mvn --quiet install

cd ../castor-1.3.1


mvn --quiet install:install-file -Dfile=lib/castor-1.3.1.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=castor \
                         -Dversion=1.3.1 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/castor-xml-1.3.1.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=castor-xml \
                         -Dversion=1.3.1 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/castor-xml-schema-1.3.1.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=castor-xml-schema \
                         -Dversion=1.3.1 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/castor-codegen-1.3.1.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=castor-codegen \
                         -Dversion=1.3.1 \
                         -Dpackaging=jar


mvn --quiet package

# move the normal version and copy the dependencies
mv target/castor-testcases-1.0.jar ../../build/castor-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/castor-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/castor-testcases-1.0.jar ../../build/lib/castor-testcases/test-cases-utils-1.0.jar ../../build/castor-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/castor-testcases/test-cases-utils-1.0.jar
rm ../../build/castor-testcases-1.0.jar

#VERSION 1: JAR with all dependencies
#mv target/castor-testcases-1.0-jar-with-dependencies.jar ../../build/castor-testcases.jar

################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/castor-1.3.1.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=$(($(jar tf lib/castor-xml-1.3.1.jar | grep '.*.class' | wc -l) + \
    $(jar tf lib/castor-xml-schema-1.3.1.jar | grep '.*.class' | wc -l) + \
    $(jar tf lib/castor-codegen-1.3.1.jar | grep '.*.class' | wc -l)))
total_in_jar=$(jar tf ../../build/castor-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "castor-1.3.1\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################