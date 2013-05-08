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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttributeProvider;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;

/**
 * 合流地点に配置される演算子を処理する。
 */
public abstract class RendezvousProcessor extends AbstractFlowElementProcessor {

    @Override
    public final Kind getKind() {
        return Kind.RENDEZVOUS;
    }

    /**
     * 指定のポートに対するシャッフルの情報を返す。
     * <p>
     * この情報は、次の構造で成り立っている。
     * </p>
     * <ul>
     * <li> シャッフル時に転送するデータの型 </li>
     * <li> シャッフル時に転送するデータのグループかおよびソート条件 </li>
     * <li> この演算子への入力をシャッフル時に転送するデータへ変換する{@link LinePartProcessor} </li>
     * </ul>
     * <p>
     * なお、この実装では演算子のポートに入力されたデータを、そのままシャッフルフェーズに渡す。
     * </p>
     * @param element 対象の要素
     * @param port 対象のポート
     * @return シャッフルの情報
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ShuffleDescription getShuffleDescription(
            FlowElementDescription element,
            FlowElementPortDescription port) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
        LinePartProcessor nop = new LinePartProcessor.Nop();
        nop.initialize(getEnvironment());
        return new ShuffleDescription(
                port.getDataType(),
                port.getShuffleKey(),
                nop);
    }

    /**
     * このプロセッサによる処理を実行する。
     * @param context 文脈オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public abstract void emitRendezvous(Context context);

    /**
     * 指定の演算子が部分的な合流処理を行える場合のみ{@code true}を返す。
     * <p>
     * このメソッドが{@code true}を返す場合、次の要件を全て満たす必要がある。
     * </p>
     * <ul>
     * <li> FlowResourceを利用しない </li>
     * <li> 入力と出力が次の関係を満たす:
     *   <ul>
     *   <li> 個数が一致 </li>
     *   <li> 同じ位置の入力と出力の型が同一 </li>
     *   <li> TODO 形式化 </li>
     *   </ul>
     * </li>
     * </ul>
     * @param description 対象の演算子
     * @return 部分的な合流処理を行える場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public boolean isPartial(FlowElementDescription description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        return false;
    }

    /**
     * 処理の文脈を表す。
     */
    public static class Context extends AbstractProcessorContext {

        private final Map<FlowElementPortDescription, Expression> inputs;

        private final Map<FlowElementPortDescription, Expression> outputs;

        private final List<Statement> beginStatements;

        private final Map<FlowElementPortDescription, List<Statement>> processStatements;

        private final List<Statement> endStatements;

        /**
         * インスタンスを生成する。
         * @param environment 環境
         * @param element target element
         * @param importer インポート
         * @param names 名前生成
         * @param desc 演算子の定義記述
         * @param inputs 入力と対応するデータオブジェクトの式
         * @param outputs 出力と対応する結果オブジェクトの式
         * @param resources リソースと式の対応表
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Context(
                FlowCompilingEnvironment environment,
                FlowElementAttributeProvider element,
                ImportBuilder importer,
                NameGenerator names,
                OperatorDescription desc,
                Map<FlowElementPortDescription, Expression> inputs,
                Map<FlowElementPortDescription, Expression> outputs,
                Map<FlowResourceDescription, Expression> resources) {
            super(environment, element, importer, names, desc, resources);
            Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(outputs, "outputs"); //$NON-NLS-1$
            this.inputs = inputs;
            this.outputs = outputs;
            this.beginStatements = Lists.create();
            this.processStatements = Maps.create();
            this.endStatements = Lists.create();
            for (FlowElementPortDescription input : inputs.keySet()) {
                processStatements.put(input, new ArrayList<Statement>());
            }
        }

        /**
         * 現在の文脈において、指定のポートに対する演算子の結果オブジェクトを返す。
         * @param port 対象のポート
         * @return 対応する結果オブジェクト
         * @throws IllegalArgumentException 指定のポートを発見できない場合
         */
        public ResultMirror getOutput(FlowElementPortDescription port) {
            Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
            Expression result = outputs.get(port);
            if (result == null) {
                throw new IllegalArgumentException(port.toString());
            }
            return new ResultMirror(factory, result);
        }

        /**
         * 指定のポートに対する{@code process phase}で利用する入力データを表す式を返す。
         * @param port 対象のポート
         * @return {@code begin phase}で利用する入力データを表す式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Expression getProcessInput(FlowElementPortDescription port) {
            Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
            return getCommonInput(port);
        }

        private Expression getCommonInput(FlowElementPortDescription port) {
            assert port != null;
            Expression input = inputs.get(port);
            if (input == null) {
                throw new IllegalArgumentException(port.toString());
            }
            return input;
        }

        /**
         * 指定のポートに対する{@code begin phase}に関する文を追加する。
         * @param statement 追加する文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public void addBegin(Statement statement) {
            Precondition.checkMustNotBeNull(statement, "statement"); //$NON-NLS-1$
            beginStatements.add(statement);
        }

        /**
         * 指定のポートに対する{@code process phase}に関する文を追加する。
         * @param port 対象のポート
         * @param statement 追加する文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public void addProcess(FlowElementPortDescription port, Statement statement) {
            Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(statement, "statement"); //$NON-NLS-1$
            List<Statement> statements = processStatements.get(port);
            if (statements == null) {
                throw new IllegalArgumentException(port.toString());
            }
            statements.add(statement);
        }

        /**
         * {@code end phase}に関する文を追加する。
         * @param statement 追加する文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public void addEnd(Statement statement) {
            Precondition.checkMustNotBeNull(statement, "statement"); //$NON-NLS-1$
            endStatements.add(statement);
        }

        /**
         * この文脈に追加された{@code begin phase}に関する文を返す。
         * @return 追加された文の一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public List<Statement> getBeginStatements() {
            return beginStatements;
        }

        /**
         * この文脈に追加された{@code process phase}に関する文を返す。
         * @param port 対象の入力ポート
         * @return 追加された文の一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public List<Statement> getProcessStatements(FlowElementPortDescription port) {
            Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
            List<Statement> statements = processStatements.get(port);
            if (statements == null) {
                throw new IllegalArgumentException(port.toString());
            }
            return statements;
        }

        /**
         * この文脈に追加された{@code end phase}に関する文を返す。
         * @return 追加された文の一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public List<Statement> getEndStatements() {
            return endStatements;
        }
    }
}
