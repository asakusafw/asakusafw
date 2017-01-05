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
package com.asakusafw.testdriver.directio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.SpiExporterRetriever;

/**
 * Test for {@link DirectFileOutputRetriever}.
 */
@RunWith(Parameterized.class)
public class DirectFileOutputRetrieverTest {

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
    public DirectFileOutputRetrieverTest(Class<? extends DataFormat<Text>> format) {
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

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();

        File file = put("base/hoge.txt", "Hello, world!");
        File deep = put("base/d/e/e/p/hoge.txt", "Hello, world!");
        File outer = put("outer/hoge.txt", "Hello, world!");
        assertThat(file.exists(), is(true));
        assertThat(deep.exists(), is(true));
        assertThat(outer.exists(), is(true));

        testee.truncate(
            new MockOutputDescription("base", "something", format),
            profile.getTextContext());
        assertThat(file.exists(), is(false));
        assertThat(deep.exists(), is(false));
        assertThat(outer.exists(), is(true));
    }

    /**
     * truncate with placeholders.
     * @throws Exception if failed
     */
    @Test
    public void truncate_placeholders() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();

        File file = put("base/hoge.txt", "Hello, world!");
        File deep = put("base/d/e/e/p/hoge.txt", "Hello, world!");
        File outer = put("outer/hoge.txt", "Hello, world!");
        assertThat(file.exists(), is(true));
        assertThat(deep.exists(), is(true));
        assertThat(outer.exists(), is(true));

        testee.truncate(
            new MockOutputDescription("base", "output-{id}", format),
            profile.getTextContext());
        assertThat(file.exists(), is(false));
        assertThat(deep.exists(), is(false));
        assertThat(outer.exists(), is(true));
    }

    /**
     * truncate with wildcard.
     * @throws Exception if failed
     */
    @Test
    public void truncate_wildcard() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();

        File file = put("base/hoge.txt", "Hello, world!");
        File deep = put("base/d/e/e/p/hoge.txt", "Hello, world!");
        File outer = put("outer/hoge.txt", "Hello, world!");
        assertThat(file.exists(), is(true));
        assertThat(deep.exists(), is(true));
        assertThat(outer.exists(), is(true));

        testee.truncate(
            new MockOutputDescription("base", "output-*", format),
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

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();
        testee.truncate(
            new MockOutputDescription("base", "something", format),
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

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();

        File file = put("base/hoge.txt", "Hello, world!");
        File deep = put("base/d/e/e/p/hoge.txt", "Hello, world!");
        File outer = put("outer/hoge.txt", "Hello, world!");
        assertThat(file.exists(), is(true));
        assertThat(deep.exists(), is(true));
        assertThat(outer.exists(), is(true));

        testee.truncate(
            new MockOutputDescription("${target}", "something", format),
            profile.getTextContext("target", "base"));
        assertThat(file.exists(), is(false));
        assertThat(deep.exists(), is(false));
        assertThat(outer.exists(), is(true));
    }

    /**
     * simple input.
     * @throws Exception if failed
     */
    @Test
    public void createInput() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        put("base/output.txt", "Hello, world!");

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();
        DataModelSource input = testee.createSource(
                new MockTextDefinition(),
                new MockOutputDescription("base", "output.txt", format),
                profile.getTextContext());
        List<String> list = get(input);
        assertThat(list, is(Arrays.asList("Hello, world!")));
    }

    /**
     * output multiple records.
     * @throws Exception if failed
     */
    @Test
    public void createInput_multirecord() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        put("base/output.txt", "Hello1", "Hello2", "Hello3");

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();
        DataModelSource input = testee.createSource(
                new MockTextDefinition(),
                new MockOutputDescription("base", "output.txt", format),
                profile.getTextContext());

        List<String> list = get(input);
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("Hello1"));
        assertThat(list, hasItem("Hello2"));
        assertThat(list, hasItem("Hello3"));
    }

    /**
     * output multiple files.
     * @throws Exception if failed
     */
    @Test
    public void createInput_multifile() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        put("base/output-1.txt", "Hello1");
        put("base/output-2.txt", "Hello2");
        put("base/output-3.txt", "Hello3");

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();
        DataModelSource input = testee.createSource(
                new MockTextDefinition(),
                new MockOutputDescription("base", "{value}.txt", format),
                profile.getTextContext());

        List<String> list = get(input);
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
    public void createInput_variables() throws Exception {
        profile.add("vars", HadoopDataSource.class, "base");
        profile.add("vars", new File(folder.getRoot(), "testing"));
        profile.put();

        put("testing/output.txt", "Hello, world!");

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();
        DataModelSource input = testee.createSource(
                new MockTextDefinition(),
                new MockOutputDescription("${vbase}", "${voutput}.txt", format),
                profile.getTextContext("vbase", "base", "voutput", "output"));
        List<String> list = get(input);
        assertThat(list, is(Arrays.asList("Hello, world!")));
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

        put("base/output-1.txt", "Hello1");
        put("base/output-2.txt", "Hello2");
        put("base/output-3.txt", "Hello3");

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();
        DataModelSource input = testee.createSource(
                new MockTextDefinition(),
                new MockOutputDescription("base", "output-{id}.txt", format),
                profile.getTextContext());
        List<String> list = get(input);
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("Hello1"));
        assertThat(list, hasItem("Hello2"));
        assertThat(list, hasItem("Hello3"));
    }

    /**
     * output with wildcard.
     * @throws Exception if failed
     */
    @Test
    public void createInput_wildcard() throws Exception {
        profile.add("root", HadoopDataSource.class, "/");
        profile.add("root", HadoopDataSourceProfile.KEY_PATH, folder.getRoot().toURI().toURL().toString());
        profile.put();

        put("base/output-1.txt", "Hello1");
        put("base/output-2.txt", "Hello2");
        put("base/output-3.txt", "Hello3");

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();
        DataModelSource input = testee.createSource(
                new MockTextDefinition(),
                new MockOutputDescription("base", "output-*.txt", format),
                profile.getTextContext());
        List<String> list = get(input);
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("Hello1"));
        assertThat(list, hasItem("Hello2"));
        assertThat(list, hasItem("Hello3"));
    }

    /**
     * configuration is not found.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void no_config() throws Exception {
        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();
        testee.truncate(
            new MockOutputDescription("base", "something", format),
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

        DirectFileOutputRetriever testee = new DirectFileOutputRetriever();
        testee.truncate(
            new MockOutputDescription("base", "something", format),
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

        put("base/output.txt", "Hello, world!");

        SpiExporterRetriever testee = new SpiExporterRetriever(getClass().getClassLoader());
        DataModelSource input = testee.createSource(
                new MockTextDefinition(),
                new MockOutputDescription("base", "output.txt", format),
                profile.getTextContext());
        List<String> list = get(input);
        assertThat(list, is(Arrays.asList("Hello, world!")));
    }

    private List<String> get(DataModelSource input) throws IOException {
        try {
            MockTextDefinition def = new MockTextDefinition();
            List<String> results = new ArrayList<>();
            while (true) {
                DataModelReflection next = input.next();
                if (next == null) {
                    break;
                }
                results.add(def.toObject(next).toString());
            }
            return results;
        } finally {
            input.close();
        }
    }

    private File put(String targetPath, String... contents) throws IOException {
        File target = new File(folder.getRoot(), targetPath);
        target.getParentFile().mkdirs();
        try (PrintWriter w = new PrintWriter(target, "UTF-8")) {
            for (String line : contents) {
                w.println(line);
            }
        }
        return target;
    }
}
