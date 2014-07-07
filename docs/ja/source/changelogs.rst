==========
Changelogs
==========

Release 0.6.2
=============
May 22, 2014

Enhancements
------------
* [ :issue:`325` ] - DMDL Compiler should fail when defining more than 3 model join as joined model.
* [ :issue:`386` ] - Optimize split combiner for tiny inputs
* [ :issue:`388` ] - Bumps up default Gradle version to 1.12.
* [ :issue:`389` ] - Add Gradle task for generating YAESS log summary report
* [ :issue:`391` ] - Add reducer simplifier for tiny inputs

Bug fixes
---------
* [ :issue:`326` ] - Unreadable error message when DMDL compiles joined model with no joined key property.
* [ :issue:`379` ] - Insert a dropped character
* [ :issue:`380` ] - Wrong index of the transaction parameter
* [ :issue:`381` ] - Wrong error message when the name not existing is specified as @Key.order
* [ :issue:`382` ] - Adds local mode detection logic to JobCompatibilty layer.
* [ :issue:`383` ] - Unclear message when enum name overlaps in Operator
* [ :issue:`385` ] - Reduce task infrequently fails with NoSuchMethodError:TaskID on MRv1
* [ :issue:`387` ] - SystemProperty configration via task property does not work on some Gradle tasks.

Others
------
* [ :issue:`390` ] - 0.6.2 Documents
* [ :issue:`392` ] - 0.6.2 Refactoring

Release 0.6.1
=============
Mar 19, 2014

Enhancements
------------
* [ :issue:`367` ] - Keeps batchapps in ASAKUSA_HOME clean when running TestDriver.
* [ :issue:`368` ] - Improves details of Batch Application Plugin [Gradle Plugin]
* [ :issue:`369` ] - Gradle Plugin supports ThunderGate
* [ :issue:`372` ] - Introduce optional input definition to DirectFileInputDescription
* [ :issue:`374` ] - Promotes inprocess testing and batch test runner from sandbox featuret
* [ :issue:`375` ] - Add RunBatchappTask for running BatchTestRunner via Gradle Task

Bug fixes
---------
* [ :issue:`371` ] - Fails to resolve data source from Direct I/O output base path with variables
* [ :issue:`373` ] - Failed to compile Operator class with overloaded private methods
* [ :issue:`378` ] - Running local job with parallel may conflict in local attempt output area

Others
------
* [ :issue:`376` ] - 0.6.1 Documents
* [ :issue:`377` ] - 0.6.1 Refactoring

Release 0.6.0
=============
Feb 17, 2014

Enhancements
------------
* [ :issue:`341` ] - Creates directories for generated sources on eclipse task [Gradle Plugin]
* [ :issue:`347` ] - Adds wrapper task with settings for batchapp to template project [Gradle Plugin]
* [ :issue:`353` ] - Enables to modify compilerArgs via build script and changes default value [Gradle Plugin]
* [ :issue:`354` ] - Adds extention point for configuring jobs to StageClient
* [ :issue:`355` ] - Adjusts application build log
* [ :issue:`358` ] - Add pluggable job executors for test driver
* [ :issue:`361` ] - Add TestDriver API for preparing and verifying test data with model object collection.
    * See: :doc:`testing/user-guide`
* [ :issue:`364` ] - Add pluggable testing environment configurator for test driver
* [ sandbox ] - Add emulation mode for TestDriver
    * See: :doc:`testing/emulation-mode`
* [ sandbox ] - Add Adaptive Hadoop local mode configurator.

