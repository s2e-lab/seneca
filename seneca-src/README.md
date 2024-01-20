# edu.rit.se.design.callgraph.cli.Salsa

Source code for **Seneca**.
It improves existing off-the-shelf pointer analysis to support the construction of callgraph that handles programs with serialization/deserialization.


## Requirements 

* Maven 3.x
* Java 1.8

## Built With 
* [WALA](https://github.com/WALA/wala)


## Compiling the Code

The code is already compiled with all dependencies inside the [target folder](target/seneca-0.2-jar-with-dependencies.jar).
But if you'd like to compile the code yourself, you can do so by:

1. `cd` to the root directory of the project (`seneca-src`) if you aren't in this folder already.
2. run the following command:
```bash
mvn clean compile assembly:single
```
This will generate a jar file with all dependencies inside the `target` folder (file with a suffix `-jar-with-dependencies.jar`).


## Using Seneca

The `edu.rit.se.design.Seneca` class is used to build the call graph using the taint-based approach.
To check the list of command-line arguments for Seneca:

```bash
java -jar seneca-src/target/seneca-1.0-jar-with-dependencies.jar
```


## Running the experiments
This section describes how to reproduce the experiments for the paper.


### RQ1 (Soundness) - Section 5.1
This RQ uses two different benchmarks: `CATs` and `XCorpus`. These experiments are implemented in `oopsla.evaluation.SalsaCatsEval`, `oopsla.evaluation.RQ1SenecaCats`, `oopsla.evaluation.RQ1XCorpus`.

#### Reproducing the results for RQ1 - CATs benchmark (Section 5.1.1)

- `oopsla.evaluation.SalsaCatsEval`: runs the experiments for the soundness of the call graph built by `Salsa` for the `CATs` benchmark.
From the top-level directory of this repository, run the following command:

```shell
java -cp seneca.jar -Dtestcase_folder=./paper-scripts/dataset/build/ -Dstatic_cgs_folder=./target/rq1/ oopsla.evaluation.SalsaCatsEval 
```
This computed the call graph for the `CATs` benchmark using `Salsa` and store the results in the folder `./target/rq1/`.



- `oopsla.evaluation.SenecaCatsEval`: runs the experiments for the soundness of the call graph built by `Seneca` for the `CATs` benchmark.

```shell
java -cp seneca.jar -Dtestcase_folder=./paper-scripts/dataset/build/ -Dstatic_cgs_folder=./target/rq1/ oopsla.evaluation.SenecaCatsEval 
```

This computed the call graph for the `CATs` benchmark using `Seneca` and store the results in the folder `./target/rq1/`.

- `oopsla.evaluation.RQ1XCorpus`: runs the experiments for the soundness of the call graph built by `Seneca`, `Salsa` for the `XCorpus` benchmark.



## Questions, comments, or feedback?  
Please contact *Joanna C. S. Santos* (`@joannacss` - Bitbucket / `@joannacss` - GitHub)


