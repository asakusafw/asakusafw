# Asakusa Framework
Asakusa is a Hadoop-based Enterprise Batch Processing Framework, to improve the efficiency of General Enterprise Systems (e.g. Supply Chain Management). Asakusa provides the DAG-based development methodology, which must be required for the large scale batch jobs. With Asakusa, the developers can build up the scalable and robust enterprise batch jobs easily and comprehensively.

Asakusa consists of the following components: (1) Asakusa DSL compiler, (2) a data model generator for Hadoop data format, and (3) integrated test suites tools. Asakusa DSL compiler compiles the DSLs (multi-layered DSLs, business workflow DSL, logic flow DSL, and data operator DSL) into MapReduce programs. The data model generator takes a simple DSL script (data model definition language: DMDL) or RDBMS schema as an input, and generates the Hadoop I/O classes and the corresponding test templates. For the ease of the development, the test suite tools integrate the Asakusa DSL compiler and data model generator.

## Resources
* [Asakusa Framework Documentaion (ja)](http://asakusafw.s3.amazonaws.com/documents/latest/release/ja/html/index.html)
* [Asakusa Framework Community Site (ja)](http://asakusafw.com)

## How to build

### Build for Hadoop 1.x

```sh
mvn install [-Phadoop1] [-Dhadoop1.version=1.x.y] -DskipTests
```

### Build for Hadoop 2.x

```sh
mvn install -Phadoop2 [-Dhadoop2.version=2.x.y] -DskipTests
```

## How to run tests
* Install [Hadoop](http://hadoop.apache.org/) with local-mode settings
* Set `hadoop` command into your PATH variable, or set it to `$HADOOP_CMD`
* And then run `mvn test [-Phadoop(1|2)] [-Dhadoop.builtin]`

## How to import projects into Eclipse
* Run `mvn install eclipse:eclipse [-Phadoop(1|2)] -DskipTests`
* And then import projects from Eclipse

If you run tests in Eclipse, please activate `Preferences > Java > Debug > 'Only include exported classpath entries when launching'`.

## License
* [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
