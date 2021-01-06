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
package com.asakusafw.windgate.stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link StreamDrainDriver}.
 */
public class StreamDrainDriverTest {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File file = folder.newFile("testing");
        try (StreamDrainDriver<StringBuilder> driver = new StreamDrainDriver<>(
                "streaming",
                "testing",
                wrap(new FileOutputStreamProvider(file)),
                new StringBuilderSupport())) {
            driver.prepare();
            driver.put(new StringBuilder("Hello, world!"));
        }
        test(file, "Hello, world!");
    }

    /**
     * empty file.
     * @throws Exception if failed
     */
    @Test
    public void empty() throws Exception {
        File file = folder.newFile("testing");
        try (StreamDrainDriver<StringBuilder> driver = new StreamDrainDriver<>(
                "streaming",
                "testing",
                wrap(new FileOutputStreamProvider(file)),
                new StringBuilderSupport())) {
            driver.prepare();
        }
        test(file);
    }

    /**
     * read multiple lines.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        File file = folder.newFile("testing");

        try (StreamDrainDriver<StringBuilder> driver = new StreamDrainDriver<>(
                "streaming",
                "testing",
                wrap(new FileOutputStreamProvider(file)),
                new StringBuilderSupport())) {
            driver.prepare();
            driver.put(new StringBuilder("Hello1, world!"));
            driver.put(new StringBuilder("Hello2, world!"));
            driver.put(new StringBuilder("Hello3, world!"));
        }
        test(file, "Hello1, world!", "Hello2, world!", "Hello3, world!");
    }

    /**
     * Failed to open.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_open_fail() throws Exception {
        File file = folder.newFile("testing");
        Assume.assumeTrue(file.delete());
        Assume.assumeTrue(file.mkdirs());

        try (StreamDrainDriver<StringBuilder> driver = new StreamDrainDriver<>(
                "streaming",
                "testing",
                wrap(new FileOutputStreamProvider(file)),
                new StringBuilderSupport())) {
            driver.prepare();
            driver.put(new StringBuilder("Hello, world!"));
            fail();
        }
    }

    /**
     * But flush must raise error on failed.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_flush_failed() throws Exception {
        try (StreamDrainDriver<StringBuilder> driver = new StreamDrainDriver<>(
                "streaming",
                "testing",
                wrap(new StreamProvider<OutputStream>() {
                    @Override
                    public String getDescription() {
                        return "example";
                    }
                    @Override
                    public OutputStream open() throws IOException {
                        return new OutputStream() {
                            @Override
                            public void write(int b) throws IOException {
                                return;
                            }
                            @Override
                            public void flush() throws IOException {
                                throw new IOException();
                            }
                        };
                    }
                }),
                new StringBuilderSupport())) {
            driver.prepare();
            driver.put(new StringBuilder("Hello, world!"));
        }
    }

    private void test(File file, String... lines) throws IOException {
        List<String> actual = new ArrayList<>();
        try (Scanner scanner = new Scanner(file, "UTF-8")) {
            while (scanner.hasNextLine()) {
                actual.add(scanner.nextLine());
            }
        }
        assertThat(actual, is(Arrays.asList(lines)));
    }

    private OutputStreamProvider wrap(StreamProvider<OutputStream> provider) {
        return new MockOutputStreamProvider(provider);
    }

    private static class FileOutputStreamProvider implements StreamProvider<OutputStream> {

        private final File file;

        FileOutputStreamProvider(File file) {
            this.file = file;
        }

        @Override
        public String getDescription() {
            return file.getName();
        }

        @Override
        public OutputStream open() throws IOException {
            return new FileOutputStream(file);
        }
    }
}
