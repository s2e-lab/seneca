#!/bin/bash

cd ../test-cases-utils
mvn --quiet install

cd ../tomcat-7.0.2

mvn --quiet install:install-file -Dfile=lib/tomcat-7.0.2.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=tomcat \
                         -Dversion=7.0.2 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/tomcat-catalina-7.0.2.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=tomcat-catalina \
                         -Dversion=7.0.2 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/tomcat-catalina-ha-7.0.2.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=tomcat-catalina-ha \
                         -Dversion=7.0.2 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/tomcat-coyote-7.0.2.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=tomcat-coyote \
                         -Dversion=7.0.2 \
                         -Dpackaging=jar

mvn --quiet install:install-file -Dfile=lib/tomcat-dbcp-7.0.2.jar \
                         -DgroupId=xcorpus \
                         -DartifactId=tomcat-dbcp \
                         -Dversion=7.0.2 \
                         -Dpackaging=jar
mvn --quiet package


# move the normal version and copy the dependencies
mv target/tomcat-testcases-1.0.jar ../../build/tomcat-testcases-1.0.jar
mvn --quiet dependency:copy-dependencies -DoutputDirectory=../../build/lib/tomcat-testcases/
# merge the JAR files
../../mergeJars.sh ../../build/tomcat-testcases-1.0.jar ../../build/lib/tomcat-testcases/test-cases-utils-1.0.jar ../../build/tomcat-testcases.jar
# remove the older versions (no longer necessary)
rm ../../build/lib/tomcat-testcases/test-cases-utils-1.0.jar
rm ../../build/tomcat-testcases-1.0.jar

#VERSION 1 - WITH DEPENDENCIES
#mv target/tomcat-testcases-1.0-jar-with-dependencies.jar ../../build/tomcat-testcases.jar


################# Prints out Project Stats ######################### 
total_classes=$(jar tf lib/tomcat-7.0.2.jar | grep '.*.class' | wc -l | xargs)
total_classes_dependencies=$(($(jar tf lib/tomcat-catalina-7.0.2.jar | grep '.*.class' | wc -l) + \
    $(jar tf lib/tomcat-catalina-ha-7.0.2.jar | grep '.*.class' | wc -l) + \
    $(jar tf lib/tomcat-coyote-7.0.2.jar | grep '.*.class' | wc -l) + \
    $(jar tf lib/tomcat-dbcp-7.0.2.jar | grep '.*.class' | wc -l)))
total_in_jar=$(jar tf ../../build/tomcat-testcases.jar | grep '.*.class' | wc -l)
total_testcases=$(ls src/main/java/testcases/*.java | wc -l | xargs)
echo  -e "tomcat-7.0.2\t${total_classes}\t${total_classes_dependencies}\t${total_testcases}"
####################################################################