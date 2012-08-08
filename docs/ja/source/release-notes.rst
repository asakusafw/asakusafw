================================
Asakusa Framework リリースノート
================================

Release 0.4.0
=============
TBA

..  todo:: Release Date
..  todo:: Add documents and link to related page to following issues.

Enhancements
------------
* [ :issue:`78` ] - \*Tester should provide the way to define precision accuracy
    * See: :doc:`testing/user-guide`
* [ :issue:`115` ] - Manage assembly descriptor as part of Maven artifact.
* [ :issue:`128` ] - Retryable Processes for WindGate
    * See: :doc:`windgate/user-guide`
* [ :issue:`160` ] - Optimize execution plan for eliminating duplication of Operator
    * See: :doc:`dsl/user-guide`
* [ :issue:`179` ] - Batch application submodule mechanism 
    * See: :doc:`dsl/user-guide`
* [ :issue:`185` ] - Generate Input/Output Description for Direct I/O SequenceFile format
    * See: :doc:`directio/user-guide`
* [ :issue:`195` ] - support Apache Hadoop 1.0.x
    * See: :doc:`product/target-platform`
* [ :issue:`197` ] - Command line tools for operation
    * See: :doc:`administration/utility-tool-user-guide`
* [ :issue:`201` ] - Direct I/O output optimization
    * See: :doc:`directio/user-guide`
* [ :issue:`202` ] - Direct I/O cleaning DSL/CLI
    * See: :doc:`directio/user-guide`
    * See: :doc:`directio/tools-guide`
* [ :issue:`204` ] - Revise cleanup in YAESS
    * See: :doc:`yaess/user-guide`
* [ :issue:`208` ] - Direct I/O sequence file compression
    * See: :doc:`directio/user-guide`
* [ :issue:`209` ] - Logging improvement for YAESS
    * See: :doc:`yaess/log-table`
* [ :issue:`210` ] - Detects difference in build version of batch application at runtime
    * See: :doc:`yaess/user-guide`
* [ :issue:`212` ] - Logging improvement for Asakusa Runtime
* [ :issue:`213` ] - Enables WindGate to pass environment variables to remote processes via SSH
    * See: :doc:`windgate/user-guide`
* [ :issue:`214` ] - WindGate hadoop/direct basePath
    * See: :doc:`windgate/user-guide`
* [ :issue:`215` ] - Thundergate should use Hadoop configuration and classpath
* [ :issue:`218` ] - WindGate profile should allow to specify environment variables more properties
    * See: :doc:`windgate/user-guide`
* [ :issue:`219` ] - YAESS profile should allow to specify environment variables more properties
    * See: :doc:`yaess/user-guide`

..  attention::
    Direct I/O is now generally available.

Bug fixes
---------
* [ :issue:`194` ] - WindGate log seems not correct
* [ :issue:`196` ] - Cleaner should use Hadoop libraries provided environment
* [ :issue:`211` ] - Unexpected exception thrown when log directory lost permission.
* [ :issue:`217` ] - Maven eclipse plugin may not create source directory for generating annotation processing
* [ :issue:`221` ] - Remove unnecessary eclipse configuration from archetype pom.xml

Revisions
---------
* [ :issue:`198` ] - Changes archetype composition
    * See: :doc:`application/maven-archetype`
* [ :issue:`207` ] - Legacy TestDriver should not use experimental shell script
* [ :issue:`220` ] - Rename asakusa-runtime.jar

Others
------
* [ :issue:`70` ] - Need more detailed and easy-to-see documentation for logging and related maintainance
    * See: :doc:`windgate/log-table`
    * See: :doc:`yaess/log-table`
* [ :issue:`88` ] - \.sql file for Oracle Database in WindGate example 
* [ :issue:`180` ] - WindGate log table document
    * See: :doc:`windgate/log-table`
