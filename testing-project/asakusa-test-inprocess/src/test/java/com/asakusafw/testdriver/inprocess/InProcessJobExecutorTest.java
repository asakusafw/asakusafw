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
package com.asakusafw.testdriver.inprocess;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.SystemUtils;
import org.apache.hadoop.conf.Configuration;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import com.asakusafw.runtime.configuration.FrameworkDeployer;
import com.asakusafw.testdriver.JobExecutor;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.testdriver.inprocess.MockHadoopJob.Callback;

/**
 * Test for {@link InProcessJobExecutor}.
 */
public class InProcessJobExecutorTest {

    static final String DUMMY_FRAMEWORK_VERSION = "0.0.0.testing";

    static final String FLOW_ID = "flow";

    static final String EXECUTION_ID = "exec";

    /**
     * The framework configurator.
     */
    @Rule
    public final FrameworkDeployer framework = new FrameworkDeployer(false) {
        @Override
        protected void deploy() throws Throwable {
            context =  new TestDriverContext(InProcessJobExecutorTest.class) {
                @Override
                public String getDevelopmentEnvironmentVersion() {
                    return DUMMY_FRAMEWORK_VERSION;
                }
            };
            context.useSystemBatchApplicationsInstallationPath(true);
            context.setFrameworkHomePath(getHome());
            context.setBatchApplicationsInstallationPath(
                    new File(getHome(), TestDriverContext.DEFAULT_BATCHAPPS_PATH));
        }
    };

    /**
     * Disposes objects.
     */
    @Rule
    public final ExternalResource disposer = new ExternalResource() {
        @Override
        protected void after() {
            if (context != null) {
                context.cleanUpTemporaryResources();
            }
        }
    };

    TestDriverContext context;

    /**
     * Without runtime framework version info.
     */
    @Test
    public void validateEnvironment_wo_runtime_version() {
        JobExecutor executor = new InProcessJobExecutor(context);
        executor.validateEnvironment();
    }

    /**
     * With valid runtime framework version info.
     */
    @Test
    public void validateEnvironment_w_valid_runtime_version() {
        Properties props = new Properties();
        props.setProperty(TestDriverContext.KEY_FRAMEWORK_VERSION, DUMMY_FRAMEWORK_VERSION);
        putPropertiesFile(props, new File(framework.getHome(), TestDriverContext.FRAMEWORK_VERSION_PATH));

        JobExecutor executor = new InProcessJobExecutor(context);
        executor.validateEnvironment();
    }

