/*
 * Copyright 2011-2014 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.gradle.plugins

import org.gradle.api.*
import org.gradle.util.ConfigureUtil

/**
 * Convention class for {@link AsakusafwPlugin}.
 * @since 0.5.2
 * @version 0.6.1
 */
class AsakusafwPluginConvention {

    /**
     * Schema version of this convention.
     */
    static final CONVENTION_SCHEMA_VERSION = '1.1.0'

    final Project project

    /**
     * Asakusa Framework Version.
     * This property must be specified in project configuration
     */
    String asakusafwVersion

    /**
     * Maximum heap size for Model Generator process.
     */
    String maxHeapSize

    /**
     * Logback configuration file path.
     */
    String logbackConf

    /** DMDL Settings */
    DmdlConfiguration dmdl
    /** Model Generator Settings */
    ModelgenConfiguration modelgen
    /** javac Settings */
    JavacConfiguration javac
    /** DSL Compiler Settings */
    CompilerConfiguration compiler
    /** Test tools Settings */
    TestToolsConfiguration testtools

    /**
     * ThunderGate Settings.
     * @since 0.6.1
     */
    ThunderGateConfiguration thundergate

    AsakusafwPluginConvention(final Project project) {
        this.project = project

        maxHeapSize = '1024m'
        logbackConf = "src/${project.sourceSets.test.name}/resources/logback-test.xml"

        dmdl = new DmdlConfiguration(project)
        modelgen = new ModelgenConfiguration(project)
        javac = new JavacConfiguration(project)
        compiler = new CompilerConfiguration(project)
        testtools = new TestToolsConfiguration(project)
        thundergate = new ThunderGateConfiguration(project)
    }

    def dmdl(Closure configureClousure) {
        ConfigureUtil.configure(configureClousure, dmdl)
    }
    def modelgen(Closure configureClousure) {
        ConfigureUtil.configure(configureClousure, modelgen)
    }
    def javac(Closure configureClousure) {
        ConfigureUtil.configure(configureClousure, javac)
    }
    def compiler(Closure configureClousure) {
        ConfigureUtil.configure(configureClousure, compiler)
    }
    def testtools(Closure configureClousure) {
        ConfigureUtil.configure(configureClousure, testtools)
    }
    def thundergate(Closure configureClousure) {
        ConfigureUtil.configure(configureClousure, thundergate)
    }

    /**
     * DMDL Settings
     */
    class DmdlConfiguration {

        /**
         * Character Encoding using DMDL.
         * [Migration from Maven-Archetype] build.properties: asakusa.dmdl.encoding
         */
        String dmdlEncoding

        /**
         * The directory stored dmdl sources.
         * [Migration from Maven-Archetype] build.properties: asakusa.dmdl.dir
         */
        String dmdlSourceDirectory

        DmdlConfiguration(Project project) {
            dmdlEncoding = 'UTF-8'
            dmdlSourceDirectory = "src/${project.sourceSets.main.name}/dmdl"
        }

        def dmdlEncoding(String dmdlEncoding) {
            this.dmdlEncoding = dmdlEncoding
        }

        def dmdlSourceDirectory(String dmdlSourceDirectory) {
            this.dmdlSourceDirectory = dmdlSourceDirectory
        }

    }

    /**
     * Model Generator Settings
     */
    class ModelgenConfiguration {
        /**
         * Package name that is used Model classes generetad by Model Generator.
         * [Migration from Maven-Archetype] build.properties: asakusa.modelgen.package
         */
        String modelgenSourcePackage

        /**
         * The directory where model sources are generated.
         * [Migration from Maven-Archetype] build.properties: asakusa.modelgen.output
         */
        String modelgenSourceDirectory

        ModelgenConfiguration(Project project) {
            modelgenSourcePackage = "${project.group}.modelgen"
            modelgenSourceDirectory = "${project.buildDir}/generated-sources/modelgen"
        }

        def modelgenSourcePackage(String modelgenSourcePackage) {
            this.modelgenSourcePackage = modelgenSourcePackage
        }

        def modelgenSourceDirectory(String modelgenSourceDirectory) {
            this.modelgenSourceDirectory = modelgenSourceDirectory
        }

    }

    /**
     * Javac Settings
     */
    class JavacConfiguration {

        /**
         * The directory where compiled operator impl/factory sources are generated.
         */
        String annotationSourceDirectory

        /**
         * Java source encoding of project.
         */
        String sourceEncoding

        /**
         * Java version compatibility to use when compiling Java source.
         */
        JavaVersion sourceCompatibility

        /**
         * Java version to generate classes for.
         */
        JavaVersion targetCompatibility

