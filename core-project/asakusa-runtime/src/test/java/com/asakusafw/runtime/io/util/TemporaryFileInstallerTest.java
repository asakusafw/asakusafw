/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link TemporaryFileInstaller}.
 */
public class TemporaryFileInstallerTest {

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
        byte[] contents = { 1, 2, 3 };
        File f = temporary.newFile();

        TemporaryFileInstaller installer = create(false, contents);
        assertThat(installer.install(f, true), is(true));
        check(f, contents);
    }

    /**
     * reuse file.
     * @throws Exception if failed
     */
    @Test
    public void reuse() throws Exception {
        byte[] contents = { 1, 2, 3 };
        File f = temporary.newFile();

        TemporaryFileInstaller installer = create(false, contents);
        assertThat(installer.install(f, true), is(true));
        assertThat(installer.install(f, true), is(false));
        check(f, contents);
    }

    /**
     * don't reuse different files.
     * @throws Exception if failed
     */
    @Test
    public void reuse_update() throws Exception {
        byte[] c0 = { 1, 2, 3 };
        byte[] c1 = { 1, 2, 4 };
        File f = temporary.newFile();

        TemporaryFileInstaller i0 = create(false, c0);
        TemporaryFileInstaller i1 = create(false, c1);
        assertThat(i0.install(f, true), is(true));
        assertThat(i0.install(f, true), is(false));
        check(f, c0);
        assertThat(i1.install(f, true), is(true));
        assertThat(i1.install(f, true), is(false));
        check(f, c1);
    }

    /**
     * denies reusing files.
     * @throws Exception if failed
     */
    @Test
    public void reuse_deny() throws Exception {
        byte[] contents = { 1, 2, 3 };
        File f = temporary.newFile();

        TemporaryFileInstaller installer = create(false, contents);
        assertThat(installer.install(f, false), is(true));
        assertThat(installer.install(f, false), is(true));
        check(f, contents);
    }

    private TemporaryFileInstaller create(boolean exec, byte... bytes) throws IOException {
        return TemporaryFileInstaller.newInstance(new ByteArrayInputStream(bytes), exec);
    }

    private void check(File f, byte[] contents) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (InputStream in = new FileInputStream(f)) {
            byte[] b = new byte[256];
            while (true) {
                int read = in.read(b);
                if (read < 0) {
                    break;
                }
                buf.write(b, 0, read);
            }
        }
        assertThat(buf.toByteArray(), equalTo(contents));
    }
}