* [ :issue:`181` ] - 0.4.0 Documentation
* [ :issue:`189` ] - Refoctoring for release 0.4.0
* [ :issue:`190` ] - Repackage javalang-tools
* [ :issue:`191` ] - Introduce hierarchical project structure to repository
* [ :issue:`192` ] - Update Maven plugins to recent versions
* [ :issue:`193` ] - Update dependency libraries to recent versions
* [ :issue:`199` ] - Clean project structure and project dependency for legacy classes
* [ :issue:`200` ] - Refactor assembly scripts
* [ :issue:`203` ] - Relocate each distribution fragments into suitable project
* [ :issue:`205` ] - Migrate JobQueue sorurces from asakusafw-sandbox repository
    * See: :doc:`yaess/jobqueue`
* [ :issue:`206` ] - 0.4.0 Javadoc
    * See: `Asakusa Framework API References (Version 0.4.0)`_
* [ :issue:`216` ] - Refoctoring Maven archetype for release 0.4.0

..  _`Asakusa Framework API References (Version 0.4.0)`: http://asakusafw.s3.amazonaws.com/documents/0.4.0/release/api/index.html

----

Release 0.2.6
=============
May 31, 2012

Enhancements
------------
* [ :issue:`84` ] - WindGate logging improvement
* [ :issue:`138` ] - Provide command script building cache for ThunderGate
    * See: :doc:`thundergate/cache`
* [ :issue:`139` ] - Specified index at duplication check for Exporter
* [ :issue:`143` ] - Specify the number of divisions to the output file of Direct I/O
    * See: :doc:`directio/user-guide`
* [ :issue:`145` ] - YAESS script for executing per JobFlow.
    * See: :doc:`yaess/user-guide`
* [ :issue:`147` ] - Generate Asakusa DSL analysis files at batch compile
    * See: :doc:`application/dsl-visualization`
* [ :issue:`148` ] - CoreOperators for eliminating to use CoreOperatorFactory
    * See: :doc:`dsl/operators`
* [ :issue:`149` ] - Run tests of archetypes in the integration-test phase of Maven.
* [ :issue:`150` ] - Report API implementation using Commons Logging.
    * See: :doc:`administration/deployment-runtime-plugins`
* [ :issue:`152` ] - Combines input splits
    * See: :doc:`administration/configure-hadoop-parameters`
* [ :issue:`153` ] - Multi-cluster support for YAESS
    * See: :doc:`yaess/multi-dispatch`
* [ :issue:`154` ] - Simple job queue for YAESS (experimental)
    * See: `Asakusa Framework Sandbox: YAESS JobQueue`_
* [ :issue:`155` ] - Skip specifing jobflows on yaess-batch.
    * See: :doc:`yaess/user-guide`
* [ :issue:`156` ] - Write execution history per jobflow on YAESS.
    * See: :doc:`yaess/user-guide`
* [ :issue:`157` ] - Specify Java command-line option on YAESS
    * See: :doc:`yaess/user-guide`
* [ :issue:`159` ] - Logging Improvement for YAESS
    * See: :doc:`yaess/log-table`
* [ :issue:`162` ] - support CDH3u3
* [ :issue:`163` ] - Add exit code for retryable abend to ThunderGate
* [ :issue:`164` ] - ThunderGate loads configuration properties with asakusa-resources.xml
* [ :issue:`165` ] - Direct I/O supports SequenceFile format
    * See: :doc:`directio/user-guide`
* [ :issue:`166` ] - Optimize execution plan for reducing output file size
* [ :issue:`171` ] - Add default YAESS plugins to deployment archive.
    * See: :doc:`administration/deployment-with-directio`
    * See: :doc:`administration/deployment-with-windgate`
* [ :issue:`172` ] - Align log code in each log record
* [ :issue:`173` ] - support CDH3u4
* [ :issue:`176` ] - Select defalut hadoop mode of ThunderGate configuration installing to local
* [ :issue:`184` ] - YAESS command option running JobFlow serialized forcibly (experimental)

