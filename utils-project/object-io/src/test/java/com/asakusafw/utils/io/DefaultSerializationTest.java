/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import java.io.ByteArrayOutputStream;

import org.junit.Test;

/**
 * Test for {@link DefaultSerialization}.
 */
public class DefaultSerializationTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        DefaultSerialization<String> ser = new DefaultSerialization<String>();
        Sink<String> sink = ser.createSink(output);
        try {
            sink.put("Hello, world!");
        } finally {
            sink.close();
        }

        Source<String> source = ser.createSource(flip());
        try {
            assertThat(source.next(), is(true));
            assertThat(source.get(), is("Hello, world!"));
            assertThat(source.next(), is(false));
        } finally {
            source.close();
        }
    }

    /**
     * Put nothing.
     * @throws Exception if failed
     */
    @Test
    public void empty() throws Exception {
        DefaultSerialization<String> ser = new DefaultSerialization<String>();
        Sink<String> sink = ser.createSink(output);
        sink.close();

        Source<String> source = ser.createSource(flip());
        try {
            assertThat(source.next(), is(false));
        } finally {
            source.close();
        }
    }

    /**
     * Puts multiple objects.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        DefaultSerialization<String> ser = new DefaultSerialization<String>();
        Sink<String> sink = ser.createSink(output);
        try {
            sink.put("Hello1");
            sink.put("Hello2");
            sink.put("Hello3");
        } finally {
            sink.close();
        }

        Source<String> source = ser.createSource(flip());
        try {
            assertThat(source.next(), is(true));
            assertThat(source.get(), is("Hello1"));
            assertThat(source.next(), is(true));
            assertThat(source.get(), is("Hello2"));
            assertThat(source.next(), is(true));
            assertThat(source.get(), is("Hello3"));
            assertThat(source.next(), is(false));
        } finally {
            source.close();
        }
    }

    private ByteArrayInputStream flip() {
        return new ByteArrayInputStream(output.toByteArray());
    }
}
