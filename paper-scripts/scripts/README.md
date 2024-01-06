# Requirements

- Java 8
- JAVA_HOME configured.
- Python 3.8+

## Setting Up Java Home

Configure the JAVA_HOME environment variable to point to where JDK is installed. Example:

`JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home`

# Folder Structure

## Python Scripts

- `compare_callgraphs.py`: it traverses the dir with static and runtime call graphs. It compares the static call graphs
  against their corresponding dynamic ones and reports the number of missing edges (i.e., edges in the runtime call
  graph but not in the static call graph).
- `compute_cats_precision.py`: script that computes the precision for the CATS dataset.
- `utils.py`: utility functions to read call graphs from a txt file in different formats.
- `download_jars.py`: it reads the list of maven libraries collected (artifacts.csv) and downloads their JAR file from
  maven central (it also downloads each API's dependencies).
- `convert_fingerprints.py`: it converts the fingerprints from the original format from Reif et al's study to the format
  in which each line is an edge with the source/dst method signatures.
- `inspect_mimatches.py`: utility script to help visualize the mismatches between static and dynamic call graphs.
 

## Shell Scripts
- `computeDynamicCGs.sh`: computes runtime call graphs using
  a [custom-built tool](https://github.com/joannacss/java-dynamic-callgraph).
- `rtAnalysis.sh` **[DEPRECATED]**: computes runtime call graphs using
  the [Java Call Graph tool](https://github.com/gousiosg/java-callgraph). This script is deprecated because the Java
  Call graph tool struggles with native code etc.
- `run_rq1.sh`: it computes the dynamic call graphs for each test case and then compute the metrics.
- `run_rq2.sh`: it computes the precision for our approach and saves the CSV results on the rq2 folder.

##  Configuration Files

- config.ini: not used for anything at this time.

## Library Files (inside `libs/` folder)

- `javacg-0.1-SNAPSHOT-dycg-agent.jar`:
- `gadget-inspector-all.jar`: it is the JAR file from Gadget Inspector that is ready to be run out of the box.
- `libdyncg.so`: 

