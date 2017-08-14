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

import java.nio.file.Files;
import java.util.Optional;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asakusafw.integration.AsakusaConfigurator;
import com.asakusafw.integration.AsakusaProject;
import com.asakusafw.integration.AsakusaProjectProvider;
import com.asakusafw.utils.gradle.Bundle;
import com.asakusafw.utils.gradle.ContentsConfigurator;

/**
 * Test for {@code $ASAKUSA_HOME/windgate} and {@code $ASAKUSA_HOME/windgate-ssh}.
 * Please see {@code src/integration-test/data/windgate/README.md} on this project.
 *
 * {@link #ssh()} method connects {@code localhost:22} to test WindGate SSH features,
 * and then uses the following system properties:
 * <ul>
 * <li> {@code ssh.key} - the SSH private key location, or skip tests if it is not defined </li>
 * <li> {@code ssh.passphase} - the pass phrase (default: "")</li>
 * </ul>
 */
@RunWith(Parameterized.class)
public class WindGateTest {

    private static final String PROCESS_CMD = "windgate/bin/process";

    private static final String FINALIZE_CMD = "windgate/bin/finalize";

    /**
     * Return the test parameters.
     * @return the test parameters
     */
    @Parameters(name = "use-hadoop:{0}")
    public static Object[][] getTestParameters() {
        return new Object[][] {
            { false },
            { true },
        };
    }

    /**
     * project provider.
     */
    @Rule
    public final AsakusaProjectProvider provider = new AsakusaProjectProvider()
            .withProject(ContentsConfigurator.copy(data("windgate")))
            .withProject(ContentsConfigurator.copy(data("logback-test")))
            .withProject(AsakusaConfigurator.projectHome());

    /**
     * Creates a new instance.
     * @param useHadoop whether or not the test uses hadoop command
     */
    public WindGateTest(boolean useHadoop) {
        if (useHadoop) {
            provider.withProject(AsakusaConfigurator.hadoop(AsakusaConfigurator.Action.SKIP_IF_UNDEFINED));
        } else {
            provider.withProject(AsakusaConfigurator.hadoop(AsakusaConfigurator.Action.UNSET_ALWAYS));
        }
    }

    /**
     * {@code process} - one-shot.
     */
    @Test
    public void oneshot() {
        AsakusaProject project = provider.newInstance("wg");
        project.gradle("installAsakusafw");

        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        PROCESS_CMD,
                        "copy",
                        "oneshot",
                        framework.get("windgate/script/copy.properties").toUri().toString(),
                        "app",
                        "flow",
                        "testing",
                        ","),
                is(0));
        assertThat(
                framework.launch(
                        FINALIZE_CMD,
                        "copy",
                        "app",
                        "flow",
                        "testing"),
                is(0));

        Bundle contents = project.getContents();
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
    }

    /**
     * {@code process} - begin and end.
     */
    @Test
    public void begin_end() {
        AsakusaProject project = provider.newInstance("wg");
        project.gradle("installAsakusafw");

        Bundle contents = project.getContents();
        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        PROCESS_CMD,
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toUri().toString(),
                        "app",
                        "flow",
                        "testing",
                        ","),
                is(0));
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
        contents.get("output.csv", Files::delete);

        assertThat(
                framework.launch(
                        PROCESS_CMD,
                        "copy",
                        "end",
                        framework.get("windgate/script/copy.properties").toUri().toString(),
                        "app",
                        "flow",
                        "testing",
                        ","),
                is(0));
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
        contents.get("output.csv", Files::delete);

        assertThat(
                framework.launch(
                        FINALIZE_CMD,
                        "copy",
                        "app",
                        "flow",
                        "testing"),
                is(0));
    }

    /**
     * {@code process} - begin and end.
     */
    @Test
    public void begin_finalize_end() {
        AsakusaProject project = provider.newInstance("wg");
        project.gradle("installAsakusafw");

        Bundle contents = project.getContents();
        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        PROCESS_CMD,
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toUri().toString(),
                        "app",
                        "flow",
                        "testing",
                        ","),
                is(0));
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
        contents.get("output.csv", Files::delete);

        assertThat(
                framework.launch(
                        FINALIZE_CMD,
                        "copy",
                        "app",
                        "flow",
                        "testing"),
                is(0));

        assertThat(
                framework.launch(
                        PROCESS_CMD,
                        "copy",
                        "end",
                        framework.get("windgate/script/copy.properties").toUri().toString(),
                        "app",
                        "flow",
                        "testing",
                        ","),
                is(not(0)));
        assertThat(contents.find("output.csv"), is(Optional.empty()));

        assertThat(
                framework.launch(
                        FINALIZE_CMD,
                        "copy",
                        "app",
                        "flow",
                        "testing"),
                is(0));
    }

    /**
     * reentrant.
     */
    @Test
    public void process_reentrant() {
        AsakusaProject project = provider.newInstance("wg");
        project.gradle("installAsakusafw");

        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        PROCESS_CMD,
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toUri().toString(),
                        "app",
                        "flow",
                        "testing",
                        ","),
                is(0));

        assertThat(
                framework.launch(
                        PROCESS_CMD,
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toUri().toString(),
                        "app",
                        "flow",
                        "testing",
                        ","),
                is(not(0)));

        assertThat(
                framework.launch(
                        FINALIZE_CMD,
                        "copy",
                        "app",
                        "flow",
                        "testing"),
                is(0));
    }

    /**
     * reentrant.
     */
    @Test
    public void process_parallel() {
        AsakusaProject project = provider.newInstance("wg");
        project.gradle("installAsakusafw");

        Bundle contents = project.getContents();
        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        PROCESS_CMD,
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toUri().toString(),
                        "app",
                        "flow",
                        "testing-1",
                        ","),
                is(0));
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
        contents.get("output.csv", Files::delete);

        assertThat(
                framework.launch(
                        PROCESS_CMD,
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toUri().toString(),
                        "app",
                        "flow",
                        "testing-2",
                        ","),
                is(0));
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
        contents.get("output.csv", Files::delete);

        assertThat(
                framework.launch(
                        FINALIZE_CMD,
                        "copy"),
                is(0));
    }

    /**
     * copy via SSH.
     */
    @Test
    public void ssh() {
        AsakusaProject project = provider.newInstance("wg");

        Assume.assumeNotNull("test requires -Dssh.key", System.getProperty("ssh.key"));
        project.withEnvironment("SSH_USER", System.getProperty("user.name"));
        project.withEnvironment("SSH_KEY", System.getProperty("ssh.key"));
        project.withEnvironment("SSH_PASS", System.getProperty("ssh.pass", ""));

        project.gradle("installAsakusafw");

        Bundle contents = project.getContents();
        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        PROCESS_CMD,
                        "copy-remote",
                        "begin",
                        framework.get("windgate/script/remote-put.properties").toUri().toString(),
                        "app",
                        "flow",
                        "testing",
                        ","),
                is(0));
        assertThat(contents.find("tmp.bin"), is(not(Optional.empty())));
        assertThat(
                framework.launch(
                        PROCESS_CMD,
                        "copy-remote",
                        "end",
                        framework.get("windgate/script/remote-get.properties").toUri().toString(),
                        "app",
                        "flow",
                        "testing",
                        ","),
                is(0));
        assertThat(
                framework.launch(
                        FINALIZE_CMD,
                        "copy-remote",
                        "app",
                        "flow",
                        "testing"),
                is(0));

        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
    }
}
