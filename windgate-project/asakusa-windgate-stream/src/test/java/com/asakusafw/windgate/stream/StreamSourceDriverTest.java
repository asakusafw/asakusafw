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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link StreamSourceDriver}.
 */
public class StreamSourceDriverTest {

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
        put(file, "Hello, world!");

        StringBuilder buf = new StringBuilder();
        try (StreamSourceDriver<StringBuilder> driver = new StreamSourceDriver<>(
                "streaming",
                "testing",
                wrap(new FileInputStreamProvider(file)),
                new StringBuilderSupport(),
                buf)) {
            driver.prepare();
            assertThat(driver.next(), is(true));
            assertThat(driver.get().toString(), is("Hello, world!"));
            assertThat(driver.next(), is(false));
        }
    }

    /**
     * empty file.
     * @throws Exception if failed
     */
    @Test
    public void empty() throws Exception {
        File file = folder.newFile("testing");
        StringBuilder buf = new StringBuilder();
        try (StreamSourceDriver<StringBuilder> driver = new StreamSourceDriver<>(
                "streaming",
                "testing",
                wrap(new FileInputStreamProvider(file)),
                new StringBuilderSupport(),
                buf)) {
            driver.prepare();
            assertThat(driver.next(), is(false));
        }
    }

    /**
     * read multiple lines.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        File file = folder.newFile("testing");
        put(file, "Hello1, world!", "Hello2, world!", "Hello3, world!");

        StringBuilder buf = new StringBuilder();
        try (StreamSourceDriver<StringBuilder> driver = new StreamSourceDriver<>(
                "streaming",
                "testing",
                wrap(new FileInputStreamProvider(file)),
                new StringBuilderSupport(),
                buf)) {
            driver.prepare();
            assertThat(driver.next(), is(true));
            assertThat(driver.get().toString(), is("Hello1, world!"));
            assertThat(driver.next(), is(true));
            assertThat(driver.get().toString(), is("Hello2, world!"));
            assertThat(driver.next(), is(true));
            assertThat(driver.get().toString(), is("Hello3, world!"));
            assertThat(driver.next(), is(false));
        }
    }

    /**
     * Failed to open.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_open_fail() throws Exception {
        File file = folder.newFile("testing");
        Assume.assumeTrue(file.delete());

        StringBuilder buf = new StringBuilder();
        try (StreamSourceDriver<StringBuilder> driver = new StreamSourceDriver<>(
                "streaming",
                "testing",
                wrap(new FileInputStreamProvider(file)),
                new StringBuilderSupport(),
                buf)) {
            driver.prepare();
            driver.next();
            fail();
        }
    }

    /**
     * suppresses error on close.
     * @throws Exception if failed
     */
    @Test
    public void suppress_close_failed() throws Exception {
        File file = folder.newFile("testing");
        put(file, "Hello, world!");

        StringBuilder buf = new StringBuilder();
        try (StreamSourceDriver<StringBuilder> driver = new StreamSourceDriver<>(
                "streaming",
                "testing",
                wrap(new StreamProvider<InputStream>() {
                    @Override
                    public String getDescription() {
                        return "example";
                    }
                    @Override
                    public InputStream open() throws IOException {
                        return new InputStream() {
                            @Override
                            public int read() throws IOException {
                                return -1;
                            }
                            @Override
                            public void close() throws IOException {
                                throw new IOException();
                            }
                        };
                    }
                }),
                new StringBuilderSupport(),
                buf)) {
            driver.prepare();
            assertThat(driver.next(), is(false));
        }
    }

    private void put(File file, String... lines) throws IOException {
        try (PrintWriter writer = new PrintWriter(file.getAbsolutePath(), "UTF-8")) {
            for (String line : lines) {
                writer.println(line);
            }
        }
    }

    private InputStreamProvider wrap(StreamProvider<InputStream> provider) {
        return new MockInputStreamProvider(provider);
    }

    private static class FileInputStreamProvider implements StreamProvider<InputStream> {

        private final File file;

        FileInputStreamProvider(File file) {
            this.file = file;
        }

        @Override
        public String getDescription() {
            return file.getName();
        }

        @Override
        public InputStream open() throws IOException {
            return new FileInputStream(file);
        }
    }
}
