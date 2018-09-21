/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.testdriver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.core.BatchContext;
import com.asakusafw.runtime.core.Report;
import com.asakusafw.runtime.core.Report.Level;
import com.asakusafw.runtime.core.ResourceConfiguration;
import com.asakusafw.runtime.flow.RuntimeResourceManager;
import com.asakusafw.testdriver.testing.dsl.SimpleOperator;
import com.asakusafw.testdriver.testing.dsl.SimpleOperatorImpl;
import com.asakusafw.testdriver.testing.dsl.SimpleStreamFormat;
import com.asakusafw.testdriver.testing.model.Simple;

/**
 * Test for {@link OperatorTestEnvironment}.
 */
public class OperatorTestEnvironmentTest {

    /**
     * Resets all Hadoop file systems.
     */
    @Rule
    public final FileSystemCleaner fsCleaner = new FileSystemCleaner();

    /**
     * test for loading configuration file.
     * @throws Throwable if failed
     */
    @Test
    public void load() throws Throwable {
        exec(getFilePath("simple.xml"), env -> {
            assertThat(Collector.lastMessage, is("setup"));

            Report.error("hello");
            assertThat(Collector.lastLevel, is(Level.ERROR));
            assertThat(Collector.lastMessage, is("hello"));
        });
        assertThat(Collector.lastMessage, is("cleanup"));
    }

    /**
     * test for reloading user configurations.
     * @throws Throwable if failed
     */
    @Test
    public void reload() throws Throwable {
        exec(getFilePath("simple.xml"), env -> {
            assertThat(Collector.lastMessage, is("setup"));
            env.configure("testing.setup", "reload-setup");
            env.configure("testing.cleanup", "reload-cleanup");
            env.reload();
            assertThat(Collector.lastMessage, is("reload-setup"));
        });
        assertThat(Collector.lastMessage, is("reload-cleanup"));
    }

    /**
     * test for reconfigure configurations.
     * @throws Throwable if failed
     */
    @Test
    public void variable() throws Throwable {
        exec(getFilePath("simple.xml"), env -> {
            env.setBatchArg("hello", "world");
            env.reload();
            assertThat(BatchContext.get("hello"), is("world"));
        });
    }

    /**
     * get implementation of operator.
     * @throws Throwable if failed
     */
    @Test
    public void new_instance() throws Throwable {
        exec(getFilePath("simple.xml"), env -> {
            SimpleOperator op = env.newInstance(SimpleOperator.class);
            assertThat(op, is(instanceOf(SimpleOperatorImpl.class)));
        });
    }

    /**
     * get implementation of operator: pass just implementation.
     * @throws Throwable if failed
     */
    @Test
    public void new_instance_impl() throws Throwable {
        exec(getFilePath("simple.xml"), env -> {
            SimpleOperator op = env.newInstance(SimpleOperatorImpl.class);
            assertThat(op, is(instanceOf(SimpleOperatorImpl.class)));
        });
    }

    /**
     * get implementation of operator: not operator but constructible.
     * @throws Throwable if failed
     */
    @Test
    public void new_instance_not_operator() throws Throwable {
        exec(getFilePath("simple.xml"), env -> {
            String obj = env.newInstance(String.class);
            assertThat(obj, is(""));
        });
    }

    /**
     * get implementation of operator: not operator.
     * @throws Throwable if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void new_instance_not_operator_error() throws Throwable {
        exec(getFilePath("simple.xml"), env -> {
            env.newInstance(TesterTestRoot.class);
        });
    }

    /**
     * loader.
     * @throws Throwable if failed
     */
    @Test
    public void loader() throws Throwable {
        exec(getFilePath("simple.xml"), env -> {
            List<Simple> list = env.loader(Simple.class, simples("Hello, world!"))
                    .asList();
            assertThat(list, is(simples("Hello, world!")));
        });
    }

