# Purpose

This folder has the configuration for DODO's program analysis.


## Subfolders / Files

- [J2EEs](J2EEs): has snapshots of the Java EE (specification). They were obtained from [Maven Central](https://mvnrepository.com/artifact/javax/javaee-api) and [Java2S](http://www.java2s.com/Code/Jar/j/Downloadjavaeeapi605jar.htm) websites.
- [JREs](JREs): snapshots of multiple JRE versions. They're obtained from [DOOP's benchmark](https://bitbucket.org/yanniss/doop-benchmarks/src/master/JREs/) repository.
- [default.properties](default.properties): sample configuration file.
- [default.properties_template](default.properties_template): just a template with an explanation of the expected values in the configuration file.
- [entrypoints.csv](default.properties): sample CSV file with a list of method signatures for entrypoints common in Web applications.
- [exclusions](exclusions): sample exclusions file
- [sinks.csv](sinks.csv): sample CSV file with a list of method signatures for sinks.
