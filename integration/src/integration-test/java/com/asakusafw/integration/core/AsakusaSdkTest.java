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

import com.asakusafw.integration.AsakusaProject;
import com.asakusafw.integration.AsakusaProjectProvider;
import com.asakusafw.utils.gradle.Bundle;
import com.asakusafw.utils.gradle.ContentsConfigurator;

/**
 * Test for {@code asakusafw-sdk}.
 */
public class AsakusaSdkTest {

    /**
     * project provider.
     */
    @Rule
    public final AsakusaProjectProvider provider = new AsakusaProjectProvider()
        .withProject(ContentsConfigurator.copy(data("sdk-simple")));

    /**
     * help.
     */
    @Test
    public void help() {
        AsakusaProject project = provider.newInstance("simple");
        project.gradle("help");
    }

    /**
     * version.
     */
    @Test
    public void version() {
        AsakusaProject project = provider.newInstance("simple");
        project.gradle("asakusaVersion");
    }

    /**
     * upgrade.
     */
    @Test
    public void upgrade() {
        AsakusaProject project = provider.newInstance("simple");
        project.gradle("asakusaUpgrade");
        Bundle contents = project.getContents();
        assertThat(contents.find("gradlew"), is(not(Optional.empty())));
        assertThat(contents.find("gradlew.bat"), is(not(Optional.empty())));
    }

    /**
     * w/ DMDL.
     */
    @Test
    public void dmdl() {
        AsakusaProject project = provider.newInstance("simple")
                .with(ContentsConfigurator.copy(data("ksv/src/main/dmdl"), "src/main/dmdl"));
        project.gradle("compileDMDL");
        Bundle contents = project.getContents();
        assertThat(
                contents.find("build/generated-sources/modelgen/com/example/modelgen/dmdl/model/Ksv.java"),
                is(not(Optional.empty())));
    }

    /**
     * w/ DMDL generate testbook.
     */
    @Test
    public void testbook() {
        AsakusaProject project = provider.newInstance("simple")
                .with(ContentsConfigurator.copy(data("ksv/src/main/dmdl"), "src/main/dmdl"));
        project.gradle("generateTestbook");
        Bundle contents = project.getContents();
        assertThat(
                contents.find("build/excel/ksv.xls"),
                is(not(Optional.empty())));
    }

    /**
     * w/ annotation processor.
     */
    @Test
    public void compile() {
        AsakusaProject project = provider.newInstance("simple")
                .with(ContentsConfigurator.copy(data("ksv/src/main"), "src/main"));
        project.gradle("compileJava");
        Bundle contents = project.getContents();
        assertThat(
                contents.find("build/generated-sources/annotations/com/example/KsvOperatorFactory.java"),
                is(not(Optional.empty())));
    }

    /**
     * assemble w/o asakusa artifacts.
     */
    @Test
    public void assemble() {
        AsakusaProject project = provider.newInstance("simple")
                .with(ContentsConfigurator.copy(data("ksv/src"), "src"));
        project.gradle("assemble");
        Bundle contents = project.getContents();
        assertThat(contents.find("build/libs/simple.jar"), is(not(Optional.empty())));
    }

    /**
     * w/ eclipse.
     */
    @Test
    public void eclipse() {
        AsakusaProject project = provider.newInstance("simple")
                .with(ContentsConfigurator.copy(data("ksv/src"), "src"))
                .with(ContentsConfigurator.copy(data("sdk-eclipse")));
        project.gradle("eclipse");
        Bundle contents = project.getContents();
        assertThat(contents.find(".project"), is(not(Optional.empty())));
        assertThat(contents.find(".classpath"), is(not(Optional.empty())));
    }
}
