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

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.ThunderGateConfiguration

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
            project.plugins.apply 'asakusafw-organizer'
            convention = project.asakusafwOrganizer

            // NOTE: must set group after convention is created
            project.group = 'com.example.testing'
            project.verion = '0.1.0'
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

        try {
            convention.getAsakusafwVersion()
            fail()
        } catch (InvalidUserDataException e) {
            // ok
        }
        assert convention.assembleDir == "${project.buildDir}/asakusafw-assembly"
        assert convention.thundergate instanceof ThunderGateConfiguration
    }

    /**
     * Test for {@code project.asakusafwOrganizer.thundergate} convention default values.
     */
    @Test
    public void thundergate_defaults() {
        assert !convention.thundergate.enabled
        assert convention.thundergate.target == null
    }

    /**
     * Test for {@code project.asakusafwOrganizer.thundergate.enable}.
     */
    @Test
    public void thundergate_enable_by_target() {
        convention.thundergate.target 'testing'
        assert convention.thundergate.enabled
        assert convention.thundergate.target == 'testing'
    }
}
