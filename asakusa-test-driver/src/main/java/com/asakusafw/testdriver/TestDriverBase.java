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

import static org.hamcrest.CoreMatchers.is;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.compiler.testing.StageInfo;
import com.asakusafw.runtime.stage.AbstractStageClient;
import com.asakusafw.testdriver.DriverOutputBase.VerifyRuleHolder;
import com.asakusafw.testdriver.TestExecutionPlan.Command;
import com.asakusafw.testdriver.TestExecutionPlan.Job;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.TestResultInspector;
import com.asakusafw.testdriver.core.VerifyContext;

/**
 * テストドライバの基底クラス。
 */
public abstract class TestDriverBase {

    private static final Logger LOG = LoggerFactory.getLogger(TestDriverBase.class);

    private static final String BUILD_PROPERTIES_FILE = "build.properties";
    private static final String COMPILERWORK_DIR_DEFAULT = "target/testdriver/batchcwork";
    private static final String HADOOPWORK_DIR_DEFAULT = "target/testdriver/hadoopwork";

    /**
     * Environmental variable: the framework home path.
     */
    private static final String ENV_FRAMEWORK_PATH = "ASAKUSA_HOME";

    /**
     * Path to the script to submit a stage job (relative path from {@link #getFrameworkHomePath()}).
     */
    protected static final String SUBMIT_JOB_SCRIPT = "experimental/bin/hadoop_job_run.sh";

    /** テストデータ格納先のデフォルト値 */
    protected static final String TESTDATA_DIR_DEFAULT = "src/test/data/excel";

    /** Hadoopコマンドの絶対パス。 */
    @Deprecated
    protected String hadoopCmd;

    /** HadoopJobRun(HadoopコマンドのWrapper)コマンドの絶対パス。 */
    @Deprecated
    protected String hadoopJobRunCmd;

    private File frameworkHomePath;

    /** build.properties */
    protected Properties buildProperties;

    /** テストドライバコンテキスト。テスト実行時のコンテキスト情報が格納される。 */
    protected TestDriverContext driverContext = new TestDriverContext(new TreeMap<String, String>(),
            new TreeMap<String, String>(), new FlowCompilerOptions());

    /**
     * コンストラクタ。
     *
     * @param callerClass 呼出元クラス
     */
    public TestDriverBase(Class<?> callerClass) {
        driverContext.setCallerClass(callerClass);
        initialize();
    }

    /**
     * テストクラスのクラス名とメソッド名を抽出する。
     */
    protected void setTestClassInformation() {

        // 呼び出し元のテストクラス名とテストメソッド名を取得
        Class<?> clazz = null;
        Method method = null;
        boolean wasCalledTestMethod = false;
        for (StackTraceElement elm : new Exception().getStackTrace()) {
            try {
                clazz = Class.forName(elm.getClassName());
                method = clazz.getDeclaredMethod(elm.getMethodName(), new Class[] {});
                if (method.getAnnotation(org.junit.Test.class) != null) {
                    wasCalledTestMethod = true;
                    break;
                }
            } catch (ClassNotFoundException ex) {
                continue;
            } catch (NoSuchMethodException ex) {
                continue;
            }
        }
        if (wasCalledTestMethod && clazz != null && method != null) {
            driverContext.setClassName(clazz.getSimpleName());
            driverContext.setMethodName(method.getName());
        } else {
            if (driverContext.getCallerClass() != null) {
                // JUnitのテストメソッドから呼ばれなかった場合
                driverContext.setClassName(driverContext.getCallerClass().getSimpleName());
                driverContext.setMethodName("");
            } else {
                throw new RuntimeException("テストメソッドからテストドライバを起動していないか、呼出元クラスがnull。");
            }
        }

        // executionIdの生成 (テスト時にわかりやすいようにクラス名_メソッド名_タイムスタンプ)
        String ts = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        driverContext.setExecutionId(driverContext.getClassName() + "_" + driverContext.getMethodName() + "_" + ts);
    }

