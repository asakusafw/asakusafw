# Asakusa Framework Integration Tests

This module provides integration tests for this repository. It checks a combination of Asakusa Framework runtime, compiler and Gradle plug-ins.

## Running tests

```sh
./gradlew integrationTest
```

* Available options
  * `-Dhadoop.cmd`
    * use the specified `hadoop` command
    * default: N/A (**skip** `hadoop` command required tests)
  * `-PmavenLocal`
    * use artifacts of *test tools* on local repository
    * default: never use artifacts on local repository
  * `-Dmaven.local=false`
    * use artifacts of *testee* only on remote repositories
    * default: `true` (use artifacts on local repositories)
  * `-Dasakusafw.version=x.y.z`
    * change Asakusa Framework version (`>= 0.9.0`)
    * default: (current version)
  * `-Dgradle.version=x.y`
    * change Gradle version (`>= 3.5`)
    * default: (tooling API version)
  * `-Dorg.slf4j.simpleLogger.defaultLogLevel=level`
    * change log level
    * default: `debug`
  * `-Dssh.key`
    * SSH private key path for `ssh://localhost:22`
    * default: N/A (**skip** SSH related tests)
  * `-Dssh.pass`
    * pass-phrase of private key specified in `-Dssh.key`
    * default: (empty pass-phrase)