Bug fixes
---------
* [ :issue:`339` ] - Fix a closing tag name
* [ :issue:`343` ] - Incorrect hadoopWorkDirectory set on Gradle Plugin
* [ :issue:`344` ] - generateTestbook task should set headless option [Gradle Plugin]
* [ :issue:`350` ] - Fix a wrong Javadoc parameter explanation 
* [ :issue:`351` ] - Generates resources.prefs file in configuration phase [Gradle Plugin]
* [ :issue:`352` ] - Maven archetype has broken example script file
* [ :issue:`356` ] - Task inputs/outputs property does not evaluate correctly when changing that [Gradle Plugin] 
* [ :issue:`357` ] - TestDriver cannot accept an empty file as a JSON data input 
* [ :issue:`359` ] - Direct I/O does not detect data source correctly when using base path with valuables
* [ :issue:`360` ] - TestDriver fails on project with blank space path
* [ :issue:`362` ] - TestDriverBase#setFrameworkHomePath does not work
* [ :issue:`365` ] - Log message is not clear when ConfigurationProvider failed to find hadoop conf.
* [ :issue:`366` ] - Framework Organizer tasks should not define in afterEvaluate block possibly [Gradle Plugin]

Others
------
* [ :issue:`340` ] - Changes standard build system on documents to Gradle-based 
    * See: :doc:`application/gradle-plugin`
* [ :issue:`342` ] - Refactoring Gradle Plugin
* [ :issue:`345` ] - Prepare for 0.6.0 release
* [ :issue:`346` ] - 0.6.0 Documents
* [ :issue:`348` ] - Refactoring Gradle Template Project [Gradle Plugin] 
* [ :issue:`349` ] - Adds eclipse.preferences.version to asakusafw project prefs [Gradle Plugin]
* [ :issue:`363` ] - 0.6.0 Refactoring

Release 0.5.3
=============
Dec 24, 2013

Enhancements
------------
* [ :issue:`312` ] - Improvement of error message for invalid DMDL property name
* [ :issue:`313` ] - (Experimental) Supports Apache Hadoop 2.2.0
    * See: :doc:`product/target-platform`
    * See: :doc:`administration/deployment-hadoop2`
* [ :issue:`314` ] - Supports JDK 7
    * See: :doc:`product/target-platform`
    * See: :doc:`application/develop-with-jdk7`
* [ :issue:`315` ] - Supports latest version of MapR
    * See: :doc:`product/target-platform`
* [ :issue:`316` ] - Skips task execution if the input resource does not exist. [Gradle Plugin]
* [ :issue:`319` ] - Generates asakusafw project prefs on eclipse task. [Gradle Plugin]
    * See: :doc:`application/gradle-plugin`
* [ :issue:`321` ] - Changes archetype pom.xml repos order to avoid unnecessary access.
* [ :issue:`324` ] - TestDriver supports more than 256 columns in excel data template
    * See: [ :issue:`335` ]
* [ :issue:`327` ] - Unreadable error message when compiling Flow DSL with non-wired operateor
* [ :issue:`330` ] - Unreadable error message when Direct I/O may override another model output.
* [ :issue:`335` ] - (Experimental) Supports Excel 2007 (\*.xlsx) format on TestDriver
    * See: :doc:`testing/using-excel`
* [ :issue:`336` ] - Simplifies default log format settings on template project
* [ sandbox ] - Direct I/O-TSV supports data header
    * See: sandbox:`Direct I/OのTSVファイル連携 <directio/tsv.html>`

Bug fixes
---------
* [ :issue:`303` ] - (Reopened issue) TestDriver fails when installed framework version is older than project framework version.
* [ :issue:`317` ] - Fails standalone use of framework organizer plugin [Gradle Plugin]
* [ :issue:`318` ] - Fails compileTestJava task when main SourceSet file does not exist. [Gradle Plugin]
* [ :issue:`322` ] - Eclipse project encoding should set all the resources instead of individual source folder.

Others
------
* [ :issue:`320` ] - Bumps up default Gradle version.
* [ :issue:`323` ] - 0.5.3 Documents
* [ :issue:`329` ] - runtime.core.Result tend to misunderstand that cannot add multiple objects
* [ :issue:`337` ] - Fix typo
* [ :issue:`338` ] - Fix a valid CoGroup operator method
* [ sandbox ] - Revised documents using on Amazon EMR
    * See: :doc:`sandbox/asakusa-on-emr`

Release 0.5.2
=============
Nov 20, 2013

Enhancements
------------
* [ :issue:`300` ] - Region is null when a cycle exists in model dependencies
* [ :issue:`301` ] - New Build System based on Gradle
    * See: :doc:`application/gradle-plugin`
