# Overview

## Folder structure

- `FeatureAnalysis.csv`: It lists projects from XCorpus and a feature analysis where we compute the number of classes with custom serialization/deserialization call back methods. 
- `dataset`: the projects used for evaluation (programs from the XCorpus dataset and  CATS dataset).
- `static-cgs`: static call graphs computed using Seneca, Salsa, Soot, Doop, Wala, and OPAL. The static call graphs for Soot, Doop, Wala, and OPAL were retrieved from Reif's et al Docker image.
- `runtime-cgs`: dynamic call graphs computed using JVMTI.
- `scripts`: auxiliary shell/python scripts to automate the generation of dynamic call graphs and compute metrics (ex: precision).
- `fingerprints`: results obtained from running the [docker image by Reif et al 2019](https://hub.docker.com/r/mreif/jcg) to compute the fingerprints for all adapters (Soot, Wala, Doop, and OPAL) and  the "SER" test case category.
- `Results.xlsx`: a spreadsheet that compiles all the results into charts and tables.


## Reproducing Results




### RQ1 Results (Section 5.1)

- **Associated Script(s)**: `./scripts/run_rq1.sh`
- **Results location**: These results were obtained by comparing the static call graphs (located within static-cgs) against the runtime call graphs located in the folder [runtime-cgs](runtime-cgs).
- **Spreadsheet tabs on Results.xlsx**: 
	- RQ1-XCorpus-Soundness
	- RQ1-CATS
	- RQ1-CATS-Sizes
- **How to reproduce RQ1 results**: 
	- Execute the script `./run_rq1.sh`
	- The command above will take some minutes to complete. This script will run a python script that compares the static call graph with the runtime call graph to create a summary of the differences (saved on `results/rq1/*.csv`).


## RQ2 Results (Section 5.2)

- **Associated Script(s)**: `./scripts/run_rq2.sh`
- **Results location**: These results were obtained by comparing the static call graphs (located within static-cgs) against the runtime call graphs located in the folder [runtime-cgs](runtime-cgs), but with the difference that we search for edges in the static call graph but that are not in the runtime call graph (imprecise).
- **Spreadsheet tabs on Results.xlsx**: 
	- RQ2-CATS-Precision
	- RQ2-XCorpus-Precision
- **How to reproduce RQ2 results**: _(You **must** have executed `run_rq1.sh` first so that all static call graphs and dynamic call graphs are computed and saved.)_
	- Execute the script `./run_rq2.sh`
	- Results will be saved on `results/rq2/*.csv`.




## RQ3 Results (Section 5.3)
- **Spreadsheet tabs on Results.xlsx**: 
	- RQ3-Performance
- **Associated Script(s)**: timestamps are already computed when running RQ1. That script already invoked Seneca that generated time information and saved on `../static-cgs/TimeStats-YYYY-MM-DD NN-NN-NN-NNN.txt`

## RQ4 Results (Section 5.4)
- **Associated Script(s)**: `./scripts/run_rq4.sh`
- **Spreadsheet tabs on Results.xlsx**: 
	- RQ4-Vulnerabilities