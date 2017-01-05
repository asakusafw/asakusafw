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
package com.asakusafw.utils.io;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import org.junit.Test;

/**
 * Test for {@link ReaderProvider}.
 */
public class ReaderProviderTest {

    static final Charset CHARSET = StandardCharsets.UTF_8;

    private final List<Object> closeables = new ArrayList<>();

    /**
     * Close all.
     * @throws Exception if failed
     */
    @After
    public void after() throws Exception {
        for (Object c : closeables) {
            if (c instanceof Closeable) {
                ((Closeable) c).close();
            }
        }
    }

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void test() throws Exception {
        Provider<InputStream> ip = new Provider<InputStream>() {
            @Override
            public InputStream open() throws IOException, InterruptedException {
                return new ByteArrayInputStream("Hello, world!".getBytes(CHARSET));
            }
            @Override
            public void close() throws IOException {
                return;
            }
        };
        closeables.add(ip);

        ReaderProvider rp = new ReaderProvider(ip, CHARSET);
        closeables.add(rp);

        Scanner s1 = new Scanner(rp.open());
        closeables.add(s1);

        assertThat(s1.hasNextLine(), is(true));
        assertThat(s1.nextLine(), is("Hello, world!"));

        assertThat(s1.hasNextLine(), is(false));

        Scanner s2 = new Scanner(rp.open());
        closeables.add(s2);

        assertThat(s2.hasNextLine(), is(true));
        assertThat(s2.nextLine(), is("Hello, world!"));

        assertThat(s2.hasNextLine(), is(false));
    }
}
