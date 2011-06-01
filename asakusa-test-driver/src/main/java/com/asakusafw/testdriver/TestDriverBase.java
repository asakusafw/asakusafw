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
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.asakusafw.compiler.flow.FlowCompilerOptions;

/**
 * テストドライバの基底クラス。
 */
public abstract class TestDriverBase {

    private static final String COMPILERWORK_DIR_DEFAULT = "target/testdriver/batchcwork";
    private static final String HADOOPWORK_DIR_DEFAULT = "target/testdriver/hadoopwork";

    /** テストドライバコンテキスト。テスト実行時のコンテキスト情報が格納される。 */
    protected TestDriverContext driverContext = new TestDriverContext();

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

    private void initialize() {
        // クラス名/メソッド名を使った変数を初期化
        setTestClassInformation();

        // OS情報
        this.driverContext.setOsUser(System.getenv("USER"));

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
        FlowCompilerOptions options = driverContext.getOptions();
        if (level <= 0) {
            options.setCompressConcurrentStage(false);
            options.setCompressFlowPart(false);
            options.setHashJoinForSmall(false);
            options.setHashJoinForTiny(false);
            options.setEnableCombiner(false);
        } else if (level == 1) {
            options.setCompressConcurrentStage(FlowCompilerOptions.Item.compressConcurrentStage.defaultValue);
            options.setCompressFlowPart(FlowCompilerOptions.Item.compressFlowPart.defaultValue);
            options.setHashJoinForSmall(FlowCompilerOptions.Item.hashJoinForSmall.defaultValue);
            options.setHashJoinForTiny(FlowCompilerOptions.Item.hashJoinForTiny.defaultValue);
            options.setEnableCombiner(FlowCompilerOptions.Item.enableCombiner.defaultValue);
        } else {
            options.setCompressConcurrentStage(true);
            options.setCompressFlowPart(true);
            options.setHashJoinForSmall(true);
            options.setHashJoinForTiny(true);
            options.setEnableCombiner(true);
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
        driverContext.setFrameworkHomePath(frameworkHomePath);
    }
}
