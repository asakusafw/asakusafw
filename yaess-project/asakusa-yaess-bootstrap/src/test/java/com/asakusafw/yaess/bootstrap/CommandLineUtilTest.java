/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.yaess.bootstrap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link CommandLineUtil}.
 */
public class CommandLineUtilTest {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Loads properties from local path.
     * @throws Exception if failed
     */
    @Test
    public void loadProperties_local() throws Exception {
        Properties p = new Properties();
        p.setProperty("hello", "world!");
        File file = store(p);
        Properties loaded = CommandLineUtil.loadProperties(file);
        assertThat(loaded, is(p));
    }

    /**
     * Parses a file list.
     * @throws Exception if failed
     */
    @Test
    public void parseFileList() throws Exception {
        File a = folder.newFile("a.properties").getCanonicalFile();
        File b = folder.newFile("c.properties").getCanonicalFile();
        File c = folder.newFile("b.properties").getCanonicalFile();

        StringBuilder buf = new StringBuilder();
        buf.append(a);
        buf.append(File.pathSeparatorChar);
        buf.append(b);
        buf.append(File.pathSeparatorChar);
        buf.append(c);
        List<File> result = canonicalize(CommandLineUtil.parseFileList(buf.toString()));
        assertThat(result, is(Arrays.asList(a, b, c)));
    }

    /**
     * Parses null as a file list.
     */
    @Test
    public void parseFileList_null() {
        List<File> result = canonicalize(CommandLineUtil.parseFileList(null));
        assertThat(result, is(Arrays.<File>asList()));
    }

    /**
     * Parses empty file list.
     */
    @Test
    public void parseFileList_empty() {
        List<File> result = canonicalize(CommandLineUtil.parseFileList(""));
        assertThat(result, is(Arrays.<File>asList()));
    }

    private List<File> canonicalize(List<File> list) {
        List<File> results = new ArrayList<>();
        for (File f : list) {
            try {
                results.add(f.getCanonicalFile());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        return results;
    }

    /**
     * Test method for {@link CommandLineUtil#buildPluginLoader(java.lang.ClassLoader, java.util.List)}.
     * @throws Exception if failed
     */
    @Test
    public void buildPluginLoader() throws Exception {
        File cp1 = folder.newFolder("cp1");
        File cp2 = folder.newFolder("cp2");
        new File(cp1, "a").createNewFile();
        new File(cp2, "b").createNewFile();
        ClassLoader cl = CommandLineUtil.buildPluginLoader(getClass().getClassLoader(), Arrays.asList(cp1, cp2));
        assertThat(cl.getResource("a"), is(not(nullValue())));
        assertThat(cl.getResource("b"), is(not(nullValue())));
        assertThat(cl.getResource("c"), is(nullValue()));
    }

    /**
     * Attempts to build plugin loader with missing path.
     * @throws Exception if failed
     */
    @Test
    public void buildPluginLoader_missing_path() throws Exception {
        File cp1 = folder.newFolder("cp1");
        File cp2 = folder.newFolder("cp2");
        new File(cp1, "a").createNewFile();
        Assume.assumeTrue(cp2.delete());
        ClassLoader cl = CommandLineUtil.buildPluginLoader(getClass().getClassLoader(), Arrays.asList(cp1, cp2));
        assertThat(cl.getResource("a"), is(not(nullValue())));
        assertThat(cl.getResource("b"), is(nullValue()));
        assertThat(cl.getResource("c"), is(nullValue()));
    }

    private File store(Properties p) throws IOException, FileNotFoundException {
        File file = folder.newFile("testing.properties");
        try (FileOutputStream out = new FileOutputStream(file)) {
            p.store(out, "testing");
        }
        return file;
    }
}
