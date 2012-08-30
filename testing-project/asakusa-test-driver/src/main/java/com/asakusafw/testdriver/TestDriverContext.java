/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowCompilerOptions.GenericOptionValue;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.TestToolRepository;
import com.asakusafw.utils.collections.Maps;

/**
 * テスト実行時のコンテキスト情報を管理する。
 * @since 0.2.0
 */
public class TestDriverContext implements TestContext {

    static final Logger LOG = LoggerFactory.getLogger(TestDriverContext.class);

    /**
     * The system property key of runtime workig directory.
     * This working directory must be a relative path from the default working directory.
     */
    public static final String KEY_RUNTIME_WORKING_DIRECTORY = "asakusa.testdriver.hadoopwork.dir";

    /**
     * The system property key of compiler working directory.
     */
    public static final String KEY_COMPILER_WORKING_DIRECTORY = "asakusa.testdriver.compilerwork.dir";

    /**
     * Environmental variable: the framework home path.
     */
    public static final String ENV_FRAMEWORK_PATH = "ASAKUSA_HOME";

    private static final String COMPILERWORK_DIR_DEFAULT = "target/testdriver/batchcwork";
    private static final String HADOOPWORK_DIR_DEFAULT = "target/testdriver/hadoopwork";

    private volatile File frameworkHomePath;
    private final Class<?> callerClass;
    private final TestToolRepository repository;
    private final Map<String, String> extraConfigurations;
    private final Map<String, String> batchArgs;
    private final FlowCompilerOptions options;

    private volatile String currentBatchId;

    private volatile String currentFlowId;

    private volatile String currentExecutionId;

    private boolean skipCleanInput;
    private boolean skipCleanOutput;
    private boolean skipPrepareInput;
    private boolean skipPrepareOutput;
    private boolean skipRunJobflow;
    private boolean skipVerify;

    /**
     * Creates a new instance.
     * @param contextClass context class (will use to detect test resources)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestDriverContext(Class<?> contextClass) {
        if (contextClass == null) {
            throw new IllegalArgumentException("contextClass must not be null"); //$NON-NLS-1$
        }
        this.callerClass = contextClass;
        this.repository = new TestToolRepository(contextClass.getClassLoader());
        this.extraConfigurations = new TreeMap<String, String>();
        this.batchArgs = new TreeMap<String, String>();
        this.options = new FlowCompilerOptions();
        configureOptions();
    }

    private void configureOptions() {
        LOG.debug("Auto detecting current execution environment");
        this.options.putExtraAttribute(
                "MAPREDUCE-370",
                GenericOptionValue.AUTO.getSymbol());
    }

    /**
     * Sets the path to the framework installed location.
     * @param frameworkHomePath the path to the framework install location, or {@code null} to reset location
     */
    public void setFrameworkHomePath(File frameworkHomePath) {
        this.frameworkHomePath = frameworkHomePath;
    }

    /**
     * Returns the framework home path.
     * @return the path, or default path from environmental variable {@code ASAKUSA_HOME}
     * @throws IllegalStateException if neither the framework home path nor the environmental variable were set
     */
    public File getFrameworkHomePath() {
        if (frameworkHomePath == null) {
            String defaultHomePath = System.getenv(ENV_FRAMEWORK_PATH);
            if (defaultHomePath == null) {
                throw new IllegalStateException(MessageFormat.format(
                        "環境変数{0}が未設定です",
                        ENV_FRAMEWORK_PATH));
            }
            return new File(defaultHomePath);
        }
        return frameworkHomePath;
    }

    /**
     * Returns the path to the jobflow package (*.jar) deployment directory.
     * This method refers the {@link #getFrameworkHomePath() framework installed location}.
     * @param batchId target batch ID
     * @return the path
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #setFrameworkHomePath(File)
     */
    public File getJobflowPackageLocation(String batchId) {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        File apps = new File(getFrameworkHomePath(), "batchapps");
        File batch = new File(apps, batchId);
        File lib = new File(batch, "lib");
        return lib;
    }

    /**
     * Returns the command context for this attempt.
     * @return the command context
     */
    public CommandContext getCommandContext() {
        CommandContext context = new CommandContext(
                getFrameworkHomePath().getAbsolutePath() + "/",
                getExecutionId(),
                getBatchArgs());
        return context;
    }

    /**
     * Returns the compiler working directory.
     * @return the compiler working directory
     */
    public File getCompilerWorkingDirectory() {
        return new File(getCompileWorkBaseDir());
    }

    /**
     * Returns the current user name in OS.
     * @return the current user name
     */
    public String getOsUser() {
        String user = System.getenv("USER");
        return user;
    }

    /**
     * Returns the path to the compiler working directory.
     * Clients can configure this property using system property {@value #KEY_COMPILER_WORKING_DIRECTORY}.
     * @return the compiler working directory
     */
    public String getCompileWorkBaseDir() {
        String dir = System.getProperty(KEY_COMPILER_WORKING_DIRECTORY);
        if (dir == null) {
            return COMPILERWORK_DIR_DEFAULT;
        }
        return dir;
    }

    /**
     * Returns the path to the runtime working directory.
     * This working directory is relative path from cluster's default working directory.
     * Clients can configure this property using system property {@value #KEY_RUNTIME_WORKING_DIRECTORY}.
     * @return the compiler working directory
     */
    public String getClusterWorkDir() {
        String dir = System.getProperty(KEY_RUNTIME_WORKING_DIRECTORY);
        if (dir == null) {
            return HADOOPWORK_DIR_DEFAULT;
        }
        return dir;
    }

