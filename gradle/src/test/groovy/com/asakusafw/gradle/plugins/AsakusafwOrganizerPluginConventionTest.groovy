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
package com.asakusafw.gradle.plugins

import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.BatchappsConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.DirectIoConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.ExtensionConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.HadoopConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.HiveConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.TestingConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.WindGateConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.YaessConfiguration

/**
 * Test for {@link AsakusafwOrganizerPluginConvention}.
 */
class AsakusafwOrganizerPluginConventionTest {

    /**
     * The test initializer.
     */
    @Rule
    public final TestRule initializer = new TestRule() {
        Statement apply(Statement stmt, Description desc) {
            project = ProjectBuilder.builder().withName(desc.methodName).build()
            project.group = 'com.example.testing'
            project.version = '0.1.0'
            project.apply plugin: 'asakusafw-organizer'
            project.asakusafwBase.frameworkVersion = '0.0.0'
            convention = project.asakusafwOrganizer
            return stmt
        }
    }

    Project project

    AsakusafwOrganizerPluginConvention convention

    /**
     * Test for {@code project.asakusafwOrganizer} convention default values.
     */
    @Test
    public void defaults() {
        assert convention != null

        project.asakusafwBase.frameworkVersion = 'AFW_TEST'
        assert convention.asakusafwVersion == project.asakusafwBase.frameworkVersion
        assert convention.assembleDir == "${project.buildDir}/asakusafw-assembly"
        assert convention.hadoop instanceof HadoopConfiguration
        assert convention.directio instanceof DirectIoConfiguration
        assert convention.windgate instanceof WindGateConfiguration
        assert convention.hive instanceof HiveConfiguration
        assert convention.yaess instanceof YaessConfiguration
        assert convention.batchapps instanceof BatchappsConfiguration
        assert convention.testing instanceof TestingConfiguration
        assert convention.extension instanceof ExtensionConfiguration
        assert convention.assembly.handlers.isEmpty()
    }

    /**
     * Test for {@code project.asakusafwOrganizer.hadoop} convention default values.
     */
    @Test
    public void hadoop_defaults() {
        project.asakusafwBase.hadoopVersion = 'TESTING'

        assert convention.hadoop.embed == false
        assert convention.hadoop.version == 'TESTING'
    }

    /**
     * Test for {@code project.asakusafwOrganizer.directio} convention default values.
     */
    @Test
    public void directio_defaults() {
        assert convention.directio.enabled == true
    }

    /**
     * Test for {@code project.asakusafwOrganizer.windgate} convention default values.
     */
    @Test
    public void windgate_defaults() {
        assert convention.windgate.enabled == true
        assert convention.windgate.sshEnabled == true
        assert convention.windgate.retryableEnabled == false
    }

    /**
     * Test for {@code project.asakusafwOrganizer.hive} convention default values.
     */
    @Test
    public void hive_defaults() {
        assert convention.hive.enabled == false
        assert convention.hive.libraries.size() == 1
        assert convention.hive.libraries[0].startsWith('org.apache.hive:hive-exec:')
    }

    /**
     * Test for {@code project.asakusafwOrganizer.extension} convention default values.
     */
    @Test
    public void extension_defaults() {
        assert convention.extension.libraries.size() == 0
    }

    /**
     * Test for {@code project.asakusafwOrganizer.yaess} convention default values.
     */
    @Test
    public void yaess_defaults() {
        assert convention.yaess.enabled == true
        assert convention.yaess.toolsEnabled == true
        assert convention.yaess.hadoopEnabled == true
        assert convention.yaess.jobqueueEnabled == false
        assert convention.yaess.iterativeEnabled == false
    }

    /**
     * Test for {@code project.asakusafwOrganizer.batchapps} convention default values.
     */
    @Test
    public void batchapps_defaults() {
        assert convention.batchapps.enabled == true
    }

