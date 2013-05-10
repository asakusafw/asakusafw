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

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.IfStatement;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link IfStatement}の実装。
 */
public final class IfStatementImpl extends ModelRoot implements IfStatement {

    /**
     * 条件式。
     */
    private Expression condition;

    /**
     * 条件成立時に実行される文。
     */
    private Statement thenStatement;

    /**
     * 条件不成立時に実行される文。
     */
    private Statement elseStatement;

    @Override
    public Expression getCondition() {
        return this.condition;
    }

    /**
     * 条件式を設定する。
     * @param condition
     *     条件式
     * @throws IllegalArgumentException
     *     {@code condition}に{@code null}が指定された場合
     */
    public void setCondition(Expression condition) {
        Util.notNull(condition, "condition"); //$NON-NLS-1$
        this.condition = condition;
    }

    @Override
    public Statement getThenStatement() {
        return this.thenStatement;
    }

    /**
     * 条件成立時に実行される文を設定する。
     * @param thenStatement
     *     条件成立時に実行される文
     * @throws IllegalArgumentException
     *     {@code thenStatement}に{@code null}が指定された場合
     */
    public void setThenStatement(Statement thenStatement) {
        Util.notNull(thenStatement, "thenStatement"); //$NON-NLS-1$
        this.thenStatement = thenStatement;
    }

    @Override
    public Statement getElseStatement() {
        return this.elseStatement;
    }

    /**
     * 条件不成立時に実行される文を設定する。
     * <p> この文が{@code if-then}文である場合、引数には{@code null}を指定する。 </p>
     * @param elseStatement
     *     条件不成立時に実行される文、
     *     ただしこの文が{@code if-then}文である場合は{@code null}
     */
    public void setElseStatement(Statement elseStatement) {
        this.elseStatement = elseStatement;
    }

    /**
     * この要素の種類を表す{@link ModelKind#IF_STATEMENT}を返す。
     * @return {@link ModelKind#IF_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.IF_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitIfStatement(this, context);
    }
}
