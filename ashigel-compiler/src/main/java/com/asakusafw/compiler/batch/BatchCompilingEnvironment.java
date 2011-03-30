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
package com.asakusafw.compiler.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;

/**
 * バッチのコンパイル環境。
 */
public class BatchCompilingEnvironment {

    static final Logger LOG = LoggerFactory.getLogger(BatchCompilingEnvironment.class);

    private BatchCompilerConfiguration configuration;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private boolean sawError;

    /**
     * インスタンスを生成する。
     * @param configuration コンパイラの設定情報
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public BatchCompilingEnvironment(BatchCompilerConfiguration configuration) {
        Precondition.checkMustNotBeNull(configuration, "configuration"); //$NON-NLS-1$
        this.configuration = configuration;
    }

    /**
     * この環境を初期化する。
     * @return このオブジェクト
     */
    public BatchCompilingEnvironment bless() {
        if (initialized.compareAndSet(false, true) == false) {
            return this;
        }
        configuration.getWorkflows().initialize(this);
        sawError = false;
        return this;
    }

    /**
     * ここまでのコンパイル結果にエラーが含まれている場合のみ{@code true}を返す。
     * @return ここまでのコンパイル結果にエラーが含まれている場合のみ{@code true}
     */
    public boolean hasError() {
        return sawError;
    }

    /**
     * 現在までに発生したエラーの情報をクリアする。
     * @see #hasError()
     */
    public void clearError() {
        sawError = false;
    }

    /**
     * このコンパイラの設定情報を返す。
     * @return このコンパイラの設定情報
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public BatchCompilerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * ワークフロー全体を処理するプロセッサーのリポジトリーを返す。
     * @return ワークフロー全体を処理するプロセッサーのリポジトリー
     */
    public WorkflowProcessor.Repository getWorkflows() {
        return configuration.getWorkflows();
    }

    /**
     * 指定位置にリソースを出力するためのストリームを開いて返す。
     * @param path 出力先からの相対パス
     * @return 対象リソースに書き出すためのストリーム
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public OutputStream openResource(String path) throws IOException {
        Precondition.checkMustNotBeNull(path, "path"); //$NON-NLS-1$
        File output = configuration.getOutputDirectory();
        File file = new File(output, path);
        File parent = file.getParentFile();
        if (parent.mkdirs() == false && parent.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to create output directory {0}",
                    parent));
        }
        return new FileOutputStream(file);
    }

    /**
     * この環境に対してエラーメッセージを追加する。
     * @param format メッセージのフォーマット ({@link MessageFormat}形式)
     * @param arguments メッセージの引数、空の配列を指定した場合は{@code format}がそのままメッセージとなる
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void error(String format, Object... arguments) {
        Precondition.checkMustNotBeNull(format, "format"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(arguments, "arguments"); //$NON-NLS-1$
        String text;
        if (arguments.length == 0) {
            text = format;
        } else {
            text = MessageFormat.format(format, arguments);
        }
        LOG.error(text);
        sawError = true;
    }

    /**
     * この環境オブジェクトを取って初期化を行うインターフェース。
     */
    public interface Initializable {

        /**
         * このオブジェクトを初期化する。
         * @param environment 環境オブジェクト
         */
        void initialize(BatchCompilingEnvironment environment);
    }

    /**
     * {@link Initializable}の骨格実装。
     */
    public abstract static class Initialized implements Initializable {

        /**
         * コンパイル環境。
         */
        private BatchCompilingEnvironment environment;

        @Override
        public final void initialize(BatchCompilingEnvironment env) {
            Precondition.checkMustNotBeNull(env, "env"); //$NON-NLS-1$
            this.environment = env;
            doInitialize();
        }

        /**
         * サブクラスでこのオブジェクトの初期化を実行する。
         */
        protected void doInitialize() {
            return;
        }

        /**
         * 環境オブジェクトを返す。
         * @return 環境オブジェクト
         */
        protected BatchCompilingEnvironment getEnvironment() {
            return environment;
        }
    }
}
