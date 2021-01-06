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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link PropertyConfigurator}.
 */
public class PropertyConfiguratorTest {

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
                .with(PropertyConfigurator.nothing());
        assertThat(project.properties().keySet(), hasSize(0));
    }

    /**
     * test for system.
     */
    @Test
    public void system() {
        String version = System.getProperty("java.version");
        Assume.assumeNotNull(version);

        BasicProject project = project()
                .with(PropertyConfigurator.system());
        assertThat(project.property("java.version"), is(version));
    }

    /**
     * test for pair.
     */
    @Test
    public void of_pair() {
        BasicProject project = project()
                .with(PropertyConfigurator.of("TESTING", "OK"));
        assertThat(project.property("TESTING"), is("OK"));

        project.with(PropertyConfigurator.of("TESTING", (String) null));
        assertThat(project.property("TESTING"), is(nullValue()));
    }

    /**
     * test for path.
     * @throws IOException if failed
     */
    @Test
    public void of_path() throws IOException {
        Path path = Paths.get("src/main/java");

        BasicProject project = project()
                .with(PropertyConfigurator.of("TESTING", path));
        assertThat(
                Files.isSameFile(path, Paths.get(project.property("TESTING"))),
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
                .with(PropertyConfigurator.of(vars));
        assertThat(project.property("A"), is("1"));
        assertThat(project.property("B"), is("2"));
        assertThat(project.property("C"), is("3"));
        assertThat(project.property("D"), is(nullValue()));

        Map<String, String> patch = new LinkedHashMap<>();
        patch.put("B", "4");
        patch.put("C", null);
        patch.put("D", "5");
        project.with(PropertyConfigurator.of(patch));
        assertThat(project.property("A"), is("1"));
        assertThat(project.property("B"), is("4"));
        assertThat(project.property("C"), is(nullValue()));
        assertThat(project.property("D"), is("5"));
    }

    /**
     * test for URL.
     */
    @Test
    public void of_url() {
        BasicProject project = project()
                .with(PropertyConfigurator.of(resource("a")));
        assertThat(project.property("a"), is("1"));
    }

    /**
     * test for URL.
     */
    @Test
    public void of_url_optional() {
        BasicProject project = project()
                .with(PropertyConfigurator.of(Optional.empty()));
        assertThat(project.properties().keySet(), hasSize(0));

        project.with(PropertyConfigurator.of(Optional.of(resource("a"))));
        assertThat(project.property("a"), is("1"));
    }

    /**
     * test for URLs.
     */
    @Test
    public void of_url_enum() {
        BasicProject project = project()
                .with(PropertyConfigurator.of(Collections.enumeration(Arrays.asList(
                        resource("a"), resource("b"), resource("c"), resource("b-override")))));
        assertThat(project.property("a"), is("1"));
        assertThat(project.property("b"), is("-"));
        assertThat(project.property("c"), is("3"));
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

    private static URL resource(String name) {
        Path path = Paths.get("src/test/data/property", name + ".properties");
        assertThat(Files.isReadable(path), is(true));
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            Assume.assumeNoException(e);
            throw new AssertionError();
        }
    }
}
