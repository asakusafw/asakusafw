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
package com.asakusafw.runtime.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.asakusafw.runtime.core.Report.Level;

/**
 * Test for {@link Report}.
 */
public class ReportTest {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        Report.setDelegate(null);
        Mock.levels.clear();
        Mock.messages.clear();
    }

    /**
     * does not set delegates.
     */
    @Test(expected = Report.FailedException.class)
    public void noDelegate() {
        Report.info("Hello");
    }

    /**
     * info report.
     */
    @Test
    public void info() {
        Report.setDelegate(new Mock());
        Report.info("Hello");
        assertThat(Mock.levels, is(list(Level.INFO)));
        assertThat(Mock.messages, is(list("Hello")));
    }

    /**
     * info report w/ failure.
     */
    @Test(expected = Report.FailedException.class)
    public void info_error() {
        Report.setDelegate(new Report.Delegate() {
            @Override
            public void report(Level level, String message) throws IOException {
                throw new IOException();
            }
        });
        Report.info("Hello");
    }

    /**
     * warn report.
     */
    @Test
    public void warn() {
        Report.setDelegate(new Mock());
        Report.warn("Hello");
        assertThat(Mock.levels, is(list(Level.WARN)));
        assertThat(Mock.messages, is(list("Hello")));
    }

    /**
     * warn report w/ failure.
     */
    @Test(expected = Report.FailedException.class)
    public void warn_error() {
        Report.setDelegate(new Report.Delegate() {
            @Override
            public void report(Level level, String message) throws IOException {
                throw new IOException();
            }
        });
        Report.warn("Hello");
    }

    /**
     * error report.
     */
    @Test
    public void testError() {
        Report.setDelegate(new Mock());
        Report.error("Hello");
        assertThat(Mock.levels, is(list(Level.ERROR)));
        assertThat(Mock.messages, is(list("Hello")));
    }

    /**
     * error report w/ failure.
     */
    @Test(expected = Report.FailedException.class)
    public void error_error() {
        Report.setDelegate(new Report.Delegate() {
            @Override
            public void report(Level level, String message) throws IOException {
                throw new IOException();
            }
        });
        Report.error("Hello");
    }

    /**
     * test for loading custom delegation class.
     * @throws Exception if failed
     */
    @Test
    public void initialize() throws Exception {
        ResourceConfiguration conf = new HadoopConfiguration();
        conf.set(Report.K_DELEGATE_CLASS, Mock.class.getName());
        Report.Initializer init = new Report.Initializer();
        init.setup(conf);
        Report.info("hello");
        init.cleanup(conf);

        assertThat(Mock.levels, is(list(Level.INFO)));
        assertThat(Mock.messages, is(list("hello")));
    }

    @SafeVarargs
    private static <T> List<T> list(T...values) {
        return Arrays.asList(values);
    }

    /**
     * mock for testing.
     */
    public static class Mock extends Report.Delegate {

        static final List<Level> levels = new ArrayList<>();
        static final List<String> messages = new ArrayList<>();

        @Override
        public void report(Level level, String message) throws IOException {
            levels.add(level);
            messages.add(message);
        }
    }
}
