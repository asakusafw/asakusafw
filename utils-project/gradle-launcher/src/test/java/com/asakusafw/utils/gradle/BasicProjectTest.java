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
package com.asakusafw.utils.gradle;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.gradle.util.GradleVersion;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link BasicProject}.
 */
public class BasicProjectTest {

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    /**
     * contents - simple.
     */
    @Test
    public void bundle_simple() {
        BasicProject project = project("simple")
                .withContents(it -> it.put("var/data.txt", f -> Files.write(
                        f, Arrays.asList("Hello, world!"), Charset.defaultCharset())));
        assertThat(lines(project.getContents().get("var/data.txt")), contains("Hello, world!"));
    }

    /**
     * contents - copy.
     */
    @Test
    public void bundle_copy() {
        BasicProject project = project("simple")
                .withContents(it -> it.copy(Paths.get("src/test/data/base-package")))
                .withContents(it -> it.copy(Paths.get("src/test/data/base-package/etc/data.txt")))
                .withContents(it -> it.copy(Paths.get("src/test/data/base-package/etc"), "usr/var"))
                .withContents(it -> it.copy(Paths.get("src/test/data/base-package/etc/data.txt"), "another.txt"));
        assertThat(lines(project.getContents().get("etc/data.txt")), contains("Hello, world!"));
        assertThat(lines(project.getContents().get("data.txt")), contains("Hello, world!"));
        assertThat(lines(project.getContents().get("usr/var/data.txt")), contains("Hello, world!"));
        assertThat(lines(project.getContents().get("another.txt")), contains("Hello, world!"));
    }

    /**
     * contents - extract.
     */
    @Test
    public void bundle_extract() {
        BasicProject project = project("simple")
                .withContents(it -> it.extract(Paths.get("src/test/data/base-archive/data.zip")))
                .withContents(it -> it.extract(Paths.get("src/test/data/base-archive/data.zip"), "usr"));
        assertThat(lines(project.getContents().get("etc/data.txt")), contains("Hello, world!"));
        assertThat(lines(project.getContents().get("usr/etc/data.txt")), contains("Hello, world!"));
    }

    /**
     * contents - extract w/ in-archive paths.
     */
    @Test
    public void bundle_extract_offset() {
        BasicProject project = project("simple")
                .withContents(it -> it.extract(Paths.get("src/test/data/base-archive/data.zip/etc")))
                .withContents(it -> it.extract(Paths.get("src/test/data/base-archive/data.zip/etc/data.txt"), "f.txt"));
        assertThat(lines(project.getContents().get("data.txt")), contains("Hello, world!"));
        assertThat(lines(project.getContents().get("f.txt")), contains("Hello, world!"));
    }

    /**
     * contents - clean.
     */
    @Test
    public void bundle_clear() {
        BasicProject project = project("simple")
                .withContents(it -> it.copy(Paths.get("src/test/data/base-package")));
        assertThat(project.getContents().find("etc/data.txt"), is(not(Optional.empty())));

        project.getContents().clean();
        assertThat(project.getContents().find("etc/data.txt"), is(Optional.empty()));
        assertThat(project.getContents().find("etc"), is(Optional.empty()));
        assertThat(Files.isDirectory(project.getContents().getDirectory()), is(true));
    }

    /**
     * contents - clean.
     */
    @Test
    public void bundle_clean_partial() {
        BasicProject project = project("simple")
                .withContents(it -> it.copy(Paths.get("src/test/data/base-package")));
        assertThat(project.getContents().find("etc/data.txt"), is(not(Optional.empty())));

        project.getContents().clean("etc/data.txt");
        assertThat(project.getContents().find("etc/data.txt"), is(Optional.empty()));
        assertThat(project.getContents().find("etc"), is(not(Optional.empty())));
        assertThat(Files.isDirectory(project.getContents().getDirectory()), is(true));

        project.getContents().clean("MISSING"); // OK
    }

    /**
     * contents - launch on windows like environment.
     */
    @Test
    public void bundle_launch_windows() {
        BasicProject project = project("simple")
                .withContents(it -> it.copy(Paths.get("src/test/data/base-package")));
        project.withContents(it -> {
            try {
                it.withLaunch("bin/make_output");
            } catch (Exception e) {
                Assume.assumeNoException("this test is designed for windows env", e);
            }
        });
        assertThat(lines(project.getContents().get("output.txt")), contains("Hello, world!"));
    }

