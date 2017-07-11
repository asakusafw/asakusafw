# Asakusa Framework Integration Tests

This module provides integration tests for this repository. It checks a combination of the runtime, compiler and Gradle plug-ins.

## Running tests

```sh
./gradlew integrationTest
```

### Available options

* `-PmavenLocal`
  * use artifacts of *test tools* on local repository
  * default: never use artifacts on local repository
* `-Dmaven.local=false`
  * use artifacts of *testee* only on remote repositories
  * default: `true` (use artifacts on local repositories)
* `-Dhadoop.cmd=/path/to/bin/hadoop`
  * use the specified `hadoop` command
  * default: N/A (**skip** `hadoop` command required tests)
* `-Dssh.key=/path/to/.ssh/id_localhost`
  * SSH private key path for `ssh://localhost:22`
  * default: N/A (**skip** SSH related tests)
* `-Dssh.pass=****`
  * pass-phrase of private key specified in `-Dssh.key`
  * default: (empty pass-phrase)
* `-Dsdk.incubating`
  * test with Asakusa SDK incubating features
  * default: `false` (disable incubating features, and **skip** incubating features related tests)
* `-Dasakusafw.version=x.y.z`
  * change Asakusa Framework version (`>= 0.9.0`)
  * default: (current version)
* `-Dgradle.version=x.y`
  * change Gradle version (`>= 3.5`)
  * default: (tooling API version)
* `-Dorg.slf4j.simpleLogger.defaultLogLevel=level`
  * change log level
  * default: `debug`

### Tips: running tests on Eclipse

1. Run `./gradlew eclipse`.
  * with `-PmavenLocal`
    * use Maven local repository to resolve dependencies
  * with `-PreferProject`
    * use related projects on Eclipse workspace
2. Import this project into the Eclipse workspace.
3. Configure *Run Configuration* of JUnit tests:
  * Add `-Dasakusafw.version=x.y.z` to `Arguments > VM arguments`
  * Add other options to `Arguments > VM arguments`
4. Run JUnit test
