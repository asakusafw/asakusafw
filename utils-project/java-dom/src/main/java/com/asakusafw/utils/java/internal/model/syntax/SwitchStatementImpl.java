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
package com.asakusafw.utils.java.internal.model.syntax;

import java.util.List;

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.SwitchStatement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link SwitchStatement}の実装。
 */
public final class SwitchStatementImpl extends ModelRoot implements SwitchStatement {

    /**
     * セレクタ式。
     */
    private Expression expression;

    /**
     * {@code switch}文の本体。
     */
    private List<? extends Statement> statements;

    @Override
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * セレクタ式を設定する。
     * @param expression
     *     セレクタ式
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    public void setExpression(Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        this.expression = expression;
    }

    @Override
    public List<? extends Statement> getStatements() {
        return this.statements;
    }

    /**
     * {@code switch}文の本体を設定する。
     * <p> 本体に一つもラベルが指定されない場合、引数には空を指定する。 </p>
     * @param statements
     *     {@code switch}文の本体
     * @throws IllegalArgumentException
     *     {@code statements}に{@code null}が指定された場合
     */
    public void setStatements(List<? extends Statement> statements) {
        Util.notNull(statements, "statements"); //$NON-NLS-1$
        Util.notContainNull(statements, "statements"); //$NON-NLS-1$
        this.statements = Util.freeze(statements);
    }

    /**
     * この要素の種類を表す{@link ModelKind#SWITCH_STATEMENT}を返す。
     * @return {@link ModelKind#SWITCH_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.SWITCH_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitSwitchStatement(this, context);
    }
}