    /**
     * Test for {@code project.asakusafwOrganizer.testing} convention default values.
     */
    @Test
    public void testing_defaults() {
        assert convention.testing.enabled == false
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles.dev} convention default values.
     */
    @Test
    public void dev_defaults() {
        project.asakusafwBase.frameworkVersion = 'AFW_TEST'
        AsakusafwOrganizerProfile profile = convention.profiles.dev
        assert profile.name == "dev"
        assert profile.asakusafwVersion == convention.asakusafwVersion
        assert profile.assembleDir == "${convention.assembleDir}-dev"
        assert profile.archiveName == "asakusafw-${project.name}-dev.tar.gz"
        assert profile.hadoop.embed == convention.hadoop.embed
        assert profile.directio.enabled == convention.directio.enabled
        assert profile.windgate.enabled == convention.windgate.enabled
        assert profile.yaess.enabled == convention.yaess.enabled
        assert profile.batchapps.enabled == false
        assert profile.testing.enabled == true
        assert profile.components.handlers.isEmpty()
        assert profile.assembly.handlers.isEmpty()
    }

    /**
     * Test for {@code project.asakusafwOrganizer.prod} convention default values.
     */
    @Test
    public void prod_defaults() {
        project.asakusafwBase.frameworkVersion = 'AFW_TEST'
        AsakusafwOrganizerProfile profile = convention.profiles.prod
        assert profile.name == "prod"
        assert profile.asakusafwVersion == convention.asakusafwVersion
        assert profile.assembleDir == "${convention.assembleDir}-prod"
        assert profile.archiveName == "asakusafw-${project.name}.tar.gz"
        assert profile.hadoop.embed == convention.hadoop.embed
        assert profile.directio.enabled == convention.directio.enabled
        assert profile.windgate.enabled == convention.windgate.enabled
        assert profile.yaess.enabled == convention.yaess.enabled
        assert profile.batchapps.enabled == convention.batchapps.enabled
        assert profile.testing.enabled == convention.testing.enabled
        assert profile.components.handlers.isEmpty()
        assert profile.assembly.handlers.isEmpty()
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles} convention default values.
     */
    @Test
    public void profiles_defaults() {
        assert convention.profiles.collect { it.name }.toSet() == ['dev', 'prod'].toSet()

        AsakusafwOrganizerProfile profile = convention.profiles.testProfile
        assert convention.profiles.collect { it.name }.toSet() == ['dev', 'prod', 'testProfile'].toSet()

        assert profile != null
        assert profile.name == "testProfile"

        convention.assembleDir = 'AFW-TEST'
        assert profile.assembleDir == "${convention.assembleDir}-testProfile"
        assert profile.archiveName == "asakusafw-${project.name}-testProfile.tar.gz"

        assert profile.hadoop.embed == convention.hadoop.embed
        convention.hadoop.embed = !convention.hadoop.embed
        assert profile.hadoop.embed == convention.hadoop.embed

        assert profile.directio.enabled == convention.directio.enabled
        convention.directio.enabled = !convention.directio.enabled
        assert profile.directio.enabled == convention.directio.enabled

        assert profile.windgate.enabled == convention.windgate.enabled
        convention.windgate.enabled = !convention.windgate.enabled
        assert profile.windgate.enabled == convention.windgate.enabled

        assert profile.yaess.enabled == convention.yaess.enabled
        convention.yaess.enabled = !convention.yaess.enabled
        assert profile.yaess.enabled == convention.yaess.enabled

        assert profile.batchapps.enabled == convention.batchapps.enabled
        convention.batchapps.enabled = !convention.batchapps.enabled
        assert profile.batchapps.enabled == convention.batchapps.enabled

        assert profile.testing.enabled == convention.testing.enabled
        convention.testing.enabled = !convention.testing.enabled
        assert profile.testing.enabled == convention.testing.enabled

        assert profile.components.handlers.isEmpty()
        assert profile.assembly.handlers.isEmpty()
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles.*} must be splitted from inherited convention values.
     */
    @Test
    public void profiles_split() {
        AsakusafwOrganizerProfile profile = convention.profiles.testProfile

        profile.assembleDir = 'AFW-TEST'
        assert profile.assembleDir != "${convention.assembleDir}-testProfile"

        profile.directio.enabled = !convention.directio.enabled
        assert profile.directio.enabled != convention.directio.enabled

        profile.windgate.enabled = !convention.windgate.enabled
        assert profile.windgate.enabled != convention.windgate.enabled

        profile.yaess.enabled = !convention.yaess.enabled
        assert profile.yaess.enabled != convention.yaess.enabled

        profile.batchapps.enabled = !convention.batchapps.enabled
        assert profile.batchapps.enabled != convention.batchapps.enabled

        profile.testing.enabled = !convention.testing.enabled
        assert profile.testing.enabled != convention.testing.enabled
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles} with direct property access.
     */
    @Test
    public void profiles_configure_property() {
        convention.extension.libraries = ['AFW-TEST']
        convention.profiles.testProfile.extension.libraries = ['TEST-PROFILE']
        AsakusafwOrganizerProfile profile = convention.profiles.testProfile
        assert profile != null
        assert profile.name == "testProfile"
        assert profile.extension.libraries == ['TEST-PROFILE']
        assert convention.extension.libraries == ['AFW-TEST']
        assert convention.profiles.other.extension.libraries == convention.extension.libraries
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles} with configuration closure.
     */
    @Test
    public void profiles_configure_closure() {
        convention.extension.libraries = ['AFW-TEST']
        convention.profiles.testProfile {
            extension.libraries = ['TEST-PROFILE']
        }
        AsakusafwOrganizerProfile profile = convention.profiles.testProfile
        assert profile != null
        assert profile.name == "testProfile"
        assert profile.extension.libraries == ['TEST-PROFILE']
        assert convention.extension.libraries == ['AFW-TEST']
        assert convention.profiles.other.extension.libraries == convention.extension.libraries
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles.*.hive.libraries} with parent configurations.
     */
    @Test
    public void profiles_hive_libraries() {
        AsakusafwOrganizerProfile profile = convention.profiles.dev
        convention.hive.libraries = ['a']
        convention.hive.libraries = ['b0', 'b1']
        assert profile.hive.libraries.toSet() == ['b0', 'b1'].toSet()

        profile.hive.libraries += ['c0']
        assert convention.hive.libraries.toSet() == ['b0', 'b1'].toSet()
        assert profile.hive.libraries.toSet() == ['b0', 'b1', 'c0'].toSet()

        profile.hive.libraries = ['d0', 'd1']
        assert convention.hive.libraries.toSet() == ['b0', 'b1'].toSet()
        assert profile.hive.libraries.toSet() == ['d0', 'd1'].toSet()

        convention.hive.libraries = ['e0']
        assert convention.hive.libraries.toSet() == ['e0'].toSet()
        assert profile.hive.libraries.toSet() == ['d0', 'd1'].toSet()
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles.*.extension.libraries} with parent configurations.
     */
    @Test
    public void profiles_extension_libraries() {
        AsakusafwOrganizerProfile profile = convention.profiles.dev
        convention.extension.libraries = ['a']
        convention.extension.libraries = ['b0', 'b1']
        assert profile.extension.libraries.toSet() == ['b0', 'b1'].toSet()

        profile.extension.libraries += ['c0']
        assert convention.extension.libraries.toSet() == ['b0', 'b1'].toSet()
        assert profile.extension.libraries.toSet() == ['b0', 'b1', 'c0'].toSet()

        profile.extension.libraries = ['d0', 'd1']
        assert convention.extension.libraries.toSet() == ['b0', 'b1'].toSet()
        assert profile.extension.libraries.toSet() == ['d0', 'd1'].toSet()

        convention.extension.libraries = ['e0']
        assert convention.extension.libraries.toSet() == ['e0'].toSet()
        assert profile.extension.libraries.toSet() == ['d0', 'd1'].toSet()
    }

    /**
     * Test for changing {@code project.asakusafwOrganizer.asakusafwVersion}.
     */
    @Test
    void asakusafwVersion_change() {
        project.asakusafwBase.frameworkVersion = '0.1.0'
        convention.asakusafwVersion = 'CHANGED' // ignored
        assert convention.asakusafwVersion == '0.1.0'
    }

    /**
     * Test for changing {@code project.asakusafwOrganizer.profiles.*.asakusafwVersion}.
     */
    @Test
    void profiles_asakusafwVersion_change() {
        project.asakusafwBase.frameworkVersion = '0.1.0'
        convention.profiles.testing.asakusafwVersion = 'CHANGED' // ignored
        assert convention.profiles.testing.asakusafwVersion == '0.1.0'
    }
}