    /**
     * loader.
     * @throws Throwable if failed
     */
    @Test
    public void loader_path() throws Throwable {
        exec(getFilePath("simple.xml"), env -> {
            List<Simple> list = env.loader(Simple.class, "data/simple-in.json")
                    .asList();
            assertThat(list, is(simples("This is a test")));
        });
    }

    /**
     * loader w/ directio path.
     * @throws Throwable if failed
     */
    @Test
    public void loader_directio_path() throws Throwable {
        exec(getFilePath("simple.xml"), env -> {
            List<Simple> list = env.loader(Simple.class, SimpleStreamFormat.class, "directio/simple.txt")
                    .asList();
            assertThat(list, is(simples("Hello, world!")));
        });
    }

    /**
     * loader w/ directio file.
     * @throws Throwable if failed
     */
    @Test
    public void loader_directio_file() throws Throwable {
        File file;
        try {
            file = new File(getClass().getResource("directio/simple.txt").toURI());
        } catch (URISyntaxException e) {
            Assume.assumeNoException(e);
            throw new AssertionError(e);
        }
        exec(getFilePath("simple.xml"), env -> {
            List<Simple> list = env.loader(Simple.class, SimpleStreamFormat.class, file)
                    .asList();
            assertThat(list, is(simples("Hello, world!")));
        });
    }

    private static List<Simple> simples(String... values) {
        return Arrays.stream(values)
                .map(s -> {
                    Simple obj = new Simple();
                    obj.setValueAsString(s);
                    return obj;
                })
                .collect(Collectors.toList());
    }

    /**
     * Missing implicit configuration file.
     * @throws Exception if failed
     */
    @Test
    public void missing_implicit_configuraion() throws Exception {
        Assume.assumeThat(
                getClass().getResource(RuntimeResourceManager.CONFIGURATION_FILE_NAME),
                is(nullValue()));

        Collector.lastMessage = null;
        exec(env -> {
            // we can use Report API even if 'asakusa-resources.xml' does not exist
            Report.info("OK");
        });
    }

    /**
     * Missing explicit configuration file.
     * @throws Exception if failed
     */
    @Test
    public void missing_explicit_configuraion() throws Exception {
        OperatorTestEnvironment env = new OperatorTestEnvironment("__MISSING__.xml");
        try {
            env.before();
            try {
                fail("missing explicit configuration");
            } finally {
                env.after();
            }
        } catch (RuntimeException e) {
            // ok.
        }
    }

    private static String getFilePath(String name) {
        String className = OperatorTestEnvironmentTest.class.getName();
        int lastDot = className.lastIndexOf('.');
        assertThat(className, lastDot, greaterThanOrEqualTo(0));
        String packageName = className.substring(0, lastDot);
        return packageName.replace('.', '/') + '/' + name;
    }

    /**
     * Collects report contents.
     */
    public static final class Collector extends Report.Delegate {

        static volatile Level lastLevel;

        static volatile String lastMessage;

        @Override
        public void report(Level level, String message) throws IOException {
            lastLevel = level;
            lastMessage = message;
        }

        @Override
        public void setup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            lastLevel = Level.INFO;
            lastMessage = configuration.get("testing.setup", "setup");
        }

        @Override
        public void cleanup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            lastLevel = Level.INFO;
            lastMessage = configuration.get("testing.cleanup", "cleanup");
        }
    }

    @FunctionalInterface
    interface Action<T extends Throwable> {
        void perform(OperatorTestEnvironment env) throws T;
    }

    <T extends Throwable> void exec(Action<T> action) throws T {
        OperatorTestEnvironment env = new OperatorTestEnvironment().reset(getClass());
        env.before();
        try {
            action.perform(env);
        } finally {
            env.after();
        }
    }

    <T extends Throwable> void exec(String configPath, Action<T> action) throws T {
        OperatorTestEnvironment env = new OperatorTestEnvironment(configPath).reset(getClass());
        env.before();
        try {
            action.perform(env);
        } finally {
            env.after();
        }
    }
}
