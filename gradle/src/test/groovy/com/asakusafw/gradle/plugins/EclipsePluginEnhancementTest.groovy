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

/**
 * Test for {@link EclipsePluginEnhancement}.
 */
class EclipsePluginEnhancementTest {

    /**
     * The test initializer.
     */
    @Rule
    public final TestRule initializer = new TestRule() {
        Statement apply(Statement stmt, Description desc) {
            project = ProjectBuilder.builder().withName(desc.methodName).build()
            project.apply plugin: 'asakusafw'
            project.asakusafwBase.frameworkVersion = '0.0.0'
            return stmt
        }
    }

    Project project

    /**
     * Test for {@code project.configurations} without eclipse plug-in.
     */
    @Test
    public void configurations_without_eclipse() {
        assert project.configurations.findByName('eclipseAnnotationProcessor') == null
    }

    /**
     * Test for {@code project.configurations}.
     */
    @Test
    public void configurations() {
        project.apply plugin: 'eclipse'
        assert project.configurations.findByName('eclipseAnnotationProcessor') != null
    }
}
