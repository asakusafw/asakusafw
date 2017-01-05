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
package com.asakusafw.yaess.basic;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link BasicExtension}.
 */
public class BasicExtensionTest {

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
        File f = temporary.newFile().getCanonicalFile();
        try (BasicExtension extension = new BasicExtension("testing", f, false)) {
            assertThat(extension.getName(), is("testing"));
            assertThat(extension.getData().getFile().getCanonicalFile(), is(f));
        }
        assertThat(f.exists(), is(true));
    }

    /**
     * delete on close.
     * @throws Exception if failed
     */
    @Test
    public void delete_on_close() throws Exception {
        File f = temporary.newFile().getCanonicalFile();
        try (BasicExtension extension = new BasicExtension("testing", f, true)) {
            assertThat(extension.getName(), is("testing"));
            assertThat(extension.getData().getFile().getCanonicalFile(), is(f));
        }
        assertThat(f.exists(), is(false));
    }
}
