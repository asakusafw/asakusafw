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
package com.asakusafw.testdriver.windgate.inprocess;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.junit.Rule;

import com.asakusafw.runtime.configuration.FrameworkDeployer;
import com.asakusafw.vocabulary.windgate.Constants;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.model.CommandTaskInfo;
import com.asakusafw.workflow.model.CommandToken;
import com.asakusafw.workflow.model.basic.BasicCommandTaskInfo;

/**
 * Common base class of WindGate in-process task executors.
 */
public abstract class InProcessWindGateTaskExecutorTestRoot {

    /**
     * The default profile name for testing.
     */
    public static final String PROFILE = "testing";

    /**
     * Environment name of testing base directory.
     */
    public static final String ENV_TEST_BASE_DIR = "TEST_TMP";

    /**
     * The framework configurator.
     */
    @Rule
    public final FrameworkDeployer framework = new FrameworkDeployer(false) {
        @Override
        protected void deploy() throws Throwable {
            copy(new File("src/test/dist"), getHome());

            Map<String, String> env = new LinkedHashMap<>(System.getenv());
            env.put(TaskExecutors.ENV_FRAMEWORK_PATH, getHome().getAbsolutePath());
            env.remove(TaskExecutors.ENV_BATCHAPPS_PATH);
            env.put(ENV_TEST_BASE_DIR, getWork("windgate").getAbsolutePath());

            context = new Mock();
            context.env.putAll(env);
        }
    };

    Mock context;

    /**
     * Returns the context.
     * @return the context
     */
    public TaskExecutionContext getContext() {
        return context;
    }

    /**
     * Returns the working directory.
     * @return the working directory
     */
    public Path getWorkingDir() {
        return Paths.get(context.getEnvironmentVariables().get(ENV_TEST_BASE_DIR), "file");
    }

    /**
     * Returns a WindGate related command.
     * @param path the command path from the framework home
     * @param args the command arguments
     * @return the built command
     */
    public static CommandTaskInfo command(String path, String... args) {
        return new BasicCommandTaskInfo(
                Constants.MODULE_NAME,
                PROFILE,
                path,
                Arrays.stream(args).map(CommandToken::of).collect(Collectors.toList()));
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
