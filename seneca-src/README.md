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
mvn clean package -DskipTests
```
This will generate a jar file with all dependencies inside the `target` folder (file with a suffix `-jar-with-dependencies.jar`). 
This jar file will then be copied to the parent directory and named as `seneca.jar`.


## Using Seneca

The `edu.rit.se.design.Seneca` class is used to build the call graph using the taint-based approach.
To check the list of command-line arguments for Seneca:

## Experiments implementation


- `oopsla.evaluation.SalsaCatsEval`: runs the experiments for the soundness of the call graph built by `Salsa` for the `CATs` benchmark.
From the top-level directory of this repository, run the following command:

```shell
java -cp seneca.jar -Dtestcase_folder=./paper-scripts/dataset/build/ -Dstatic_cgs_folder=./paper-scripts/static-cgs/ oopsla.evaluation.SalsaCatsEval 
```
This computed the call graph for the `CATs` benchmark using `Salsa` and store the results in the folder specified by `-Dstatic_cgs_folder`.



- `oopsla.evaluation.SenecaCatsEval`: runs the experiments for the soundness of the call graph built by `Seneca` for the `CATs` benchmark.

```shell
java -cp seneca.jar -Dtestcase_folder=./paper-scripts/dataset/build/ -Dstatic_cgs_folder=./paper-scripts/static-cgs/ oopsla.evaluation.SenecaCatsEval 
```

This computed the call graph for the `CATs` benchmark using `Seneca` and store the results in the folder specified by `-Dstatic_cgs_folder`.

- `oopsla.evaluation.RQ1XCorpus`: runs the experiments for the soundness, precision of the call graph built by `Seneca`, `Salsa` for the `XCorpus` benchmark. While computing these call graphs, the scripts also measure the time and extra number of iterations (which are needed to answer RQ3). 



```shell
java -cp seneca.jar -Dtestcase_folder=./paper-scripts/dataset/build/ -Dstatic_cgs_folder=./paper-scripts/static-cgs/ oopsla.evaluation.XCorpusEval 
```





## Questions, comments, or feedback?  
Please contact *Joanna C. S. Santos* (`@joannacss` - Bitbucket / `@joannacss` - GitHub)


