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
package com.asakusafw.operation.tools.directio.file;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.asakusafw.operation.tools.directio.DirectIoToolsTestRoot;

/**
 * Test for {@link FileGetCommand}.
 */
public class FileGetCommandTest extends DirectIoToolsTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        File current = openWorkingDir();

        add("root", "/", "a.txt");
        invoke("get", "a.txt", ".");

        assertThat(new File(current, "a.txt"), is(file()));
    }

    /**
     * w/ verbose.
     */
    @Test
    public void verbose() {
        File current = openWorkingDir();

        add("root", "/", "a.txt");
        invoke("get", "a.txt", ".", "-v");

        assertThat(new File(current, "a.txt"), is(file()));
    }

    /**
     * w/ asterisk.
     */
    @Test
    public void asterisk() {
        File current = openWorkingDir();

        add("root", "/", "a.txt", "b.txt", "c.csv");
        invoke("get", "*.txt", ".");

        assertThat(new File(current, "a.txt"), is(file()));
        assertThat(new File(current, "b.txt"), is(file()));
        assertThat(new File(current, "c.csv"), not(exists()));
    }

    /**
     * w/ traverse.
     */
    @Test
    public void traverse() {
        File current = openWorkingDir();

        add("root", "/", "a.txt", "d/b.txt", "c.csv");
        invoke("get", "**/*.txt", ".");

        assertThat(new File(current, "a.txt"), is(file()));
        assertThat(new File(current, "b.txt"), is(file()));
        assertThat(new File(current, "d/b.txt"), not(exists()));
        assertThat(new File(current, "c.csv"), not(exists()));
    }

    /**
     * skip directories.
     */
    @Test
    public void skip_directory() {
        File current = openWorkingDir();

        add("root", "/", "a.txt", "b/f.txt", "c.csv");
        invoke("get", "*", ".");

        assertThat(new File(current, "a.txt"), is(file()));
        assertThat(new File(current, "b"), not(exists()));
        assertThat(new File(current, "c.csv"), is(file()));
    }

    /**
     * copy directories.
     */
    @Test
    public void copy_directory() {
        File current = openWorkingDir();

        add("root", "/", "a.txt", "b/f.txt", "c.csv");
        invoke("get", "*", ".", "--recursive");

        assertThat(new File(current, "a.txt"), is(file()));
        assertThat(new File(current, "b/f.txt"), is(file()));
        assertThat(new File(current, "c.csv"), is(file()));
    }

    /**
     * w/ parallel.
     */
    @Test
    public void parallel() {
        File current = openWorkingDir();
        add("root", "/",
                "f0.txt",
                "a/f0.txt",
                "a/f1.txt",
                "a/f2.txt",
                "b/c/f0.txt",
                "b/d/f1.txt",
                "c/d/e/f0.txt");
        invoke("get", "*", ".", "-r", "-p");
        assertThat(current, exists("f0.txt"));
        assertThat(current, exists("a/f0.txt"));
        assertThat(current, exists("a/f0.txt"));
        assertThat(current, exists("a/f0.txt"));
        assertThat(current, exists("b/c/f0.txt"));
        assertThat(current, exists("b/d/f1.txt"));
        assertThat(current, exists("c/d/e/f0.txt"));
    }

    /**
     * copy to single target.
     */
    @Test
    public void single_target() {
        File current = openWorkingDir();

        add("root", "/", "a.txt");
        invoke("get", "a.txt", "f.txt");

        assertThat(new File(current, "f.txt"), is(file()));
        assertThat(new File(current, "a.txt"), not(exists()));
    }

    /**
     * copy to single target.
     */
    @Test(expected = RuntimeException.class)
    public void single_target_ambiguous() {
        openWorkingDir();
        add("root", "/", "a.txt", "b.txt");
        invoke("get", "a.txt", "b.txt", "f.txt");
    }

    /**
     * copy to single existing target.
     */
    @Test(expected = RuntimeException.class)
    public void single_target_existing() {
        File current = openWorkingDir();

        add("root", "/", "a.txt");
        touch(current, "existing.txt");
        invoke("get", "a.txt", "existing.txt");
    }

    /**
     * copy to single existing target.
     */
    @Test
    public void single_target_overwrite() {
        File current = openWorkingDir();

        File base = add("root", "/");
        write(touch(base, "a.txt"), "NEW");

        File file = write(touch(current, "existing.txt"), "OLD");

        invoke("get", "a.txt", "existing.txt", "--overwrite");
        assertThat(file, is(file()));
        assertThat(read(file), is("NEW"));
    }

    /**
     * missing target file.
     */
    @Test(expected = RuntimeException.class)
    public void missing_file() {
        openWorkingDir();
        add("root", "/");
        invoke("get", "MISSING", ".");
    }

    /**
     * file already exists.
     */
    @Test(expected = RuntimeException.class)
    public void existing_file() {
        File current = openWorkingDir();

        add("root", "/", "a.txt");
        touch(current, "a.txt");

        invoke("get", "a.txt", ".");
    }

    /**
     * file already exists.
     */
    @Test(expected = RuntimeException.class)
    public void existing_directory() {
        File current = openWorkingDir();

        add("root", "/", "a/");
        touch(current, "a/");

        invoke("get", "a", ".", "--recursive");
    }

    /**
     * overwrite existing file.
     */
    @Test
    public void overwrite_file() {
        File current = openWorkingDir();

        File base = add("root", "/");
        write(touch(base, "a.txt"), "NEW");
        File dst = write(touch(current, "a.txt"), "OLD");

        invoke("get", "a.txt", ".", "--overwrite");
        assertThat(dst, is(file()));
        assertThat(read(dst), is("NEW"));
    }

    /**
     * merge directory contents.
     */
    @Test
    public void merge_directory() {
        File current = openWorkingDir();

        File base = add("root", "/");
        touch(base, "a/b.txt");
        touch(current, "a/c.txt");

        invoke("get", "a", ".", "--recursive", "--overwrite");
        assertThat(current, exists("a/b.txt"));
        assertThat(current, exists("a/c.txt"));
    }

    /**
     * overwrite existing directory by file.
     */
    @Test(expected = RuntimeException.class)
    public void overwrite_directory_by_file() {
        File current = openWorkingDir();
        File base = add("root", "/");
        touch(base, "a");
        touch(current, "a/");
        invoke("get", "a", ".", "--overwrite");
    }

    /**
     * overwrite existing file by directory.
     */
    @Test(expected = RuntimeException.class)
    public void overwrite_file_by_directory() {
        File current = openWorkingDir();
        File base = add("root", "/");
        touch(base, "a/");
        touch(current, "a");
        invoke("get", "a", ".", "--recursive", "--overwrite");
    }

    /**
     * copy root.
     */
    @Test(expected = RuntimeException.class)
    public void copy_root() {
        openWorkingDir();

        add("root", "/", "a.txt");
        invoke("get", "/", ".");
    }

    /**
     * w/ conflict files.
     */
    @Test(expected = RuntimeException.class)
    public void copy_conflict() {
        openWorkingDir();

        add("root", "/", "a.txt", "d/a.txt");
        invoke("get", "**/a.txt", ".");
    }

    /**
     * w/ missing target.
     */
    @Test(expected = RuntimeException.class)
    public void invalid_destination() {
        openWorkingDir();

        add("root", "/", "a.txt");
        invoke("get", "a.txt", "missing/target");
    }

    /**
     * missing args.
     */
    @Test(expected = RuntimeException.class)
    public void missing_args() {
        openWorkingDir();
        invoke("get");
    }

    /**
     * relative destination w/o CWD hints.
     */
    @Test(expected = RuntimeException.class)
    public void unresolved_destination() {
        add("root", "/", "a.txt");
        invoke("get", "a.txt", ".");
    }
}
