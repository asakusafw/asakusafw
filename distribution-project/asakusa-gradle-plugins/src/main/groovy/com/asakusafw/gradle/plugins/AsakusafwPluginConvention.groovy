package com.asakusafw.gradle.plugins

import org.gradle.api.*
import org.gradle.util.ConfigureUtil

class AsakusafwPluginConvention {
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
    DmdlConfiguration dmdl;
    /** Model Generator Settings */
    ModelgenConfiguration modelgen;
    /** javac Settings */
    JavacConfiguration javac;
    /** DSL Compiler Settings */
    CompilerConfiguration compiler;
    /** Test tools Settings */
    TestToolsConfiguration testtools;


    AsakusafwPluginConvention(final Project project) {
        this.project = project

        maxHeapSize = '1024m'
        logbackConf = "src/${project.sourceSets.test.name}/resources/logback-test.xml"

        dmdl = new DmdlConfiguration(project)
        modelgen = new ModelgenConfiguration(project)
        javac = new JavacConfiguration(project)
        compiler = new CompilerConfiguration(project)
        testtools = new TestToolsConfiguration(project)
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
            hadoopWorkDirectory = "${project.buildDir}/hadoopwork/" + '${execution_id}'
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
         * The format of test data sheet (DATA|RULE|INOUT|INSPECT|ALL)
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

}




