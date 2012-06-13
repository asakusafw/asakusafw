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

import com.asakusafw.compiler.flow.FlowCompilerOptions;

/**
 * テストドライバの基底クラス。
 */
public abstract class TestDriverBase {

    /** テストドライバコンテキスト。テスト実行時のコンテキスト情報が格納される。 */
    protected TestDriverContext driverContext;

    /**
     * Creates a new instance.
     * @param callerClass the caller class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestDriverBase(Class<?> callerClass) {
        if (callerClass == null) {
            throw new IllegalArgumentException("callerClass must not be null"); //$NON-NLS-1$
        }
        this.driverContext = new TestDriverContext(callerClass);
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

    /**
     * 入力データのクリーニング(truncate)をスキップするかを設定する。
     *
     * @param skip
     *            入力データのクリーニング(truncate)をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipCleanInput(boolean skip) {
        driverContext.setSkipCleanInput(skip);
    }

    /**
     * 出力データのクリーニング(truncate)をスキップするかを設定する。
     *
     * @param skip
     *            出力データのクリーニング(truncate)をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipCleanOutput(boolean skip) {
        driverContext.setSkipCleanOutput(skip);
    }

    /**
     * 入力データのセットアップ(prepare)をスキップするかを設定する。
     *
     * @param skip
     *            入力データのセットアップ(prepare)をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipPrepareInput(boolean skip) {
        driverContext.setSkipPrepareInput(skip);
    }

    /**
     * 出力データのセットアップ(prepare)をスキップするかを設定する。
     *
     * @param skip
     *            出力データのセットアップ(prepare)をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipPrepareOutput(boolean skip) {
        driverContext.setSkipPrepareOutput(skip);
    }

    /**
     * ジョブフローの実行をスキップするかを設定する。
     *
     * @param skip
     *            ジョブフローの実行をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipRunJobflow(boolean skip) {
        driverContext.setSkipRunJobflow(skip);
    }

    /**
     * テスト結果の検証をスキップするかを設定する。
     *
     * @param skip
     *            テスト結果の検証をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipVerify(boolean skip) {
        driverContext.setSkipVerify(skip);
    }

}
