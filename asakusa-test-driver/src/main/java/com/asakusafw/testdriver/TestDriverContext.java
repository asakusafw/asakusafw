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


    private File frameworkHomePath;
    /** OSのユーザ名。 */
    private String osUser;
    /** AshigelCompilerのローカルワークディレクトリ。 */
    private String compileWorkBaseDir;
    /** Hadoopのワークディレクトリ。 */
    private String clusterWorkDir;
    /** テストクラスのクラス名。 */
    private String className;
    /** テストクラスのメソッド名。 */
    private String methodName;
    /** テストクラスの呼出元クラス。 */
    private Class<?> callerClass;

    /** ジョブフローの実行ID。 (テストドライバでダミーの値をセットする)*/
    private String executionId;
    /** 実行時の追加設定一覧 (Property Name, Property Value)。*/
    private Map<String, String> extraConfigurations;
    /** バッチ実行時引数 (ASAKUSA_BATCH_ARGS)。*/
    private Map<String, String> batchArgs;
    /** コンパイラオプション。*/
    private FlowCompilerOptions options;

    /**
     * Creates a new instance.
     */
    public TestDriverContext() {
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
     * @see #setCompileWorkBaseDir(String)
     * @see #setClassName(String)
     * @see #setMethodName(String)
     */
    public File getCompilerWorkingDirectory() {
        File work;
        File base = new File(getCompileWorkBaseDir());
        if (getClassName() != null) {
            String sub = getClassName();
            sub = sub.substring(sub.lastIndexOf('.') + 1);
            if (getMethodName() != null) {
                sub = sub + "_" + getMethodName();
            }
            work = new File(base, sub);
        } else {
            work = base;
        }
        return work;
    }

    /**
     * @return the osUser
     */
    public String getOsUser() {
        return osUser;
    }

    /**
     * @param osUser the osUser to set
     */
    public void setOsUser(String osUser) {
        this.osUser = osUser;
    }

    /**
     * @return the compileWorkBaseDir
     */
    public String getCompileWorkBaseDir() {
        return compileWorkBaseDir;
    }

    /**
     * @param compileWorkBaseDir the compileWorkBaseDir to set
     */
    public void setCompileWorkBaseDir(String compileWorkBaseDir) {
        this.compileWorkBaseDir = compileWorkBaseDir;
    }

    /**
     * @return the clusterWorkDir
     */
    public String getClusterWorkDir() {
        return clusterWorkDir;
    }

    /**
     * @param clusterWorkDir the clusterWorkDir to set
     */
    public void setClusterWorkDir(String clusterWorkDir) {
        this.clusterWorkDir = clusterWorkDir;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @return the executionId
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * @param executionId the executionId to set
     */
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    /**
     * @return the extraConfigurations
     */
    public Map<String, String> getExtraConfigurations() {
        return extraConfigurations;
    }

    /**
     * @param extraConfigurations the extraConfigurations to set
     */
    public void setExtraConfigurations(Map<String, String> extraConfigurations) {
        this.extraConfigurations = extraConfigurations;
    }

    /**
     * @return the batchArgs
     */
    public Map<String, String> getBatchArgs() {
        return batchArgs;
    }

    /**
     * @param batchArgs the batchArgs to set
     */
    public void setBatchArgs(Map<String, String> batchArgs) {
        this.batchArgs = batchArgs;
    }

    /**
     * @return the options
     */
    public FlowCompilerOptions getOptions() {
        return options;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(FlowCompilerOptions options) {
        this.options = options;
    }

    /**
     * @return the callerClass
     */
    protected Class<?> getCallerClass() {
        return callerClass;
    }

    /**
     * @param callerClass the callerClass to set
     */
    protected void setCallerClass(Class<?> callerClass) {
        this.callerClass = callerClass;
    }

}