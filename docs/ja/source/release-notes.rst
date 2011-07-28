:orphan:

===============================
Asakusa Framework Release Notes
===============================

Release 0.2.1
=============

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

Release 0.2.0
=============

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
