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
import java.util.Map;
import java.util.TreeMap;

import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;

/**
 * テスト実行時のコンテキスト情報を管理する。
 * @since 0.2.0
 */
public class TestDriverContext {

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

    private int executionCount;

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
     * Returns the current execution ID.
     * @return current execution ID
     * @see #changeExecutionId()
     */
    public String getExecutionId() {
        return String.format("%s_%d", getCallerClass().getSimpleName(), executionCount);
    }

    /**
     * Change current {@link #getExecutionId() execution ID} into a unique string.
     */
    public void changeExecutionId() {
        executionCount++;
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

    /**
     * @return the options
     */
    public FlowCompilerOptions getOptions() {
        return options;
    }
}