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
package com.asakusafw.compiler.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.ImportBuilder;
import com.ashigeru.lang.java.model.util.Models;

/**
 * ラインに配置される演算子を処理する。
 */
public abstract class LineProcessor extends AbstractFlowElementProcessor {

    /**
     * ラインに配置される演算子の処理文脈の基底となるクラス。
     */
    public abstract static class LineProcessorContext extends AbstractProcessorContext {

        /**
         * 生成された文の一覧。
         */
        protected final List<Statement> generatedStatements;

        /**
         * インスタンスを生成する。
         * @param environment 環境
         * @param importer インポート
         * @param names 名前生成
         * @param desc 演算子の定義記述
         * @param resources リソースと式の対応表
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        protected LineProcessorContext(
                FlowCompilingEnvironment environment,
                ImportBuilder importer,
                NameGenerator names,
                OperatorDescription desc,
                Map<FlowResourceDescription, Expression> resources) {
            super(environment, importer, names, desc, resources);
            this.generatedStatements = new ArrayList<Statement>();
        }

        /**
         * 指定の文を追加する。
         * @param statement 追加する文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public void add(Statement statement) {
            Precondition.checkMustNotBeNull(statement, "statement"); //$NON-NLS-1$
            generatedStatements.add(statement);
        }

        /**
         * ここまでにこの文脈で生成された文の一覧を返す。
         * @return この文脈で生成された文の一覧
         */
        public List<Statement> getGeneratedStatements() {
            return generatedStatements;
        }

        /**
         * 指定の型を持つローカル変数宣言を追加する。
         * @param type 対象の型
         * @param initializer 初期化子 (省略可)
         * @return 生成したローカル変数を参照するための式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Expression createLocalVariable(java.lang.reflect.Type type, Expression initializer) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            return createLocalVariable(Models.toType(factory, type), initializer);
        }

        /**
         * 指定の型を持つローカル変数宣言を追加する。
         * @param type 対象の型
         * @param initializer 初期化子 (省略可)
         * @return 生成したローカル変数を参照するための式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Expression createLocalVariable(Type type, Expression initializer) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            SimpleName name = names.create("v");
            add(new ExpressionBuilder(factory, initializer)
                .toLocalVariableDeclaration(importer.resolve(type), name));
            return name;
        }
    }
}