    /**
     * テスト実行前の初期化処理。
     *
     * @throws RuntimeException
     *             初期化に失敗した場合
     */
    protected void initialize() throws RuntimeException {
        // クラス名/メソッド名を使った変数を初期化
        setTestClassInformation();

        File buildPropertiesFile = new File(BUILD_PROPERTIES_FILE);
        if (buildPropertiesFile.exists()) {
            LOG.info("ビルド設定情報をロードしています: {}", buildPropertiesFile);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(buildPropertiesFile);
                buildProperties = new Properties();
                buildProperties.load(fis);
                System.setProperty("ASAKUSA_MODELGEN_PACKAGE", buildProperties.getProperty("asakusa.modelgen.package"));
                System.setProperty("ASAKUSA_MODELGEN_OUTPUT", buildProperties.getProperty("asakusa.modelgen.output"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        } else {
            LOG.info("ビルド設定情報が存在しないため、スキップします: {}", BUILD_PROPERTIES_FILE);
        }

        // OS情報
        this.driverContext.setOsUser(System.getenv("USER"));

        // パス関連
        this.hadoopCmd = new File(System.getenv("HADOOP_HOME"), "bin/hadoop").getPath();
        this.hadoopJobRunCmd = new File(System.getenv(ENV_FRAMEWORK_PATH), SUBMIT_JOB_SCRIPT).getPath();

        this.driverContext.setCompileWorkBaseDir(System.getProperty("asakusa.testdriver.compilerwork.dir"));
        if (driverContext.getCompileWorkBaseDir() == null) {
            driverContext.setCompileWorkBaseDir(COMPILERWORK_DIR_DEFAULT);
        }
        this.driverContext.setClusterWorkDir(System.getProperty("asakusa.testdriver.hadoopwork.dir"));
        if (driverContext.getClusterWorkDir() == null) {
            driverContext.setClusterWorkDir(HADOOPWORK_DIR_DEFAULT);
        }
    }

    /**
     * クラスタ上のディレクトリを削除する。
     *
     * @param pathString
     *            削除するディレクトリへのパス (ホームディレクトリからの相対パス)
     * @throws IOException
     *             削除に失敗した場合
     * @throws IllegalArgumentException
     *             引数に{@code null}が指定された場合
     */
    protected void initializeClusterDirectory(String pathString) throws IOException {
        if (pathString == null) {
            throw new IllegalArgumentException("pathString must not be null"); //$NON-NLS-1$
        }
        Configuration conf = new Configuration();
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path path = new Path(fs.getHomeDirectory(), pathString);
            LOG.debug("クラスタワークディレクトリを初期化します。Path: {}", path);
            fs.delete(path, true);
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }

    /**
     * 指定のジョブフローを実行するための計画を作成する。
     *
     * @param info
     *            ジョブフローの情報
     * @param context
     *            コマンドの実行コンテキスト
     * @param properties
     *            プロパティの一覧
     * @return 作成した実行計画
     */
    protected TestExecutionPlan createExecutionPlan(JobflowInfo info, CommandContext context,
            Map<String, String> properties) {

        List<Job> jobs = new ArrayList<Job>();
        for (StageInfo stage : info.getStages()) {
            jobs.add(new Job(stage.getClassName(), context.getExecutionId(), properties));
        }

        List<Command> initializers = new ArrayList<Command>();
        List<Command> importers = new ArrayList<Command>();
        List<Command> exporters = new ArrayList<Command>();
        List<Command> finalizers = new ArrayList<Command>();
        for (ExternalIoCommandProvider provider : info.getJobflow().getCompiled().getCommandProviders()) {
            initializers.addAll(convert(provider.getInitializeCommand(context)));
            importers.addAll(convert(provider.getImportCommand(context)));
            exporters.addAll(convert(provider.getExportCommand(context)));
            finalizers.addAll(convert(provider.getFinalizeCommand(context)));
        }

        return new TestExecutionPlan(info.getJobflow().getFlowId(), context.getExecutionId(), initializers, importers,
                jobs, exporters, finalizers);
    }

    private List<TestExecutionPlan.Command> convert(List<ExternalIoCommandProvider.Command> commands) {
        List<TestExecutionPlan.Command> results = new ArrayList<TestExecutionPlan.Command>();
        for (ExternalIoCommandProvider.Command cmd : commands) {
            results.add(new TestExecutionPlan.Command(cmd.getCommandTokens(), cmd.getModuleName(),
                    cmd.getProfileName(), cmd.getEnvironment()));
        }
        return results;
    }

    /**
     * 指定の実行計画を永続化して保存する。
     *
     * @param targetDirectory
     *            保存対象のディレクトリ
     * @param plan
     *            保存する実行計画
     * @throws IOException
     *             保存に失敗した場合
     */
    protected void savePlan(File targetDirectory, TestExecutionPlan plan) throws IOException {
        File file = new File(targetDirectory, "test-execution-plan.ser");
        LOG.info("{}のテスト用実行計画を保存しています: {}", driverContext.getExecutionId(), file.getAbsolutePath());
        FileOutputStream output = new FileOutputStream(file);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(output);
            oos.writeObject(plan);
            oos.close();
        } finally {
            output.close();
        }
    }

    /**
     * 実行計画をもとに、ジョブフローのプログラムを実行する。
     *
     * @param plan
     *            実行計画
     * @param jobflowPackageFile
     *            ジョブフローのクラスライブラリ
     * @throws RuntimeException
     *             実行に失敗した場合
     */
    protected void executePlan(TestExecutionPlan plan, File jobflowPackageFile) throws RuntimeException {
        try {
            runJobFlowCommands(plan.getInitializers());
            runJobFlowCommands(plan.getImporters());
            runJobflowJobs(jobflowPackageFile, plan.getJobs());
            runJobFlowCommands(plan.getExporters());
        } finally {
            runJobFlowCommands(plan.getFinalizers());
        }
    }

    /**
     * Hadoopに引き渡すプロパティ情報を構築する。
     *
     * @param context
     *            コマンドの情報
     * @return プロパティの一覧
     * @throws IllegalArgumentException
     *             引数に{@code null}が指定された場合
     */
    protected Map<String, String> createHadoopProperties(CommandContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        // ジョブの実行時にOSユーザ名とMMインスタンスIDをJavaシステムプロパティとして渡す
        Map<String, String> dPropMap = new HashMap<String, String>();
        dPropMap.put(AbstractStageClient.PROP_USER, driverContext.getOsUser());
        dPropMap.put(AbstractStageClient.PROP_EXECUTION_ID, driverContext.getExecutionId());

        // 変数表を設定に渡す
        dPropMap.put(AbstractStageClient.PROP_ASAKUSA_BATCH_ARGS, context.getVariableList());

        // 各種プラグインの初期化情報をシステムプロパティとして渡す
        dPropMap.putAll(getPluginProperties());

        // 追加設定の情報をシステムプロパティとして渡す
        dPropMap.putAll(driverContext.getExtraConfigurations());
        return dPropMap;
    }

    /**
     * ジョブの一覧を実行する。
     *
     * @param jobflowPackageFile
     *            ジョブフローのクラスライブラリ
     * @param jobs
     *            実行するジョブの一覧
     * @throws RuntimeException
     *             実行に失敗した場合
     */
    private void runJobflowJobs(File jobflowPackageFile, List<Job> jobs) throws RuntimeException {
        // DSLコンパイラが生成したHadoopジョブの各ステージを順番に実行する。
        // 各Hadoopジョブを実行した都度、hadoopコマンドの戻り値の検証を行う。
        for (Job job : jobs) {
            HadoopJobInfo jobElement = new HadoopJobInfo(job.getExecutionId(), jobflowPackageFile.getAbsolutePath(),
                    job.getClassName(), job.getProperties());
            runHadoopJob(jobElement);
        }
    }

    /**
     * コマンドの一覧を実行する。
     *
     * @param cmdList
     *            コマンドの一覧
     * @throws RuntimeException
     *             実行に失敗した場合
     */
    private void runJobFlowCommands(List<TestExecutionPlan.Command> cmdList) throws RuntimeException {
        // DSLコンパイラが生成したコマンドを順番に実行する。
        // 各コマンドを実行した都度、終了コードの検証を行う。
        for (TestExecutionPlan.Command command : cmdList) {
            List<String> cmdToken = command.getCommandTokens();
            String[] cmd = cmdToken.toArray(new String[cmdToken.size()]);
            runShellAndAssert(cmd, Collections.<String, String> emptyMap());
        }
    }

    /**
     * Hadoopジョブを実行する。
     *
     * @param hadoopJobInfo
     *            実行するHadoopジョブの情報
     * @throws RuntimeException
     *             ジョブの実行に失敗した場合
     */
    protected void runHadoopJob(HadoopJobInfo hadoopJobInfo) throws RuntimeException {

        String[] shellCmd = {
                new File(getFrameworkHomePath(), SUBMIT_JOB_SCRIPT).getAbsolutePath(),
                hadoopJobInfo.getClassName(),
                hadoopJobInfo.getJarName()
        };
        Map<String, String> dPropMap = hadoopJobInfo.getDPropMap();
        if (dPropMap != null) {
            dPropMap.keySet();
            List<String> list = new ArrayList<String>();
            list.addAll(Arrays.asList(shellCmd));
            for (Map.Entry<String, String> entry : dPropMap.entrySet()) {
                list.add("-D");
                list.add(entry.getKey() + "=" + entry.getValue());
            }
            shellCmd = list.toArray(new String[list.size()]);
        }

        Map<String, String> variables = new HashMap<String, String>();
        variables.put(ENV_FRAMEWORK_PATH, getFrameworkHomePath().getAbsolutePath());

        int exitValue = runShell(shellCmd, variables);
        if (exitValue != 0) {
            // 異常終了
            Assert.assertThat("Hadoopジョブの実行に失敗しました。ジョブフローID= " + hadoopJobInfo.getJobFlowId() + ", コマンド= "
                    + toStirngShellCmdArray(shellCmd), exitValue, is(0));
        }
    }

    /**
     * シェル／シェルスクリプトを実行する。
     *
     * @param shellCmd
     *            コマンド文字列の配列
     * @param environmentVariables
     *            追加する環境変数の一覧
     * @return シェル実行時の戻り値
     * @throws RuntimeException
     *             スクリプトの実行に失敗した場合
     */
    protected int runShell(String[] shellCmd, Map<String, String> environmentVariables) throws RuntimeException {

        LOG.info("【COMMAND】 " + toStirngShellCmdArray(shellCmd));

        ProcessBuilder builder = new ProcessBuilder(shellCmd);
        builder.redirectErrorStream(true);
        builder.environment().putAll(environmentVariables);

        int exitValue;
        Process process = null;
        InputStream is = null;
        try {
            process = builder.start();
            is = process.getInputStream();
            InputStreamThread it = new InputStreamThread(is);

            it.start();
            exitValue = process.waitFor();
            it.join();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (process != null) {
                    process.getOutputStream().close();
                    process.getErrorStream().close();
                    process.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return exitValue;
    }

    /**
     * シェル／シェルスクリプトを実行し、戻り値を検証する。
     * <p>
     * 戻り値が0であれば検証成功、0以外は検証失敗と判断する。
     * </p>
     *
     * @param shellCmd
     *            コマンド文字列の配列
     * @param variables
     *            環境変数の一覧
     * @throws RuntimeException
     *             スクリプトの実行に失敗した場合
     */
    protected void runShellAndAssert(String[] shellCmd, Map<String, String> variables) throws RuntimeException {
        int exitValue = runShell(shellCmd, variables);
        Assert.assertThat("コマンドの実行に失敗しました。= " + toStirngShellCmdArray(shellCmd), exitValue, is(0));
    }

    /**
     * 設定項目を追加する。
     *
     * @param key
     *            追加する項目のキー名
     * @param value
     *            追加する項目の値、{@code null}の場合には項目を削除する
     * @throws IllegalArgumentException
     *             引数に{@code null}が指定された場合
     */
    public void configure(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        if (value != null) {
            driverContext.getExtraConfigurations().put(key, value);
        } else {
            driverContext.getExtraConfigurations().remove(key);
        }
    }

    /**
     * バッチ実行時引数を設定する。
     *
     * @param key
     *            追加する項目のキー名
     * @param value
     *            追加する項目の値、{@code null}の場合には項目を削除する
     * @throws IllegalArgumentException
     *             引数に{@code null}が指定された場合
     */
    public void setBatchArg(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        if (value != null) {
            driverContext.getBatchArgs().put(key, value);
        } else {
            driverContext.getBatchArgs().remove(key);
        }
    }

    /**
     * コンパイラの最適化レベルを変更する。
     *
     * @param level
     *            0: 設定可能な最適化を全て行わない、1: デフォルト、2~: 最適化を積極的に行う
     */
    public void setOptimize(int level) {
        if (level <= 0) {
            driverContext.getOptions().setCompressConcurrentStage(false);
            driverContext.getOptions().setCompressFlowPart(false);
            driverContext.getOptions().setHashJoinForSmall(false);
            driverContext.getOptions().setHashJoinForTiny(false);
            driverContext.getOptions().setEnableCombiner(false);
        } else if (level == 1) {
            driverContext.getOptions().setCompressConcurrentStage(
                    FlowCompilerOptions.Item.compressConcurrentStage.defaultValue);
            driverContext.getOptions().setCompressFlowPart(FlowCompilerOptions.Item.compressFlowPart.defaultValue);
            driverContext.getOptions().setHashJoinForSmall(FlowCompilerOptions.Item.hashJoinForSmall.defaultValue);
            driverContext.getOptions().setHashJoinForTiny(FlowCompilerOptions.Item.hashJoinForTiny.defaultValue);
            driverContext.getOptions().setEnableCombiner(FlowCompilerOptions.Item.enableCombiner.defaultValue);
        } else {
            driverContext.getOptions().setCompressConcurrentStage(true);
            driverContext.getOptions().setCompressFlowPart(true);
            driverContext.getOptions().setHashJoinForSmall(true);
            driverContext.getOptions().setHashJoinForTiny(true);
            driverContext.getOptions().setEnableCombiner(true);
        }
    }

    /**
     * コンパイラが生成するコードのデバッグ情報について変更する。
     *
     * @param enable
     *            デバッグ情報を残す場合に{@code true}、捨てる場合に{@code false}
     */
    public void setDebug(boolean enable) {
        driverContext.getOptions().setEnableDebugLogging(enable);
    }

    /**
     * フレームワークのホームパス({@code ASAKUSA_HOME})を設定する。
     * <p>
     * この値が未設定の場合、環境変数に設定された値を利用する。
     * </p>
     * @param frameworkHomePath フレームワークのホームパス、未設定に戻す場合は{@code null}
     */
    public void setFrameworkHomePath(File frameworkHomePath) {
        this.frameworkHomePath = frameworkHomePath;
    }

    /**
     * Returns the framework home path.
     * @return the path, or default path from environmental variable {@code ASAKUSA_HOME}
     * @throws IllegalStateException if neither the framework home path nor the environmental variable were set
     */
    protected File getFrameworkHomePath() {
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
     * テスト用のプラグイン設定情報({@code -D})を返す。
     *
     * @return テスト用のプラグイン設定情報
     */
    protected Map<String, String> getPluginProperties() {
        Map<String, String> results = new HashMap<String, String>();
        // no special properties
        return results;
    }

    private String toStirngShellCmdArray(String[] shellCmd) {
        StringBuilder sb = new StringBuilder();
        for (String cmd : shellCmd) {
            sb.append(cmd).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Inspects output and returns differences between expected and actual results.
     * @param <T> output data type
     * @param output output object
     * @param context verification context
     * @param inspector inspector to be used in verification
     * @return the differences if exists, or empty list otherwise
     * @throws IOException if failed to obtain expected or actual results
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected <T> List<Difference> inspect(
            DriverOutputBase<T> output,
            VerifyContext context,
            TestResultInspector inspector) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (inspector == null) {
            throw new IllegalArgumentException("inspector must not be null"); //$NON-NLS-1$
        }
        VerifyRuleHolder<T> ruleHolder = output.getVerifyRule();
        if (ruleHolder.hasUri()) {
            return inspector.inspect(output.getModelType(),
                    output.getExporterDescription(),
                    context,
                    output.getExpectedUri(),
                    ruleHolder.getUri());
        } else {
            return inspector.inspect(output.getModelType(),
                    output.getExporterDescription(),
                    output.getExpectedUri(),
                    inspector.rule(output.getModelType(), ruleHolder.getVerifier()));
        }
    }
}

/**
 * InputStreamを読み込むスレッド。
 */
class InputStreamThread extends Thread {

    private BufferedReader br;

    private final List<String> list = new ArrayList<String>();

    /**
     * コンストラクタ。
     *
     * @param is
     *            入力ストリーム
     */
    public InputStreamThread(InputStream is) {
        br = new BufferedReader(new InputStreamReader(is));
    }

    /**
     * コンストラクタ。
     *
     * @param is
     *            入力ストリーム
     * @param charset
     *            Readerに渡すcharset
     */
    public InputStreamThread(InputStream is, String charset) {
        try {
            br = new BufferedReader(new InputStreamReader(is, charset));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        for (;;) {
            try {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                list.add(line);
                System.out.println(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
