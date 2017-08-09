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
package com.asakusafw.windgate.cli;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.windgate.cli.CommandLineUtil;

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
        Properties loaded = CommandLineUtil.loadProperties(new URI(file.toURI().getPath()), null);
        assertThat(loaded, is(p));
    }

    /**
     * Loads properties from URI.
     * @throws Exception if failed
     */
    @Test
    public void loadProperties_uri() throws Exception {
        Properties p = new Properties();
        p.setProperty("hello", "world!");
        File file = store(p);
        Properties loaded = CommandLineUtil.loadProperties(file.toURI(), null);
        assertThat(loaded, is(p));
    }

    /**
     * Loads properties from classpath.
     * @throws Exception if failed
     */
    @Test
    public void loadProperties_classpath() throws Exception {
        String className = getClass().getName();
        String packageName = className.substring(0, className.lastIndexOf('.'));
        URI uri = new URI("classpath:" + packageName.replace('.', '/') + "/loadProperties.properties");
        Properties loaded = CommandLineUtil.loadProperties(uri, getClass().getClassLoader());

        Properties p = new Properties();
        p.setProperty("hello", "world!");
        assertThat(loaded, is(p));
    }

    /**
     * Loads properties from classpath.
     * @throws Exception expected
     */
    @Test(expected = IOException.class)
    public void loadProperties_classpath_missing() throws Exception {
        URI uri = new URI("classpath:MISSING-FILE.properties");
        CommandLineUtil.loadProperties(uri, null);
    }

    /**
     * Parses a file list.
     * @throws Exception if failed
     */
    @Test
    public void parseFileList() throws Exception {
        File a = folder.newFile("a.properties");
        File b = folder.newFile("c.properties");
        File c = folder.newFile("b.properties");

        StringBuilder buf = new StringBuilder();
        buf.append(a);
        buf.append(File.pathSeparatorChar);
        buf.append(b);
        buf.append(File.pathSeparatorChar);
        buf.append(c);
        List<File> result = canonicalize(CommandLineUtil.parseFileList(buf.toString()));
        assertThat(result, is(canonicalize(a, b, c)));
    }

    /**
     * Parses null as a file list.
     */
    @Test
    public void parseFileList_null() {
        List<File> result = canonicalize(CommandLineUtil.parseFileList(null));
        assertThat(result, is(canonicalize()));
    }

    /**
     * Parses empty file list.
     */
    @Test
    public void parseFileList_empty() {
        List<File> result = canonicalize(CommandLineUtil.parseFileList(""));
        assertThat(result, is(canonicalize()));
    }

    private List<File> canonicalize(File... files) {
        return canonicalize(Arrays.asList(files));
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

    /**
     * Parses a simple argument.
     */
    @Test
    public void parseArguments() {
        Map<String, String> parsed = CommandLineUtil.parseArguments("hello=world!").getPairs();
        Map<String, String> answer = new HashMap<>();
        answer.put("hello", "world!");
        assertThat(parsed, is(answer));
    }

    /**
     * Parses multiple arguments.
     */
    @Test
    public void parseArguments_multiple() {
        Map<String, String> parsed = CommandLineUtil.parseArguments("a=b,c=d,e=f").getPairs();
        Map<String, String> answer = new HashMap<>();
        answer.put("a", "b");
        answer.put("c", "d");
        answer.put("e", "f");
        assertThat(parsed, is(answer));
    }

    /**
     * Parses an argument with escaped value.
     */
    @Test
    public void parseArguments_escaped() {
        Map<String, String> parsed = CommandLineUtil.parseArguments("\\\\\\,\\==\\=\\,\\\\").getPairs();
        Map<String, String> answer = new HashMap<>();
        answer.put("\\,=", "=,\\");
        assertThat(parsed, is(answer));
    }

    /**
     * Parses an argument without key and value.
     */
    @Test
    public void parseArguments_empty_keyvaule() {
        Map<String, String> parsed = CommandLineUtil.parseArguments("=").getPairs();
        Map<String, String> answer = new HashMap<>();
        answer.put("", "");
        assertThat(parsed, is(answer));
    }

    /**
     * Parses an argument without key.
     */
    @Test
    public void parseArguments_empty_key() {
        Map<String, String> parsed = CommandLineUtil.parseArguments("=world!").getPairs();
        Map<String, String> answer = new HashMap<>();
        answer.put("", "world!");
        assertThat(parsed, is(answer));
    }

    /**
     * Parses an argument without value.
     */
    @Test
    public void parseArguments_empty_value() {
        Map<String, String> parsed = CommandLineUtil.parseArguments("hello=").getPairs();
        Map<String, String> answer = new HashMap<>();
        answer.put("hello", "");
        assertThat(parsed, is(answer));
    }

    /**
     * Parses duplicated arguments.
     */
    @Test
    public void parseArguments_duplicate_pair() {
        Map<String, String> parsed = CommandLineUtil.parseArguments("a=b,a=c").getPairs();
        Map<String, String> answer = new HashMap<>();
        answer.put("a", "b");
        assertThat(parsed, is(answer));
    }

    /**
     * Parses empty string as arguments.
     */
    @Test
    public void parseArguments_empty() {
        Map<String, String> parsed = CommandLineUtil.parseArguments("").getPairs();
        Map<String, String> answer = new HashMap<>();
        assertThat(parsed, is(answer));
    }

    /**
     * Parses null as arguments.
     */
    @Test
    public void parseArguments_null() {
        Map<String, String> parsed = CommandLineUtil.parseArguments(null).getPairs();
        Map<String, String> answer = new HashMap<>();
        assertThat(parsed, is(answer));
    }

    /**
     * Attempts to parse arguments with empty pair.
     */
    @Test
    public void parseArguments_empty_pair() {
        Map<String, String> parsed = CommandLineUtil.parseArguments("a=b,,c=d").getPairs();
        Map<String, String> answer = new HashMap<>();
        answer.put("a", "b");
        answer.put("c", "d");
        assertThat(parsed, is(answer));
    }

    /**
     * Attempts to parse an argument invalid form of pair.
     */
    @Test
    public void parseArguments_invalid_pair() {
        Map<String, String> parsed = CommandLineUtil.parseArguments("a=b=c").getPairs();
        Map<String, String> answer = new HashMap<>();
        assertThat(parsed, is(answer));
    }

    /**
     * Attempts to parse an argument invalid form of pair.
     */
    @Test
    public void parseArguments_keyonly() {
        Map<String, String> parsed = CommandLineUtil.parseArguments("a,b=c").getPairs();
        Map<String, String> answer = new HashMap<>();
        answer.put("b", "c");
        assertThat(parsed, is(answer));
    }

    private File store(Properties p) throws IOException, FileNotFoundException {
        File file = folder.newFile("testing.properties");
        try (FileOutputStream out = new FileOutputStream(file)) {
            p.store(out, "testing");
        }
        return file;
    }
}
