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
package com.asakusafw.testdriver.inprocess;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hadoop.conf.Configuration;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.configuration.FrameworkDeployer;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.model.HadoopTaskInfo;
import com.asakusafw.workflow.model.basic.BasicHadoopTaskInfo;

/**
 * Test for {@link InProcessHadoopTaskExecutor}.
 */
public class InProcessHadoopTaskExecutorTest {

    /**
     * The framework configurator.
     */
    @Rule
    public final FrameworkDeployer framework = new FrameworkDeployer(false) {
        @Override
        protected void deploy() throws Throwable {
            Map<String, String> env = new LinkedHashMap<>(System.getenv());
            env.put(TaskExecutors.ENV_FRAMEWORK_PATH, getHome().getAbsolutePath());
            env.remove(TaskExecutors.ENV_BATCHAPPS_PATH);

            context = new Mock();
            context.env.putAll(env);
        }
    };

    Mock context;

    private final TaskExecutor executor = new InProcessHadoopTaskExecutor();

    /**
     * Test method for executing Hadoop job.
     */
    @Test
    public void execute_simple() {
        prepareJobflow();
        AtomicBoolean call = new AtomicBoolean();
        MockHadoopJob.callback((args, conf) -> {
            call.set(true);
            return 0;
        });
        try {
            executor.execute(context, task(MockHadoopJob.class.getName()));
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
        assertThat(call.get(), is(true));
    }

    /**
     * Test method for executing Hadoop job w/ properties.
     */
    @Test
    public void execute_configuration() {
        prepareJobflow();
        AtomicBoolean call = new AtomicBoolean();
        MockHadoopJob.callback((args, conf) -> {
            call.set(true);
            assertThat(conf.get("com.example.testing"), is("true"));
            return 0;
        });

        context.conf.put("com.example.testing", "true");
        try {
            executor.execute(context, task(MockHadoopJob.class.getName()));
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
        assertThat(call.get(), is(true));
    }

    /**
     * Test method for executing Hadoop job w/ {@code asakusa-resources.xml}.
     */
    @Test
    public void execute_resources() {
        prepareJobflow();
        AtomicBoolean call = new AtomicBoolean();
        MockHadoopJob.callback((args, conf) -> {
            call.set(true);
            assertThat(conf.get("com.example.testing"), is("true"));
            return 0;
        });
        deploy("dummy.xml", TaskExecutors.findCoreConfigurationFile(context).get());
        try {
            executor.execute(context, task(MockHadoopJob.class.getName()));
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
        assertThat(call.get(), is(true));
    }

    /**
     * Test method for executing Hadoop job with non-zero exit value.
     */
    @Test
    public void execute_nonzero() {
        prepareJobflow();
        MockHadoopJob.callback((args, conf) -> 1);
        try {
            executor.execute(context, task(MockHadoopJob.class.getName()));
        } catch (IOException e) {
            // ok.
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Test method for executing Hadoop job with errors.
     */
    @Test
    public void execute_error() {
        prepareJobflow();
        MockHadoopJob.callback((args, conf) -> {
            throw new IOException();
        });
        try {
            executor.execute(context, task(MockHadoopJob.class.getName()));
        } catch (IOException e) {
            // ok
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private static HadoopTaskInfo task(String className) {
        return new BasicHadoopTaskInfo(className);
    }

    private void prepareJobflow() {
        deploy("dummy.jar", TaskExecutors.findJobflowLibrary(context).get());
    }

    private void deploy(String source, Path dest) {
        try (InputStream in = getClass().getResourceAsStream(source)) {
            assertThat(in, is(notNullValue()));
            framework.dump(in, dest.toFile());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static class Mock implements TaskExecutionContext {

        final Map<String, String> conf = new LinkedHashMap<>();

        final Map<String, String> env = new LinkedHashMap<>();

        final Map<String, String> args = new LinkedHashMap<>();

        Mock() {
            return;
        }

        @Override
        public ClassLoader getClassLoader() {
            return getClass().getClassLoader();
        }

        @Override
        public Map<String, String> getConfigurations() {
            return conf;
        }

        @Override
        public Map<String, String> getEnvironmentVariables() {
            return env;
        }

        @Override
        public Map<String, String> getBatchArguments() {
            return args;
        }

        @Override
        public <T> Optional<T> findResource(Class<T> type) {
            if (type == Configuration.class) {
                return Optional.of(type.cast(new Configuration()));
            }
            return null;
        }

        @Override
        public String getBatchId() {
            return "b";
        }

        @Override
        public String getFlowId() {
            return "f";
        }

        @Override
        public String getExecutionId() {
            return "e";
        }
    }
}
