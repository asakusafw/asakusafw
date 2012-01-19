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
package com.asakusafw.compiler.flow;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.Precondition;
import com.ashigeru.lang.java.model.syntax.CompilationUnit;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.Name;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.util.Models;

/**
 * 個々のジョブフローをコンパイルする際の環境。
 */
public class FlowCompilingEnvironment {

    static final Logger LOG = LoggerFactory.getLogger(FlowCompilingEnvironment.class);

    private final FlowCompilerConfiguration config;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private final AtomicInteger counter = new AtomicInteger();

    private String firstError;

    /**
     * インスタンスを生成する。
     * @param config 設定
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowCompilingEnvironment(FlowCompilerConfiguration config) {
        Precondition.checkMustNotBeNull(config, "config"); //$NON-NLS-1$
        this.config = config;
        clearError();
    }

    /**
     * この環境を初期化する。
     * @return このオブジェクト
     */
    public FlowCompilingEnvironment bless() {
        if (initialized.compareAndSet(false, true) == false) {
            return this;
        }
        config.getDataClasses().initialize(this);
        config.getExternals().initialize(this);
        config.getPackager().initialize(this);
        config.getProcessors().initialize(this);
        config.getGraphRewriters().initialize(this);
        clearError();
        return this;
    }

    /**
     * Returns a previous error message.
     * @return a previous error message, or {@code null} if not error
     */
    public String getErrorMessage() {
        return firstError;
    }

    /**
     * ここまでのコンパイル結果にエラーが含まれている場合のみ{@code true}を返す。
     * @return ここまでのコンパイル結果にエラーが含まれている場合のみ{@code true}
     */
    public final boolean hasError() {
        return firstError != null;
    }

    /**
     * 現在までに発生したエラーの情報をクリアする。
     * @see #hasError()
     */
    public final void clearError() {
        firstError = null;
    }

    /**
     * この環境で利用可能なモデルファクトリーを返す。
     * @return 利用可能なモデルファクトリー
     */
    public ModelFactory getModelFactory() {
        return config.getFactory();
    }

    /**
     * この環境で利用可能なプロセッサのリポジトリを返す。
     * @return 利用可能なプロセッサのリポジトリ
     */
    public FlowElementProcessor.Repository getProcessors() {
        return config.getProcessors();
    }

    /**
     * この環境で利用可能なデータクラスのリポジトリを返す。
     * @return 利用可能なデータクラスのリポジトリ
     */
    public DataClassRepository getDataClasses() {
        return config.getDataClasses();
    }

    /**
     * この環境で利用可能な外部入出力プロセッサのリポジトリを返す。
     * @return 利用可能な外部入出力プロセッサのリポジトリ
     */
    public ExternalIoDescriptionProcessor.Repository getExternals() {
        return config.getExternals();
    }

    /**
     * 演算子グラフを書き換えるエンジンのリポジトリを返す。
     * @return 演算子グラフを書き換えるエンジンのリポジトリ
     */
    public FlowGraphRewriter.Repository getGraphRewriters() {
        return config.getGraphRewriters();
    }

    /**
     * コンパイル対象に対するバッチ識別子を返す。
     * @return コンパイル対象に対するバッチ識別子
     */
    public String getBatchId() {
        return config.getBatchId();
    }

    /**
     * コンパイル対象に対するジョブフロー識別子を返す。
     * @return コンパイル対象に対するジョブフロー識別子
     */
    public String getFlowId() {
        return config.getFlowId();
    }

    /**
     * コンパイル対象に対する識別子を返す。
     * @return コンパイル対象に対する識別子
     */
    public String getTargetId() {
        return MessageFormat.format("{0}.{1}", getBatchId(), getFlowId());
    }

    /**
     * コンパイル対象に対するパッケージ名を返す。
     * @return コンパイル対象に対するパッケージ名
     */
    public Name getTargetPackageName() {
        Name root = Models.toName(getModelFactory(), config.getRootPackageName());
        Name batch = Models.toName(getModelFactory(), normalize(getBatchId()));
        Name flow = Models.toName(getModelFactory(), normalize(getFlowId()));
        return Models.append(getModelFactory(), root, batch, flow);
    }

    private String normalize(String name) {
        assert name != null;
        StringBuilder buf = new StringBuilder();
        String[] segments = name.split(Pattern.quote("."));
        buf.append(memberName(segments[0]));
        for (int i = 1; i < segments.length; i++) {
            buf.append('.');
            buf.append(memberName(segments[i]));
        }
        return buf.toString();
    }

    private String memberName(String string) {
        assert string != null;
        if (string.isEmpty()) {
            return "_";
        }
        return JavaName.of(string).toMemberName();
    }

