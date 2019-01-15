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
package com.asakusafw.runtime.util.hadoop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link ConfigurationDetecter}.
 */
public class ConfigurationDetecterTest {

    /**
     * Temporary folder for testing.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private ClassLoader testLoader;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        this.testLoader = AccessController.doPrivileged((PrivilegedExceptionAction<ClassLoader>) () ->
                new URLClassLoader(new URL[] { folder.getRoot().toURI().toURL() }));
    }

    /**
     * Test method for simple execution.
     * @throws Exception if failed
     */
    @Test
    public void execute() throws Exception {
        File marker = folder.newFile(ConfigurationDetecter.MARKER_FILE_NAME);
        File output = folder.newFile();
        Assume.assumeTrue(output.delete() || output.exists() == false);

        assertThat(ConfigurationDetecter.execute(output, testLoader), is(0));

        File result = ConfigurationDetecter.read(output);
        assertThat(result.getCanonicalPath(), is(marker.getParentFile().getCanonicalPath()));
    }

    /**
     * Test method for missing conf.
     * @throws Exception if failed
     */
    @Test
    public void execute_no_conf() throws Exception {
        File output = folder.newFile();
        Assume.assumeTrue(output.delete() || output.exists() == false);
        assertThat(ConfigurationDetecter.execute(output, testLoader), is(not(0)));
    }
}
