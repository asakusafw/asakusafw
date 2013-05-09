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
package com.asakusafw.utils.java.internal.model.syntax;

import com.asakusafw.utils.java.model.syntax.Block;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SynchronizedStatement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link SynchronizedStatement}の実装。
 */
public final class SynchronizedStatementImpl extends ModelRoot implements SynchronizedStatement {

    /**
     * 同期オブジェクト。
     */
    private Expression expression;

    /**
     * 本体ブロック。
     */
    private Block body;

    @Override
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 同期オブジェクトを設定する。
     * @param expression
     *     同期オブジェクト
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    public void setExpression(Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        this.expression = expression;
    }

    @Override
    public Block getBody() {
        return this.body;
    }

    /**
     * 本体ブロックを設定する。
     * @param body
     *     本体ブロック
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    public void setBody(Block body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    /**
     * この要素の種類を表す{@link ModelKind#SYNCHRONIZED_STATEMENT}を返す。
     * @return {@link ModelKind#SYNCHRONIZED_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.SYNCHRONIZED_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitSynchronizedStatement(this, context);
    }
}