        JavacConfiguration(Project project) {
            annotationSourceDirectory = "${project.buildDir}/generated-sources/annotations"
            sourceEncoding = 'UTF-8'
            sourceCompatibility = JavaVersion.toVersion(1.6)
            targetCompatibility = JavaVersion.toVersion(1.6)
        }

        def annotationSourceDirectory(String annotationSourceDirectory) {
            this.annotationSourceDirectory = annotationSourceDirectory
        }

        def sourceEncoding(String sourceEncoding) {
            this.sourceEncoding = sourceEncoding
        }

        def sourceCompatibility(Object sourceCompatibility) {
            this.sourceCompatibility = JavaVersion.toVersion(sourceCompatibility)
        }

        def targetCompatibility(Object targetCompatibility) {
            this.targetCompatibility = JavaVersion.toVersion(targetCompatibility)
        }

    }

    /**
     * DSL Compiler Settings
     */
    class CompilerConfiguration {

        /**
         * Package name that is used batch compiled classes for Hadoop MapReduce, JobClient and so on.
         * [Migration from Maven-Archetype] build.properties: asakusa.package.default
         */
        String compiledSourcePackage

        /**
         * The directory where batch compiled sources are stored.
         * [Migration from Maven-Archetype] build.properties: asakusa.batchc.dir
         */
        String compiledSourceDirectory

        /**
         * DSL Compiler options
         * [Migration from Maven-Archetype] pom.xml: asakusa.compiler.options
         */
        String compilerOptions

        /**
         * The directory where work files for batch compile are stored.
         * [Migration from Maven-Archetype] build.properties: asakusa.compilerwork.dir
         */
        String compilerWorkDirectory

        /**
         * The working root directory when used hadoop job execution.
         * [Migration from Maven-Archetype] build.properties: asakusa.hadoopwork.dir
         */
        String hadoopWorkDirectory

        CompilerConfiguration(Project project) {
            compiledSourcePackage = "${project.group}.batchapp"
            compiledSourceDirectory = "${project.buildDir}/batchc"
            compilerOptions = ''
            compilerWorkDirectory = "${project.buildDir}/batchcwork"
            hadoopWorkDirectory = "target/hadoopwork/" + '${execution_id}'
        }

        def compiledSourcePackage(String compiledSourcePackage) {
            this.compiledSourcePackage = compiledSourcePackage
        }

        def compiledSourceDirectory(String compiledSourceDirectory) {
            this.compiledSourceDirectory = compiledSourceDirectory
        }

        def compilerOptions(String compilerOptions) {
            this.compilerOptions = compilerOptions
        }

        def compilerWorkDirectory(String compilerWorkDirectory) {
            this.compilerWorkDirectory = compilerWorkDirectory
        }

        def hadoopWorkDirectory(String hadoopWorkDirectory) {
            this.hadoopWorkDirectory = hadoopWorkDirectory
        }

    }

    /**
     * Test tools Settings
     */
    class TestToolsConfiguration {

        /**
         * The format of test data sheet (DATA|RULE|INOUT|INSPECT|ALL|DATAX|RULEX|INOUTX|INSPECTX|ALLX)
         * [Migration from Maven-Archetype] build.properties: asakusa.testdatasheet.format
         */
        String testDataSheetFormat

        /**
         * The directory where test data sheet files are generated.
         * [Migration from Maven-Archetype] build.properties: asakusa.testdatasheet.output
         */
        String testDataSheetDirectory

        TestToolsConfiguration(Project project) {
            testDataSheetFormat = 'ALL'
            testDataSheetDirectory = "${project.buildDir}/excel"
        }

        def testDataSheetFormat(String testDataSheetFormat) {
            this.testDataSheetFormat = testDataSheetFormat
        }

        def testDataSheetDirectory(String testDataSheetDirectory) {
            this.testDataSheetDirectory = testDataSheetDirectory
        }

    }

    /**
     * ThunderGate Settings.
     * @since 0.6.1
     */
    class ThunderGateConfiguration {

        /**
         * The ThunderGate default name using in the development environment (optional).
         * ThunderGate facilities will be enabled when this value is non-null.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.database.target} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} </dd>
         * </dl>
         */
        String target = null

        /**
         * DDL sources charset encoding name (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} (use default system encoding) </dd>
         * </dl>
         */
        String ddlEncoding = null

        /**
         * The DDL source path.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code 'src/main/sql/modelgen'} </dd>
         * </dl>
         */
        String ddlSourceDirectory = 'src/main/sql/modelgen'

        /**
         * The inclusion target table/view name pattern in regular expression (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> {@code asakusa.modelgen.includes} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} (includes all targets) </dd>
         * </dl>
         */
        String includes = null

