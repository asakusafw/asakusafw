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
public class WindGateTest {

    /**
     * project provider.
     */
    @Rule
    public final AsakusaProjectProvider provider = new AsakusaProjectProvider()
            .withProject(ContentsConfigurator.copy(data("windgate")))
            .withProject(AsakusaConfigurator.projectHome())
            .withProject(AsakusaConfigurator.hadoop(AsakusaConfigurator.Action.SKIP_IF_UNDEFINED));

    /**
     * {@code process.sh} - one-shot.
     */
    @Test
    public void oneshot() {
        AsakusaProject project = provider.newInstance("wg");
        project.gradle(g -> g.withArguments("-i").launch("installAsakusafw"));

        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        "windgate/bin/process.sh",
                        "copy",
                        "oneshot",
                        framework.get("windgate/script/copy.properties").toAbsolutePath().toString(),
                        "app",
                        "flow",
                        "testing",
                        ""),
                is(0));
        assertThat(
                framework.launch(
                        "windgate/bin/finalize.sh",
                        "copy",
                        "app",
                        "flow",
                        "testing"),
                is(0));

        Bundle contents = project.getContents();
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
    }

    /**
     * {@code process.sh} - begin and end.
     */
    @Test
    public void begin_end() {
        AsakusaProject project = provider.newInstance("wg");
        project.gradle(g -> g.withArguments("-i").launch("installAsakusafw"));

        Bundle contents = project.getContents();
        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        "windgate/bin/process.sh",
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toAbsolutePath().toString(),
                        "app",
                        "flow",
                        "testing",
                        ""),
                is(0));
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
        contents.get("output.csv", Files::delete);

        assertThat(
                framework.launch(
                        "windgate/bin/process.sh",
                        "copy",
                        "end",
                        framework.get("windgate/script/copy.properties").toAbsolutePath().toString(),
                        "app",
                        "flow",
                        "testing",
                        ""),
                is(0));
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
        contents.get("output.csv", Files::delete);

        assertThat(
                framework.launch(
                        "windgate/bin/finalize.sh",
                        "copy",
                        "app",
                        "flow",
                        "testing"),
                is(0));
    }

    /**
     * {@code process.sh} - begin and end.
     */
    @Test
    public void begin_finalize_end() {
        AsakusaProject project = provider.newInstance("wg");
        project.gradle(g -> g.withArguments("-i").launch("installAsakusafw"));

        Bundle contents = project.getContents();
        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        "windgate/bin/process.sh",
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toAbsolutePath().toString(),
                        "app",
                        "flow",
                        "testing",
                        ""),
                is(0));
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
        contents.get("output.csv", Files::delete);

        assertThat(
                framework.launch(
                        "windgate/bin/finalize.sh",
                        "copy",
                        "app",
                        "flow",
                        "testing"),
                is(0));

        assertThat(
                framework.launch(
                        "windgate/bin/process.sh",
                        "copy",
                        "end",
                        framework.get("windgate/script/copy.properties").toAbsolutePath().toString(),
                        "app",
                        "flow",
                        "testing",
                        ""),
                is(not(0)));
        assertThat(contents.find("output.csv"), is(Optional.empty()));

        assertThat(
                framework.launch(
                        "windgate/bin/finalize.sh",
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
        project.gradle(g -> g.withArguments("-i").launch("installAsakusafw"));

        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        "windgate/bin/process.sh",
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toAbsolutePath().toString(),
                        "app",
                        "flow",
                        "testing",
                        ""),
                is(0));

        assertThat(
                framework.launch(
                        "windgate/bin/process.sh",
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toAbsolutePath().toString(),
                        "app",
                        "flow",
                        "testing",
                        ""),
                is(not(0)));

        assertThat(
                framework.launch(
                        "windgate/bin/finalize.sh",
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
        project.gradle(g -> g.withArguments("-i").launch("installAsakusafw"));

        Bundle contents = project.getContents();
        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        "windgate/bin/process.sh",
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toAbsolutePath().toString(),
                        "app",
                        "flow",
                        "testing-1",
                        ""),
                is(0));
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
        contents.get("output.csv", Files::delete);

        assertThat(
                framework.launch(
                        "windgate/bin/process.sh",
                        "copy",
                        "begin",
                        framework.get("windgate/script/copy.properties").toAbsolutePath().toString(),
                        "app",
                        "flow",
                        "testing-2",
                        ""),
                is(0));
        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
        contents.get("output.csv", Files::delete);

        assertThat(
                framework.launch(
                        "windgate/bin/finalize.sh",
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

        project.gradle(g -> g.withArguments("-i").launch("installAsakusafw"));

        Bundle contents = project.getContents();
        Bundle framework = project.getFramework();
        assertThat(
                framework.launch(
                        "windgate/bin/process.sh",
                        "copy-remote",
                        "begin",
                        framework.get("windgate/script/remote-put.properties").toAbsolutePath().toString(),
                        "app",
                        "flow",
                        "testing",
                        ""),
                is(0));
        assertThat(contents.find("tmp.bin"), is(not(Optional.empty())));
        assertThat(
                framework.launch(
                        "windgate/bin/process.sh",
                        "copy-remote",
                        "end",
                        framework.get("windgate/script/remote-get.properties").toAbsolutePath().toString(),
                        "app",
                        "flow",
                        "testing",
                        ""),
                is(0));
        assertThat(
                framework.launch(
                        "windgate/bin/finalize.sh",
                        "copy-remote",
                        "app",
                        "flow",
                        "testing"),
                is(0));

        assertThat(contents.find("output.csv"), is(not(Optional.empty())));
    }
}
