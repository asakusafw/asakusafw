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
package com.asakusafw.operation.tools.directio.file;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.asakusafw.operation.tools.directio.DirectIoToolsTestRoot;

/**
 * Test for {@link FileListCommand}.
 */
public class FileListCommandTest extends DirectIoToolsTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        File root = add("root", "/", "a.txt");
        List<File> results = files(invoke("list"));
        assertThat(results, samePaths(new File(root, "a.txt")));
    }

    /**
     * show help.
     */
    @Test
    public void help() {
        invoke("list", "--help");
    }

    /**
     * multiple files.
     */
    @Test
    public void multiple_files() {
        File root = add("root", "/",
                "a.txt",
                "b.txt",
                "c.txt");
        List<File> results = files(invoke("list"));
        assertThat(results, samePaths(
                new File(root, "a.txt"),
                new File(root, "b.txt"),
                new File(root, "c.txt")));
    }

    /**
     * multiple data sources.
     */
    @Test
    public void multiple_ds() {
        File a = add("a", "a", "f.txt");
        File b = add("b", "b", "f.txt");
        File c = add("c", "c", "f.txt");
        List<File> results = files(invoke("list"));
        assertThat(results, samePaths(
                new File(a, "f.txt"),
                new File(b, "f.txt"),
                new File(c, "f.txt")));
    }

    /**
     * w/ verbose.
     */
    @Test
    public void verbose() {
        File root = add("root", "/",
                "a.txt",
                "b/c.txt",
                "d/");
        List<String> output = invoke("list", "-v");
        assertThat(output.get(0), is(equalToIgnoringWhiteSpace("total 4")));
        List<File> results = files(output.stream()
                .filter(s -> s.startsWith("file:"))
                .collect(Collectors.toList()));
        assertThat(results, samePaths(
                new File(root, "a.txt"),
                new File(root, "b"),
                new File(root, "b/c.txt"),
                new File(root, "d")));
    }

    /**
     * w/ path.
     */
    @Test
    public void path() {
        File root = add("root", "/",
                "a.txt",
                "b.txt",
                "c.txt");
        List<File> results = files(invoke("list", "b.txt"));
        assertThat(results, samePaths(new File(root, "b.txt")));
    }

    /**
     * w/ asterisk.
     */
    @Test
    public void asterisk() {
        File root = add("root", "/",
                "a.txt",
                "b.csv",
                "c/d.txt");
        List<File> results = files(invoke("list", "*.txt"));
        assertThat(results, samePaths(new File(root, "a.txt")));
    }

    /**
     * w/ traverse.
     */
    @Test
    public void traverse() {
        File root = add("root", "/",
                "a.txt",
                "b.csv",
                "c/d.txt");
        List<File> results = files(invoke("list", "**/*.txt"));
        assertThat(results, samePaths(
                new File(root, "a.txt"),
                new File(root, "c/d.txt")));
    }

    /**
     * w/ component root.
     */
    @Test
    public void root() {
        File a = add("a", "a", "f.txt");
        File b = add("b", "b", "f.txt");
        File c = add("c", "c", "f.txt");
        List<File> results = files(invoke("list", "a", "b", "c"));
        assertThat(results, samePaths(a, b, c));
    }

    /**
     * base path longest match.
     */
    @Test
    public void longer_base_path() {
        add("root", "/", "other/f.txt");
        File base = add("other", "other", "f.txt");
        List<File> results = files(invoke("list", "other/f.txt"));
        assertThat(results, samePaths(new File(base, "f.txt")));
    }

    /**
     * w/ custom data source.
     */
    @Test
    public void custom_ds() {
        File a = add("root", "/", "other/f.txt");
        add("other", "other", "f.txt");
        List<File> results = files(invoke("list", "other/f.txt", "--data-source", "root"));
        assertThat(results, samePaths(new File(a, "other/f.txt")));
    }

    /**
     * multiple data sources.
     */
    @Test
    public void multiple_ds_all() {
        add("a", "a", "f.txt");
        File b = add("b", "b", "f.txt");
        add("c", "c", "f.txt");
        List<File> results = files(invoke("list", "--data-source", "b"));
        assertThat(results, samePaths(new File(b, "f.txt")));
    }

    /**
     * w/o any data sources.
     */
    @Test(expected = RuntimeException.class)
    public void no_datasource() {
        invoke("list");
    }

    /**
     * w/o any data sources.
     */
    @Test(expected = RuntimeException.class)
    public void miss_datasource() {
        invoke("list", "*");
    }

    /**
     * missing custom data source.
     */
    @Test(expected = RuntimeException.class)
    public void miss_custom_datasource() {
        add("root", "/", "other/f.txt");
        invoke("list", "other/f.txt", "--data-source", "MISSING");
    }

    private static List<File> files(List<String> uris) {
        return uris.stream()
                .map(URI::create)
                .map(File::new)
                .collect(Collectors.toList());
    }
}