* [ :issue:`305` ] - Direct I/O-CSV supports compression and decompression
    * See: :doc:`directio/user-guide`
* [ :issue:`309` ] - TestDriver should clean compiler working directory after finishing runTest.
* [ Sandbox ] - Direct I/O-TSV supports compression and decompression
    * See: :doc:`sandbox/directio-tsv`

Bug fixes
---------
* [ :issue:`297` ] - "true","false","null" has leaked from the check of SimpleName
* [ :issue:`298` ] - Fix documentation bugs
* [ :issue:`299` ] - Fix documentation bugs
* [ :issue:`302` ] - YS-CORE-I01003 shows incorrect flowId
* [ :issue:`303` ] - TestDriver fails when installed framework version is older than project framework version.
* [ :issue:`304` ] - StageInputRecordReader throws NPE when closing unprepared RecordReader.
* [ :issue:`307` ] - Port name of MasterJoinUpdate has wrong in operator reference

Others
------
* [ :issue:`306` ] - Bumps up default dependency hadoop version
    * See: :doc:`product/target-platform`
* [ :issue:`308` ] - Updates document for using Direct I/O with splittable configuration on S3
    * See: :doc:`directio/user-guide`
* [ :issue:`310` ] - 0.5.2 Documentation
* [ :issue:`311` ] - 0.5.2 Refactoring

Release 0.5.1
=============
Jul 26, 2013

Others
------

Enhancements
------------
* [ :issue:`282` ] - Error message improvement in DMDL
* [ :issue:`287` ] - Manages application dependency libraries per project
    * See: :doc:`application/maven-archetype`
* [ :issue:`289` ] - Japanese message resources of DMDL Compiler
* [ :issue:`291` ] - Dataflow tracing on TestDriver
    * See: :doc:`testing/user-guide`
* [ :issue:`292` ] - Reduce memory usage of Java DOM library
* [ :issue:`294` ] - Skips tests not found JDK on Windows

Bug fixes
---------
* [ :issue:`284` ] - Tester assertion seems strange in case of verify key duplication
* [ :issue:`285` ] - Value of emum counted port at @MasterBranch operator
* [ :issue:`286` ] - Delete a wrong sentence.
* [ :issue:`288` ] - The useIncrementalCompilation option should disable on archetype default configuration.
* [ :issue:`295` ] - Regression: Failed to compile DMDL script on Windows.
* [ :issue:`296` ] - Dependency for hadoop on dmdl-java should have provided scope

Others
------
* [ :issue:`290` ] - 0.5.1 Documentation
* [ :issue:`293` ] - Update Maven plugins to recent versions (0.5.1)

Release 0.5.0
=============
May 9, 2013

Enhancements
------------
* [ :issue:`246` ] - Make retry interval of RetryableProcessProvider configurable
    * See: :doc:`windgate/user-guide`
* [ :issue:`249` ] - Improve error handling on FileSystem.listFiles().
* [ :issue:`250` ] - Introduce API Compatibility Layer between Hadoop 1.x and 2.x.
* [ :issue:`251` ] - Batchapp should not use unstable APIs directly.
* [ :issue:`252` ] - Improve local symlink file detection in cleaner.
* [ :issue:`253` ] - Improve debug logs in test driver.
* [ :issue:`254` ] - HADOOP_HOME should not use by default in component properties
* [ :issue:`259` ] - Enable to specify retry interval to Windgate Retryable Plugin
    * See: :doc:`windgate/user-guide`
* [ :issue:`260` ] - Obtains HADOOP_CONF via installed hadoop command
* [ :issue:`267` ] - Adds Java annotation for identifing Operator Factory Class
* [ :issue:`269` ] - Adds Java annotation for identifing Operator Factory Method
* [ :issue:`273` ] - Adds Java annotation for describing Batch DSL Specification
    * See: :doc:`dsl/user-guide`
* [ :issue:`274` ] - YAESS start log should output command line arguments completely
* [ :issue:`275` ] - Verifying Asakusa DSL
    * See: :doc:`dsl/user-guide`
