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
 * Test for {@link FilePutCommand}.
 */
public class FilePutCommandTest extends DirectIoToolsTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        File current = openWorkingDir();
        File root = add("root", "/");

        touch(current, "a.txt");
        invoke("put", "a.txt", "/");

        assertThat(new File(root, "a.txt"), is(file()));
    }

    /**
     * show help.
     */
    @Test
    public void help() {
        invoke("put", "--help");
    }

    /**
     * w/ verbose.
     */
    @Test
    public void verbose() {
        File current = openWorkingDir();
        File root = add("root", "/");

        touch(current, "a.txt");
        invoke("put", "a.txt", "/", "-v");

        assertThat(new File(root, "a.txt"), is(file()));
    }

    /**
     * multiple files.
     */
    @Test
    public void multiple() {
        File current = openWorkingDir();
        File root = add("root", "/");

        touch(current, "a.txt");
        touch(current, "b.txt");
        touch(current, "d/c.txt");
        invoke("put", "a.txt", "b.txt", "d/c.txt", "/");

        assertThat(new File(root, "a.txt"), is(file()));
        assertThat(new File(root, "b.txt"), is(file()));
        assertThat(new File(root, "c.txt"), is(file()));
    }

    /**
     * skip directory.
     */
    @Test
    public void skip_directory() {
        File current = openWorkingDir();
        File root = add("root", "/");

        touch(current, "d/a.txt");
        invoke("put", "d", "/");

        assertThat(new File(root, "d"), not(exists()));
    }

    /**
     * copy directory.
     */
    @Test
    public void copy_directory() {
        File current = openWorkingDir();
        File root = add("root", "/");

        touch(current, "d/a.txt");
        invoke("put", "d", "/", "--recursive");

        assertThat(new File(root, "d/a.txt"), is(file()));
    }

    /**
     * w/ single target.
     */
    @Test
    public void single_target() {
        File current = openWorkingDir();
        File root = add("root", "/");

        touch(current, "a.txt");
        invoke("put", "a.txt", "f.txt");

        assertThat(new File(root, "a.txt"), not(exists()));
        assertThat(new File(root, "f.txt"), is(file()));
    }

    /**
     * simple case.
     */
    @Test
    public void parallel() {
        File current = openWorkingDir();
        File root = add("root", "/");
        touch(current, "a.txt");
        touch(current, "a/b.txt");
        touch(current, "a/c.txt");
        touch(current, "d/e.txt");
        touch(current, "d/f.txt");

        invoke("put", "a.txt", "a", "d", "/", "--recursive", "-p");

        assertThat(new File(root, "a.txt"), is(file()));
        assertThat(new File(root, "a/b.txt"), is(file()));
        assertThat(new File(root, "a/c.txt"), is(file()));
        assertThat(new File(root, "d/e.txt"), is(file()));
        assertThat(new File(root, "d/f.txt"), is(file()));
    }

    /**
     * cannot overwrite existing files.
     */
    @Test(expected = RuntimeException.class)
    public void exist_file() {
        File current = openWorkingDir();
        add("root", "/", "a.txt");

        touch(current, "a.txt");
        invoke("put", "a.txt", "/");
    }

    /**
     * overwrite existing files.
     */
    @Test
    public void overwrite_file() {
        File current = openWorkingDir();
        File root = add("root", "/");

        File file = write(touch(root, "a.txt"), "OLD");
        write(touch(current, "a.txt"), "NEW");

        invoke("put", "a.txt", "/", "--overwrite");

        assertThat(file, is(file()));
        assertThat(read(file), is("NEW"));
    }

    /**
     * cannot overwrite existing directory.
     */
    @Test(expected = RuntimeException.class)
    public void exist_directory() {
        File current = openWorkingDir();
        add("root", "/", "a/");

        touch(current, "a/");
        invoke("put", "a", "/", "--recursive");
    }

    /**
     * merge directory.
     */
    @Test
    public void merge_directory() {
        File current = openWorkingDir();
        File root = add("root", "/", "d/a.txt");
        touch(current, "d/b.txt");

        write(touch(root, "d/c.txt"), "OLD");
        write(touch(current, "d/c.txt"), "NEW");

        invoke("put", "d", "/", "--recursive", "--overwrite");

        assertThat(new File(root, "d/a.txt"), is(file()));
        assertThat(new File(root, "d/b.txt"), is(file()));
        assertThat(new File(root, "d/c.txt"), is(file()));
        assertThat(read(new File(root, "d/c.txt")), is("NEW"));
    }

    /**
     * overwrite existing directory by file.
     */
    @Test(expected = RuntimeException.class)
    public void overwrite_file_by_directory() {
        File current = openWorkingDir();
        add("root", "/", "f");
        touch(current, "f/");

        invoke("put", "f", "/", "--recursive", "--overwrite");
    }

    /**
     * overwrite existing directory by file.
     */
    @Test(expected = RuntimeException.class)
    public void overwrite_directory_by_file() {
        File current = openWorkingDir();
        add("root", "/", "d/");
        touch(current, "d");

        invoke("put", "d", "/", "--overwrite");
    }

    /**
     * w/ single target.
     */
    @Test(expected = RuntimeException.class)
    public void single_target_ambiguous() {
        File current = openWorkingDir();
        add("root", "/");

        touch(current, "a.txt");
        touch(current, "b.txt");
        invoke("put", "a.txt", "b.txt", "f.txt");
    }

    /**
     * w/ single target.
     */
    @Test(expected = RuntimeException.class)
    public void single_conflict() {
        File current = openWorkingDir();
        File root = add("root", "/");

        touch(current, "a.txt");
        touch(root, "f.txt");

        invoke("put", "a.txt", "f.txt");
    }

    /**
     * w/ single target.
     */
    @Test
    public void single_overwrite_file() {
        File current = openWorkingDir();
        File root = add("root", "/");

        write(touch(current, "a.txt"), "NEW");
        File file = write(touch(root, "f.txt"), "OLD");

        invoke("put", "a.txt", "f.txt", "--overwrite");
        assertThat(file, is(file()));
        assertThat(read(file), is("NEW"));
    }

    /**
     * w/ missing source.
     */
    @Test(expected = RuntimeException.class)
    public void missing_source() {
        File current = openWorkingDir();
        add("root", "/");

        touch(current, "a.txt");
        invoke("put", "MISSING", ".");
    }

    /**
     * w/ missing target.
     */
    @Test(expected = RuntimeException.class)
    public void missing_target() {
        File current = openWorkingDir();
        add("root", "/");

        touch(current, "a.txt");
        invoke("put", "a.txt", "missing/destination");
    }

    /**
     * w/ invalid target.
     */
    @Test(expected = RuntimeException.class)
    public void invalid_target() {
        File current = openWorkingDir();
        add("root", "/");

        touch(current, "a.txt");
        invoke("put", "a.txt", "*");
    }

    /**
     * conflict destination.
     */
    @Test(expected = RuntimeException.class)
    public void conflict() {
        File current = openWorkingDir();
        add("root", "/");

        touch(current, "a.txt");
        touch(current, "d/a.txt");
        invoke("put", "a.txt", "d/a.txt", ".");
    }

    /**
     * w/ invalid command args.
     */
    @Test(expected = RuntimeException.class)
    public void invalid_arguments() {
        invoke("put");
    }
}