    /**
     * 指定のステージの内容を書き出すパッケージ名を返す。
     * @param stageNumber 対象のステージ番号
     * @return 対応するパッケージ名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Name getStagePackageName(int stageNumber) {
        if (stageNumber < 0) {
            throw new IllegalArgumentException("stageNumber must be a positive integer"); //$NON-NLS-1$
        }
        return Models.append(
                config.getFactory(),
                getTargetPackageName(),
                String.format("stage%04d", stageNumber));
    }

    /**
     * リソースを出力するパッケージ名を返す。
     * @param resourceKind リソースの種類
     * @return 対応するパッケージ名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Name getResourcePackage(String resourceKind) {
        Precondition.checkMustNotBeNull(resourceKind, "resourceKind"); //$NON-NLS-1$
        return Models.append(getModelFactory(),
                getTargetPackageName(),
                normalize(resourceKind));
    }

    /**
     * 内蔵するシーケンス番号を元にユニークな名前を返す。
     * @param prefix 接頭辞
     * @return ユニークな名前
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public SimpleName createUniqueName(String prefix) {
        Precondition.checkMustNotBeNull(prefix, "prefix"); //$NON-NLS-1$
        return getModelFactory().newSimpleName(prefix + counter.incrementAndGet());
    }

    /**
     * 指定のモジュールに対するプロローグ処理の内容を書き出すパッケージ名を返す。
     * @param moduleId 対象の識別子
     * @return 対応するパッケージ名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Name getProloguePackageName(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return Models.append(
                config.getFactory(),
                getTargetPackageName(),
                MessageFormat.format("{0}.prologue", memberName(moduleId)));
    }

    /**
     * 指定のモジュールに対するエピローグ処理の内容を書き出すパッケージ名を返す。
     * @param moduleId モジュール識別子
     * @return 対応するパッケージ名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Name getEpiloguePackageName(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return Models.append(
                config.getFactory(),
                getTargetPackageName(),
                MessageFormat.format("{0}.epilogue", memberName(moduleId)));
    }

    /**
     * コンパイル対象が利用するリソース位置を返す。
     * @return コンパイル対象が利用するリソース位置
     */
    public Location getTargetLocation() {
        return config
            .getRootLocation()
            .append(getBatchId())
            .append(getFlowId());
    }

    /**
     * 指定のステージが利用するリソース位置を返す。
     * @param stageNumber 対象のステージ番号
     * @return 対応するリソース位置
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Location getStageLocation(int stageNumber) {
        if (stageNumber < 0) {
            throw new IllegalArgumentException("stageNumber must be a positive integer"); //$NON-NLS-1$
        }
        String stageSuffix = String.format("stage%04d", stageNumber);
        return getTargetLocation().append(stageSuffix);
    }

    /**
     * 指定のモジュールに対するプロローグ処理が利用するリソース位置を返す。
     * @param moduleId モジュール識別子
     * @return 対応するリソース位置
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Location getPrologueLocation(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return getTargetLocation()
            .append("prologue")
            .append(moduleId);
    }

    /**
     * 指定のモジュールに対するエピローグ処理が利用するリソース位置を返す。
     * @param moduleId モジュール識別子
     * @return 対応するリソース位置
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Location getEpilogueLocation(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return getTargetLocation()
            .append("epilogue")
            .append(moduleId);
    }

    /**
     * 指定のソースプログラムを出力する。
     * @param source 出力するソースプログラム
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void emit(CompilationUnit source) throws IOException {
        Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
        PrintWriter writer = config.getPackager().openWriter(source);
        try {
            Models.emit(source, writer);
        } finally {
            writer.close();
        }
    }

    /**
     * 指定位置にリソースを出力するためのストリームを開いて返す。
     * @param packageNameOrNull 対象のパッケージ
     * @param subPath パッケージ下のサブパス
     * @return 対象リソースに書き出すためのストリーム
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public OutputStream openResource(Name packageNameOrNull, String subPath) throws IOException {
        Precondition.checkMustNotBeNull(subPath, "subPath"); //$NON-NLS-1$
        return config.getPackager().openStream(packageNameOrNull, subPath);
    }

    /**
     * サービスをロードするためのクラスローダを返す。
     * @return サービスをロードするためのクラスローダ
     */
    public ClassLoader getServiceClassLoader() {
        return config.getServiceClassLoader();
    }

    /**
     * コンパイラのオプション設定を返す。
     * @return コンパイラのオプション設定
     */
    public FlowCompilerOptions getOptions() {
        return config.getOptions();
    }

    /**
     * この環境に対してエラーメッセージを追加する。
     * @param format メッセージのフォーマット ({@link MessageFormat}形式)
     * @param args メッセージの引数、空の配列を指定した場合は{@code format}がそのままメッセージとなる
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void error(String format, Object...args) {
        Precondition.checkMustNotBeNull(format, "format"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(args, "args"); //$NON-NLS-1$
        String text = format(format, args);
        LOG.error(text);
        if (firstError == null) {
            firstError = text;
        }
    }

    private String format(String format, Object[] args) {
        assert format != null;
        assert args != null;
        if (args.length == 0) {
            return format;
        }
        return MessageFormat.format(format, args);
    }

    /**
     * この環境オブジェクトを取って初期化を行うインターフェース。
     */
    public interface Initializable {

        /**
         * このオブジェクトを初期化する。
         * @param environment 環境オブジェクト
         */
        void initialize(FlowCompilingEnvironment environment);
    }

    /**
     * {@link Initializable}の骨格実装。
     */
    public abstract static class Initialized implements Initializable {

        /**
         * コンパイル環境。
         */
        private FlowCompilingEnvironment environment;

        @Override
        public final void initialize(FlowCompilingEnvironment env) {
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
        protected FlowCompilingEnvironment getEnvironment() {
            return environment;
        }
    }
}
