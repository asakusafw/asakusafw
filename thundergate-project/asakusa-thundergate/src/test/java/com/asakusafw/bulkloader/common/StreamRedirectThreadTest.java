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
package com.asakusafw.bulkloader.common;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

/**
 * Test for {@link StreamRedirectThread}.
 */
public class StreamRedirectThreadTest {

    private static final byte[] BYTES = new byte[65536];
    static {
        for (int i = 0; i < BYTES.length; i++) {
            BYTES[i] = (byte) ((i >> 8) ^ i);
        }
    }

    private final TestInputStream in = new TestInputStream(BYTES);

    private final TestOutputStream out = new TestOutputStream();

    /**
     * Simple testing.
     */
    @Test(timeout = 10000)
    public void redirect() {
        StreamRedirectThread t = new StreamRedirectThread(in, out);
        t.run();
        assertThat(out.toByteArray(), is(BYTES));
        assertThat(in.read(), is(-1));
        assertThat(in.closed, is(false));
        assertThat(out.closed, is(false));
    }

    /**
     * Exit quietly on input error.
     * @throws Exception if occur
     */
    @Test(timeout = 10000)
    public void quietExitOnInputError() throws Exception {
        StreamRedirectThread t = new StreamRedirectThread(new ErroneousInputStream(65534), out);
        t.run();
    }

    /**
     * Exit quietly on output error.
     * @throws Exception if occur
     */
    @Test(timeout = 10000)
    public void quietExitOnOutputError() throws Exception {
        StreamRedirectThread t = new StreamRedirectThread(in, new ErroneousOutputStream(65534));
        t.run();
        assertThat("input stream must be consumed", in.read(), is(-1));
    }

    /**
     * Exit quietly on output error.
     * @throws Exception if occur
     */
    @Test(timeout = 10000)
    public void consumeInputOnOutputError() throws Exception {
        StreamRedirectThread t = new StreamRedirectThread(in, new ErroneousOutputStream(5));
        t.run();
        assertThat("input stream must be consumed", in.read(), is(-1));
    }

    /**
     * automatically close input.
     */
    @Test(timeout = 10000)
    public void closeInput() {
        StreamRedirectThread t = new StreamRedirectThread(in, out, true, false);
        t.run();
        assertThat(out.toByteArray(), is(BYTES));
        assertThat(in.closed, is(true));
        assertThat(out.closed, is(false));
    }

    /**
     * automatically close input.
     */
    @Test(timeout = 10000)
    public void closeOutput() {
        StreamRedirectThread t = new StreamRedirectThread(in, out, false, true);
        t.run();
        assertThat(out.toByteArray(), is(BYTES));
        assertThat(in.closed, is(false));
        assertThat(out.closed, is(true));
    }

    static class ErroneousInputStream extends InputStream {

        private volatile int rest;

        ErroneousInputStream(int rest) {
            this.rest = rest;
        }

        @Override
        public int read() throws IOException {
            if (--rest < 0) {
                throw new IOException();
            }
            return 1;
        }
    }

    static class ErroneousOutputStream extends OutputStream {

        private volatile int rest;

        ErroneousOutputStream(int rest) {
            this.rest = rest;
        }

        @Override
        public void write(int b) throws IOException {
            if (--rest < 0) {
                throw new IOException();
            }
        }
    }

    static class TestInputStream extends ByteArrayInputStream {

        boolean closed;

        TestInputStream(byte[] buf) {
            super(buf);
        }

        @Override
        public void close() throws IOException {
            this.closed = true;
        }
    }

    static class TestOutputStream extends ByteArrayOutputStream {

        boolean closed;

        @Override
        public void close() throws IOException {
            this.closed = true;
        }
    }
}
