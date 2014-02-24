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

import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.DmdlConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.JavacConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ModelgenConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ThunderGateConfiguration
import com.asakusafw.gradle.tasks.CompileBatchappTask
import com.asakusafw.gradle.tasks.CompileDmdlTask
import com.asakusafw.gradle.tasks.GenerateTestbookTask
import com.asakusafw.gradle.tasks.GenerateThunderGateDataModelTask

/**
 * Test for {@link AsakusafwPlugin}.
 */
class AsakusafwPluginTest {

    /**
     * The test initializer.
     */
    @Rule
    public final TestRule initializer = new TestRule() {
        Statement apply(Statement stmt, Description desc) {
            project = ProjectBuilder.builder().withName(desc.methodName).build()
            project.plugins.apply 'asakusafw'
            return stmt
        }
    }

    Project project

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
        assert project.sourceSets.main.allJava.srcDirs.contains(project.file(conf.annotationSourceDirectory))
    }

    /**
     * Test for {@code project.sourceSets.main.thundergateDdl}.
     */
    @Test
    void sourceSets_thundergate() {
        ThunderGateConfiguration conf = project.asakusafw.thundergate
        SourceDirectorySet dirs = project.sourceSets.main.thundergateDdl

        conf.ddlSourceDirectory 'src/main/testing'

        assert dirs.srcDirs.contains(project.file(conf.ddlSourceDirectory))
    }

    /**
     * Test for {@code project.sourceSets.main.java}.
     */
    @Test
    void sourceSets_java_extension() {
        ModelgenConfiguration conf = project.asakusafw.modelgen
        SourceDirectorySet dirs = project.sourceSets.main.java

        conf.modelgenSourceDirectory "${project.buildDir}/testing"

        assert dirs.srcDirs.contains(project.file(conf.modelgenSourceDirectory))
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
        assert task.sourcepath.contains(project.sourceSets.main.dmdl)
        assert task.systemProperties.isEmpty()
        assert task.jvmArgs.isEmpty()

        assert task.packageName == convention.modelgen.modelgenSourcePackage
        assert task.sourceEncoding == convention.dmdl.dmdlEncoding
        assert task.targetEncoding == convention.javac.sourceEncoding
        assert task.outputDirectory == project.file(convention.modelgen.modelgenSourceDirectory)
    }

    /**
     * Test for {@code project.tasks.compileBatchapp}.
     */
    @Test
    void tasks_compileBatchapp() {
        AsakusafwPluginConvention convention = project.asakusafw
        convention.logbackConf 'testing/logback'
        convention.maxHeapSize '1G'

        convention.asakusafwVersion 'testing/compiled'
        convention.compiler.compiledSourcePackage 'testing.batchapps'
        convention.compiler.compilerOptions 'Xtesting=true'
        convention.compiler.compilerWorkDirectory 'testing/work'
        convention.compiler.hadoopWorkDirectory 'testing/hadoop'
        convention.compiler.compiledSourceDirectory 'testing/compiled'

        CompileBatchappTask task = project.tasks.compileBatchapp
        assert task.logbackConf == project.file(convention.logbackConf)
        assert task.maxHeapSize == convention.maxHeapSize
        assert project.files(task.sourcepath).contains(project.sourceSets.main.output.classesDir)
        assert task.systemProperties.isEmpty()
        assert task.jvmArgs.isEmpty()

        assert task.frameworkVersion == convention.asakusafwVersion
        assert task.packageName == convention.compiler.compiledSourcePackage
        assert task.compilerOptions == convention.compiler.compilerOptions
        assert task.workingDirectory == project.file(convention.compiler.compilerWorkDirectory)
        assert task.hadoopWorkingDirectory == convention.compiler.hadoopWorkDirectory
        assert task.outputDirectory == project.file(convention.compiler.compiledSourceDirectory)
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
     * Test for {@code project.tasks.generateThunderGateDataModel}.
     */
    @Test
    void tasks_generateThunderGateDataModel() {
        def home = project.file('testing/framework')
        home.mkdirs()
        project.plugins.getAt("asakusafw").frameworkHome = home

        AsakusafwPluginConvention convention = project.asakusafw
        convention.logbackConf 'testing/logback'
        convention.maxHeapSize '1G'

        convention.dmdl.dmdlEncoding 'ASCII'
        convention.thundergate.target 'testing'
        convention.thundergate.dmdlOutputDirectory 'testing/dmdlout'
        convention.thundergate.includes 'IN'
        convention.thundergate.excludes 'EX'
        convention.thundergate.sidColumn 'ID'
        convention.thundergate.timestampColumn 'TS'
        convention.thundergate.deleteColumn 'DEL'
        convention.thundergate.deleteValue '"D"'
        convention.thundergate.ddlOutputDirectory 'testing/ddlout'

        GenerateThunderGateDataModelTask task = project.tasks.generateThunderGateDataModel
        assert task.logbackConf == project.file(convention.logbackConf)
        assert task.maxHeapSize == convention.maxHeapSize
        assert task.sourcepath.contains(project.sourceSets.main.thundergateDdl)
        assert task.systemProperties.isEmpty()
        assert task.jvmArgs.isEmpty()

        assert task.ddlEncoding == convention.thundergate.ddlEncoding
        assert task.jdbcConfiguration == new File(home, "bulkloader/conf/${convention.thundergate.target}-jdbc.properties")
        assert task.dmdlOutputDirectory == project.file(convention.thundergate.dmdlOutputDirectory)
        assert task.dmdlOutputEncoding == convention.dmdl.dmdlEncoding
        assert task.includePattern == convention.thundergate.includes
        assert task.excludePattern == convention.thundergate.excludes
        assert task.sidColumnName == convention.thundergate.sidColumn
        assert task.timestampColumnName == convention.thundergate.timestampColumn
        assert task.deleteFlagColumnName == convention.thundergate.deleteColumn
        assert task.deleteFlagColumnValue == convention.thundergate.deleteValue
        assert task.recordLockDdlOutput.canonicalPath.startsWith(project.file(convention.thundergate.ddlOutputDirectory).canonicalPath)
        assert project.files(task.systemDdlFiles).contains(new File(home, 'bulkloader/sql/create_table.sql'))
        assert project.files(task.systemDdlFiles).contains(new File(home, 'bulkloader/sql/insert_import_table_lock.sql'))
    }
}