        /**
         * The exclusion target table/view name pattern in regular expression (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> {@code asakusa.modelgen.excludes} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} (no exclusion targets) </dd>
         * </dl>
         */
        String excludes = null

        /**
         * The generated DMDL files output path from table/view definitions using DDLs.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> {@code asakusa.dmdl.fromddl.output} </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"${project.buildDir}/thundergate/dmdl"</code> </dd>
         * </dl>
         */
        String dmdlOutputDirectory

        /**
         * The generated SQL files output path from table/view definitions using DDLs.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"${project.buildDir}/thundergate/sql"</code> </dd>
         * </dl>
         */
        String ddlOutputDirectory

        /**
         * The system ID column name (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> {@code asakusa.modelgen.sid.column} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code 'SID'} </dd>
         * </dl>
         */
        String sidColumn = 'SID'

        /**
         * The last modified timestamp column name (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> {@code asakusa.modelgen.timestamp.column} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code 'UPDT_DATETIME'} </dd>
         * </dl>
         */
        String timestampColumn = 'UPDT_DATETIME'

        /**
         * The logical delete flag column name (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> {@code asakusa.modelgen.delete.column} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code 'DELETE_FLAG'} </dd>
         * </dl>
         */
        String deleteColumn = 'DELETE_FLAG'

        /**
         * The logical delete flag value representation in DMDL (optional).
         * Note that the text values must be enclosed with double-quotations like as {@code "<text-value>"}.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> {@code asakusa.modelgen.delete.column} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code '"1"'} </dd>
         * </dl>
         */
        String deleteValue = '"1"'

        ThunderGateConfiguration(Project project) {
            this.dmdlOutputDirectory = "${project.buildDir}/thundergate/dmdl"
            this.ddlOutputDirectory = "${project.buildDir}/thundergate/sql"
        }

        /**
         * Sets the ThunderGate default name using in the development environment.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration target(String value) {
            this.target = value
            return this
        }

        /**
         * Sets DDL sources charset encoding name.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration ddlEncoding(String value) {
            this.ddlEncoding = value
            return this
        }

        /**
         * Sets the DDL source path.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration ddlSourceDirectory(String value) {
            this.ddlSourceDirectory = value
            return this
        }

        /**
         * Sets the inclusion target table/view name pattern in regular expression.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration includes(String value) {
            this.includes = value
            return this
        }

        /**
         * Sets the exclusion target table/view name pattern in regular expression.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration excludes(String value) {
            this.excludes = value
            return this
        }

        /**
         * Sets the generated DMDL files output path from table/view definitions using DDLs.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration dmdlOutputDirectory(String value) {
            this.dmdlOutputDirectory = value
            return this
        }

        /**
         * Sets the generated SQL files output path from table/view definitions using DDLs.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration ddlOutputDirectory(String value) {
            this.ddlOutputDirectory = value
            return this
        }

        /**
         * Sets the system ID column name.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration sidColumn(String value) {
            this.sidColumn = value
            return this
        }

        /**
         * Sets the last modified timestamp column name.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration timestampColumn(String value) {
            this.timestampColumn = value
            return this
        }

        /**
         * Sets the logical delete flag column name.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration deleteColumn(String value) {
            this.deleteColumn = value
            return this
        }

        /**
         * Sets the logical delete flag value representation in DMDL.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration deleteValue(Object value) {
            this.deleteValue = value?.toString()
            return this
        }
    }

    def getConventionProperties() {
        def commonPrefix = 'com.asaksuafw.asakusafw.'
        def convention = [:]

        convention.put(commonPrefix + 'conventionSchemaVersion', CONVENTION_SCHEMA_VERSION)
        convention.put(commonPrefix + 'asakusafwVersion', asakusafwVersion)
        convention.put(commonPrefix + 'maxHeapSize', maxHeapSize)
        convention.put(commonPrefix + 'logbackConf', logbackConf)

        convention.putAll(asMap(dmdl, commonPrefix + 'dmdl.'))
        convention.putAll(asMap(modelgen, commonPrefix + 'modelgen.'))
        convention.putAll(asMap(javac, commonPrefix + 'javac.'))
        convention.putAll(asMap(compiler, commonPrefix + 'compiler.'))
        convention.putAll(asMap(testtools, commonPrefix + 'testtools.'))
        convention.putAll(asMap(thundergate, commonPrefix + 'thundergate.'))

        return convention
    }

    static def asMap(Object obj, String keyPrefix) {
        obj.class.declaredFields.findAll{ !it.synthetic }.collectEntries {
            [keyPrefix + it.name, obj[it.name]]
        }
    }
}




