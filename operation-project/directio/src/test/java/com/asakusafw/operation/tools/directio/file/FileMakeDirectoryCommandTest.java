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
 * Test for {@link FileMakeDirectoryCommand}.
 */
public class FileMakeDirectoryCommandTest extends DirectIoToolsTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        File root = add("root", "/");
        invoke("mkdir", "testing");
        assertThat(new File(root, "testing"), is(directory()));
    }

    /**
     * w/ verbose.
     */
    @Test
    public void verbose() {
        File root = add("root", "/");
        invoke("mkdir", "testing", "-v");
        assertThat(new File(root, "testing"), is(directory()));
    }

    /**
     * nested directory.
     */
    @Test
    public void nested() {
        File root = add("root", "/");
        invoke("mkdir", "a/b/c");
        assertThat(new File(root, "a/b/c"), is(directory()));
    }

    /**
     * w/ multiple dirs.
     */
    @Test
    public void multiple() {
        File root = add("root", "/");
        invoke("mkdir", "a", "b", "c");
        assertThat(new File(root, "a"), is(directory()));
        assertThat(new File(root, "b"), is(directory()));
        assertThat(new File(root, "c"), is(directory()));
    }

    /**
     * directory existing.
     */
    @Test
    public void exist_directory() {
        File root = add("root", "/", "exist/");
        invoke("mkdir", "exist");
        assertThat(new File(root, "exist"), is(directory()));
    }

    /**
     * w/ meta-characters.
     */
    @Test(expected = RuntimeException.class)
    public void meta() {
        add("root", "/");
        invoke("mkdir", "*");
    }

    /**
     * file existing.
     */
    @Test(expected = RuntimeException.class)
    public void exist_file() {
        add("root", "/", "exist");
        invoke("mkdir", "exist");
    }

    /**
     * mkdir nothing.
     */
    @Test(expected = RuntimeException.class)
    public void nothing() {
        add("root", "/");
        invoke("mkdir");
    }
}
