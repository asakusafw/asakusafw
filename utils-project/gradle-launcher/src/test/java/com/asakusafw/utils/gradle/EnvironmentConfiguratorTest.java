/**
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
package com.asakusafw.utils.gradle;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link EnvironmentConfigurator}.
 */
public class EnvironmentConfiguratorTest {

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    /**
     * test for nothing.
     */
    @Test
    public void nothing() {
        BasicProject project = project()
                .with(EnvironmentConfigurator.nothing());
        assertThat(project.environment().keySet(), hasSize(0));
    }

    /**
     * test for system.
     */
    @Test
    public void system() {
        String path = System.getenv("PATH");
        Assume.assumeNotNull(path);

        BasicProject project = project()
                .with(EnvironmentConfigurator.system());
        assertThat(project.environment("PATH"), is(path));
    }

    /**
     * test for pair.
     */
    @Test
    public void of_pair() {
        BasicProject project = project()
                .with(EnvironmentConfigurator.of("TESTING", "OK"));
        assertThat(project.environment("TESTING"), is("OK"));

        project.with(EnvironmentConfigurator.of("TESTING", (String) null));
        assertThat(project.environment("TESTING"), is(nullValue()));
    }

    /**
     * test for path.
     * @throws IOException if failed
     */
    @Test
    public void of_path() throws IOException {
        Path path = Paths.get("src/main/java");

        BasicProject project = project()
                .with(EnvironmentConfigurator.of("TESTING", path));
        assertThat(
                Files.isSameFile(path, Paths.get(project.environment("TESTING"))),
                is(true));
    }

    /**
     * test for map.
     * @throws IOException if failed
     */
    @Test
    public void of_map() throws IOException {
        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("A", "1");
        vars.put("B", "2");
        vars.put("C", "3");

        BasicProject project = project()
                .with(EnvironmentConfigurator.of(vars));
        assertThat(project.environment("A"), is("1"));
        assertThat(project.environment("B"), is("2"));
        assertThat(project.environment("C"), is("3"));
        assertThat(project.environment("D"), is(nullValue()));

        Map<String, String> patch = new LinkedHashMap<>();
        patch.put("B", "4");
        patch.put("C", null);
        patch.put("D", "5");
        project.with(EnvironmentConfigurator.of(patch));
        assertThat(project.environment("A"), is("1"));
        assertThat(project.environment("B"), is("4"));
        assertThat(project.environment("C"), is(nullValue()));
        assertThat(project.environment("D"), is("5"));
    }

    private BasicProject project() {
        Path directory;
        try {
            directory = temporary.newFolder().toPath();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return new BasicProject(directory);
    }
}