..  attention::
    Direct I/O is still an experimental feature.

..  _`Asakusa Framework Sandbox: YAESS JobQueue`: http://asakusafw.s3.amazonaws.com/documents/sandbox/ja/html/yaess/jobqueue.html

Bug fixes
---------
* [ :issue:`140` ] - NPE when running DMDL Genarator without encoding option
* [ :issue:`141` ] - Code example for generated DMDL is wrong
* [ :issue:`144` ] - Failed with NPE when Direct I/O outputs with specifing date format
* [ :issue:`146` ] - Misleading description about batch compiler option
* [ :issue:`151` ] - Cause message which include exception is not shown when running WindGate with Postgresql
* [ :issue:`158` ] - Improper use of IOException on logging YAESS.
* [ :issue:`161` ] - Eliminates unnecessary output files in map task
* [ :issue:`167` ] - Batch application with distributed cache may not work on standalone mode
* [ :issue:`168` ] - Invalid script message to finalizer.sh and recoverer.sh
* [ :issue:`170` ] - Legacy TestDriver does not guarantee ordering to load test data sheet files.
* [ :issue:`175` ] - Multipart upload of S3 with Direct I/O does not work.
* [ :issue:`177` ] - File will not be split if @directio.csv.file_name is used
* [ :issue:`178` ] - The jar file without the necessity that the recoverer of ThunderGate reads is read
* [ :issue:`182` ] - build-cache.sh failed at reading import DSL property.
* [ :issue:`183` ] - DbImporterDescription has wrong description of JavaDoc.

Others
------
* [ :issue:`142` ] - 0.2.6 Documentation
* [ :issue:`169` ] - Refoctoring for release 0.2.6

----

Release 0.2.5
=============
Jan 31, 2012

Enhancements
------------
* [ :issue:`131` ] - Direct I/O - direct data access facility from Hadoop cluster
    * See: :doc:`directio/index`
* [ :issue:`134` ] - Original Apache Hadoop Support
    * See: :doc:`product/target-platform`
* [ :issue:`135` ] - Add pom.xml default settings of archetype for using Eclipse m2e plugin.

..  attention::
    Direct I/O is still an experimental feature.

Bug fixes
---------
* [ :issue:`137` ] - "Reduce output records" counter is wrong

Others
------
* [ :issue:`129` ] - 0.2.5 Documentation
* [ :issue:`130` ] - Refoctoring for release 0.2.5

----

Release 0.2.4
=============
Dec 19, 2011

Enhancements
------------

* [ :issue:`59` ] - Assembly support for batch project
* [ :issue:`82` ] - WindGate Documentaion
* [ :issue:`83` ] - WindGate performance improvement (still working)
* [ :issue:`87` ] - Difficult to distinguish <h2> and <h3> in documents
* [ :issue:`111` ] - WindGate for CSV files in local file system
* [ :issue:`112` ] - JdbcImporter/ExporterDescription should be auto generated
* [ :issue:`113` ] - Test driver should refer WindGate plug-ins
* [ :issue:`117` ] - JDBC Connection Properties should be configurable on WindGate
* [ :issue:`120` ] - WindGate should accept Java VM options
* [ :issue:`121` ] - The script files for build should externalize from application project
* [ :issue:`128` ] - Retryable Processes for WindGate (still working - Retryable Processes is still an experimental feature in this version) .

..  attention::
    WindGate is now generally available.

Revisions
---------
* [ :issue:`105` ] - Shoud there be existed a copy constructor at DecimalOption
* [ :issue:`114` ] - Change default configuration of archetype for WindGate for using local file (CSV) .
* [ :issue:`116` ] - Deployment archive for WindGate should be included files for running Hadoop on local.
* [ :issue:`123` ] - Archetype for ThunderGate should rename archetype ID.
* [ :issue:`126` ] - Deployment archive for WindGate should be included jsch for WindGate plugin