    /**
     * With valid runtime framework version info.
     */
    @Test
    public void validateEnvironment_w_invalid_runtime_version() {
        Properties props = new Properties();
        props.setProperty(TestDriverContext.KEY_FRAMEWORK_VERSION, "INVALID");
        putPropertiesFile(props, new File(framework.getHome(), TestDriverContext.FRAMEWORK_VERSION_PATH));
        try {
            JobExecutor executor = new InProcessJobExecutor(context);
            executor.validateEnvironment();
            throw new IllegalStateException();
        } catch (AssertionError e) {
            // ok.
        } catch (IllegalStateException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Validates empty plan.
     */
    @Test
    public void validatePlan_empty() {
        MockTestExecutionPlanBuilder builder = builder();

        JobExecutor executor = new InProcessJobExecutor(context, emptyConfigurations());
        executor.validatePlan(builder.build());
        // OK.
    }

    /**
     * Validates Hadoop jobs only plan.
     */
    @Test
    public void validatePlan_hadoop() {
        MockTestExecutionPlanBuilder builder = builder();
        builder.addHadoopJob(job("com.example.Dummy"));

        JobExecutor executor = new InProcessJobExecutor(context, emptyConfigurations());
        executor.validatePlan(builder.build());
        // OK.
    }

    /**
     * Validates plan with not-emulated command w/o hadoop command.
     */
    @Test
    public void validatePlan_direct_command_wo_hadoop() {
        MockTestExecutionPlanBuilder builder = builder();
        builder.addImporter(command("unknown", "unknown"));

        JobExecutor executor = new InProcessJobExecutor(context, emptyConfigurations());
        try {
            executor.validatePlan(builder.build());
            throw new IllegalStateException();
        } catch (AssertionError e) {
            // ok.
        } catch (IllegalStateException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Validates plan with emulated command w/o hadoop command.
     */
    @Test
    public void validatePlan_emulated_command_wo_hadoop() {
        MockTestExecutionPlanBuilder builder = builder();
        builder.addImporter(command("mock", "mock"));

        JobExecutor executor = new InProcessJobExecutor(context, emptyConfigurations());
        executor.validatePlan(builder.build());
    }

    /**
     * Test method for executing Hadoop job.
     */
    @Test
    public void executeJob_simple() {
        prepareJobflow();
        final AtomicBoolean call = new AtomicBoolean();
        MockHadoopJob.callback(new MockHadoopJob.Callback() {
            @Override
            public int run(String[] args, Configuration conf) {
                call.set(true);
                return 0;
            }
        });

        JobExecutor executor = new InProcessJobExecutor(context);
        try {
            executor.execute(job(MockHadoopJob.class.getName()), Collections.<String, String>emptyMap());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertThat(call.get(), is(true));
    }

    /**
     * Test method for executing Hadoop job w/ properties.
     */
    @Test
    public void executeJob_w_properties() {
        prepareJobflow();
        final AtomicBoolean call = new AtomicBoolean();
        MockHadoopJob.callback(new MockHadoopJob.Callback() {
            @Override
            public int run(String[] args, Configuration conf) {
                call.set(true);
                assertThat(conf.get("com.example.testing"), is("true"));
                return 0;
            }
        });

        TestExecutionPlan.Job job = job(MockHadoopJob.class.getName(), "com.example.testing", "true");

        JobExecutor executor = new InProcessJobExecutor(context);
        try {
            executor.execute(job, Collections.<String, String>emptyMap());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertThat(call.get(), is(true));
    }

    /**
     * Test method for executing Hadoop job w/ {@code asakusa-resources.xml}.
     */
    @Test
    public void executeJob_w_resources() {
        prepareJobflow();
        final AtomicBoolean call = new AtomicBoolean();
        MockHadoopJob.callback(new MockHadoopJob.Callback() {
            @Override
            public int run(String[] args, Configuration conf) {
                call.set(true);
                assertThat(conf.get("com.example.testing"), is("true"));
                return 0;
            }
        });

        JobExecutor executor = new InProcessJobExecutor(context);
        deploy("dummy.xml", new File(framework.getHome(), InProcessJobExecutor.PATH_ASAKUSA_RESOURCES));
        try {
            executor.execute(job(MockHadoopJob.class.getName()), Collections.<String, String>emptyMap());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertThat(call.get(), is(true));
    }

    /**
     * Test method for executing Hadoop job with non-zero exit value.
     */
    @Test
    public void executeJob_nonzero() {
        prepareJobflow();
        MockHadoopJob.callback(new MockHadoopJob.Callback() {
            @Override
            public int run(String[] args, Configuration conf) {
                return 1;
            }
        });

        JobExecutor executor = new InProcessJobExecutor(context);
        try {
            executor.execute(job(MockHadoopJob.class.getName()), Collections.<String, String>emptyMap());
            throw new IOException();
        } catch (IOException e) {
            throw new AssertionError(e);
        } catch (AssertionError e) {
            // ok.
        }
    }

    /**
     * Test method for executing Hadoop job with errors.
     */
    @Test
    public void executeJob_error() {
        prepareJobflow();
        MockHadoopJob.callback(new Callback() {
            @Override
            public int run(String[] args, Configuration conf) throws Exception {
                throw new InterruptedException();
            }
        });

        JobExecutor executor = new InProcessJobExecutor(context);
        try {
            executor.execute(job(MockHadoopJob.class.getName()), Collections.<String, String>emptyMap());
            throw new IOException();
        } catch (IOException e) {
            throw new AssertionError(e);
        } catch (AssertionError e) {
            // ok.
        }
    }

    /**
     * Test method for executing emulated command.
     */
    @Test
    public void executeCommand_simple() {
        final AtomicBoolean call = new AtomicBoolean();
        MockCommandEmulator.callback(new MockCommandEmulator.Callback() {
            @Override
            public void run(List<String> args) throws IOException, InterruptedException {
                call.set(true);
                assertThat(args, contains("hello", "world"));
            }
        });
        JobExecutor executor = new InProcessJobExecutor(context);
        try {
            executor.execute(command("mock", "hello", "world"), Collections.<String, String>emptyMap());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertThat(call.get(), is(true));
    }

    /**
     * Test method for executing emulated command.
     */
    @Test
    public void executeCommand_error() {
        MockCommandEmulator.callback(new MockCommandEmulator.Callback() {
            @Override
            public void run(List<String> args) throws IOException, InterruptedException {
                throw new IOException();
            }
        });
        JobExecutor executor = new InProcessJobExecutor(context);
        try {
            executor.execute(command("mock"), Collections.<String, String>emptyMap());
            throw new IllegalStateException();
        } catch (IOException e) {
            // ok.
        } catch (AssertionError e) {
            // ok.
        }
    }

    /**
     * Test method for executing emulated command.
     */
    @Test
    public void executeCommand_interrupt() {
        MockCommandEmulator.callback(new MockCommandEmulator.Callback() {
            @Override
            public void run(List<String> args) throws IOException, InterruptedException {
                throw new InterruptedException();
            }
        });
        JobExecutor executor = new InProcessJobExecutor(context);
        try {
            executor.execute(command("mock"), Collections.<String, String>emptyMap());
            throw new IllegalStateException();
        } catch (IOException e) {
            // ok.
        } catch (AssertionError e) {
            // ok.
        }
    }

    /**
     * Test method for executing non-emulated command.
     */
    @Test
    public void executeCommand_delegate() {
        Assume.assumeTrue("not unix-like", SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_LINUX);

        File touch = new File("/usr/bin/touch");
        Assume.assumeTrue("no 'touch' command", touch.isFile() && touch.canExecute());

        final AtomicBoolean call = new AtomicBoolean();
        MockCommandEmulator.callback(new MockCommandEmulator.Callback() {
            @Override
            public void run(List<String> args) throws IOException, InterruptedException {
                call.set(true);
            }
        });
        File target = new File(framework.getWork("working"), "target");
        Assume.assumeFalse(target.exists());

        // exec: touch .../target
        TestExecutionPlan.Command command = command("generic", touch.getPath(), target.getAbsolutePath());
        JobExecutor executor = new InProcessJobExecutor(context);
        try {
            executor.execute(command, Collections.<String, String>emptyMap());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertThat(target.exists(), is(true));
        assertThat(call.get(), is(false));
    }

    private ConfigurationFactory emptyConfigurations() {
        ConfigurationFactory.Preferences prefs = new ConfigurationFactory.Preferences();
        prefs.getEnvironmentVariables().clear();
        return new ConfigurationFactory(prefs);
    }

    private MockTestExecutionPlanBuilder builder() {
        return new MockTestExecutionPlanBuilder(FLOW_ID, EXECUTION_ID);
    }

    private TestExecutionPlan.Job job(String className, String... properties) {
        Map<String, String> props = new HashMap<String, String>();
        for (int i = 0, n = properties.length; i < n; i+=2) {
            props.put(properties[i], properties[i + 1]);
        }
        return new TestExecutionPlan.Job(className, EXECUTION_ID, props);
    }

    private TestExecutionPlan.Command command(String moduleName, String... commandLine) {
        return new TestExecutionPlan.Command(
                Arrays.asList(commandLine), moduleName, null, Collections.<String, String>emptyMap());
    }

    private void putPropertiesFile(Properties props, File file) {
        try {
            file.getParentFile().mkdirs();
            OutputStream output = new FileOutputStream(file);
            try {
                props.store(output, null);
            } finally {
                output.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void prepareJobflow() {
        context.setCurrentBatchId("testing");
        context.setCurrentFlowId(FLOW_ID);
        context.setCurrentExecutionId(EXECUTION_ID);
        deploy("dummy.jar", EmulatorUtils.getJobflowLibraryPath(context));
    }

    private void deploy(String source, File dest) {
        InputStream in = getClass().getResourceAsStream(source);
        assertThat(in, is(notNullValue()));
        try {
            try {
                framework.dump(in, dest);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
