# Dataset Folder Overview

**Scripts**:

- `buildTestCases.sh`: runs all the `build.sh` scripts within the test cases subfolders.
- `mergeJars.sh`: it merges two JAR files into a single jar.

**Folders:**

- `build`: it contains all the JAR files for the compiled test projects (along with their dependencies).
- `cats`: it contains the test cases
  from [CATS](https://bitbucket.org/delors/cats/src/master/jcg_testcases/src/main/resources/Serialization.md) to verify
  how an algorithm handles callbacks related to the `java.io.Serializable` classes.
- `testcases`: this folder contains all the test cases manually created for evaluation. The JAR files for the projects
  from the XCorpus are inside the `lib` subfolder in each test case project.
- `vulnerabilities`: vulnerable OSS projects to evaluate Seneca's efficiency.




