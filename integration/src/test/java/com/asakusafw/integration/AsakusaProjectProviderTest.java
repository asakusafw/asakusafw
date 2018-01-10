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
package com.asakusafw.integration;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.utils.gradle.Bundle;
import com.asakusafw.utils.gradle.ContentsConfigurator;
import com.asakusafw.utils.gradle.EnvironmentConfigurator;
import com.asakusafw.utils.gradle.PropertyConfigurator;

/**
 * Test for {@link AsakusaProjectProvider}.
 */
public class AsakusaProjectProviderTest {

    /**
     * project provider.
     */
    @Rule
    public final AsakusaProjectProvider provider = new AsakusaProjectProvider();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        AsakusaProject project = provider.newInstance("testing");
        assertThat(String.valueOf(project.getContents().getDirectory().getFileName()), is("testing"));
    }

    /**
     * w/ provider conf.
     */
    @Test
    public void with_provider_conf() {
        provider.withProvider(it -> it.withProject(p -> p.withProperty("action.provider", "true")));
        AsakusaProject project = provider.newInstance("testing");
        assertThat(project.property("action.provider"), is("true"));
    }

    /**
     * w/ project conf.
     */
    @Test
    public void with_project_conf() {
        provider.withProject(it -> it.withProperty("action.project", "true"));
        AsakusaProject project = provider.newInstance("testing");
        assertThat(project.property("action.project"), is("true"));
    }

    /**
     * check environment variables.
     */
    @Test
    public void environment() {
        String path = System.getenv("PATH");
        Assume.assumeNotNull(path);

        AsakusaProject project = provider.newInstance("testing");
        assertThat(project.environment("PATH"), is(path));
    }

    /**
     * check system properties.
     */
    @Test
    public void properties() {
        String version = System.getProperty("java.version");
        Assume.assumeNotNull(version);

        AsakusaProject project = provider.newInstance("testing");
        assertThat(project.property("java.version"), is(version));
    }

    /**
     * check loading {@code META-INF/asakusa-integration/system.properties}.
     */
    @Test
    public void load_embed_properties() {
        AsakusaProject project = provider.newInstance("testing");
        assertThat(project.property("asakusafw.version"), is(notNullValue()));
    }

    /**
     * can overwrite {@code META-INF/asakusa-integration/system.properties}.
     */
    @Test
    public void asakusafw_version_override() {
        provider.withProject(PropertyConfigurator.of("asakusafw.version", "TESTING"));
        AsakusaProject project = provider.newInstance("testing");
        assertThat(project.property("asakusafw.version"), is("TESTING"));
    }

    /**
     * ASAKUSA_HOME must be overwritten by the provider.
     */
    @Test
    public void asakusa_home_temporary() {
        provider.withProject(EnvironmentConfigurator.of("ASAKUSA_HOME", "N/A"));
        AsakusaProject project = provider.newInstance("testing");
        assertThat(project.environment("ASAKUSA_HOME"), is(not("N/A")));
    }

    /**
     * w/ ASAKUSA_HOME.
     */
    @Test
    public void asakusa_home() {
        AsakusaProject project = provider.newInstance("testing")
                .with(ContentsConfigurator.copy("src/test/data/home"));
        assertThat(project.environment("ASAKUSA_HOME"), is(notNullValue()));
        project.gradle("putHome");
        project.withFramework(f -> {
            assertThat(lines(f.get("output.txt")), contains("OK"));
        });
    }

    /**
     * w/ ASAKUSA_HOME - not prepared.
     */
    @Test(expected = RuntimeException.class)
    public void asakusa_home_nothing() {
        AsakusaProject project = provider.newInstance("testing");
        project.getFramework();
    }

    /**
     * w/ bundle.
     */
    @Test
    public void add_bundle() {
        AsakusaProject project = provider.newInstance("testing")
                .with(ContentsConfigurator.copy("src/test/data/bundle"));

        Bundle bundle = project.addBundle("testing");
        project.gradle("putBundle");
        assertThat(lines(bundle.get("output.txt")), contains("OK"));
    }

    /**
     * w/ bundle.
     */
    @Test
    public void add_bundle_existing() {
        AsakusaProject project = provider.newInstance("testing")
                .with(ContentsConfigurator.copy("src/test/data/bundle"));

        Bundle bundle = project.addBundle("origin");
        project.addBundle("testing", bundle.getDirectory());

        project.gradle("putBundle");
        assertThat(lines(bundle.get("output.txt")), contains("OK"));
    }

    private static List<String> lines(Path file) {
        try {
            return Files.readAllLines(file, Charset.defaultCharset()).stream()
                    .map(String::trim)
                    .filter(it -> it.isEmpty() == false)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
