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
package com.asakusafw.workflow.hadoop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.windows.WindowsSupport;

/**
 * Test for {@link HadoopDelete}.
 */
public class HadoopDeleteTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport(true);

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File file = temporary.newFile();
        int status = HadoopDelete.exec(file.toURI().toString());
        assertThat(status, is(0));
        assertThat(file.exists(), is(false));
    }

    /**
     * delete recursively.
     * @throws Exception if failed
     */
    @Test
    public void recursive() throws Exception {
        File dir = temporary.newFolder();
        File a = new File(dir, "a");
        File b = new File(dir, "b");
        File c = new File(dir, "c");
        a.createNewFile();
        b.createNewFile();
        c.createNewFile();
        int status = HadoopDelete.exec(dir.toURI().toString());
        assertThat(status, is(0));
        assertThat(dir.exists(), is(false));
    }

    /**
     * missing target file.
     * @throws Exception if failed
     */
    @Test
    public void missing() throws Exception {
        File file = new File(temporary.getRoot(), "__MISSING__");
        int status = HadoopDelete.exec(file.toURI().toString());
        assertThat("exit with 0 even if target is missing", status, is(0));
    }

    /**
     * multiple files.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        File a = temporary.newFile();
        File b = temporary.newFile();
        File c = temporary.newFile();
        int status = HadoopDelete.exec(a.toURI().toString(), b.toURI().toString(), c.toURI().toString());
        assertThat(status, is(0));
        assertThat(a.exists(), is(false));
        assertThat(b.exists(), is(false));
        assertThat(c.exists(), is(false));
    }
}