    /**
     * contents - launch on linux like environment.
     */
    @Test
    public void bundle_launch_linux() {
        BasicProject project = project("simple")
                .withContents(it -> it.copy(Paths.get("src/test/data/base-package")));
        project.withContents(it -> {
            try {
                it.withLaunch("bin/make_output.sh");
            } catch (Exception e) {
                Assume.assumeNoException("this test is designed for linux env", e);
            }
        });
        assertThat(lines(project.getContents().get("output.txt")), contains("Hello, world!"));
    }

    /**
     * simple gradle.
     */
    @Test
    public void gradle_simple() {
        BasicProject project = project("simple");
        project.gradle("help");
    }

    /**
     * gradle w/ file output.
     */
    @Test
    public void gradle_file() {
        BasicProject project = project("simple")
                .withContents(it -> it.copy(Paths.get("src/test/data/base-gradle")));
        project.gradle("hello");
        assertThat(lines(project.getContents().get("testing")), contains("Hello, world!"));
    }

    /**
     * gradle w/ custom version.
     */
    @Test
    public void gradle_name() {
        BasicProject project = project("simple")
                .withContents(it -> it.copy(Paths.get("src/test/data/base-gradle")));
        project.gradle("putName");
        assertThat(lines(project.getContents().get("testing")), contains("simple"));
    }

    /**
     * gradle w/ custom version.
     */
    @Test
    public void gradle_version() {
        BasicProject project = project("simple")
                .withProperty("gradle.version", "3.5.1")
                .withContents(it -> it.copy(Paths.get("src/test/data/base-gradle")));
        project.gradle("putVersion");
        assertThat(lines(project.getContents().get("testing")), contains("3.5.1"));
        project.withProperty("gradle.version", null).gradle("putVersion");
        assertThat(lines(project.getContents().get("testing")), contains(GradleVersion.current().getVersion()));
    }

    /**
     * gradle w/ custom environment variable.
     */
    @Test
    public void gradle_environment() {
        BasicProject project = project("simple")
                .withEnvironment("TESTING", "OK")
                .withContents(it -> it.copy(Paths.get("src/test/data/base-gradle")));
        project.gradle("putEnvironment");
        assertThat(lines(project.getContents().get("testing")), contains("OK"));
        project.withEnvironment("TESTING", null).gradle("putEnvironment");
        assertThat(lines(project.getContents().get("testing")), contains("N/A"));
    }

    /**
     * gradle w/ custom property.
     */
    @Test
    public void gradle_property() {
        BasicProject project = project("simple")
                .withProperty("TESTING", "OK")
                .withContents(it -> it.copy(Paths.get("src/test/data/base-gradle")));
        project.gradle("putProperty");
        assertThat(lines(project.getContents().get("testing")), contains("OK"));
        project.withProperty("TESTING", null).gradle("putProperty");
        assertThat(lines(project.getContents().get("testing")), contains("N/A"));
    }

    /**
     * gradle w/ custom property.
     */
    @Test
    public void gradle_argument() {
        BasicProject project = project("simple")
                .withContents(it -> it.copy(Paths.get("src/test/data/base-gradle")));
        project.gradle(gradle -> gradle.withArguments("-PTESTING=OK").launch("putArgument"));
        assertThat(lines(project.getContents().get("testing")), contains("OK"));
        project.gradle("putArgument");
        assertThat(lines(project.getContents().get("testing")), contains("N/A"));
    }

    /**
     * environment variables for case insensitive platform.
     */
    @Test
    public void environment_case() {
        Assume.assumeFalse("designed only for case insensitive environment variables", BaseProject.CASE_SENSITIVE);
        BasicProject project = project("simple")
                .withEnvironment("a", "OK")
                .withEnvironment("B", "OK");
        assertThat(project.environment("A"), is("OK"));
        assertThat(project.environment("b"), is("OK"));
    }

    private BasicProject project(String name) {
        Path directory;
        try {
            directory = temporary.newFolder(name).toPath();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return new BasicProject(directory)
                .with(EnvironmentConfigurator.system())
                .with(PropertyConfigurator.system());
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
