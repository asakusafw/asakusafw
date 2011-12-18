:orphan:

================================
Asakusa Framework リリースノート
================================

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
    * See: :doc:`dmdl/with-thundergate`
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
    * See: :doc:`dmdl/with-thundergate`
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

