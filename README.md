# Asakusa Framework

Asakusa is a Hadoop-based Enterprise Batch Processing Framework, to improve the efficiency of General Enterprise Systems (e.g. Supply Chain Management). Asakusa provides the DAG-based development methodology, which must be required for the large scale batch jobs. With Asakusa, the developers can build up the scalable and robust enterprise batch jobs easily and comprehensively.

Asakusa consists of the following components: (1) MapReduce DSL compiler (Ashigel Compiler), (2) a data model generator for Hadoop data format, and (3) integrated test suites tools. Ashigel compiler compiles the DSLs (multi-layered DSLs, business workflow DSL, logic flow DSL, and data operator DSL) into MapReduce programs. The model generator takes RDBMS schema as an input, and generates the Hadoop I/O classes and the corresponding test codes. For the ease of the development, the test suite tools integrate the Ashigel compiler and the model generator.

## Resources
### Japanese Documents
* [Technical WhitePaper (PDF)](https://asakusafw.s3.amazonaws.com/documents/Whitepaper_014.pdf)
* [Getting Started (ja)][https://github.com/asakusafw/asakusafw/wiki/Getting-Started-(ja)]
* [Application Development Guide (ja)][https://github.com/asakusafw/asakusafw/wiki/Application-Development-Guide-(ja)]
* [Deployment Guide (ja)][https://github.com/asakusafw/asakusafw/wiki/Deployment-Guide-(ja)]
* [Target Platform (ja)][https://github.com/asakusafw/asakusafw/wiki/Target-Platform-(ja)]
