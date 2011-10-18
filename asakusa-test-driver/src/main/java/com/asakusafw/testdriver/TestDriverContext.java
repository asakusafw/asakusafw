/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.runtime.stage.AbstractStageClient;
import com.asakusafw.testdriver.core.TestContext;

/**
 * テスト実行時のコンテキスト情報を管理する。
 * @since 0.2.0
 */
public class TestDriverContext implements TestContext {

    /**
     * Environmental variable: the framework home path.
     */
    public static final String ENV_FRAMEWORK_PATH = "ASAKUSA_HOME";

    /**
     * Path to the script to submit a stage job (relative path from {@link TestDriverContext#getFrameworkHomePath()}).
     */
    public static final String SUBMIT_JOB_SCRIPT = "experimental/bin/hadoop_job_run.sh";


    private static final String COMPILERWORK_DIR_DEFAULT = "target/testdriver/batchcwork";
    private static final String HADOOPWORK_DIR_DEFAULT = "target/testdriver/hadoopwork";

    private File frameworkHomePath;
    private final Class<?> callerClass;
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
        this.extraConfigurations = new TreeMap<String, String>();
        this.batchArgs = new TreeMap<String, String>();
        this.options = new FlowCompilerOptions();
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
     * @return the osUser
     */
    public String getOsUser() {
        String user = System.getenv("USER");
        return user;
    }

    /**
     * @return the compileWorkBaseDir
     */
    public String getCompileWorkBaseDir() {
        String dir = System.getProperty("asakusa.testdriver.compilerwork.dir");
        if (dir == null) {
            return COMPILERWORK_DIR_DEFAULT;
        }
        return dir;
    }

    /**
     * @return the clusterWorkDir
     */
    public String getClusterWorkDir() {
        String dir = System.getProperty("asakusa.testdriver.hadoopwork.dir");
        if (dir == null) {
            return HADOOPWORK_DIR_DEFAULT;
        }
        return dir;
    }

    /**
     * @return the callerClass
     */
    public Class<?> getCallerClass() {
        return callerClass;
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
                "{0}.{1}.{2}",
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
     * @return the extraConfigurations
     */
    public Map<String, String> getExtraConfigurations() {
        return extraConfigurations;
    }

    /**
     * @return the batchArgs
     */
    public Map<String, String> getBatchArgs() {
        return batchArgs;
    }

    @Override
    public Map<String, String> getArguments() {
        Map<String, String> copy = new HashMap<String, String>(getBatchArgs());
        if (currentBatchId != null) {
            copy.put(AbstractStageClient.VAR_BATCH_ID, currentBatchId);
        }
        if (currentFlowId != null) {
            copy.put(AbstractStageClient.VAR_FLOW_ID, currentFlowId);
        }
        if (currentExecutionId != null) {
            copy.put(AbstractStageClient.VAR_EXECUTION_ID, currentExecutionId);
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * @return the options
     */
    public FlowCompilerOptions getOptions() {
        return options;
    }

    @Override
    public ClassLoader getClassLoader() {
        return callerClass.getClassLoader();
    }


    /**
     * @return the currentBatchId
     */
    public String getCurrentBatchId() {
        return currentBatchId;
    }


    /**
     * @param currentBatchId the currentBatchId to set
     */
    public void setCurrentBatchId(String currentBatchId) {
        this.currentBatchId = currentBatchId;
    }


    /**
     * @return the currentFlowId
     */
    public String getCurrentFlowId() {
        return currentFlowId;
    }


    /**
     * @param currentFlowId the currentFlowId to set
     */
    public void setCurrentFlowId(String currentFlowId) {
        this.currentFlowId = currentFlowId;
    }


    /**
     * @return the currentExecutionId
     */
    public String getCurrentExecutionId() {
        return currentExecutionId;
    }


    /**
     * @param currentExecutionId the currentExecutionId to set
     */
    public void setCurrentExecutionId(String currentExecutionId) {
        this.currentExecutionId = currentExecutionId;
    }


    /**
     * @return the skipCleanInput
     */
    public boolean isSkipCleanInput() {
        return skipCleanInput;
    }


    /**
     * @param skipCleanInput the skipCleanInput to set
     */
    public void setSkipCleanInput(boolean skipCleanInput) {
        this.skipCleanInput = skipCleanInput;
    }


    /**
     * @return the skipCleanOutput
     */
    public boolean isSkipCleanOutput() {
        return skipCleanOutput;
    }


    /**
     * @param skipCleanOutput the skipCleanOutput to set
     */
    public void setSkipCleanOutput(boolean skipCleanOutput) {
        this.skipCleanOutput = skipCleanOutput;
    }


    /**
     * @return the skipPrepareInput
     */
    public boolean isSkipPrepareInput() {
        return skipPrepareInput;
    }


    /**
     * @param skipPrepareInput the skipPrepareInput to set
     */
    public void setSkipPrepareInput(boolean skipPrepareInput) {
        this.skipPrepareInput = skipPrepareInput;
    }


    /**
     * @return the skipPrepareOutput
     */
    public boolean isSkipPrepareOutput() {
        return skipPrepareOutput;
    }


    /**
     * @param skipPrepareOutput the skipPrepareOutput to set
     */
    public void setSkipPrepareOutput(boolean skipPrepareOutput) {
        this.skipPrepareOutput = skipPrepareOutput;
    }


    /**
     * @return the skipRunJobflow
     */
    public boolean isSkipRunJobflow() {
        return skipRunJobflow;
    }


    /**
     * @param skipRunJobflow the skipRunJobflow to set
     */
    public void setSkipRunJobflow(boolean skipRunJobflow) {
        this.skipRunJobflow = skipRunJobflow;
    }


    /**
     * @return the skipVerify
     */
    public boolean isSkipVerify() {
        return skipVerify;
    }


    /**
     * @param skipVerify the skipVerify to set
     */
    public void setSkipVerify(boolean skipVerify) {
        this.skipVerify = skipVerify;
    }


    /**
     * @return the envFrameworkPath
     */
    public static String getEnvFrameworkPath() {
        return ENV_FRAMEWORK_PATH;
    }


    /**
     * @return the submitJobScript
     */
    public static String getSubmitJobScript() {
        return SUBMIT_JOB_SCRIPT;
    }


    /**
     * @return the compilerworkDirDefault
     */
    public static String getCompilerworkDirDefault() {
        return COMPILERWORK_DIR_DEFAULT;
    }


    /**
     * @return the hadoopworkDirDefault
     */
    public static String getHadoopworkDirDefault() {
        return HADOOPWORK_DIR_DEFAULT;
    }


}