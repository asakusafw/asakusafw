/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.integration.core;

import static com.asakusafw.integration.core.Util.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;

import org.junit.AssumptionViolatedException;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.integration.AsakusaProject;
import com.asakusafw.integration.AsakusaProjectProvider;
import com.asakusafw.utils.gradle.Bundle;
import com.asakusafw.utils.gradle.CommandPath;
import com.asakusafw.utils.gradle.ContentsConfigurator;

/**
 * Test for setup tools.
 */
public class SetupToolsTest {

    private static final String SETUP_JAR = "tools/bin/setup.jar";

    /**
     * project provider.
     */
    @Rule
    public final AsakusaProjectProvider provider = new AsakusaProjectProvider()
            .withProject(ContentsConfigurator.copy(data("organizer-simple")));

    private static Path java() {
        return CommandPath.system().find("java")
                .orElseThrow(() -> new AssumptionViolatedException("missing java command"));
    }

    /**
     * {@code setup}.
     */
    @Test
    public void setup() {
        Path java = java();

        AsakusaProject project = provider.newInstance("tls");
        project.gradle("installAsakusafw");

        int exit = project.getCommandLauncher().launch(
                java,
                "-jar",
                project.getFramework().get(SETUP_JAR).toAbsolutePath().toString());

        assertThat(exit, is(0));
    }

    /**
     * {@code setup --help}.
     */
    @Test
    public void setup_help() {
        Path java = java();

        AsakusaProject project = provider.newInstance("tls");
        project.gradle("installAsakusafw");

        int exit = project.getCommandLauncher().launch(
                java,
                "-jar",
                project.getFramework().get(SETUP_JAR).toAbsolutePath().toString(),
                "--help");

        assertThat(exit, is(0));
    }

    /**
     * {@code setup} with non-default home.
     */
    @Test
    public void setup_copy() {
        Path java = java();

        AsakusaProject project = provider.newInstance("tls");
        project.gradle("installAsakusafw");

        Bundle copy = project.addBundle("copy").copy(project.getFramework().getDirectory());
        int exit = project.getCommandLauncher().launch(
                java,
                "-jar",
                project.getFramework().get(SETUP_JAR).toAbsolutePath().toString(),
                copy.getDirectory().toAbsolutePath().toString());

        assertThat(exit, is(0));
    }

    /**
     * {@code setup} with invalid home.
     */
    @Test
    public void setup_not_home() {
        Path java = java();

        AsakusaProject project = provider.newInstance("tls");
        project.gradle("installAsakusafw");

        Bundle invalid = project.addBundle("invalid");
        int exit = project.getCommandLauncher().launch(
                java,
                "-jar",
                project.getFramework().get(SETUP_JAR).toAbsolutePath().toString(),
                invalid.getDirectory().toAbsolutePath().toString());

        assertThat(exit, is(not(0)));
    }
}
