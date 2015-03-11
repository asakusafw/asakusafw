/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.yaess.tools.log.cli;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import com.asakusafw.utils.io.Sink;
import com.asakusafw.utils.io.Source;
import com.asakusafw.utils.io.Sources;
import com.asakusafw.yaess.tools.log.YaessLogInput;
import com.asakusafw.yaess.tools.log.YaessLogOutput;
import com.asakusafw.yaess.tools.log.YaessLogRecord;

/**
 * Test for {@link Main}.
 */
public class MainTest {

    /**
     * Resource initializer.
     */
    @Rule
    public final ExternalResource resources = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            closeQuietly(Input.SOURCE.getAndSet(null));
            closeQuietly(Output.SINK.getAndSet(null));
        }
        @Override
        protected void after() {
            closeQuietly(Input.SOURCE.getAndSet(null));
            closeQuietly(Output.SINK.getAndSet(null));
        }
        private void closeQuietly(Object object) {
            if (object instanceof Closeable) {
                try {
                    ((Closeable) object).close();
                } catch (IOException e) {
                    throw new AssertionError();
                }
            }
        }
    };

    private final YaessLogRecord mark = new YaessLogRecord();
    {
        mark.setCode("TESTING");
    }

    /**
     * Simple smoke test.
     */
    @Test
    public void smoke() {
        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-i", Input.class.getName());
        Collections.addAll(arguments, "-I", "testing=ok");
        Collections.addAll(arguments, "-o", Output.class.getName());
        Collections.addAll(arguments, "-O", "testing=ok");

        List<YaessLogRecord> buffer = new ArrayList<YaessLogRecord>();
        Input.SOURCE.set(Sources.wrap(Arrays.asList(mark).iterator()));
        Output.SINK.set(new ListSink<YaessLogRecord>(buffer));

        int exit = Main.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(0));

        assertThat(Input.SOURCE.get(), is(nullValue()));
        assertThat(Output.SINK.get(), is(nullValue()));

        assertThat(buffer, hasSize(1));
        assertThat(buffer.get(0), is(mark));
    }

    /**
     * W/o input.
     */
    @Test
    public void wo_input() {
        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-I", "testing=ok");
        Collections.addAll(arguments, "-o", Output.class.getName());
        Collections.addAll(arguments, "-O", "testing=ok");

        int exit = Main.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(not(0)));
    }

    /**
     * W/o input class.
     */
    @Test
    public void unknown_input() {
        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-i", Input.class.getName() + "__MISSING__");
        Collections.addAll(arguments, "-I", "testing=ok");
        Collections.addAll(arguments, "-o", Output.class.getName());
        Collections.addAll(arguments, "-O", "testing=ok");

        int exit = Main.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(not(0)));
    }

    /**
     * W/o input class.
     */
    @Test
    public void invalid_input() {
        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-i", String.class.getName());
        Collections.addAll(arguments, "-I", "testing=ok");
        Collections.addAll(arguments, "-o", Output.class.getName());
        Collections.addAll(arguments, "-O", "testing=ok");

        int exit = Main.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(not(0)));
    }

    /**
     * W/o output class.
     */
    @Test
    public void unknown_output() {
        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-i", Input.class.getName());
        Collections.addAll(arguments, "-I", "testing=ok");
        Collections.addAll(arguments, "-o", Output.class.getName() + "__MISSING__");
        Collections.addAll(arguments, "-O", "testing=ok");

        int exit = Main.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(not(0)));
    }

    /**
     * W/o output class.
     */
    @Test
    public void invalid_output() {
        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-i", Input.class.getName());
        Collections.addAll(arguments, "-I", "testing=ok");
        Collections.addAll(arguments, "-o", String.class.getName());
        Collections.addAll(arguments, "-O", "testing=ok");

        int exit = Main.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(not(0)));
    }

    /**
     * W/o output.
     */
    @Test
    public void wo_output() {
        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-i", Input.class.getName());
        Collections.addAll(arguments, "-I", "testing=ok");
        Collections.addAll(arguments, "-O", "testing=ok");

        int exit = Main.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(not(0)));
    }

    /**
     * W/o input arguments.
     */
    @Test
    public void wo_input_argument() {
        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-i", Input.class.getName());
        Collections.addAll(arguments, "-o", Output.class.getName());
        Collections.addAll(arguments, "-O", "testing=ok");

        Input.SOURCE.set(Sources.wrap(Arrays.asList(mark).iterator()));
        Output.SINK.set(new ListSink<YaessLogRecord>());

        int exit = Main.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(not(0)));
    }

    /**
     * W/o output arguments.
     */
    @Test
    public void wo_output_argument() {
        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-i", Input.class.getName());
        Collections.addAll(arguments, "-I", "testing=ok");
        Collections.addAll(arguments, "-o", Output.class.getName());

        Input.SOURCE.set(Sources.wrap(Arrays.asList(mark).iterator()));
        Output.SINK.set(new ListSink<YaessLogRecord>());

        int exit = Main.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(not(0)));
    }

    /**
     * Fail on main process.
     */
    @Test
    public void fail_process() {
        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-i", Input.class.getName());
        Collections.addAll(arguments, "-I", "testing=ok");
        Collections.addAll(arguments, "-o", Output.class.getName());
        Collections.addAll(arguments, "-O", "testing=ok");

        int exit = Main.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(not(0)));
    }

    static void checkOpts(Map<String, String> options) {
        if (options.size() != 1) {
            throw new IllegalArgumentException();
        }
        if ("ok".equals(options.get("testing")) == false) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Mock input.
     */
    public static class Input implements YaessLogInput {

        static final AtomicReference<Source<? extends YaessLogRecord>> SOURCE =
                new AtomicReference<Source<? extends YaessLogRecord>>();

        @Override
        public Map<String, String> getOptionsInformation() {
            return Collections.singletonMap("testing", "ok");
        }

        @Override
        public Source<? extends YaessLogRecord> createSource(Map<String, String> options) throws IOException {
            checkOpts(options);
            Source<? extends YaessLogRecord> result = SOURCE.getAndSet(null);
            if (result == null) {
                throw new IOException();
            }
            return result;
        }
    }

    /**
     * Mock output.
     */
    public static class Output implements YaessLogOutput {

        static final AtomicReference<Sink<? super YaessLogRecord>> SINK =
                new AtomicReference<Sink<? super YaessLogRecord>>();

        @Override
        public Map<String, String> getOptionsInformation() {
            return Collections.singletonMap("testing", "ok");
        }

        @Override
        public Sink<? super YaessLogRecord> createSink(Map<String, String> options) throws IOException {
            checkOpts(options);
            Sink<? super YaessLogRecord> result = SINK.getAndSet(null);
            if (result == null) {
                throw new IOException();
            }
            return result;
        }
    }
}
