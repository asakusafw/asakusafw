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
package com.asakusafw.compiler.directio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asakusafw.compiler.directio.testing.model.Line1;
import com.asakusafw.compiler.directio.testing.model.Line2;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.runtime.directio.DataFilter;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;
import com.asakusafw.vocabulary.directio.DirectFileOutputDescription;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Test for {@link DirectFileIoProcessor}.
 */
@RunWith(Parameterized.class)
public class DirectFileIoProcessorRunTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * Compiler tester.
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    private final Class<? extends DataFormat<Text>> format;

    /**
     * Returns the parameters.
     * @return the parameters
     */
    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { LineFormat.class },
                { LineFileFormat.class },
        });
    }

    /**
     * Creates a new instance.
     * @param format the format.
     */
    public DirectFileIoProcessorRunTest(Class<? extends DataFormat<Text>> format) {
        this.format = format;
    }

    /**
     * simple.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        put("input/input.txt", "1Hello", "2Hello", "3Hello");
        In<Line1> in = tester.input("in1", new Input(format, "input", "*"));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/output.txt");
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
        assertThat(list, hasItem("3Hello"));
    }

    /**
     * tiny input.
     * @throws Exception if failed
     */
    @Test
    public void tiny() throws Exception {
        put("input/input.txt", "1Hello", "2Hello", "3Hello");
        In<Line1> in = tester.input("in1",
                new Input(Line1.class, format, "input", "*", DataSize.TINY));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/output.txt");
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
        assertThat(list, hasItem("3Hello"));
    }

    /**
     * multiple input.
     * @throws Exception if failed
     */
    @Test
    public void input_multi() throws Exception {
        put("input/input-1.txt", "1Hello");
        put("input/input-2.txt", "2Hello");
        put("input/input-3.txt", "3Hello");
        put("input/other.txt", "4Hello");
        In<Line1> in = tester.input("in1", new Input(format, "input", "input-*"));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/output.txt");
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
        assertThat(list, hasItem("3Hello"));
    }

    /**
     * ordering.
     * @throws Exception if failed
     */
    @Test
    public void order() throws Exception {
        put("input/input.txt", "1Hello", "2Hello", "3Hello");
        In<Line1> in = tester.input("in1", new Input(format, "input", "*"));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt", "-value"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        assertThat(get("output/output.txt"), is(list("3Hello", "2Hello", "1Hello")));
    }

    /**
     * file partitioning.
     * @throws Exception if failed
     */
    @Test
    public void partition() throws Exception {
        put("input/input.txt", "a1", "b1", "b2", "c1", "c2", "c3");
        In<Line1> in = tester.input("in1", new Input(format, "input", "*"));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "{first}-output.txt", "+value"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        assertThat(get("output/a-output.txt"), is(list("a1")));
        assertThat(get("output/b-output.txt"), is(list("b1", "b2")));
        assertThat(get("output/c-output.txt"), is(list("c1", "c2", "c3")));
    }

    /**
     * file partitioning by random.
     * @throws Exception if failed
     */
    @Test
    public void random() throws Exception {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            lines.add(String.format("%03d", i));
        }
        put("input/input.txt", lines.toArray(new String[lines.size()]));
        In<Line1> in = tester.input("in1", new Input(format, "input", "*"));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output-[1..4].txt", "+value"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> o1 = get("output/output-1.txt");
        List<String> o2 = get("output/output-2.txt");
        List<String> o3 = get("output/output-3.txt");
        List<String> o4 = get("output/output-4.txt");

        // probably ok
        assertThat(o1.size(), is(greaterThan(0)));
        assertThat(o2.size(), is(greaterThan(0)));
        assertThat(o3.size(), is(greaterThan(0)));
        assertThat(o4.size(), is(greaterThan(0)));

        List<String> results = new ArrayList<>();
        results.addAll(o1);
        results.addAll(o2);
        results.addAll(o3);
        results.addAll(o4);

        Collections.sort(results);
        assertThat(results, is(lines));
    }

    /**
     * wildcard.
     * @throws Exception if failed
     */
    @Test
    public void wildcard() throws Exception {
        put("input/input.txt", "1Hello", "2Hello", "3Hello");
        In<Line1> in = tester.input("in1", new Input(format, "input", "*"));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output-*.txt"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/output-*.txt");
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
        assertThat(list, hasItem("3Hello"));
    }

    /**
     * use variables.
     * @throws Exception if failed
     */
    @Test
    public void variable() throws Exception {
        put("input/input-1.txt", "1Hello");
        put("input/input-2.txt", "2Hello");
        put("input/input-3.txt", "3Hello");
        put("input/other.txt", "4Hello");
        In<Line1> in = tester.input("in1", new Input(format, "${input-dir}", "${input-pattern}"));
        Out<Line1> out = tester.output("out1", new Output(format, "${output-dir}", "${output-pattern}"));

        tester.variables().defineVariable("input-dir", "input");
        tester.variables().defineVariable("input-pattern", "input-*");
        tester.variables().defineVariable("output-dir", "output");
        tester.variables().defineVariable("output-pattern", "output.txt");
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/output.txt");
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
        assertThat(list, hasItem("3Hello"));
    }

    /**
     * use variables.
     * @throws Exception if failed
     */
    @Test
    public void variable_wildcard() throws Exception {
        put("input/input-1.txt", "1Hello");
        put("input/input-2.txt", "2Hello");
        put("input/input-3.txt", "3Hello");
        put("input/other.txt", "4Hello");
        In<Line1> in = tester.input("in1", new Input(format, "${input-dir}", "${input-pattern}"));
        Out<Line1> out = tester.output("out1", new Output(format, "${output-dir}", "${output-pattern}-*.txt"));

        tester.variables().defineVariable("input-dir", "input");
        tester.variables().defineVariable("input-pattern", "input-*");
        tester.variables().defineVariable("output-dir", "output");
        tester.variables().defineVariable("output-pattern", "output");
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/output-*.txt");
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
        assertThat(list, hasItem("3Hello"));
    }

    /**
     * use variables in base path.
     * @throws Exception if failed
     */
    @Test
    public void variable_basepath() throws Exception {
        useFrameworkConf("split-io-conf.xml");
        put("input-split/input.txt", "1Hello");
        put("other-split/other.txt", "2Hello");
        In<Line1> in = tester.input("in1", new Input(format, "${input}", "input.txt"));
        Out<Line1> out = tester.output("out1", new Output(format, "${output}", "output.txt"));

        tester.variables().defineVariable("input", "input");
        tester.variables().defineVariable("output", "output");
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output-split/output.txt");
        assertThat(list.size(), is(1));
        assertThat(list, hasItem("1Hello"));
    }

    /**
     * delete file.
     * @throws Exception if failed
     */
    @Test
    public void delete() throws Exception {
        put("input/input.txt", "1Hello", "2Hello", "3Hello");
        put("output/deleted.txt", "4Hello");
        put("output/keep.txt", "5Hello");
        put("output/deleted-1.txt", "6Hello");
        put("output/deleted-2.txt", "7Hello");

        In<Line1> in = tester.input("in1", new Input(format, "input", "*"));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt")
            .delete("deleted.txt")
            .delete("deleted-*.txt"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/*.txt");
        assertThat(list.size(), is(4));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
        assertThat(list, hasItem("3Hello"));
        assertThat(list, hasItem("5Hello"));
    }

    /**
     * delete file.
     * @throws Exception if failed
     */
    @Test
    public void delete_variable() throws Exception {
        put("input/input.txt", "1Hello", "2Hello", "3Hello");
        put("output/keep.txt", "4Hello");
        put("output/deleted-1.txt", "5Hello");
        put("output/deleted-2.txt", "6Hello");

        In<Line1> in = tester.input("in1", new Input(format, "input", "*"));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt")
            .delete("${pattern}-*.txt"));

        tester.variables().defineVariable("pattern", "deleted");
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/*.txt");
        assertThat(list.size(), is(4));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
        assertThat(list, hasItem("3Hello"));
        assertThat(list, hasItem("4Hello"));
    }

    /**
     * empty input.
     * @throws Exception if failed
     */
    @Test
    public void input_empty() throws Exception {
        put("input/input-1.txt");
        put("input/input-2.txt");
        put("input/input-3.txt");
        put("input/other.txt", "Hello");
        In<Line1> in = tester.input("in1", new Input(format, "input", "input-*"));
        Out<Line1> out = tester.output("out1", new Output(format, "output-1", "output.txt"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<Path> list = find("output/output-*.txt");
        assertThat(list.toString(), list.size(), is(0));
    }

    /**
     * empty input.
     * @throws Exception if failed
     */
    @Test
    public void input_empty_noreduce() throws Exception {
        put("input/input-1.txt");
        put("input/input-2.txt");
        put("input/input-3.txt");
        put("input/other.txt", "Hello");
        In<Line1> in = tester.input("in1", new Input(format, "input", "input-*"));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<Path> list = find("output/output-*.txt");
        assertThat(list.toString(), list.size(), is(0));
    }

    /**
     * optional inputs.
     * @throws Exception if failed
     */
    @Test
    public void optional() throws Exception {
        put("input/input.txt", "1Hello", "2Hello", "3Hello");
        In<Line1> in = tester.input("in1", new Input(format, "input", "*", true));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/output.txt");
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
        assertThat(list, hasItem("3Hello"));
    }

    /**
     * filter by path.
     * @throws Exception if failed
     */
    @Test
    public void input_filter_path() throws Exception {
        put("input/input-1.txt", "1Hello");
        put("input/input-2.txt", "2Hello");
        put("input/input-3.txt", "3Hello");
        put("input/other.txt", "4Hello");
        In<Line1> in = tester.input("in1", new Input(format, "input", "input-*").withFilter(MockFilterPath.class));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt"));
        tester.variables().defineVariable("filter", ".*input-2\\.txt");
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/output.txt");
        assertThat(list.size(), is(2));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("3Hello"));
    }

    /**
     * filter by object.
     * @throws Exception if failed
     */
    @Test
    public void input_filter_object() throws Exception {
        put("input/input-1.txt", "1Hello");
        put("input/input-2.txt", "2Hello");
        put("input/input-3.txt", "3Hello");
        put("input/other.txt", "4Hello");
        In<Line1> in = tester.input("in1", new Input(format, "input", "input-*").withFilter(MockFilterObject.class));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt"));
        tester.variables().defineVariable("filter", "3Hello");
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/output.txt");
        assertThat(list.size(), is(2));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
    }

    /**
     * filter by path.
     * @throws Exception if failed
     */
    @Test
    public void input_filter_path_disabled() throws Exception {
        put("input/input-1.txt", "1Hello");
        put("input/input-2.txt", "2Hello");
        put("input/input-3.txt", "3Hello");
        put("input/other.txt", "4Hello");
        In<Line1> in = tester.input("in1", new Input(format, "input", "input-*").withFilter(MockFilterPath.class));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt"));
        tester.variables().defineVariable("filter", ".*input-2\\.txt");
        tester.options().putExtraAttribute(DirectFileIoProcessor.OPTION_FILTER_ENABLED, "false");
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/output.txt");
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
        assertThat(list, hasItem("3Hello"));
    }

    /**
     * filter by object.
     * @throws Exception if failed
     */
    @Test
    public void input_filter_object_disabled() throws Exception {
        put("input/input-1.txt", "1Hello");
        put("input/input-2.txt", "2Hello");
        put("input/input-3.txt", "3Hello");
        put("input/other.txt", "4Hello");
        In<Line1> in = tester.input("in1", new Input(format, "input", "input-*").withFilter(MockFilterObject.class));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt"));
        tester.variables().defineVariable("filter", "3Hello");
        tester.options().putExtraAttribute(DirectFileIoProcessor.OPTION_FILTER_ENABLED, "false");
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));

        List<String> list = get("output/output.txt");
        assertThat(list.size(), is(3));
        assertThat(list, hasItem("1Hello"));
        assertThat(list, hasItem("2Hello"));
        assertThat(list, hasItem("3Hello"));
    }

    /**
     * dual in/out.
     * @throws Exception if failed
     */
    @Test
    public void dual_io() throws Exception {
        put("input/input-1.txt", "Hello1");
        put("input/input-2.txt", "Hello2");
        In<Line1> in1 = tester.input("in1",
                new Input(Line1.class, format, "input", "input-1.txt", DataSize.LARGE));
        In<Line2> in2 = tester.input("in2",
                new Input(Line2.class, format, "input", "input-2.txt", DataSize.TINY));
        Out<Line1> out1 = tester.output("out1",
                new Output(Line1.class, format, "output-1", "output.txt"));
        Out<Line2> out2 = tester.output("out2",
                new Output(Line2.class, format, "output-2", "output.txt"));
        assertThat(tester.runFlow(new DualIdentityFlow<>(in1, in2, out1, out2)), is(true));

        assertThat(get("output-1/output.txt"), is(list("Hello1")));
        assertThat(get("output-2/output.txt"), is(list("Hello2")));
    }

    /**
     * dual in/out.
     * @throws Exception if failed
     */
    @Test
    public void dual_io_noreduce() throws Exception {
        put("input/input-1.txt", "Hello1");
        put("input/input-2.txt", "Hello2");
        In<Line1> in1 = tester.input("in1",
                new Input(Line1.class, format, "input", "input-1.txt", DataSize.LARGE));
        In<Line2> in2 = tester.input("in2",
                new Input(Line2.class, format, "input", "input-2.txt", DataSize.TINY));
        Out<Line1> out1 = tester.output("out1",
                new Output(Line1.class, format, "output-1", "output-*.txt"));
        Out<Line2> out2 = tester.output("out2",
                new Output(Line2.class, format, "output-2", "output-*.txt"));
        assertThat(tester.runFlow(new DualIdentityFlow<>(in1, in2, out1, out2)), is(true));

        assertThat(get("output-1/output-*.txt"), is(list("Hello1")));
        assertThat(get("output-2/output-*.txt"), is(list("Hello2")));
    }

    /**
     * dual in/out.
     * @throws Exception if failed
     */
    @Test
    public void dual_io_mixed() throws Exception {
        put("input/input-1.txt", "Hello1");
        put("input/input-2.txt", "Hello2");
        In<Line1> in1 = tester.input("in1",
                new Input(Line1.class, format, "input", "input-1.txt", DataSize.LARGE));
        In<Line2> in2 = tester.input("in2",
                new Input(Line2.class, format, "input", "input-2.txt", DataSize.TINY));
        Out<Line1> out1 = tester.output("out1",
                new Output(Line1.class, format, "output-1", "output-1.txt"));
        Out<Line2> out2 = tester.output("out2",
                new Output(Line2.class, format, "output-2", "output-*.txt"));
        assertThat(tester.runFlow(new DualIdentityFlow<>(in1, in2, out1, out2)), is(true));

        assertThat(get("output-1/output-*.txt"), is(list("Hello1")));
        assertThat(get("output-2/output-*.txt"), is(list("Hello2")));
    }

    /**
     * input is missing.
     * @throws Exception if failed
     */
    @Test
    public void input_missing() throws Exception {
        In<Line1> in = tester.input("in1", new Input(format, "input", "*"));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(false));
    }

    /**
     * input is missing, but its input is optional.
     * @throws Exception if failed
     */
    @Test
    public void input_missing_optional() throws Exception {
        In<Line1> in = tester.input("in1", new Input(format, "input", "*", true));
        Out<Line1> out = tester.output("out1", new Output(format, "output", "output.txt"));
        assertThat(tester.runFlow(new IdentityFlow<>(in, out)), is(true));
    }

    private List<String> list(String... values) {
        return Arrays.asList(values);
    }

    private Path getPath(String target) {
        return new Path("target/testing/directio-fs", target);
    }

    private List<Path> find(String target) throws IOException {
        FileSystem fs = FileSystem.get(tester.configuration());
        FileStatus[] list = fs.globStatus(getPath(target));
        if (list == null) {
            return Collections.emptyList();
        }
        List<Path> results = new ArrayList<>();
        for (FileStatus file : list) {
            results.add(file.getPath());
        }
        return results;
    }

    private List<String> get(String target) throws IOException {
        FileSystem fs = FileSystem.get(tester.configuration());
        List<String> results = new ArrayList<>();
        for (Path path : find(target)) {
            try (InputStream input = fs.open(path); Scanner s = new Scanner(new InputStreamReader(input, "UTF-8"))) {
                while (s.hasNextLine()) {
                    results.add(s.nextLine());
                }
            }
        }
        return results;
    }

    private void put(String target, String... contents) throws IOException {
        FileSystem fs = FileSystem.get(tester.configuration());
        try (OutputStream output = fs.create(getPath(target), true);
                PrintWriter w = new PrintWriter(new OutputStreamWriter(output, "UTF-8"))) {
            for (String line : contents) {
                w.println(line);
            }
        }
    }

    private void useFrameworkConf(String name) {
        File file = tester.framework().getCoreConfigurationFile();
        if (file == null) {
            throw new AssertionError("Missing original framework conf");
        }
        try (InputStream input = getClass().getResourceAsStream(name);
                OutputStream output = new FileOutputStream(file)) {
            assertThat(name, input, is(notNullValue()));
            byte[] buf = new byte[256];
            while (true) {
                int read = input.read(buf);
                if (read < 0) {
                    break;
                }
                output.write(buf, 0, read);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static class Input extends DirectFileInputDescription {

        private final Class<?> modelType;
        private final Class<? extends DataFormat<?>> format;
        private final String basePath;
        private final String resourcePattern;
        private final Boolean optional;
        private final DataSize dataSize;
        private Class<? extends DataFilter<?>> filter;

        Input(
                Class<?> modelType,
                Class<? extends DataFormat<?>> format,
                String basePath,
                String resourcePattern,
                DataSize dataSize) {
            this(modelType, format, basePath, resourcePattern, null, dataSize);
        }

        Input(
                Class<? extends DataFormat<?>> format,
                String basePath,
                String resourcePattern) {
            this(Line1.class, format, basePath, resourcePattern, null, null);
        }

        Input(
                Class<? extends DataFormat<?>> format,
                String basePath,
                String resourcePattern,
                boolean optional) {
            this(Line1.class, format, basePath, resourcePattern, optional, null);
        }

        Input(
                Class<?> modelType,
                Class<? extends DataFormat<?>> format,
                String basePath,
                String resourcePattern,
                Boolean optional,
                DataSize dataSize) {
            this.modelType = modelType;
            this.basePath = basePath;
            this.resourcePattern = resourcePattern;
            this.format = format;
            this.optional = optional;
            this.dataSize = dataSize;
        }

        @Override
        public Class<?> getModelType() {
            return modelType;
        }

        @Override
        public Class<? extends DataFormat<?>> getFormat() {
            return format;
        }

        @Override
        public String getBasePath() {
            return basePath;
        }

        @Override
        public String getResourcePattern() {
            return resourcePattern;
        }

        @Override
        public boolean isOptional() {
            if (optional != null) {
                return optional;
            }
            return super.isOptional();
        }

        @Override
        public DataSize getDataSize() {
            if (dataSize != null) {
                return dataSize;
            }
            return super.getDataSize();
        }

        @Override
        public Class<? extends DataFilter<?>> getFilter() {
            return filter;
        }

        public Input withFilter(Class<? extends DataFilter<?>> newValue) {
            this.filter = newValue;
            return this;
        }
    }

    private static class Output extends DirectFileOutputDescription {

        private final Class<?> modelType;
        private final Class<? extends DataFormat<?>> format;
        private final String basePath;
        private final String resourcePattern;
        private final String[] order;
        private final List<String> deletes = new ArrayList<>();

        Output(
                Class<?> modelType,
                Class<? extends DataFormat<?>> format,
                String basePath,
                String resourcePattern,
                String... order) {
            this.modelType = modelType;
            this.format = format;
            this.basePath = basePath;
            this.resourcePattern = resourcePattern;
            this.order = order;
        }

        Output(
                Class<? extends DataFormat<?>> format,
                String basePath,
                String resourcePattern,
                String... order) {
            this.modelType = Line1.class;
            this.format = format;
            this.basePath = basePath;
            this.resourcePattern = resourcePattern;
            this.order = order;
        }

        Output delete(String pattern) {
            deletes.add(pattern);
            return this;
        }

        @Override
        public Class<?> getModelType() {
            return modelType;
        }

        @Override
        public Class<? extends DataFormat<?>> getFormat() {
            return format;
        }

        @Override
        public String getBasePath() {
            return basePath;
        }

        @Override
        public String getResourcePattern() {
            return resourcePattern;
        }

        @Override
        public List<String> getOrder() {
            return Arrays.asList(order);
        }

        @Override
        public List<String> getDeletePatterns() {
            return deletes;
        }
    }

    /**
     * filters by path.
     */
    public static class MockFilterPath extends DataFilter<Object> {

        private Pattern pattern;

        @Override
        public void initialize(DataFilter.Context context) {
            pattern = Pattern.compile(context.getBatchArguments().get("filter"));
        }

        @Override
        public boolean acceptsPath(String path) {
            return pattern.matcher(path).matches() == false;
        }
    }

    /**
     * filters by object.
     */
    public static class MockFilterObject extends DataFilter<Line1> {

        private Pattern pattern;

        @Override
        public void initialize(DataFilter.Context context) {
            pattern = Pattern.compile(context.getBatchArguments().get("filter"));
        }

        @Override
        public boolean acceptsData(Line1 data) {
            return pattern.matcher(data.getValueAsString()).matches() == false;
        }
    }
}
