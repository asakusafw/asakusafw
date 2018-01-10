/*
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.gradle.plugins.internal

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import com.asakusafw.gradle.plugins.AsakusafwBasePlugin
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.DmdlConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.JavacConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ModelgenConfiguration
import com.asakusafw.gradle.tasks.CompileDmdlTask
import com.asakusafw.gradle.tasks.GenerateHiveDdlTask
import com.asakusafw.gradle.tasks.GenerateTestbookTask

/**
 * Test for {@link AsakusaSdkPlugin}.
 */
class AsakusaSdkPluginTest {

    /**
     * The test initializer.
     */
    @Rule
    public final TestRule initializer = new TestRule() {
        Statement apply(Statement stmt, Description desc) {
            project = ProjectBuilder.builder().withName(desc.methodName).build()
            project.apply plugin: 'asakusafw-sdk'
            project.asakusafwBase.frameworkVersion = '0.0.0'
            return stmt
        }
    }

    Project project

    /**
     * Test for {@code get}.
     */
    @Test
    void get() {
        AsakusafwPluginConvention convention = AsakusaSdkPlugin.get(project)
        assert convention == project.asakusafw
    }

    /**
     * Test for {@code project.sourceSets.main.libs}.
     */
    @Test
    void sourceSets_libs() {
        SourceDirectorySet dirs = project.sourceSets.main.libs

        assert dirs.srcDirs.contains(project.file('src/main/libs'))
    }

    /**
     * Test for {@code project.sourceSets.main.dmdl}.
     */
    @Test
    void sourceSets_dmdl() {
        DmdlConfiguration conf = project.asakusafw.dmdl
        SourceDirectorySet dirs = project.sourceSets.main.dmdl

        conf.dmdlSourceDirectory 'src/main/testing'

        assert dirs.srcDirs.size() == 1
        assert dirs.srcDirs.contains(project.file(conf.dmdlSourceDirectory))
    }

    /**
     * Test for {@code project.sourceSets.main.annotations}.
     */
    @Test
    void sourceSets_annotations() {
        JavacConfiguration conf = project.asakusafw.javac
        SourceDirectorySet dirs = project.sourceSets.main.annotations

        conf.annotationSourceDirectory 'src/main/testing'

        assert dirs.srcDirs.contains(project.file(conf.annotationSourceDirectory))
        // assert project.sourceSets.main.allJava.srcDirs.contains(project.file(conf.annotationSourceDirectory))
    }

    /**
     * Test for {@code project.sourceSets.main.java}.
     */
    @Test
    void sourceSets_java_extension() {
        ModelgenConfiguration conf = project.asakusafw.modelgen
        SourceDirectorySet dirs = project.sourceSets.main.java

        conf.modelgenSourceDirectory "${project.buildDir}/testing"

        // assert dirs.srcDirs.contains(project.file(conf.modelgenSourceDirectory))
    }

    /**
     * Test for {@code project.tasks.compileDMDL}.
     */
    @Test
    void tasks_compileDMDL() {
        AsakusafwPluginConvention convention = project.asakusafw
        convention.logbackConf 'testing/logback'
        convention.maxHeapSize '1G'

        convention.modelgen.modelgenSourcePackage 'testing'
        convention.dmdl.dmdlEncoding 'ASCII'
        convention.javac.sourceEncoding 'MS932'
        convention.modelgen.modelgenSourceDirectory 'testing/modelgen'

        CompileDmdlTask task = project.tasks.compileDMDL
        assert task.logbackConf == project.file(convention.logbackConf)
        assert task.maxHeapSize == convention.maxHeapSize
        // assert task.sourcepath.contains(project.sourceSets.main.dmdl)
        assert task.systemProperties.isEmpty()
        assert task.jvmArgs.isEmpty()

        assert task.packageName == convention.modelgen.modelgenSourcePackage
        assert task.sourceEncoding == convention.dmdl.dmdlEncoding
        assert task.targetEncoding == convention.javac.sourceEncoding
        assert task.outputDirectory == project.file(convention.modelgen.modelgenSourceDirectory)
    }

    /**
     * Test for {@code project.tasks.generateTestbook}.
     */
    @Test
    void tasks_generateTestbook() {
        AsakusafwPluginConvention convention = project.asakusafw
        convention.logbackConf 'testing/logback'
        convention.maxHeapSize '1G'

        convention.dmdl.dmdlEncoding 'ASCII'
        convention.testtools.testDataSheetFormat 'DATAX'
        convention.testtools.testDataSheetDirectory 'testing/datasheets'

        GenerateTestbookTask task = project.tasks.generateTestbook
        assert task.logbackConf == project.file(convention.logbackConf)
        assert task.maxHeapSize == convention.maxHeapSize
        assert task.sourcepath.contains(project.sourceSets.main.dmdl)
        assert task.systemProperties.isEmpty()
        assert task.jvmArgs.isEmpty()

        assert task.sourceEncoding == convention.dmdl.dmdlEncoding
        assert task.outputSheetFormat == convention.testtools.testDataSheetFormat
        assert task.outputDirectory == project.file(convention.testtools.testDataSheetDirectory)
    }

    /**
     * Test for {@code project.tasks.compileBatchapp}.
     */
    @Test
    void tasks_compileBatchapp() {
        Task task = project.tasks.findByName('compileBatchapp')
        assert task != null
    }

    /**
     * Test for {@code project.tasks.jarBatchapp}.
     */
    @Test
    void tasks_jarBatchapp() {
        Task task = project.tasks.findByName('jarBatchapp')
        assert task instanceof Jar
    }

    /**
     * Test for {@code project.tasks.generateHiveDDL}.
     */
    @Test
    void tasks_generateHiveDDL() {
        AsakusafwPluginConvention convention = project.asakusafw
        convention.logbackConf 'testing/logback'
        convention.maxHeapSize '1G'

        GenerateHiveDdlTask task = project.tasks.generateHiveDDL
        assert task.logbackConf == project.file(convention.logbackConf)
        assert task.maxHeapSize == convention.maxHeapSize
        assert task.systemProperties.isEmpty()
        assert task.jvmArgs.isEmpty()

        assert task.sourcepath.contains(project.sourceSets.main.output.classesDir)
        assert task.pluginClasspath.isEmpty()

        assert task.outputFile == project.file("${project.buildDir}/hive-ddl/${project.name}.sql")
        task.setOptOutput('testing/output')
        assert task.outputFile == project.file('testing/output')

        assert task.include == null
        task.setOptInclude('testing_include')
        assert task.include == 'testing_include'

        assert task.location == null
        task.setOptLocation('/testing/location')
        assert task.location == '/testing/location'

        assert task.databaseName == null
        task.setOptDatabaseName('testdb')
        assert task.databaseName == 'testdb'
    }

    /**
     * Test for {@code version}.
     */
    @Test
    void version() {
        project.asakusafwBase.frameworkVersion = '__VERSION__'
        assert project.asakusafw.core.version == '__VERSION__'
    }
}