    /**
     * Returns the caller class.
     * This is ordinally used for detect test dataset on the classpath.
     * @return the caller class
     */
    public Class<?> getCallerClass() {
        return callerClass;
    }

    /**
     * Returns the test tool repository.
     * @return the repository
     * @since 0.2.3
     */
    public TestToolRepository getRepository() {
        return repository;
    }

    /**
     * Changes current Jobflow.
     * @param info target jobflow
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.2
     */
    public void prepareCurrentJobflow(JobflowInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("info must not be null"); //$NON-NLS-1$
        }
        this.currentBatchId = info.getJobflow().getBatchId();
        this.currentFlowId = info.getJobflow().getFlowId();
        this.currentExecutionId = MessageFormat.format(
                "{0}-{1}-{2}",
                getCallerClass().getSimpleName(),
                currentBatchId,
                currentFlowId);
    }

    /**
     * Returns the current execution ID.
     * @return current execution ID
     * @see #prepareCurrentJobflow(JobflowInfo)
     */
    public String getExecutionId() {
        if (currentExecutionId == null) {
            throw new IllegalStateException("prepareCurrentJobflow was not invoked");
        }
        return currentExecutionId;
    }

    /**
     * Change current {@link #getExecutionId() execution ID} into a unique string.
     * @deprecated Please invoke {@link #prepareCurrentJobflow(JobflowInfo)}
     */
    @Deprecated
    public void changeExecutionId() {
        // do nothing
    }

    /**
     * Returns extra configurations for the runtime.
     * For Hadoop, these configurations are passed using {@code -D <key>=<value>}.
     * @return the extra configurations (key value pairs)
     */
    public Map<String, String> getExtraConfigurations() {
        return extraConfigurations;
    }

    /**
     * Returns the batch arguments.
     * @return the batch arguments
     */
    public Map<String, String> getBatchArgs() {
        return batchArgs;
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return System.getenv();
    }

    @Override
    public Map<String, String> getArguments() {
        Map<String, String> copy = Maps.from(getBatchArgs());
        if (currentBatchId != null) {
            copy.put(StageConstants.VAR_BATCH_ID, currentBatchId);
        }
        if (currentFlowId != null) {
            copy.put(StageConstants.VAR_FLOW_ID, currentFlowId);
        }
        if (currentExecutionId != null) {
            copy.put(StageConstants.VAR_EXECUTION_ID, currentExecutionId);
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Returns the compiler options.
     * @return the compiler options
     */
    public FlowCompilerOptions getOptions() {
        return options;
    }

    @Override
    public ClassLoader getClassLoader() {
        return callerClass.getClassLoader();
    }


    /**
     * Returns the current batch ID.
     * @return the current batch ID, or {@code null} if not set
     * @see #setCurrentBatchId(String)
     */
    public String getCurrentBatchId() {
        return currentBatchId;
    }


    /**
     * Configures the current batch ID.
     * @param currentBatchId the ID
     */
    public void setCurrentBatchId(String currentBatchId) {
        this.currentBatchId = currentBatchId;
    }


    /**
     * Returns the current flow ID.
     * @return the ID, or {@code null} if not set
     * @see #setCurrentFlowId(String)
     */
    public String getCurrentFlowId() {
        return currentFlowId;
    }


    /**
     * Configures the current flow ID.
     * @param currentFlowId the ID
     */
    public void setCurrentFlowId(String currentFlowId) {
        this.currentFlowId = currentFlowId;
    }


    /**
     * Returns the current execution ID.
     * @return the ID, or {@code null} if not set
     * @see #setCurrentExecutionId(String)
     */
    public String getCurrentExecutionId() {
        return currentExecutionId;
    }


    /**
     * Returns the current execution ID.
     * @param currentExecutionId the ID
     */
    public void setCurrentExecutionId(String currentExecutionId) {
        this.currentExecutionId = currentExecutionId;
    }


    /**
     * Returns whether this test skips to cleanup input data source.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipCleanInput() {
        return skipCleanInput;
    }


    /**
     * Sets whether this test skips to cleanup input data source (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipCleanInput(boolean skip) {
        this.skipCleanInput = skip;
    }


    /**
     * Returns whether this test skips to cleanup input data source.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipCleanOutput() {
        return skipCleanOutput;
    }


    /**
     * Sets whether this test skips to cleanup output data source (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipCleanOutput(boolean skip) {
        this.skipCleanOutput = skip;
    }


    /**
     * Returns whether this test skips to cleanup input data source.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipPrepareInput() {
        return skipPrepareInput;
    }


    /**
     * Sets whether this test skips to prepare input data source (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipPrepareInput(boolean skip) {
        this.skipPrepareInput = skip;
    }


    /**
     * Returns whether this test skips to prepare output data source.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipPrepareOutput() {
        return skipPrepareOutput;
    }


    /**
     * Sets whether this test skips to prepare output data source (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipPrepareOutput(boolean skip) {
        this.skipPrepareOutput = skip;
    }


    /**
     * Returns whether this test skips to execute jobflows.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipRunJobflow() {
        return skipRunJobflow;
    }


    /**
     * Sets whether this test skips to execute jobflows (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipRunJobflow(boolean skip) {
        this.skipRunJobflow = skip;
    }


    /**
     * Returns whether this test skips to verify the testing result.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipVerify() {
        return skipVerify;
    }


    /**
     * Sets whether this test skips to verify the testing result (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipVerify(boolean skip) {
        this.skipVerify = skip;
    }
}