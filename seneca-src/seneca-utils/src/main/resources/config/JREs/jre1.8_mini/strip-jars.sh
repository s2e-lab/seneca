#!/usr/bin/env bash

cp ${DOOP_BENCHMARKS}/JREs/jre1.8/lib/*.jar .
rm original-*.jar

for j in *.jar; do
    ORIGINAL_JAR="original-${j}"
    cp ${j} ${ORIGINAL_JAR}
    BASENAME=$(basename ${j} .jar)
    rm -rf ${BASENAME}
    mkdir ${BASENAME}
    cd ${BASENAME}
    jar xf ../${j}
    rm -rf com/oracle com/sun sun java/applet java/awt javax/swing java/beans javax/management org/omg org/xml
    cd ..
    rm ${j}
    jar cf ${j} -C ${BASENAME} .
    echo "JAR ${j}: "$(jar tf ${j} | wc -l)" entries (from "$(jar tf ${ORIGINAL_JAR} | wc -l)" entries)"
done
