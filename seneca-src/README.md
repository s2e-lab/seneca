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
This will generate a jar file with all dependencies inside the `target` folder.




## Using Seneca

The `Seneca` class is used to build the call graph using the taint-based approach.
The following is the list of command-line arguments:

```bash
java -jar seneca-core/target/seneca-core-0.2-jar-with-dependencies.jar
```

The following is an example of how to run the `Seneca` class:

```bash
```


## Questions, comments, or feedback?  
* For Bugs/Questions: *File a bug report.*
* Improvement feedback: please contact *Joanna C. S. Santos* (`@joannacss` - Bitbucket / `@joannacss` - GitHub)


