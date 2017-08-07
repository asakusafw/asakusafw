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

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.integration.AsakusaConfigurator;
import com.asakusafw.integration.AsakusaProject;
import com.asakusafw.integration.AsakusaProjectProvider;
import com.asakusafw.utils.gradle.Bundle;
import com.asakusafw.utils.gradle.ContentsConfigurator;

/**
 * Test for {@code $ASAKUSA_HOME/yaess}.
 * Please see {@code src/integration-test/data/yaess/README.md} on this project.
 */
public class YaessTest {

    /**
     * project provider.
     */
    @Rule
    public final AsakusaProjectProvider provider = new AsakusaProjectProvider()
            .withProject(ContentsConfigurator.copy(data("yaess")))
            .withProject(AsakusaConfigurator.projectHome())
            .withProject(AsakusaConfigurator.hadoop(AsakusaConfigurator.Action.UNSET_ALWAYS));

    /**
     * {@code yaess-batch.sh}.
     */
    @Test
    public void batch() {
        AsakusaProject project = provider.newInstance("yss");
        project.gradle("installAsakusafw");

        Bundle framework = project.getFramework();
        assertThat(
                framework.launch("yaess/bin/yaess-batch.sh", "testing"),
                is(0));

        Bundle contents = project.getContents();
        assertThat(contents.find("prepare.txt"), is(not(Optional.empty())));
        assertThat(contents.find("import.txt"), is(not(Optional.empty())));
        assertThat(contents.find("main.txt"), is(not(Optional.empty())));
        assertThat(contents.find("export.txt"), is(not(Optional.empty())));
        assertThat(contents.find("finalize.txt"), is(not(Optional.empty())));
    }

    /**
     * {@code yaess-flow.sh}.
     */
    @Test
    public void flow() {
        AsakusaProject project = provider.newInstance("yss");
        project.gradle("installAsakusafw");

        Bundle framework = project.getFramework();
        assertThat(
                framework.launch("yaess/bin/yaess-flow.sh", "testing", "work", "E"),
                is(0));

        Bundle contents = project.getContents();
        assertThat(contents.find("prepare.txt"), is(Optional.empty()));
        assertThat(contents.find("import.txt"), is(not(Optional.empty())));
        assertThat(contents.find("main.txt"), is(not(Optional.empty())));
        assertThat(contents.find("export.txt"), is(not(Optional.empty())));
        assertThat(contents.find("finalize.txt"), is(not(Optional.empty())));
    }

    /**
     * {@code yaess-flow.sh}.
     */
    @Test
    public void flow_generate_id() {
        AsakusaProject project = provider.newInstance("yss");
        project.gradle("installAsakusafw");

        Bundle framework = project.getFramework();
        assertThat(
                framework.launch("yaess/bin/yaess-flow.sh", "testing", "work", "<generate>"),
                is(0));

        Bundle contents = project.getContents();
        assertThat(contents.find("prepare.txt"), is(Optional.empty()));
        assertThat(contents.find("import.txt"), is(not(Optional.empty())));
        assertThat(contents.find("main.txt"), is(not(Optional.empty())));
        assertThat(contents.find("export.txt"), is(not(Optional.empty())));
        assertThat(contents.find("finalize.txt"), is(not(Optional.empty())));
    }

    /**
     * {@code yaess-phase.sh}.
     */
    @Test
    public void phase() {
        AsakusaProject project = provider.newInstance("yss");
        project.gradle("installAsakusafw");

        Bundle framework = project.getFramework();
        assertThat(
                framework.launch("yaess/bin/yaess-phase.sh", "testing", "work", "main", "E"),
                is(0));

        Bundle contents = project.getContents();
        assertThat(contents.find("prepare.txt"), is(Optional.empty()));
        assertThat(contents.find("import.txt"), is(Optional.empty()));
        assertThat(contents.find("main.txt"), is(not(Optional.empty())));
        assertThat(contents.find("export.txt"), is(Optional.empty()));
        assertThat(contents.find("finalize.txt"), is(Optional.empty()));
    }

    /**
     * script includes hadoop command, and use system hadoop.
     */
    @Test
    public void hadoop_system() {
        doHadoop(provider.newInstance("yss")
                .with(AsakusaConfigurator.hadoop(AsakusaConfigurator.Action.SKIP_IF_UNDEFINED)));
    }

    /**
     * script includes hadoop command, and use embedded hadoop.
     */
    @Test
    public void hadoop_embed() {
        doHadoop(provider.newInstance("yss")
                .with(AsakusaConfigurator.hadoop(AsakusaConfigurator.Action.UNSET_ALWAYS)));
    }

    private static void doHadoop(AsakusaProject project) {
        project.gradle("installAsakusafw");
        Bundle framework = project.getFramework();
        assertThat(
                framework.launch("yaess/bin/yaess-batch.sh", "whadoop"),
                is(0));
        Bundle contents = project.getContents();
        assertThat(contents.find("hadoop.txt"), is(not(Optional.empty())));
        assertThat(contents.find("cleanup.txt"), is(not(Optional.empty())));
    }
}
