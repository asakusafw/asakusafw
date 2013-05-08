/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.OutputFormat;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * {@link ImporterDescription}と{@link ExporterDescription}を処理する。
 * <p>
 * このクラスのメソッドに渡される全ての{@link InputDescription}に含まれる
 * {@link ImporterDescription}は、{@link #getImporterDescriptionType()}
 * が返すクラスのサブクラスであることが保証される。
 * 同様に、メソッドに渡される全ての{@link OutputDescription}に含まれる
 * {@link ExporterDescription}は、常に{@link #getImporterDescriptionType()}
 * が返すクラスのサブクラスであることが保証される。
 * </p>
 */
public abstract class ExternalIoDescriptionProcessor extends FlowCompilingEnvironment.Initialized {

    /**
     * このプロセッサが対象とする{@link ImporterDescription}の種類を返す。
     * @return 対象とする{@code ImporterDescription}の種類
     */
    public abstract Class<? extends ImporterDescription> getImporterDescriptionType();

    /**
     * このプロセッサが対象とする{@link ExporterDescription}の種類を返す。
     * @return 対象とする{@code ExporterDescription}の種類
     */
    public abstract Class<? extends ExporterDescription> getExporterDescriptionType();

    /**
     * このプロセッサが対象とする入出力の妥当性を検査する。
     * @param inputs 入力の一覧
     * @param outputs 出力の一覧
     * @return 入出力に問題が無い場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public abstract boolean validate(List<InputDescription> inputs, List<OutputDescription> outputs);

    /**
     * Returns source information for the input.
     * @param description target description
     * @return resolved information
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract SourceInfo getInputInfo(InputDescription description);

    /**
     * 指定の入力に対して、前処理を実行するクライアントプログラムを返す。
     * <p>
     * {@link #getInputInfo(InputDescription)}は、この前処理が
     * <em>完了した後</em>のパスを指定する必要がある。
     * </p>
     * <p>
     * 前処理が不要である場合、このメソッドは空のリストを返す。
     * </p>
     * @param context 文脈情報
     * @return 前処理を実行するクライアントプログラムの一覧
     * @throws IOException プログラムの出力に失敗した場合
     */
    public List<CompiledStage> emitPrologue(IoContext context) throws IOException {
        return Collections.emptyList();
    }

    /**
     * 指定の出力に対して、後処理を実行するクライアントプログラムの限定名を返す。
     * <p>
     * 後処理が不要である場合、このメソッドは空のリストを返す。
     * </p>
     * @param context 文脈情報
     * @return 後処理を実行するクライアントプログラムの一覧
     * @throws IOException プログラムの出力に失敗した場合
     */
    public List<CompiledStage> emitEpilogue(IoContext context) throws IOException {
        return Collections.emptyList();
    }

    /**
     * 出力するパッケージに対して、このインポーターおよびエクスポーターに対する任意の処理を実行する。
     * @param context 文脈情報
     * @throws IOException 処理に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void emitPackage(IoContext context) throws IOException {
        return;
    }

    /**
     * I/O処理を行うコマンドプロバイダーを生成して返す。
     * @param context 文脈情報
     * @return 生成したコマンドプロバイダー
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ExternalIoCommandProvider createCommandProvider(IoContext context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        return new ExternalIoCommandProvider();
    }

    /**
     * IOに関する文脈情報。
     */
    public static class IoContext {

        private final List<Input> inputs;

        private final List<Output> outputs;

        /**
         * インスタンスを生成する。
         * @param inputs 処理対象の入力一覧 (関連するもののみ)
         * @param outputs 処理対象の出力一覧 (関連するもののみ)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public IoContext(List<Input> inputs, List<Output> outputs) {
            this.inputs = inputs;
            this.outputs = outputs;
        }

        /**
         * 処理中のフローに対する入力一覧(関係するもののみ)を返す。
         * @return 処理中のフローに対する入力一覧
         */
        public List<Input> getInputs() {
            return inputs;
        }

        /**
         * 処理中のフローに対する出力一覧(関係するもののみ)を返す。
         * @return 処理中のフローに対する出力一覧
         */
        public List<Output> getOutputs() {
            return outputs;
        }
    }

    /**
     * 出力を表す。
     */
    public static class Output {

        private final OutputDescription description;

        private final List<SourceInfo> sources;

        /**
         * インスタンスを生成する。
         * @param description 出力要素の記述
         * @param sources この出力の元となる情報の一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Output(OutputDescription description, List<SourceInfo> sources) {
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(sources, "sources"); //$NON-NLS-1$
            this.description = description;
            this.sources = sources;
        }

        /**
         * 出力要素の記述を返す。
         * @return 出力要素の記述
         */
        public OutputDescription getDescription() {
            return description;
        }

        /**
         * この出力の元となる情報の一覧を返す。
         * @return この出力の元となる情報の一覧
         */
        public List<SourceInfo> getSources() {
            return sources;
        }
    }

    /**
     * 入力を表す。
     */
    public static class Input {

        private final InputDescription description;

        private final Class<? extends OutputFormat<?, ?>> format;

        /**
         * インスタンスを生成する。
         * @param description 入力要素の記述
         * @param format 入力を配置する際のフォーマット
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Input(InputDescription description, Class<? extends OutputFormat> format) {
            this.description = description;
            this.format = (Class<? extends OutputFormat<?, ?>>) format;
        }

        /**
         * 入力要素の記述を返す。
         * @return 入力要素の記述
         */
        public InputDescription getDescription() {
            return description;
        }

        /**
         * 入力を配置する際のフォーマットを返す。
         * @return 入力を配置する際のフォーマット
         */
        public Class<? extends OutputFormat<?, ?>> getFormat() {
            return format;
        }
    }

    /**
     * 入出力の元となる情報。
     */
    public static class SourceInfo {

        private final Set<Location> locations;

        private final Class<? extends InputFormat<?, ?>> format;

        private final Map<String, String> attributes;

        /**
         * Creates a new instance.
         * @param locations input source locations
         * @param format input format
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        @SuppressWarnings({ "rawtypes" })
        public SourceInfo(
                Set<Location> locations,
                Class<? extends InputFormat> format) {
            this(locations, format, Collections.<String, String>emptyMap());
        }

        /**
         * Creates a new instance.
         * @param locations input source locations
         * @param format input format
         * @param attributes attributes
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public SourceInfo(
                Set<Location> locations,
                Class<? extends InputFormat> format,
                Map<String, String> attributes) {
            Precondition.checkMustNotBeNull(locations, "locations"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(format, "format"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(attributes, "attributes"); //$NON-NLS-1$
            this.locations = locations;
            this.format = (Class<? extends InputFormat<?, ?>>) format;
            this.attributes = Maps.freeze(attributes);
        }

        /**
         * 入力元の位置を返す。
         * @return 入力元の位置
         */
        public Set<Location> getLocations() {
            return locations;
        }

        /**
         * 入力フォーマットを返す。
         * @return 入力フォーマット
         */
        public Class<? extends InputFormat<?, ?>> getFormat() {
            return format;
        }

        /**
         * Returns the attributes.
         * @return the attributes
         */
        public Map<String, String> getAttributes() {
            return attributes;
        }
    }

    /**
     * {@link ExternalIoDescriptionProcessor}を取得するためのリポジトリ。
     */
    public interface Repository extends FlowCompilingEnvironment.Initializable {

        /**
         * 指定の入力記述に対するプロセッサーを返す。
         * @param description 対象の入力記述
         * @return 対応するプロセッサー、存在しない場合は{@code null}
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        ExternalIoDescriptionProcessor findProcessor(InputDescription description);

        /**
         * 指定の出力記述に対するプロセッサーを返す。
         * @param description 対象の入力記述
         * @return 対応するプロセッサー、存在しない場合は{@code null}
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        ExternalIoDescriptionProcessor findProcessor(OutputDescription description);
    }
}
