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

import java.lang.annotation.Annotation;
import java.util.Map;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttributeProvider;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Identity;

/**
 * ラインのいずれかに配置される演算子を処理する。
 */
public abstract class LinePartProcessor extends LineProcessor {

    @Override
    public final Kind getKind() {
        return Kind.LINE_PART;
    }

    /**
     * このプロセッサによる処理を実行する。
     * @param context 文脈オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public abstract void emitLinePart(Context context);

    /**
     * 処理の文脈を表す。
     */
    public static class Context extends LineProcessorContext {

        private final Expression input;

        private Expression resultValue;

        /**
         * インスタンスを生成する。
         * @param environment 環境
         * @param element target element
         * @param importer インポート
         * @param names 名前生成
         * @param desc 演算子の定義記述
         * @param input 演算子への入力
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
                Map<FlowResourceDescription, Expression> resources) {
            super(environment, element, importer, names, desc, resources);
            Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
            this.input = input;
            this.resultValue = null;
        }

        /**
         * 現在の文脈における入力データを表す式を返す。
         * @return 入力データを表す式
         */
        public Expression getInput() {
            return input;
        }

        /**
         * 現在の文脈において、指定の式を演算子の適用結果に設定する。
         * @param expresion 演算子の適用結果を表す式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public void setOutput(Expression expresion) {
            Precondition.checkMustNotBeNull(expresion, "expresion"); //$NON-NLS-1$
            if (this.resultValue != null) {
                throw new IllegalStateException();
            }
            this.resultValue = expresion;
        }

        /**
         * 現在の文脈において、演算子の適用結果として指定された式を返す。
         * @return 演算子の適用結果として指定された式
         */
        public Expression getOutput() {
            if (resultValue == null) {
                throw new IllegalStateException();
            }
            return resultValue;
        }
    }

    /**
     * 何も行わない{@link LinePartProcessor}の実装。
     */
    @TargetOperator(Identity.class)
    public static class Nop extends LinePartProcessor {
        @Override
        protected Class<? extends Annotation> loadTargetAnnotationType() {
            return Identity.class;
        }
        @Override
        public void emitLinePart(Context context) {
            context.setOutput(context.getInput());
        }
    }
}
