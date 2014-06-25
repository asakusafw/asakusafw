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

import static org.junit.Assert.*

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.CompilerConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.DmdlConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.JavacConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ModelgenConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.TestToolsConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ThunderGateConfiguration

/**
 * Test for {@link AsakusafwPluginConvention}.
 */
class AsakusafwPluginConventionTest {

    /**
     * The test initializer.
     */
    @Rule
    public final TestRule initializer = new TestRule() {
        Statement apply(Statement stmt, Description desc) {
            project = ProjectBuilder.builder().withName(desc.methodName).build()
            project.plugins.apply 'asakusafw'
            convention = project.asakusafw

            // NOTE: must set group after convention is created
            project.group = 'com.example.testing'
            project.version = '0.1.0'
            return stmt
        }
    }

    Project project

    AsakusafwPluginConvention convention

    /**
     * Test for {@code project.asakusafw} convention default values.
     */
    @Test
    void defaults() {
        assert convention != null

        try {
            convention.getAsakusafwVersion()
            fail()
        } catch (RuntimeException e) {
            // ok
        }
        assert convention.maxHeapSize == '1024m'
        assert convention.logbackConf == "src/${project.sourceSets.test.name}/resources/logback-test.xml"
        assert convention.basePackage == project.group

        assert convention.dmdl instanceof DmdlConfiguration
        assert convention.modelgen instanceof ModelgenConfiguration
        assert convention.javac instanceof JavacConfiguration
        assert convention.compiler instanceof CompilerConfiguration
        assert convention.testtools instanceof TestToolsConfiguration
        assert convention.thundergate instanceof ThunderGateConfiguration
    }

    /**
     * Test for {@code project.asakusafw.dmdl} default values.
     */
    @Test
    void dmdl_defaults() {
        assert convention.dmdl.dmdlEncoding == 'UTF-8'
        assert convention.dmdl.dmdlSourceDirectory == "src/${project.sourceSets.main.name}/dmdl"
    }

    /**
     * Test for {@code project.asakusafw.modelgen} convention default values.
     */
    @Test
    void modelgen_defaults() {
        assert convention.modelgen.modelgenSourcePackage == "${project.asakusafw.basePackage}.modelgen"
        assert convention.modelgen.modelgenSourceDirectory == "${project.buildDir}/generated-sources/modelgen"
    }

    /**
     * Test for {@code project.asakusafw.javac} convention default values.
     */
    @Test
    void javac_defaults() {
        assert convention.javac.annotationSourceDirectory == "${project.buildDir}/generated-sources/annotations"
        assert convention.javac.sourceEncoding == "UTF-8"
        assert convention.javac.sourceCompatibility == JavaVersion.VERSION_1_6
        assert convention.javac.targetCompatibility == JavaVersion.VERSION_1_6
    }

    /**
     * Test for {@code project.asakusafw.compiler} convention default values.
     */
    @Test
    void compiler_defaults() {
        assert convention.compiler.compiledSourcePackage == "${project.asakusafw.basePackage}.batchapp"
        assert convention.compiler.compiledSourceDirectory == "${project.buildDir}/batchc"
        assert convention.compiler.compilerOptions == ''
        assert convention.compiler.compilerWorkDirectory == "${project.buildDir}/batchcwork"
        assert convention.compiler.hadoopWorkDirectory == 'target/hadoopwork/${execution_id}'
    }

    /**
     * Test for {@code project.asakusafw.testtools} convention default values.
     */
    @Test
    void testtools_defaults() {
        assert convention.testtools.testDataSheetFormat == "ALL"
        assert convention.testtools.testDataSheetDirectory == "${project.buildDir}/excel"
    }

    /**
     * Test for {@code project.asakusafw.thundergate} convention default values.
     */
    @Test
    void thundergate_defaults() {
        assert convention.thundergate.ddlEncoding == null
        assert convention.thundergate.ddlOutputDirectory == "${project.buildDir}/thundergate/sql"
        assert convention.thundergate.ddlSourceDirectory == "src/${project.sourceSets.main.name}/sql/modelgen"
        assert convention.thundergate.deleteColumn == 'DELETE_FLAG'
        assert convention.thundergate.deleteValue == '"1"'
        assert convention.thundergate.dmdlOutputDirectory == "${project.buildDir}/thundergate/dmdl"
        assert convention.thundergate.excludes == null
        assert convention.thundergate.includes == null
        assert convention.thundergate.sidColumn == 'SID'
        assert convention.thundergate.target == null
        assert convention.thundergate.timestampColumn == 'UPDT_DATETIME'
    }

    /**
     * Test for {@code project.asakusafw.basePackage} value.
     */
    @Test
    void basePackage_transitive() {
        project.group = null
        try {
            convention.modelgen.getModelgenSourcePackage()
            fail()
        } catch (Exception e) {
            // ok
        }
        try {
            convention.compiler.getCompiledSourcePackage()
            fail()
        } catch (Exception e) {
            // ok
        }

        project.group 'testing.t1'
        assert convention.basePackage == 'testing.t1'
        assert convention.modelgen.modelgenSourcePackage == 'testing.t1.modelgen'
        assert convention.compiler.compiledSourcePackage == 'testing.t1.batchapp'

        convention.basePackage 'testing.t2'
        assert convention.modelgen.modelgenSourcePackage == 'testing.t2.modelgen'
        assert convention.compiler.compiledSourcePackage == 'testing.t2.batchapp'
    }

    /**
     * Test for {@code project.asakusafw} convention default values.
     */
    @Test
    void conventionProperties_defaults() {
        assert convention.getConventionProperties() != null
    }
}
