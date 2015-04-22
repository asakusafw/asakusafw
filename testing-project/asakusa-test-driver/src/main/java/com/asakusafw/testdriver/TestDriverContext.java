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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.processor.DependencyLibrariesProcessor;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowCompilerOptions.GenericOptionValue;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.TestToolRepository;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;
import com.asakusafw.utils.collections.Maps;

/**
 * テスト実行時のコンテキスト情報を管理する。
 * @since 0.2.0
 * @version 0.6.1
 */
public class TestDriverContext implements TestContext {

    static final Logger LOG = LoggerFactory.getLogger(TestDriverContext.class);

    static final ResourceBundle INFORMATION;
    static {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("com.asakusafw.testdriver.information"); //$NON-NLS-1$
        } catch (MissingResourceException e) {
            LOG.warn("Missing framework information resource", e); //$NON-NLS-1$
            bundle = new ListResourceBundle() {
                @Override
                protected Object[][] getContents() {
                    return new Object[0][];
                }
            };
        }
        INFORMATION = bundle;
    }

    /**
     * The system property key of runtime working directory.
     * This working directory must be a relative path from the default working directory.
     */
    public static final String KEY_RUNTIME_WORKING_DIRECTORY = "asakusa.testdriver.hadoopwork.dir"; //$NON-NLS-1$

    /**
     * The system property key of compiler working directory.
     */
    public static final String KEY_COMPILER_WORKING_DIRECTORY = "asakusa.testdriver.compilerwork.dir"; //$NON-NLS-1$

    /**
     * The system property key of the default framework installation path.
     * This property will overwrite environment variables.
     * @since 0.6.0
     */
    public static final String KEY_FRAMEWORK_PATH = "asakusa.testdriver.framework"; //$NON-NLS-1$

    /**
     * The system property key of the default batch applications installation base path.
     * This property will overwrite environment variables.
     * @since 0.6.1
     */
    public static final String KEY_BATCHAPPS_PATH = "asakusa.testdriver.batchapps"; //$NON-NLS-1$

    /**
     * The system property key of ignoring environment checking.
     * @see #validateExecutionEnvironment()
     * @since 0.5.2
     */
    public static final String KEY_FORCE_EXEC = "asakusa.testdriver.exec.force"; //$NON-NLS-1$

    /**
     * Environmental variable: the framework home path.
     */
    public static final String ENV_FRAMEWORK_PATH = "ASAKUSA_HOME"; //$NON-NLS-1$

    /**
     * Environmental variable: the batch applications installation base path.
     * @since 0.6.1
     */
    public static final String ENV_BATCHAPPS_PATH = "ASAKUSA_BATCHAPPS_HOME"; //$NON-NLS-1$

    /**
     * The default path of batch application installation base path (relative from the framework home path).
     * @since 0.6.1
     */
    public static final String DEFAULT_BATCHAPPS_PATH = "batchapps"; //$NON-NLS-1$

    /**
     * The path to the external dependency libraries folder (relative from working directory).
     * @since 0.5.1
     */
    public static final String EXTERNAL_LIBRARIES_PATH = DependencyLibrariesProcessor.LIBRARY_DIRECTORY_PATH;

    /**
     * The path to the framework version file (relative from the framework home path).
     * @since 0.5.2
     */
    public static final String FRAMEWORK_VERSION_PATH = "VERSION"; //$NON-NLS-1$

    /**
     * The entry key of the test-runtime framework version.
     * @since 0.5.2
     */
    public static final String KEY_FRAMEWORK_VERSION = "asakusafw.version"; //$NON-NLS-1$

    /**
     * The system property key of {@link JobExecutorFactory}'s implementation class name.
     * @see #setJobExecutorFactory(JobExecutorFactory)
     * @since 0.6.0
     */
    public static final String KEY_JOB_EXECUTOR_FACTORY = "asakusa.testdriver.exec.factory"; //$NON-NLS-1$

    private static final String HADOOPWORK_DIR_DEFAULT = "target/testdriver/hadoopwork"; //$NON-NLS-1$

    static {
        TestingEnvironmentConfigurator.initialize();
    }

    private volatile File frameworkHomePath;

    private final Class<?> callerClass;

    private final TestToolRepository repository;

    private final Map<String, String> extraConfigurations;

    private final Map<String, String> batchArgs;

    private final Map<String, String> environmentVariables;

    private final FlowCompilerOptions options;

    private final Set<TestExecutionPhase> skipPhases;

    private volatile File librariesPath;

    private volatile String currentBatchId;

    private volatile String currentFlowId;

    private volatile String currentExecutionId;

    private volatile File explicitCompilerWorkingDirectory;

    private volatile File generatedCompilerWorkingDirectory;

    private volatile boolean useSystemBatchappsHomePath;

    private volatile File explicitBatchappsHomePath;

    private volatile File generatedBatchappsHomePath;

    private volatile JobExecutorFactory jobExecutorFactory;

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
        this.environmentVariables = new HashMap<String, String>(System.getenv());
        this.options = new FlowCompilerOptions();
        configureOptions();
        this.skipPhases = EnumSet.noneOf(TestExecutionPhase.class);
    }

    private void configureOptions() {
        // disables Direct I/O input filters
        if (checkClass("com.asakusafw.compiler.directio.DirectFileIoProcessor")) { //$NON-NLS-1$
            LOG.debug("diasbles Direct I/O input filters"); //$NON-NLS-1$
            this.options.putExtraAttribute(
                    "directio.input.filter.enabled", //$NON-NLS-1$
                    GenericOptionValue.DISABLED.getSymbol());
        }
    }

    private boolean checkClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Validates current compiler environment.
     * @throws AssertionError if current test environment is invalid
     * @since 0.5.1
     */
    public void validateCompileEnvironment() {
        if (ToolProvider.getSystemJavaCompiler() == null) {
            // validates runtime environment first
            validateExecutionEnvironment();
            throw new AssertionError("この環境ではJavaコンパイラを利用できません（JDKを利用してテストを実行してください）");
        }
    }

    /**
     * Returns the development environment version.
     * @return the development environment version
     * @throws IllegalStateException if the version is not defined
     * @since 0.5.2
     */
    public String getDevelopmentEnvironmentVersion() {
        try {
            String version = INFORMATION.getString(KEY_FRAMEWORK_VERSION);
            return version;
        } catch (MissingResourceException e) {
            throw new IllegalStateException(
                    MessageFormat.format(
                            "この開発環境のバージョンが不明です ({e})",
                            KEY_COMPILER_WORKING_DIRECTORY), e);
        }
    }

    /**
     * Returns the runtime environment version.
     * @return the runtime environment version, or {@code null} if it is not defined
     * @since 0.5.2
     */
    public String getRuntimeEnvironmentVersion() {
        File path = getFrameworkHomePathOrNull();
        if (path == null) {
            return null;
        }
        File version = new File(path, FRAMEWORK_VERSION_PATH);
        if (version.isFile() == false) {
            LOG.warn(MessageFormat.format(
                    "テスト実行環境にバージョン情報が見つかりませんでした：{0}",
                    version.getAbsolutePath()));
            return null;
        }
        Properties p = new Properties();
        try {
            InputStream in = new FileInputStream(version);
            try {
                p.load(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            LOG.warn(MessageFormat.format(
                    "テスト実行環境のバージョン情報を読み出せませんでした：{0}",
                    version.getAbsolutePath()), e);
            return null;
        }
        String value = p.getProperty(KEY_FRAMEWORK_VERSION);
        if (value == null) {
            LOG.warn(MessageFormat.format(
                    "テスト実行環境のバージョン情報が欠落しています：{0} ({1})",
                    version.getAbsolutePath(),
                    KEY_FRAMEWORK_VERSION));
            return null;
        }
        return value;
    }

    /**
     * Validates current test execution environment.
     * @throws AssertionError if current test environment is invalid
     * @since 0.5.1
     */
    public void validateExecutionEnvironment() {
        getJobExecutor().validateEnvironment();
    }

    /**
     * Validates current test environment.
     * @throws AssertionError if current test environment is invalid
     * @since 0.5.0
     */
    public void validateEnvironment() {
        validateExecutionEnvironment();
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
        File result = getFrameworkHomePathOrNull();
        if (result == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "環境変数{0}が未設定です",
                    ENV_FRAMEWORK_PATH));
        }
        return result;
    }

    /**
     * Returns the framework home path.
     * @return the path, default path from environmental variable {@code ASAKUSA_HOME}, or {@code null}
     */
    public File getFrameworkHomePathOrNull() {
        if (frameworkHomePath == null) {
            String homePath = System.getProperty(KEY_FRAMEWORK_PATH);
            if (homePath != null) {
                return new File(homePath);
            }
            String defaultHomePath = getEnvironmentVariables0().get(ENV_FRAMEWORK_PATH);
            if (defaultHomePath != null) {
                return new File(defaultHomePath);
            }
            return null;
        }
        return frameworkHomePath;
    }

    /**
     * Enable to use the system batch applications installation base location instead of using generated path.
     * @param use {@code true} to use system location, or {@code false} otherwise
     * @since 0.6.1
     */
    public void useSystemBatchApplicationsInstallationPath(boolean use) {
        this.useSystemBatchappsHomePath = use;
    }

    /**
     * Sets the path to the batch applications installation location.
     * @param path the path to the batch applications installation location, or {@code null} to reset location
     * @since 0.6.1
     */
    public void setBatchApplicationsInstallationPath(File path) {
        this.explicitBatchappsHomePath = path;
    }

    /**
     * Returns the path to the batch applications installation location.
     * @return path to the batch applications installation location
     * @since 0.6.1
     */
    public File getBatchApplicationsInstallationPath() {
        if (explicitBatchappsHomePath != null) {
            return explicitBatchappsHomePath;
        }
        if (generatedBatchappsHomePath != null) {
            return generatedBatchappsHomePath;
        }
        String path = System.getProperty(KEY_BATCHAPPS_PATH);
        if (path != null) {
            return new File(path);
        }
        if (useSystemBatchappsHomePath) {
            String envPath = getEnvironmentVariables0().get(ENV_BATCHAPPS_PATH);
            if (envPath != null) {
                return new File(envPath);
            }
            File frameworkHome = getFrameworkHomePathOrNull();
            if (frameworkHome != null) {
                return new File(frameworkHome, DEFAULT_BATCHAPPS_PATH);
            }
        }
        generatedBatchappsHomePath = createTempDirPath();
        return generatedBatchappsHomePath;
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
        File apps = getBatchApplicationsInstallationPath();
        File batch = new File(apps, batchId);
        File lib = new File(batch, "lib"); //$NON-NLS-1$
        return lib;
    }

    /**
     * Returns the path to the external libraries (*.jar) deployment directory.
     * @param batchId target batch ID
     * @return the path
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #setLibrariesPath(File)
     * @since 0.5.1
     */
    public File getLibrariesPackageLocation(String batchId) {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        File apps = getBatchApplicationsInstallationPath();
        File batch = new File(apps, batchId);
        File lib = new File(batch, DependencyLibrariesProcessor.OUTPUT_DIRECTORY_PATH);
        return lib;
    }

    /**
     * Returns the command context for this attempt.
     * @return the command context
     */
    public CommandContext getCommandContext() {
        CommandContext context = new CommandContext(
                getFrameworkHomePath().getAbsolutePath() + "/", //$NON-NLS-1$
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
     * Sets the compiler working directory.
     * @param path the compiler working directory
     * @since 0.5.2
     */
    public void setCompilerWorkingDirectory(File path) {
        this.explicitCompilerWorkingDirectory = path;
    }

    /**
     * Returns the current user name in OS.
     * @return the current user name
     */
    public String getOsUser() {
        Map<String, String> envp = getEnvironmentVariables0();
        String user = System.getProperty("user.name", envp.get("USER")); //$NON-NLS-1$ //$NON-NLS-2$
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
            if (explicitCompilerWorkingDirectory != null) {
                return explicitCompilerWorkingDirectory.getAbsolutePath();
            }
            if (generatedCompilerWorkingDirectory == null) {
                generatedCompilerWorkingDirectory = createTempDirPath();
                LOG.debug("Created a temporary compiler working directory: {}", //$NON-NLS-1$
                        generatedCompilerWorkingDirectory);
            }
            return generatedCompilerWorkingDirectory.getAbsolutePath();
        }
        return dir;
    }

    private File createTempDirPath() {
        try {
            File file = File.createTempFile("asakusa", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
            if (file.delete() == false) {
                throw new AssertionError(MessageFormat.format(
                        "テスト用のテンポラリディレクトリの作成に失敗しました: {0}",
                        file));
            }
            return file;
        } catch (IOException e) {
            throw (AssertionError) new AssertionError("テスト用のテンポラリディレクトリの作成に失敗しました").initCause(e);
        }
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
     * Returns the path to the dependency libraries path.
     * The dependency library files are in the target folder directly.
     * @return the librariesPath the libraries path
     * @since 0.5.1
     */
    public File getLibrariesPath() {
        if (librariesPath == null) {
            return new File(EXTERNAL_LIBRARIES_PATH);
        }
        return librariesPath;
    }

    /**
     * Sets the path to the external dependency libraries folder.
     * @param librariesPath the libraries folder path
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public void setLibrariesPath(File librariesPath) {
        this.librariesPath = librariesPath;
    }

    /**
     * Returns the caller class.
     * This is ordinary used for detect test dataset on the classpath.
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
                "{0}-{1}-{2}", //$NON-NLS-1$
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
            throw new IllegalStateException("prepareCurrentJobflow was not invoked"); //$NON-NLS-1$
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
        File home = getFrameworkHomePathOrNull();
        Map<String, String> envp = getEnvironmentVariables0();
        if (home != null) {
            envp.put(ENV_FRAMEWORK_PATH, home.getAbsolutePath());
        }
        envp.put(ENV_BATCHAPPS_PATH, getBatchApplicationsInstallationPath().getAbsolutePath());
        return envp;
    }

    private Map<String, String> getEnvironmentVariables0() {
        return environmentVariables;
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
     * @since 0.7.0
     */
    public boolean isSkipValidateCondition() {
        return skipPhases.contains(TestExecutionPhase.VALIDATE_CONDITION);
    }

    /**
     * Sets whether this test skips to cleanup input data source (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     * @since 0.7.0
     */
    public void setSkipValidateCondition(boolean skip) {
        setSkipPhase(TestExecutionPhase.VALIDATE_CONDITION, skip);
    }

    /**
     * Returns whether this test skips to cleanup input data source.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipCleanInput() {
        return skipPhases.contains(TestExecutionPhase.CLEAN_INPUT);
    }

    /**
     * Sets whether this test skips to cleanup input data source (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipCleanInput(boolean skip) {
        setSkipPhase(TestExecutionPhase.CLEAN_INPUT, skip);
    }

    /**
     * Returns whether this test skips to cleanup input data source.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipCleanOutput() {
        return skipPhases.contains(TestExecutionPhase.CLEAN_OUTPUT);
    }

    /**
     * Sets whether this test skips to cleanup output data source (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipCleanOutput(boolean skip) {
        setSkipPhase(TestExecutionPhase.CLEAN_OUTPUT, skip);
    }

    /**
     * Returns whether this test skips to cleanup input data source.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipPrepareInput() {
        return skipPhases.contains(TestExecutionPhase.PREPARE_INPUT);
    }

    /**
     * Sets whether this test skips to prepare input data source (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipPrepareInput(boolean skip) {
        setSkipPhase(TestExecutionPhase.PREPARE_INPUT, skip);
    }

    /**
     * Returns whether this test skips to prepare output data source.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipPrepareOutput() {
        return skipPhases.contains(TestExecutionPhase.PREPARE_OUTPUT);
    }

    /**
     * Sets whether this test skips to prepare output data source (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipPrepareOutput(boolean skip) {
        setSkipPhase(TestExecutionPhase.PREPARE_OUTPUT, skip);
    }

    /**
     * Returns whether this test skips to execute jobflows.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipRunJobflow() {
        return skipPhases.contains(TestExecutionPhase.EXECUTE);
    }

    /**
     * Sets whether this test skips to execute jobflows (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipRunJobflow(boolean skip) {
        setSkipPhase(TestExecutionPhase.EXECUTE, skip);
    }

    /**
     * Returns whether this test skips to verify the testing result.
     * @return {@code true} to skip, otherwise {@code false}
     */
    public boolean isSkipVerify() {
        return skipPhases.contains(TestExecutionPhase.VERIFY);
    }

    /**
     * Sets whether this test skips to verify the testing result (default: {@code false}).
     * @param skip {@code true} to skip, otherwise {@code false}
     */
    public void setSkipVerify(boolean skip) {
        setSkipPhase(TestExecutionPhase.VERIFY, skip);
    }

    private void setSkipPhase(TestExecutionPhase phase, boolean skip) {
        assert phase != null;
        if (skip) {
            skipPhases.add(phase);
        } else {
            skipPhases.remove(phase);
        }
    }

    /**
     * Removes all temporary resources generated in this context.
     * @since 0.5.2
     */
    public void cleanUpTemporaryResources() {
        if (generatedCompilerWorkingDirectory != null && generatedCompilerWorkingDirectory.exists()) {
            LOG.debug("Deleting temporary compiler working directory: {}", //$NON-NLS-1$
                    generatedCompilerWorkingDirectory);
            removeAll(generatedCompilerWorkingDirectory);
            this.generatedCompilerWorkingDirectory = null;
        }
        if (generatedBatchappsHomePath != null && generatedBatchappsHomePath.exists()) {
            LOG.debug("Deleting temporary batchapps directory: {}", //$NON-NLS-1$
                    generatedBatchappsHomePath);
            removeAll(generatedBatchappsHomePath);
            this.generatedBatchappsHomePath = null;
        }
    }

    /**
     * Sets the {@link JobExecutorFactory} for executing jobs in this context.
     * @param factory the factory, or {@code null} to use a default implementation
     * @since 0.6.0
     * @see #getJobExecutor()
     */
    public void setJobExecutorFactory(JobExecutorFactory factory) {
        if (factory == null) {
            this.jobExecutorFactory = new DefaultJobExecutorFactory();
        } else {
            this.jobExecutorFactory = factory;
        }
    }

    /**
     * Returns the {@link JobExecutor} for executes jobs in this context.
     * @return the {@link JobExecutor} instance
     * @since 0.6.0
     */
    public JobExecutor getJobExecutor() {
        if (jobExecutorFactory != null) {
            return jobExecutorFactory.newInstance(this);
        }
        String className = System.getProperty(KEY_JOB_EXECUTOR_FACTORY, DefaultJobExecutorFactory.class.getName());
        try {
            Class<?> aClass = Class.forName(className, false, getClassLoader());
            this.jobExecutorFactory = aClass.asSubclass(JobExecutorFactory.class).newInstance();
        } catch (Exception e) {
            throw (AssertionError) new AssertionError(MessageFormat.format(
                    "Failed to create job executor factory from \"{0}\": {1}",
                    KEY_JOB_EXECUTOR_FACTORY,
                    className)).initCause(e);
        }
        return jobExecutorFactory.newInstance(this);
    }

    private boolean removeAll(File path) {
        assert path != null;
        boolean deleted = true;
        if (path.isDirectory()) {
            for (File child : path.listFiles()) {
                deleted &= removeAll(child);
            }
        }
        if (deleted) {
            if (path.delete() == false) {
                LOG.warn(MessageFormat.format(
                        "テスト用のテンポラリファイルの削除に失敗しました: {0}",
                        path.getAbsolutePath()));
                deleted = false;
            }
        }
        return deleted;
    }

    /**
     * Represents each phase in test execution.
     * @since 0.6.0
     */
    public enum TestExecutionPhase {

        /**
         * Validating test conditions.
         * @since 0.7.0
         */
        VALIDATE_CONDITION,

        /**
         * Cleaning input.
         */
        CLEAN_INPUT,

        /**
         * Cleaning output.
         */
        CLEAN_OUTPUT,

        /**
         * Preparing input.
         */
        PREPARE_INPUT,

        /**
         * Preparing output.
         */
        PREPARE_OUTPUT,

        /**
         * Performing execution.
         */
        EXECUTE,

        /**
         * Performing verification.
         */
        VERIFY,
    }
}
