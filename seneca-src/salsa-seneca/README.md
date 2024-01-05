# SENECA: Serialization-aware Call Graph Construction for Java Applications

This repository contains the source code for Seneca, a taint-based call graph construction for Java applications. 
This tool is part of the following paper:

```
Santos, J.C.S., Mirakhorli, M. & Shokri, Ali. "Seneca: Taint-Based Call Graph Construction for Java Object Deserialization."
OOPSLA (2024). https://doi.org/10.1145/nnnnnnn.nnnnnnn
```


## Repository Structure
The code has two submodules as follows:

- [salsa-seneca-core](salsa-seneca): used to build serialization-aware call graphs. This module has the implementation for two approaches: downcast-based (**Salsa**), and taint-based (**Seneca**). **Salsa** was a tool that was previously developed by the authors of this paper in a previous publication. **Seneca** is the new tool that is presented in this paper. 
- [salsa-seneca-utils](salsa-seneca-utils): Utility classes to manipulate Wala data structures and visualize call graphs. 

## Building the Project
This project uses Maven to build the code. All dependencies need to compile this project are listed in the `pom.xml` file.
To build the project, run the following command:

```
mvn clean install
```

## Running the Project
The project has two main classes: `Seneca` and `Salsa`. 
The `Seneca` class is used to build the call graph using the taint-based approach. The `Salsa` class is used to build the call graph using the downcast-based approach. Both classes have the same command-line arguments. The following is the list of command-line arguments:

```
usage: Seneca
 -appJar <arg>           Path to the application jar file
 -exclusionFile <arg>    Path to the exclusion file
 -outputDir <arg>        Path to the output directory
 -pta <arg>              Points-to analysis to use: zero-one-cfa, n-cfa,
                         vanilla-zero-one-cfa, vanilla-n-cfa, zero-one-cfa-
                         context, n-cfa-context, vanilla-zero-one-cfa-
                         context, vanilla-n-cfa-context, object-sensitive,
                         vanilla-object-sensitive, type-sensitive,
                         vanilla-type-sensitive, zero-one-cfa-type, n-cfa-
                         type, vanilla-zero-one-cfa-type, vanilla-n-cfa-
                         type, zero-one-cfa-type-context, n-cfa-type-
                         context, vanilla-zero-one-cfa-type-context,
                         vanilla-n-cfa-type-context, object-sensitive-type,
                         vanilla-object-sensitive-type, type-sensitive-
                         type, vanilla-type-sensitive-type
 -taintAnalysis <arg>    Taint analysis to use: flowdroid, heros
```

The following is an example of how to run the `Seneca` class:

```
java -cp salsa-seneca-core-1.0-SNAPSHOT-jar-with-dependencies.jar edu.vanderbilt.isis.seneca.Seneca -appJar /path/to/app.jar -exclusionFile /path/to/exclusion.txt -outputDir /path/to/output/dir -pta vanilla-zero-one-cfa -taintAnalysis flowdroid
```