# SENECA: Serialization-aware Call Graph Construction for Java Applications


The code has multiple submodules as follows:

- [Salsa-Seneca](Salsa-Seneca): used to build serialization-aware call graphs (using two approaches: downcast-based -- Salsa -- and taint-based -- Seneca).
- [Utils](Utils): Utility classes used across submodules.
- [config](../config): Configuration of the project (such as sink statements, entrypoints, Java runtime version, etc)


## Setting up the dependencies
All dependencies need to compile this project are listed in the `pom.xml` file. 

## Static Analyzer Module

This submodule of the DODO project is used to extract a series of analysis in a project, generating a `ProjectAnalysis` object. This object caches multiple data structures that are needed for source code analysis (ex: call graph, class hierarchy information etc).


### Configuration File 
To set up the analysis you have to create a properties file (`*.properties`) that has the following format:


```
CONFIG_FOLDER    = ...   # path to where the "config" folder is 
                         # (this config folder contains the JAR files for multiple JRE/J2EE versions)
JRE_VERSION      = ...   # the JRE version (JRE1_8, JRE1_7, JRE1_6, JRE1_5, JRE1_4, JRE1_3) 
J2EE_VERSION     = ...   # the J2EE version (J2EE8, J2EE7, J2EE6)
ENTRYPOINTS_FILE = ...   # CSV file that has a list of entrypoint methods signatures
SINKS_FILE       = ...   # CSV file that a list of sink methods signatures
EXCLUSIONS_FILE  = ...   # TXT file lists classes that should be excluded from the class hierarchy 
                         # (i.e., previously known to be irrelevant for analysis (e.g. UI classes from java)
DEPENDENCIES.    = ...   # (optional) a comma-separated list of JARs/folders to be included in the classpath. 
                         # If it is a folder, any JAR file in that folder is included in the classpath
```


#### Entrypoint Methods Format (ENTRYPOINTS_FILE) 


#### Sink Methods Format (SINKS_FILE)


#### Exclusion File Format (EXCLUSIONS_FILE)
A text file that contains a list of classes/packages to be disregarded during analysis. See exclusions.txt inside the config folder for an example.



## Object Generation Module


## Salsa/Seneca Module