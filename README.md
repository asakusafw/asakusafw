# Asakusa Framework

Asakusa is a full stack framework for distributed/parallel computing, which provides with a development platform and runtime libraries supporting various distributed/parallel computing environments such as [Hadoop](https://hadoop.apache.org), [Spark](https://spark.apache.org), [M<sup>3</sup> for Batch Processing](https://github.com/fixstars/m3bp), and so on. Users can enjoy the best performance on distributed/parallel computing transparently changing execution engines among MapReduce, SparkRDD, and C++ native based on their data size.

Other than query-based languages, Asakusa helps to develop more complicated data flow programs more easily, efficiently, and comprehensively due to following components.

* Data-flow oriented DSL

  Data-flow based approach is suitable for DAG constructions which is appropriate for distributed/parallel computing. Asakusa offers Domain Specific Language based on Java with data-flow design, which is integrated with compilers.

* Compilers

  A multi-tier compiler is supported. Java based source code is once compiled to inter-mediated representation and then optimized for each execution environments such that Hadoop(MapReduce), Spark(RDD), M<sup>3</sup> for Batch Processing(C++ Native), respectively.

* Data-Modeling language

  Data-Model language is supported, which is comprehensive for mapping with relational models, CSVs, or other data formats.

* Test Environment

  JUnit based unit testing and end-to-end testing are supported, which are portable among each execution environments. Source code, test code, and test data are fully compatible across Hadoop, Spark, M<sup>3</sup> for Batch Processing and others.

* Runtime execution driver

  A transparent job execution driver is supported.

All these features have been well designed and developed with the expertise from experiences on enterprise-scale system developments over decades and promised to contribute to large scale systems on distributed/parallel environments to be more robust and stable.

## How to build

### Maven artifacts

```sh
./mvnw clean install -DskipTests
```

### Gradle plug-ins

```sh
cd gradle
./gradlew clean [build] install
```

## How to run tests

### Maven artifacts

```sh
export HADOOP_CMD=/path/to/bin/hadoop
./mvnw test
```

### Gradle plug-ins

```sh
cd gradle
./gradlew [clean] check
```

## How to import projects into Eclipse

### Maven artifacts

```sh
./mvnw eclipse:eclipse
```

And then import existing projects from Eclipse.

If you run tests in Eclipse, please activate `Preferences > Java > Debug > 'Only include exported classpath entries when launching'`.

### Gradle plug-ins

```sh
cd gradle
./gradlew eclipse
```

And then import existing projects from Eclipse.

## Sub Projects
* [Asakusa Framework Language Toolset](https://github.com/asakusafw/asakusafw-compiler)
* [Asakusa on Spark](https://github.com/asakusafw/asakusafw-spark)
* [Asakusa on M<sup>3</sup>BP](https://github.com/asakusafw/asakusafw-m3bp)
* [Asakusa Framework Documentation](https://github.com/asakusafw/asakusafw-documentation)

## Related Projects
* [Asakusa Framework Examples](https://github.com/asakusafw/asakusafw-examples)
* [Asakusa Framework Legacy Modules](https://github.com/asakusafw/asakusafw-legacy)
* [Jinrikisha](https://github.com/asakusafw/asakusafw-starter)
* [Shafu](https://github.com/asakusafw/asakusafw-shafu)

## Resources
* [Asakusa Framework Documentation (ja)](https://docs.asakusafw.com/)
* [Asakusa Framework Community Site (ja)](https://asakusafw.com)

## Bug reports, Patch contribution
* Please report any issues to [repository for issue tracking](https://github.com/asakusafw/asakusafw-issues/issues)
* Please contribute with patches according to our [contribution guide (Japanese only, English version to be added)](https://docs.asakusafw.com/latest/release/ja/html/contribution.html)

## License
* [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
