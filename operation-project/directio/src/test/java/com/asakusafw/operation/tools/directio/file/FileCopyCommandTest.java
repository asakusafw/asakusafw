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

import org.junit.Test;

import com.asakusafw.operation.tools.directio.DirectIoToolsTestRoot;

/**
 * Test for {@link FileCopyCommand}.
 */
public class FileCopyCommandTest extends DirectIoToolsTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        File root = add("root", "/");

        write(touch(root, "a.txt"), "OK");
        invoke("copy", "a.txt", "b.txt");

        assertThat(new File(root, "a.txt"), is(file()));
        assertThat(new File(root, "b.txt"), is(file()));
        assertThat(read(new File(root, "b.txt")), is("OK"));
    }

    /**
     * show help.
     */
    @Test
    public void help() {
        invoke("copy", "--help");
    }

    /**
     * w/ verbose.
     */
    @Test
    public void verbose() {
        File root = add("root", "/");

        write(touch(root, "a.txt"), "OK");
        invoke("copy", "a.txt", "b.txt", "-v");

        assertThat(new File(root, "a.txt"), is(file()));
        assertThat(new File(root, "b.txt"), is(file()));
        assertThat(read(new File(root, "b.txt")), is("OK"));
    }

    /**
     * to directory.
     */
    @Test
    public void to_directory() {
        File root = add("root", "/");

        write(touch(root, "a.txt"), "OK");
        File dst = touch(root, "dir/");
        invoke("copy", "a.txt", "dir");

        assertThat(new File(root, "a.txt"), is(file()));
        assertThat(new File(dst, "a.txt"), is(file()));
        assertThat(read(new File(dst, "a.txt")), is("OK"));
    }

    /**
     * multiple files.
     */
    @Test
    public void multiple() {
        File root = add("root", "/");

        write(touch(root, "a.txt"), "A");
        write(touch(root, "b.txt"), "B");
        write(touch(root, "d/c.txt"), "C");
        File dst = touch(root, "dir/");
        invoke("copy", "a.txt", "b.txt", "d/c.txt", "dir");

        assertThat(new File(root, "a.txt"), is(file()));
        assertThat(new File(root, "b.txt"), is(file()));
        assertThat(new File(root, "d/c.txt"), is(file()));
        assertThat(new File(dst, "a.txt"), is(file()));
        assertThat(new File(dst, "b.txt"), is(file()));
        assertThat(new File(dst, "c.txt"), is(file()));
        assertThat(read(new File(dst, "a.txt")), is("A"));
        assertThat(read(new File(dst, "b.txt")), is("B"));
        assertThat(read(new File(dst, "c.txt")), is("C"));
    }

    /**
     * skip directory.
     */
    @Test
    public void skip_directory() {
        File root = add("root", "/");

        write(touch(root, "d/a.txt"), "OK");
        File dst = touch(root, "dir/");

        invoke("copy", "d", "dir");

        assertThat(new File(root, "d/a.txt"), is(exists()));
        assertThat(new File(dst, "d/a.txt"), is(not(exists())));
    }

    /**
     * copy directory.
     */
    @Test
    public void copy_directory() {
        File root = add("root", "/");

        write(touch(root, "d/a.txt"), "OK");
        File dst = touch(root, "dir/");

        invoke("copy", "d", "dir", "--recursive");

        assertThat(new File(root, "d/a.txt"), is(exists()));
        assertThat(new File(dst, "d/a.txt"), is(file()));
    }

    /**
     * w/ overwrite.
     */
    @Test
    public void overwrite() {
        File root = add("root", "/");

        write(touch(root, "a.txt"), "OK");
        write(touch(root, "b.txt"), "OLD");
        invoke("copy", "a.txt", "b.txt", "--overwrite");

        assertThat(new File(root, "a.txt"), is(file()));
        assertThat(new File(root, "b.txt"), is(file()));
        assertThat(read(new File(root, "b.txt")), is("OK"));
    }

    /**
     * empty source.
     */
    @Test(expected = RuntimeException.class)
    public void empty_source() {
        add("root", "/");
        invoke("copy", "*.txt", "b.txt");
    }

    /**
     * destination file already exists.
     */
    @Test(expected = RuntimeException.class)
    public void file_exists() {
        File root = add("root", "/");

        touch(root, "a.txt");
        touch(root, "b.txt");
        invoke("copy", "a.txt", "b.txt");
    }

    /**
     * multiple files into single target.
     */
    @Test(expected = RuntimeException.class)
    public void ambiguous_source() {
        File root = add("root", "/");

        touch(root, "a.txt");
        touch(root, "b.txt");
        invoke("copy", "a.txt", "b.txt", "c.txt");
    }

    /**
     * conflict source.
     */
    @Test(expected = RuntimeException.class)
    public void conflict_source() {
        File root = add("root", "/");

        touch(root, "d1/a.txt");
        touch(root, "d2/a.txt");
        invoke("copy", "d1/*", "d2/*", "/");
    }

    /**
     * conflict source.
     */
    @Test(expected = RuntimeException.class)
    public void conflict_destination() {
        File root = add("root", "/");

        touch(root, "d1/a.txt");
        touch(root, "d2/a.txt");
        invoke("copy", "d1/*", "d2");
    }

    /**
     * conflict source.
     */
    @Test(expected = RuntimeException.class)
    public void copy_root() {
        File root = add("root", "/");

        touch(root, "a.txt");
        touch(root, "dir");
        invoke("copy", "/", "dir", "--recursive");
    }

    /**
     * parent directory of destination does not exist.
     */
    @Test(expected = RuntimeException.class)
    public void missing_destination() {
        File root = add("root", "/");

        touch(root, "a.txt");
        invoke("copy", "a.txt", "missing/parent.txt");
    }

    /**
     * conflict source.
     */
    @Test(expected = RuntimeException.class)
    public void conflict_file_directory() {
        File root = add("root", "/");

        touch(root, "a.txt");
        touch(root, "dir/a.txt/");
        invoke("copy", "a.txt", "dir", "--overwrite");
    }

    /**
     * conflict source.
     */
    @Test(expected = RuntimeException.class)
    public void conflict_directory_file() {
        File root = add("root", "/");

        touch(root, "d/a.txt");
        touch(root, "dir/d");
        invoke("copy", "d", "dir", "--overwrite", "--recursive");
    }

    /**
     * conflict source.
     */
    @Test(expected = RuntimeException.class)
    public void same_target() {
        File root = add("root", "/");

        touch(root, "d/a.txt");
        invoke("copy", "d", "d", "--overwrite");
    }

    /**
     * conflict source.
     */
    @Test(expected = RuntimeException.class)
    public void nested_target() {
        File root = add("root", "/");

        touch(root, "d/a.txt");
        touch(root, "d/d/");
        invoke("copy", "d", "d/d", "--overwrite");
    }
}
