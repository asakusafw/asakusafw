#In-process Hadoop Job Executor Plug-in for Asakusa Test Driver

This enables to run Hadoop jobs in test execution process.

Please add this artifact to the target project dependency, and put a system property

```
-Dasakusa.testdriver.exec.factory=com.asakusafw.testdriver.inprocess.InProcessJobExecutorFactory
```
on running flowpart/jobflow/batch test cases.

Note that, this still spawns sub-processes for command jobs such as ThunderGate or WindGate.