Bug fixes
---------
* [ :issue:`118` ] - ThunderGate raises unknown error if cache lock was conflicted
* [ :issue:`119` ] - ThunderGate recoverer and release cache lock have same job ID
* [ :issue:`124` ] - asakusa-resources.xml has incorrect default configuration.
* [ :issue:`125` ] - Show DMDL compiler usage when model generator failed.
* [ :issue:`127` ] - WindGate HadoopFS/SSH sometimes does not return exit status

Others
------
* [ :issue:`106` ] - 0.2.4 Documentation

----


Release 0.2.3
=============
Nov 16, 2011

Enhancements
------------
* [ :issue:`60` ] - Test driver message is not easy to understand
* [ :issue:`67` ] - Support fine grain verification on TestDriver
* [ :issue:`81` ] - support CDH3u1 
* [ :issue:`86` ] - Pluggable compare for \*Tester
* [ :issue:`91` ] - Enabled to dump all actual data when running testdriver.
    * See: :doc:`testing/user-guide`
* [ :issue:`92` ] - Difference report on \*Tester
    * See: :doc:`testing/user-guide`
* [ :issue:`93` ] - YAESS - Portable Workflow Processor
    * See: :doc:`yaess/index`
* [ :issue:`96` ] - Skip each phase of TestDriver execution.
    * See: :doc:`testing/user-guide`
* [ :issue:`98` ] - Cache for ThunderGate
    * See: :doc:`thundergate/cache`
* [ :issue:`99` ] - support CDH3u2
* [ :issue:`102` ] - Simplify test driver internal APIs

..  attention::
    WindGate is still an experimental feature.

Bug fixes
---------
* [ :issue:`85` ] - FileExporterDescription failed to output to multiple files
* [ :issue:`90` ] - typo in documents
* [ :issue:`95` ] - Extractor returns invalid return code
* [ :issue:`100` ] - Test driver fails with IllegalArgumentException if batch argument value for Context API includes space character
* [ :issue:`101` ] - "execution_id" is not available in BatchContext
* [ :issue:`103` ] - WindGate stays running after OutOfMemoryError is occurred
* [ :issue:`104` ] - dbcleaner.sh does not include in prod-db tarball.

Others
------
* [ :issue:`89` ] - 0.2.3 Documentation

----


Release 0.2.2
=============
Sep 29, 2011

Enhancements
------------
* [ :issue:`61` ] - ThunderGate log messages improvement
* [ :issue:`63` ] - Reduce dependency of MultipleOutputs
* [ :issue:`64` ] - Enable to input expect data from database table.
* [ :issue:`69` ] - WindGate
    * See: :doc:`windgate/index`
* [ :issue:`74` ] - Write framework version to build.log at batch compile

..  attention::
    WindGate is still an experimental feature.

Bug fixes
---------
* [ :issue:`53` ] - Batch compile error message on importer type unmatch seems strange
* [ :issue:`57` ] - Correct messages
* [ :issue:`58` ] - Error message when jobflow output missing is difficult to understand
* [ :issue:`65` ] - Redundant assert log message with date type.
* [ :issue:`71` ] - FlowPartTester#setOptimaze seems does not work
* [ :issue:`72` ] - Invalid summarize operation if grouping key is also used for aggregation
* [ :issue:`73` ] - Raised internal error if grouping key is an empty string
* [ :issue:`75` ] - It is cause error using excel file in jar as tester input
* [ :issue:`76` ] - It is difficult to understand message \*Tester test failed
* [ :issue:`77` ] - Exponent notation is not suitable \*Tester test message when DecimalOption assertion failed
* [ :issue:`80` ] - Failed to compile operator by using reserved keywords in Java for Enum constant

Others
------
* [ :issue:`54` ] - 0.2.2 Documentation

----

Release 0.2.1
=============
Jul 27, 2011

