#!/bin/bash

# creates the build directory to host the JAR files
mkdir -p build

# compiles the test case utilities
cd ./testcases/test-cases-utils
mvn --quiet clean 
mvn --quiet install -DskipTests
# comes back to previous folder
cd ../../

echo "Building XCorpus Test Cases"
for f in testcases/*; do
    if [ -d "$f" ] && [ -a "$f/build.sh" ]; then
        cd ${f} && ./build.sh && cd ../../
    fi
done

echo "Building CATS Test Cases"
for f in cats/*; do
    if [ -d "$f" ] && [ -a "$f/ser/Demo.java" ]; then
    	testcase_name="${f##*/}"
    	testcase_name=`echo $testcase_name|tr 's' 'S'`
    	cd ${f} && mkJar ser/Demo.java 1.8 Demo.jar && mv Demo.jar ../../build/${testcase_name}-JRE1.8.jar && cd ../../
    fi
done