* [ :issue:`276` ] - MapReduce Job Name should include Execution ID
* [ :issue:`277` ] - Simplifies application project configuration
    * See: :doc:`administration/framework-organizer`
* [ :issue:`279` ] - Keep flowpart parameters information for visualization capability
* [ :issue:`281` ] - Extra compiler plugin directories.

Bug fixes
---------
* [ :issue:`243` ] - DirectI/O user-guide AmazonS3-example typo
* [ :issue:`257` ] - Export target table does not clear when changing to DELETE query and exporting empty data.
* [ :issue:`258` ] - Example value of windgate profile should have same as defalut value
* [ :issue:`261` ] - Output port name of operators has wrong on operator reference.
* [ :issue:`262` ] - Fail to build framework on Windows
* [ :issue:`264` ] - DOM library generates model with wrong method invocation qualifier.
* [ :issue:`268` ] - Testdata Template Generator should output log before its completed


Others
------
* [ :issue:`248` ] - Changes default version and distribution of dependency Hadoop library
    * See: :doc:`product/target-platform`
* [ :issue:`255` ] - Update dependency testing libraries to recent versions
* [ :issue:`256` ] - 0.5.0 Documentation
* [ :issue:`263` ] - Refactor parent pom for introducing build-tools project
* [ :issue:`265` ] - Refactor parent pom for removing unnecessary dependencies
* [ :issue:`272` ] - Skips tests using Hadoop on Windows
* [ :issue:`278` ] - Update Maven plugins and depedency libraries to recent versions (0.5.0)

Release 0.4.0
=============
Aug 30, 2012

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
* [ :issue:`222` ] - Performance tuning of CSV Parser
* [ :issue:`226` ] - Add version information in ASAKUSA_HOME
* [ :issue:`227` ] - Add build timestamp in application build log
* [ :issue:`232` ] - Improve sample application includes archetype
* [ :issue:`240` ] - Supports CDH3u5
    * See: :doc:`product/target-platform`

..  attention::
    Direct I/O is now generally available.

Bug fixes
---------
* [ :issue:`194` ] - WindGate log seems not correct
* [ :issue:`196` ] - Cleaner should use Hadoop libraries provided environment
* [ :issue:`211` ] - Unexpected exception thrown when log directory lost permission.
* [ :issue:`217` ] - Maven eclipse plugin may not create source directory for generating annotation processing
* [ :issue:`221` ] - Remove unnecessary eclipse configuration from archetype pom.xml
* [ :issue:`223` ] - Incorrect error message of CSV Parser
* [ :issue:`224` ] - Compiler does not stop when overwriting output error at batch compile
* [ :issue:`229` ] - Some of the test method of ThunderGate does not close JDBC resource
* [ :issue:`233` ] - Incorrect log level of ThunderGate
* [ :issue:`234` ] - BridgeInputFormat may cause StackOverFlowError with inconsistent framework environment
* [ :issue:`235` ] - BasePath not found error when connecting flow from Direct I/O input to WindGate output directly
* [ :issue:`236` ] - Redundant warning log for creating symlink on standalone mode
* [ :issue:`237` ] - Partitioners hash algorithm is not strong.
* [ :issue:`238` ] - DMDL Compiler generates DataModel Class incorrectly with hierarchical namespace attributes
* [ :issue:`239` ] - DMDL Compiler does not detect inconsistent type of join keys.
* [ :issue:`242` ] - TestDriver resolves working directory with user home directory

Revisions
---------
* [ :issue:`198` ] - Changes archetype composition
    * See: :doc:`application/maven-archetype`
* [ :issue:`207` ] - Legacy TestDriver should not use experimental shell script
* [ :issue:`220` ] - Rename asakusa-runtime.jar
* [ :issue:`225` ] - Changes default value of PartialAggregation parameter in Summarize operator

Others
------
* [ :issue:`70` ] - Need more detailed and easy-to-see documentation for logging and related maintainance
    * See: :doc:`windgate/log-table`
    * See: :doc:`yaess/log-table`
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
    * See: :doc:`yaess/jobqueue`
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

