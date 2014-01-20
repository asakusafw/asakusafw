/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.util.Map;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttributeProvider;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;

/**
 * ラインの末尾に配置される演算子を処理する。
 */
public abstract class LineEndProcessor extends LineProcessor {

    @Override
    public final Kind getKind() {
        return Kind.LINE_END;
    }

    /**
     * このプロセッサによる処理を実行する。
     * @param context 文脈オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public abstract void emitLineEnd(Context context);

    /**
     * 処理の文脈を表す。
     */
    public static class Context extends LineProcessorContext {

        private final Expression input;

        private final Map<FlowElementPortDescription, Expression> outputs;

        /**
         * インスタンスを生成する。
         * @param environment 環境
         * @param element target element
         * @param importer インポート
         * @param names 名前生成
         * @param desc 演算子の定義記述
         * @param input 演算子への入力
         * @param outputs 出力ポートごとに割り当てられた結果オブジェクトの式
         * @param resources リソースと式の対応表
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Context(
                FlowCompilingEnvironment environment,
                FlowElementAttributeProvider element,
                ImportBuilder importer,
                NameGenerator names,
                OperatorDescription desc,
                Expression input,
                Map<FlowElementPortDescription, Expression> outputs,
                Map<FlowResourceDescription, Expression> resources) {
            super(environment, element, importer, names, desc, resources);
            Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(outputs, "outputs"); //$NON-NLS-1$
            this.input = input;
            this.outputs = outputs;
        }

        /**
         * 現在の文脈における入力データを表す式を返す。
         * @return 入力データを表す式
         */
        public Expression getInput() {
            return input;
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
                throw new IllegalArgumentException();
            }
            return new ResultMirror(factory, result);
        }
    }
}
