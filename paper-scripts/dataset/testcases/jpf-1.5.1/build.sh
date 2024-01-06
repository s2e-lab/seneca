#!/bin/bash
cd ../test-cases-utils
mvn --quiet install

cd ../jpf-1.5.1

mvn --quiet install:install-file -Dfile=lib/jpf-1.5.1.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=jpf \
                         -Dversion=1.5.1 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/default-lib/ant-commons-logging.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=ant-commons-logging \
                         -Dversion=1.0 \
                         -Dpackaging=jar

mvn --quiet package

# move the normal version and copy the dependencies
mv target/jpf-testcases-1.0.jar ../../build/jpf-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/jpf-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/jpf-testcases-1.0.jar ../../build/lib/jpf-testcases/test-cases-utils-1.0.jar ../../build/jpf-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/jpf-testcases/test-cases-utils-1.0.jar
rm ../../build/jpf-testcases-1.0.jar

#VERSION 1 - WITH DEPENDENCIES
#mv target/jpf-testcases-1.0-jar-with-dependencies.jar ../../build/jpf-testcases.jar


################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/jpf-1.5.1.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=$(($(jar tf lib/default-lib/ant-commons-logging.jar | grep '.*.class' | wc -l)))
total_in_jar=$(jar tf ../../build/jpf-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "jpf-1.5.1\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################