Enhancements
------------
* [ :issue:`38` ] - Supports CLOB for property type
    * See: :doc:`thundergate/with-dmdl`
* [ :issue:`41` ] - Support new operator "Extract"
    * See: :doc:`dsl/operators`
* [ :issue:`50` ] - Support new operator "Restructure"
    * See: :doc:`dsl/operators`

Bug fixes
---------
* [ :issue:`49` ] - Failed to synthesize record models with same property
* [ :issue:`51` ] - Repository url of pom.xml defines https unnecessarily

Others
------
* [ :issue:`52` ] - 0.2.1 Documentation

----

Release 0.2.0
=============
Jun 29, 2011

Enhancements
------------
* [ :issue:`10` ] - support CDH3u0
* [ :issue:`17` ] - New data model generator
    * See: :doc:`dmdl/index`
* [ :issue:`18` ] - Generic operators support
    * See: :doc:`dsl/generic-dataflow`
* [ :issue:`19` ] - TestDriver enhancement for loosely-coupled architecture
    * See: :doc:`testing/index`
* [ :issue:`23` ] - Floating point number support
    * See: :doc:`thundergate/with-dmdl`
* [ :issue:`32` ] - CoGroup/GroupSort for very large group
    * See: :doc:`dsl/operators`
* [ :issue:`36` ] - ThunderGate should show # of imported/exporting records

Revisions
---------
* [ :issue:`26` ] - modelgen should be bound to generate-sources phase (not process-resources phase).
* [ :issue:`40` ] - Enable compiler option "compressFlowPart" default value

Bug fixes
---------
* [ :issue:`3` ] - 'mvn test' fails if X window system is not available
* [ :issue:`4` ] - testtools.properties does not use on a project generated with archetype
* [ :issue:`5` ] - system property and environment variables "NS\_" -> "ASAKUSA\_" corresponding leakage of renaming
* [ :issue:`6` ] - The argument of FlowPartTestDriver#createIn should use <T> instead of <?>
* [ :issue:`7` ] - ThunderGate does not work on Ubuntu for using source command in shell scripts
* [ :issue:`8` ] - some asakusa-runtime tests fail because of the Windows NewLine Code
* [ :issue:`9` ] - empty cells are treaded as an invaid value in the Test Data Definition Sheet
* [ :issue:`11` ] - The cache file table on ThunderGate is unnecessary
* [ :issue:`12` ] - the unnecessary property of ThunderGate configration
* [ :issue:`13` ] - ThunderGate setup DDL must be modified when specified non default database name
* [ :issue:`14` ] - Cleaner does not check errors to get FileSystem
* [ :issue:`15` ] - Inefficient process of getting FileSystem in HDFSCleaner
* [ :issue:`16` ] - output.directory should be defined in build.properties instead of testtools.properties
* [ :issue:`20` ] - Build failed when mvn clean install
* [ :issue:`21` ] - Failed to create join tables from distributed cache
* [ :issue:`22` ] - the NOTICE file typo
* [ :issue:`24` ] - TestUtilsTest.testNormal failed in rare cases
* [ :issue:`27` ] - the logback-test.xml used old format.
* [ :issue:`28` ] - missing classpath exclude definition of pom.xml generated from archetype.
* [ :issue:`29` ] - stage planner does not expand nested flow parts
* [ :issue:`30` ] - bash dependency problems for some shell scripts
* [ :issue:`31` ] - Failed to "side data join" if input is not a SequenceFile
* [ :issue:`44` ] - Javac hides direct cause of compilation errors
* [ :issue:`46` ] - cleanHDFS.sh/cleanLocalFS.sh does not work.
* [ :issue:`47` ] - ThunderGate closes standard error stream unexpectedly

Others
------
* [ :issue:`25` ] - 0.2.0 Documentation

----

Release 0.1.0
=============
Mar 30, 2011

* The first release of Asakusa Framework.

