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
package com.asakusafw.runtime.directio.api;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSource;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceProfile;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.OperatorTestEnvironment;

/**
 * Test for {@link DirectIo}.
 */
public class DirectIoTest {

    /**
     * testing environment.
     */
    @Rule
    public OperatorTestEnvironment env = new OperatorTestEnvironment();

    /**
     * temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File root = injectDataSource("testing", folder.newFolder());
        env.reload();
        put(new File(root, "testing.txt"), "Hello, world!");

        Set<String> results;
        try (ModelInput<StringBuilder> input = DirectIo.open(MockFormat.class, "testing", "*.txt")) {
            results = consume(input);
        }
        assertThat(results, is(set("Hello, world!")));
    }

    /**
     * missing files.
     * @throws Exception if failed
     */
    @Test
    public void missing() throws Exception {
        injectDataSource("testing", folder.newFolder());
        env.reload();
        Set<String> results;
        try (ModelInput<StringBuilder> input = DirectIo.open(MockFormat.class, "testing", "*.txt")) {
            results = consume(input);
        }
        assertThat(results, is(empty()));
    }

    /**
     * multiple files.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        File root = injectDataSource("testing", folder.newFolder());
        env.reload();
        put(new File(root, "t1.txt"), "Hello1");
        put(new File(root, "t2.txt"), "Hello2");
        put(new File(root, "t3.txt"), "Hello3");

        Set<String> results;
        try (ModelInput<StringBuilder> input = DirectIo.open(MockFormat.class, "testing", "*.txt")) {
            results = consume(input);
        }

        assertThat(results, is(set("Hello1", "Hello2", "Hello3")));
    }

    private File injectDataSource(String path, File mapped) {
        env.configure(qualify(), HadoopDataSource.class.getName());
        env.configure(qualify(HadoopDataSourceUtil.KEY_PATH), path);
        env.configure(qualify(HadoopDataSourceProfile.KEY_PATH), mapped.toURI().toString());
        return mapped;
    }

    private String qualify() {
        return String.format("%s%s", HadoopDataSourceUtil.PREFIX, "t");
    }

    private String qualify(String key) {
        return String.format("%s%s.%s", HadoopDataSourceUtil.PREFIX, "t", key);
    }

    private Set<String> set(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    private File put(File file, String... lines) throws IOException {
        file.getAbsoluteFile().getParentFile().mkdirs();
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            for (String line : lines) {
                writer.println(line);
            }
        }
        return file;
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
        public long getPreferredFragmentSize() {
            return -1;
        }

        @Override
        public long getMinimumFragmentSize() {
            return -1;
        }

        @Override
        public ModelInput<StringBuilder> createInput(
                Class<? extends StringBuilder> dataType, String path,
                InputStream stream, long offset, long fragmentSize)
                throws IOException, InterruptedException {
            assertThat(offset, is(0L));
            final Scanner scanner = new Scanner(stream, "UTF-8");
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
            throw new UnsupportedOperationException();
        }
    }
}
