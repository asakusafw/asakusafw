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
package com.asakusafw.testdriver.directio.api;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.api.DirectIo;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSource;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceProfile;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.testdriver.OperatorTestEnvironment;
import com.asakusafw.utils.io.Provider;
import com.asakusafw.utils.io.Source;
import com.asakusafw.utils.io.Sources;

/**
 * Test for {@link DirectIoOperatorTester}.
 */
public class DirectIoOperatorTesterTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Test target.
     */
    @Rule
    public final OperatorTestEnvironment environment = new OperatorTestEnvironment();

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        injectDataSource("testing", folder.newFolder());
        environment.reload();

        DirectIoTester.with(environment)
            .resource(MockFormat.class, "testing", "*.txt")
            .prepare(objects("Hello, world!"));

        ModelInput<StringBuilder> input = DirectIo.open(MockFormat.class, "testing", "*.txt");
        check(input, "Hello, world!");
    }

    /**
     * prepare with resource.
     * @throws Exception if failed
     */
    @Test
    public void prepare_with_path() throws Exception {
        injectDataSource("testing", folder.newFolder());
        environment.reload();

        DirectIoTester.with(environment)
            .resource(MockFormat.class, "testing", "*.txt")
            .prepare("hello.json");

        ModelInput<StringBuilder> input = DirectIo.open(MockFormat.class, "testing", "*.txt");
        check(input, "Hello, world!");
    }

    /**
     * prepare with provider.
     * @throws Exception if failed
     */
    @Test
    public void prepare_with_provider() throws Exception {
        injectDataSource("testing", folder.newFolder());
        environment.reload();

        DirectIoTester.with(environment)
            .resource(MockFormat.class, "testing", "*.txt")
            .prepare(new Provider<Source<StringBuilder>>() {
                @Override
                public void close() throws IOException {
                    return;
                }

                @Override
                public Source<StringBuilder> open() {
                    return Sources.wrap(objects("Hello, world!").iterator());
                }
            });

        ModelInput<StringBuilder> input = DirectIo.open(MockFormat.class, "testing", "*.txt");
        check(input, "Hello, world!");
    }

    static List<StringBuilder> objects(String...values) {
        List<StringBuilder> results = new ArrayList<>();
        for (String s : values) {
            results.add(new StringBuilder(s));
        }
        return results;
    }

    private File injectDataSource(String path, File mapped) {
        environment.configure(qualify(), HadoopDataSource.class.getName());
        environment.configure(qualify(HadoopDataSourceUtil.KEY_PATH), path);
        environment.configure(qualify(HadoopDataSourceProfile.KEY_PATH), mapped.toURI().toString());
        return mapped;
    }

    private String qualify() {
        return String.format("%s%s", HadoopDataSourceUtil.PREFIX, "t");
    }

    private String qualify(String key) {
        return String.format("%s%s.%s", HadoopDataSourceUtil.PREFIX, "t", key);
    }

    private void check(ModelInput<StringBuilder> input, String... values) throws IOException {
        Set<String> results;
        try {
            results = consume(input);
        } finally {
            input.close();
        }
        assertThat(results, is(set(values)));
    }

    private Set<String> set(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    private Set<String> consume(ModelInput<StringBuilder> input) throws IOException {
        Set<String> results = new HashSet<>();
        StringBuilder buf = new StringBuilder();
        while (input.readTo(buf)) {
            results.add(buf.toString());
        }
        return results;
    }

    /**
     * mock data format.
     */
    public static class MockFormat extends BinaryStreamFormat<StringBuilder> {

        @Override
        public Class<StringBuilder> getSupportedType() {
            return StringBuilder.class;
        }

        @Override
        public ModelInput<StringBuilder> createInput(
                Class<? extends StringBuilder> dataType, String path,
                InputStream stream, long offset, long fragmentSize)
                throws IOException, InterruptedException {
            assertThat(offset, is(0L));
            Scanner scanner = new Scanner(stream, "UTF-8");
            return new ModelInput<StringBuilder>() {
                @Override
                public boolean readTo(StringBuilder model) throws IOException {
                    if (scanner.hasNextLine()) {
                        model.setLength(0);
                        model.append(scanner.nextLine());
                        return true;
                    } else {
                        return false;
                    }
                }
                @Override
                public void close() throws IOException {
                    scanner.close();
                }
            };
        }

        @Override
        public ModelOutput<StringBuilder> createOutput(
                Class<? extends StringBuilder> dataType, String path,
                OutputStream stream) throws IOException, InterruptedException {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
            return new ModelOutput<StringBuilder>() {
                @Override
                public void write(StringBuilder model) throws IOException {
                    writer.println(model.toString());
                }
                @Override
                public void close() throws IOException {
                    writer.close();
                }
            };
        }
    }
}
