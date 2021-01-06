/*
 * Copyright 2011-2021 Asakusa Framework Team.
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

import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName

/**
 * Tests for cross Gradle versions compatibility.
 */
class AsakusaUpgradeTest {

    /**
     * temporary project directory.
     */
    @Rule
    public final TemporaryFolder projectDir = new TemporaryFolder()

    /**
     * handles running test name.
     */
    @Rule
    public final TestName testName = new TestName()

    /**
     * Test for the system Gradle version.
     */
    @Test
    void system() {
        doUpgrade(GradleVersion.current().version)
    }

    /**
     * Test for {@code 4.7} (Asakusa {@code 0.10.1}).
     */
    @Test
    void 'v4.7'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 4.3.1} (Asakusa {@code 0.10.0}).
     */
    @Test
    void 'v4.3.1'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 3.4.1} (Asakusa {@code 0.9.1}).
     */
    @Test
    void 'v3.4.1'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 3.1} (Asakusa {@code 0.9.0}).
     */
    @Test
    void 'v3.1'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 2.14.1} (Asakusa {@code 0.8.1}).
     */
    @Test
    void 'v2.14.1'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 2.12} (Asakusa {@code 0.8.0}).
     */
    @Test
    void 'v2.12'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 2.8} (Asakusa {@code 0.7.5}).
     */
    @Test
    void 'v2.8'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 2.4} (Fixed on Asakusa {@code 0.7.4}).
     */
    @Test
    void 'v2.4'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 2.1} (Asakusa {@code 0.7.0}).
     */
    @Test
    void 'v2.1'() {
        doUpgradeFromTestName()
    }

    private void doUpgradeFromTestName() {
        doUpgrade(testName.methodName.replaceFirst('v', ''))
    }

    private void doUpgrade(String version) {
        Set<File> classpath = GradleTestkitHelper.toClasspath(AsakusafwBasePlugin, 'META-INF/gradle-plugins/asakusafw-sdk.properties')
        String script = GradleTestkitHelper.getSimpleBuildScript(classpath, 'asakusafw-sdk', 'asakusafw-organizer')
        GradleTestkitHelper.runGradle(projectDir.root, version, script, AsakusafwBasePlugin.TASK_UPGRADE)
    }
}
