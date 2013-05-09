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

import com.asakusafw.utils.java.model.syntax.EnhancedForStatement;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link EnhancedForStatement}の実装。
 */
public final class EnhancedForStatementImpl extends ModelRoot implements EnhancedForStatement {

    /**
     * ループ変数。
     */
    private FormalParameterDeclaration parameter;

    /**
     * ループ対象式。
     */
    private Expression expression;

    /**
     * ループ本体。
     */
    private Statement body;

    @Override
    public FormalParameterDeclaration getParameter() {
        return this.parameter;
    }

    /**
     * ループ変数を設定する。
     * @param parameter
     *     ループ変数
     * @throws IllegalArgumentException
     *     {@code parameter}に{@code null}が指定された場合
     */
    public void setParameter(FormalParameterDeclaration parameter) {
        Util.notNull(parameter, "parameter"); //$NON-NLS-1$
        this.parameter = parameter;
    }

    @Override
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * ループ対象式を設定する。
     * @param expression
     *     ループ対象式
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    public void setExpression(Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        this.expression = expression;
    }

    @Override
    public Statement getBody() {
        return this.body;
    }

    /**
     * ループ本体を設定する。
     * @param body
     *     ループ本体
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    public void setBody(Statement body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    /**
     * この要素の種類を表す{@link ModelKind#ENHANCED_FOR_STATEMENT}を返す。
     * @return {@link ModelKind#ENHANCED_FOR_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ENHANCED_FOR_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitEnhancedForStatement(this, context);
    }
}
