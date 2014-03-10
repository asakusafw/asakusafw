#In-process Hadoop Job Executor Plug-in for Asakusa Test Driver

This enables to run Hadoop jobs in test execution process.

Please add this artifact to the target project dependency, and run flowpart/jobflow/batch test cases.
You can disable this feature to set "-Dasakusa.testdriver.configurator.inprocess=false"

Note that, this still spawns sub-processes for command jobs such as ThunderGate or WindGate.
