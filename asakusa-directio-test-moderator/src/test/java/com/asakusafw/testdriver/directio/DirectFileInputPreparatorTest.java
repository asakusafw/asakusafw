/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.testdriver.directio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.hadoop.io.Text;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSource;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceProfile;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.SpiImporterPreparator;

/**
 * Test for {@link DirectFileInputPreparator}.
 */
@RunWith(Parameterized.class)
public class DirectFileInputPreparatorTest {

    /**
     * Profile context.
     */
    @Rule
    public final ProfileContext profile = new ProfileContext();

    /**
     * A temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final Class<? extends DataFormat<Text>> format;

    /**
     * Returns the parameters.
     * @return the parameters
     */
    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { MockStreamFormat.class },
                { MockFileFormat.class },
        });
    }

    /**
     * Creates a new instance.
     * @param format the format.
     */
    public DirectFileInputPreparatorTest(Class<? extends DataFormat<Text>> format) {
        this.format = format;
    }

    /**
     * truncate.
     * @throws Exception if failed
     */
    @Test
    public void truncate() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        DirectFileInputPreparator testee = new DirectFileInputPreparator();

        File file = put("base/hoge.txt", "Hello, world!");
        File deep = put("base/d/e/e/p/hoge.txt", "Hello, world!");
        File outer = put("outer/hoge.txt", "Hello, world!");
        assertThat(file.exists(), is(true));
        assertThat(deep.exists(), is(true));
        assertThat(outer.exists(), is(true));

        testee.truncate(
            new MockInputDescription("base", "something", format),
            profile.getTextContext());
        assertThat(file.exists(), is(false));
        assertThat(deep.exists(), is(false));
        assertThat(outer.exists(), is(true));
    }

    /**
     * truncate empty target.
     * @throws Exception if failed
     */
    @Test
    public void truncate_empty() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        DirectFileInputPreparator testee = new DirectFileInputPreparator();
        testee.truncate(
            new MockInputDescription("base", "something", format),
            profile.getTextContext());
    }

    /**
     * truncate with variables in path.
     * @throws Exception if failed
     */
    @Test
    public void truncate_variable() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        DirectFileInputPreparator testee = new DirectFileInputPreparator();

        File file = put("base/hoge.txt", "Hello, world!");
        File deep = put("base/d/e/e/p/hoge.txt", "Hello, world!");
        File outer = put("outer/hoge.txt", "Hello, world!");
        assertThat(file.exists(), is(true));
        assertThat(deep.exists(), is(true));
        assertThat(outer.exists(), is(true));

        testee.truncate(
            new MockInputDescription("${target}", "something", format),
            profile.getTextContext("target", "base"));
        assertThat(file.exists(), is(false));
        assertThat(deep.exists(), is(false));
        assertThat(outer.exists(), is(true));
    }

    /**
     * simple output.
     * @throws Exception if failed
     */
    @Test
    public void createOutput() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        DirectFileInputPreparator testee = new DirectFileInputPreparator();
        ModelOutput<Text> output = testee.createOutput(
                new MockTextDefinition(),
                new MockInputDescription("base", "input.txt", format),
                profile.getTextContext());
        put(output, "Hello, world!");
        assertThat(get("base/input.txt"), is(Arrays.asList("Hello, world!")));
    }

    /**
     * output multiple records.
     * @throws Exception if failed
     */
    @Test
    public void createOutput_multirecord() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        DirectFileInputPreparator testee = new DirectFileInputPreparator();
        ModelOutput<Text> output = testee.createOutput(
                new MockTextDefinition(),
                new MockInputDescription("base", "input.txt", format),
                profile.getTextContext());
        put(output, "Hello1", "Hello2", "Hello3");

        List<String> list = get("base/input.txt");
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("Hello1"));
        assertThat(list, hasItem("Hello2"));
        assertThat(list, hasItem("Hello3"));
    }

    /**
     * output with variables.
     * @throws Exception if failed
     */
    @Test
    public void createOutput_variables() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        DirectFileInputPreparator testee = new DirectFileInputPreparator();
        ModelOutput<Text> output = testee.createOutput(
                new MockTextDefinition(),
                new MockInputDescription("${vbase}", "${vinput}.txt", format),
                profile.getTextContext("vbase", "base", "vinput", "input"));
        put(output, "Hello, world!");
        assertThat(get("base/input.txt"), is(Arrays.asList("Hello, world!")));
    }

    /**
     * output with placeholders.
     * @throws Exception if failed
     */
    @Test
    public void createInput_placeholders() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        DirectFileInputPreparator testee = new DirectFileInputPreparator();
        ModelOutput<Text> output = testee.createOutput(
                new MockTextDefinition(),
                new MockInputDescription("base", "{alpha|beta|gamma/data}/{a/x|b|c}-*.csv", format),
                profile.getTextContext());
        put(output, "Hello, world!");
        List<File> files = find("base");
        assertThat(files.toString(), files.size(), is(1));
        assertThat(get(files.get(0)), is(Arrays.asList("Hello, world!")));
    }

    /**
     * output with placeholders.
     * @throws Exception if failed
     */
    @Test
    public void createOutput_placeholders() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        DirectFileInputPreparator testee = new DirectFileInputPreparator();
        ModelOutput<Text> output = testee.createOutput(
                new MockTextDefinition(),
                new MockInputDescription("base", "**/data-*.csv", format),
                profile.getTextContext());
        put(output, "Hello, world!");
        List<File> files = find("base");
        assertThat(files.toString(), files.size(), is(1));
        assertThat(get(files.get(0)), is(Arrays.asList("Hello, world!")));
    }

    /**
     * configuration is not found.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void no_config() throws Exception {
        DirectFileInputPreparator testee = new DirectFileInputPreparator();
        testee.truncate(
            new MockInputDescription("base", "something", format),
            profile.getTextContext());
    }

    /**
     * datasource is not found.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void no_datasource() throws Exception {
        profile.add("root", HadoopDataSource.class, "other");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        DirectFileInputPreparator testee = new DirectFileInputPreparator();
        testee.truncate(
            new MockInputDescription("base", "something", format),
            profile.getTextContext());
    }

    /**
     * using SPI.
     * @throws Exception if failed
     */
    @Test
    public void spi() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        SpiImporterPreparator testee = new SpiImporterPreparator(getClass().getClassLoader());
        ModelOutput<Text> output = testee.createOutput(
                new MockTextDefinition(),
                new MockInputDescription("base", "input.txt", format),
                profile.getTextContext());
        put(output, "Hello, world!");
        assertThat(get("base/input.txt"), is(Arrays.asList("Hello, world!")));
    }

    private List<File> find(String targetPath) {
        List<File> results = new ArrayList<File>();
        LinkedList<File> work = new LinkedList<File>();
        work.add(new File(folder.getRoot(), targetPath));
        while (work.isEmpty() == false) {
            File file = work.removeFirst();
            if (file.getName().startsWith(".")) {
                continue;
            }
            if (file.isDirectory()) {
                Collections.addAll(work, file.listFiles());
            } else {
                results.add(file);
            }
        }
        return results;
    }

    private List<String> get(String targetPath) throws IOException {
        return get(new File(folder.getRoot(), targetPath));
    }

    private List<String> get(File target) throws FileNotFoundException {
        Scanner s = new Scanner(target, "UTF-8");
        try {
            List<String> results = new ArrayList<String>();
            while (s.hasNextLine()) {
                results.add(s.nextLine());
            }
            return results;
        } finally {
            s.close();
        }
    }

    private void put(ModelOutput<Text> output, String... contents) throws IOException {
        try {
            Text text = new Text();
            for (String line : contents) {
                text.set(line);
                output.write(text);
            }
        } finally {
            output.close();
        }
    }

    private File put(String targetPath, String... contents) throws IOException {
        File target = new File(folder.getRoot(), targetPath);
        target.getParentFile().mkdirs();
        PrintWriter w = new PrintWriter(target, "UTF-8");
        try {
            for (String line : contents) {
                w.println(line);
            }
        } finally {
            w.close();
        }
        return target;
    }
}
