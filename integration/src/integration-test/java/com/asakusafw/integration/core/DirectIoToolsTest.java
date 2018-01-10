/**
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
package com.asakusafw.integration.core;

import static com.asakusafw.integration.core.Util.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asakusafw.integration.AsakusaConfigurator;
import com.asakusafw.integration.AsakusaProject;
import com.asakusafw.integration.AsakusaProjectProvider;
import com.asakusafw.integration.PlatformUtil;
import com.asakusafw.utils.gradle.Bundle;
import com.asakusafw.utils.gradle.ContentsConfigurator;

/**
 * Test for {@code $ASAKUSA_HOME/directio}.
 * Please see {@code src/integration-test/data/directio-tools/README.md} on this project.
 */
@RunWith(Parameterized.class)
public class DirectIoToolsTest {

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
     * Skip this test class for Windows platform.
     */
    @BeforeClass
    public static void checkCkass() {
        PlatformUtil.skipIfWindows();
    }

    /**
     * project provider.
     */
    @Rule
    public final AsakusaProjectProvider provider = new AsakusaProjectProvider()
            .withProject(ContentsConfigurator.copy(data("directio-tools")));

    /**
     * Creates a new instance.
     * @param useHadoop whether or not the test uses hadoop command
     */
    public DirectIoToolsTest(boolean useHadoop) {
        if (useHadoop) {
            provider.withProject(AsakusaConfigurator.hadoop(AsakusaConfigurator.Action.SKIP_IF_UNDEFINED));
        } else {
            provider.withProject(AsakusaConfigurator.hadoop(AsakusaConfigurator.Action.UNSET_ALWAYS));
        }
    }

    /**
     * {@code list-file.sh}.
     */
    @Test
    public void list_file() {
        AsakusaProject project = provider.newInstance("dio");
        project.gradle(it -> it.withArguments("-i", "-s").launch("installAsakusafw"));
        Bundle framework = project.getFramework();
        assertThat(
                framework.launch("directio/bin/list-file.sh", "/", "**"),
                is(0));
    }

    /**
     * {@code delete-file.sh}.
     */
    @Test
    public void delete_file() {
        AsakusaProject project = provider.newInstance("dio");
        project.gradle("installAsakusafw");
        Bundle framework = project.getFramework();

        assertThat(framework.find("var/data/applied.txt"), is(not(Optional.empty())));
        assertThat(
                framework.launch("directio/bin/delete-file.sh", "/", "applied.txt"),
                is(0));

        assertThat(framework.find("var/data/applied.txt"), is(Optional.empty()));
        assertThat(
                "exit status is 0 even if there are no target files",
                framework.launch("directio/bin/delete-file.sh", "/", "applied.txt"),
                is(0));
    }

    /**
     * {@code list-transaction.sh}.
     */
    @Test
    public void list_transaction() {
        AsakusaProject project = provider.newInstance("dio");
        project.gradle("installAsakusafw");
        Bundle framework = project.getFramework();
        assertThat(
                framework.launch("directio/bin/list-transaction.sh"),
                is(0));
    }

    /**
     * {@code apply-transaction.sh}.
     */
    @Test
    public void apply_transaction() {
        AsakusaProject project = provider.newInstance("dio");
        project.gradle("installAsakusafw");
        Bundle framework = project.getFramework();

        assertThat(framework.find("var/data/committed.txt"), is(Optional.empty()));

        assertThat(
                framework.launch("directio/bin/apply-transaction.sh", "committed"),
                is(0));
        assertThat(framework.find("var/data/committed.txt"), is(not(Optional.empty())));

        framework.launch("directio/bin/delete-file.sh", "/", "committed.txt");

        assertThat(
                "target tx is already applied",
                framework.launch("directio/bin/apply-transaction.sh", "committed"),
                is(not(0)));
        assertThat(
                "can apply onle once",
                framework.find("var/data/committed.txt"),
                is(Optional.empty()));
    }

    /**
     * {@code apply-transaction.sh}.
     */
    @Test
    public void apply_transaction_uncommitted() {
        AsakusaProject project = provider.newInstance("dio");
        project.gradle("installAsakusafw");
        Bundle framework = project.getFramework();

        assertThat(
                "target tx is not committed",
                framework.launch("directio/bin/apply-transaction.sh", "uncommitted"),
                is(not(0)));
        assertThat(
                "cannot apply uncommitted transactions",
                framework.find("var/data/uncommitted.txt"),
                is(Optional.empty()));
    }

    /**
     * {@code abort-transaction.sh}.
     */
    @Test
    public void abort_transaction() {
        AsakusaProject project = provider.newInstance("dio");
        project.gradle("installAsakusafw");
        Bundle framework = project.getFramework();

        assertThat(
                framework.launch("directio/bin/abort-transaction.sh", "uncommitted"),
                is(0));
        assertThat(
                "target tx is already aborted",
                framework.launch("directio/bin/abort-transaction.sh", "uncommitted"),
                is(not(0)));

        assertThat(
                framework.launch("directio/bin/abort-transaction.sh", "committed"),
                is(0));
        assertThat(
                "target tx is already aborted",
                framework.launch("directio/bin/apply-transaction.sh", "committed"),
                is(not(0)));
        assertThat(
                "can apply onle once",
                framework.find("var/data/committed.txt"),
                is(Optional.empty()));
    }
}
