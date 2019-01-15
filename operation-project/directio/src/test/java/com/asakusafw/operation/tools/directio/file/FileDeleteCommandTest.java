/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import org.junit.Test;

import com.asakusafw.operation.tools.directio.DirectIoToolsTestRoot;

/**
 * Test for {@link FileDeleteCommand}.
 */
public class FileDeleteCommandTest extends DirectIoToolsTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        File root = add("root", "/", "a.txt");
        invoke("delete", "a.txt");
        assertThat(root, not(exists("a.txt")));
    }

    /**
     * w/ verbose.
     */
    @Test
    public void verbose() {
        File root = add("root", "/", "a.txt");
        invoke("delete", "a.txt", "-v");
        assertThat(root, not(exists("a.txt")));
    }

    /**
     * w/ asterisk.
     */
    @Test
    public void asterisk() {
        File root = add("root", "/",
                "a.txt",
                "b.txt",
                "c.csv");
        invoke("delete", "*.txt");
        assertThat(root, not(exists("a.txt")));
        assertThat(root, not(exists("b.txt")));
        assertThat(root, exists("c.csv"));
    }

    /**
     * skip directory.
     */
    @Test
    public void skip_directory() {
        File root = add("root", "/", "a/b.txt");
        invoke("delete", "a");
        assertThat(root, exists("a/b.txt"));
    }

    /**
     * w/ recursive.
     */
    @Test
    public void recursive() {
        File root = add("root", "/", "a/b.txt");
        invoke("delete", "a", "--recursive");
        assertThat(root, not(exists("a/b.txt")));
    }

    /**
     * delete nothing.
     */
    @Test(expected = RuntimeException.class)
    public void nothing() {
        add("root", "/");
        invoke("delete");
    }

    /**
     * error if the path does not include any files.
     */
    @Test(expected = RuntimeException.class)
    public void error_missing() {
        add("root", "/", "a.txt");
        invoke("delete", "b.txt");
    }

    /**
     * delete component root.
     */
    @Test(expected = RuntimeException.class)
    public void error_component_root() {
        add("root", "/", "a.txt");
        invoke("delete", "/");
    }

    /**
     * w/ parallel.
     */
    @Test
    public void parallel() {
        File root = add("root", "/",
                "f0.txt",
                "a/f0.txt",
                "a/f1.txt",
                "a/f2.txt",
                "b/c/f0.txt",
                "b/c/f1.txt",
                "c/d/e/f0.txt");
        invoke("delete", "*", "-r", "-p");
        assertThat(root, not(exists("f0.txt")));
        assertThat(root, not(exists("a")));
        assertThat(root, not(exists("b")));
        assertThat(root, not(exists("c")));
    }
}
