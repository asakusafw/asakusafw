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
package com.asakusafw.runtime.windows;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link WinUtilsInstaller}.
 */
public class WinUtilsInstallerTest {

    /**
     * temporary folder for testing.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File folder = temporary.newFolder();
        File file = WinUtilsInstaller.put(folder);
        assertThat(file.isFile(), is(true));
        assertThat(file.getName(), endsWith(".exe"));
        assertThat(file.canExecute(), is(true));
        assertThat(file.getCanonicalFile().getParentFile(), is(folder.getCanonicalFile()));
    }

    /**
     * case if we cannot install winutils into the default location.
     * @throws Exception if failed
     */
    @Test
    public void escape() throws Exception {
        File folder = temporary.newFolder();
        File d = WinUtilsInstaller.put(folder);
        Assume.assumeTrue(d.delete());
        Assume.assumeTrue(d.mkdirs() || d.isDirectory());

        File file = WinUtilsInstaller.put(folder);
        assertThat(file.isFile(), is(true));
        assertThat(file.getName(), endsWith(".exe"));
        assertThat(file.canExecute(), is(true));
        assertThat(file.getCanonicalFile(), is(not(d.getCanonicalFile())));
    }

    /**
     * case if target directory is not writable.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void fail() throws Exception {
        File folder = temporary.newFolder();
        folder.setWritable(false);
        Assume.assumeFalse(folder.canWrite());
        try {
            WinUtilsInstaller.put(folder);
        } finally {
            folder.setWritable(true);
        }
    }
}
