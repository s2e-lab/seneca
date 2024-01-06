# SENECA: Taint-Based Call Graph Construction for Java Object Deserialization

This repository contains the source code for `Seneca`, a taint-based call graph construction for Java programs. This tool is part of the following paper:

```
Santos, J.C.S., Mirakhorli, M. & Shokri, Ali. "Seneca: Taint-Based Call Graph Construction for Java Object Deserialization."
OOPSLA (2024). https://doi.org/10.1145/nnnnnnn.nnnnnnn
```


As shown in the figure below, to support serialization-related features, `Seneca` employs an on-the-fly iterative call graph construction technique that involves two major phases: 

**(1)** Iterating over a worklist of methods to create the initial call graph using an underlying pointer analysis method; 

**(2)** Refinement of the initial call graph by making a set of assumptions performed iteratively until a fixpoint is reached (i.e., when there are no more methods left in the worklist to
be visited).

![An overview of Seneca](seneca.png)




The technical details of how the approach perform these two phases is described in the **Section 3** of the paper.


## Repository Structure

This repository has two folders:

- `paper-scripts`: scripts used to compute the results shown in the paper (Section 4).
- `seneca-src`: the actual source code for seneca.



## Using Seneca (`seneca-src`)


### Requirements
- Java 8 (and above)
- Maven (3.8+) 

The code was tested with Java 11 and Maven 3.8.7 on macOS Monterey with 32 GB  of memory.



### Running Seneca using Java directly (through its Command Line Interface)

The code has been compiled using Java 11 with all of its dependencies and it is located on this artifact's root folder (file: `seneca.jar`).

`Seneca` has a command line interface that can be used as follows:

```
java -jar seneca.jar <parameters>
```

**Required Parameters:**

```
 -f,--format <arg>          Output format (possible values: dot, jdyn). JDyn is a custom format that saves the call graph as tuples (caller, callee)
 -j,--jar <arg>             Path to the project's JAR file 
 -o,--output <arg>          Path to the output file with the serialized call graph
 -pta,--main-policy <arg>   Pointer analysis choice (n-CFA, 0-n-CFA)
```

**Optional Parameters:**

```
 -e,--exclusions <arg>      Path to an exclusions file
    --view-ui               Shows call graph in a Java Swing UI
    --print-models          Prints to the console all the synthetic methods created
```



***Sample Usage using the provided compiled JAR:***

The command below will generate a call graph for the program example in the paper (**Listing 3**) using `0-1-CFA` as the pointer analysis policy. The call graph is serialized into the DOT format saved on the file `OOPSLAPaperExample.dot` and it will also open a Window with the call graph.

```bash
java -jar seneca.jar \
		-j paper-scripts/dataset/sample-programs/OOPSLAPaperExample-JRE1.7.jar \
		-f dot \
		-o OOPSLAPaperExample.dot \
		-pta 0-1-CFA \
		--view-ui
```



### Running Seneca using Docker

1. Build the docker image

	```
	docker build -t seneca .
	```

2. Run it using:

	```
	docker run -it --rm seneca <parameters>
	```



***Sample Usage using the Dockerfile***



```bash
docker run -it --rm seneca -j /usr/src/seneca/paper-scripts/dataset/sample-programs/OOPSLAPaperExample-JRE1.7.jar -f dot -o OOPSLAPaperExample.dot -pta 0-1-CFA 
```



## Results Folder

All the results presented in **Section 5** are located in the folder `paper-scripts`.
The [README.md](paper-scripts/README.md) in that folder contains a detailed description of each file and how they related to the results for each research question.

