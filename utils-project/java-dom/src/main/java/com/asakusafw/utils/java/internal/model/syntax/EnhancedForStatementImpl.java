/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
 * An implementation of {@link EnhancedForStatement}.
 */
public final class EnhancedForStatementImpl extends ModelRoot implements EnhancedForStatement {

    private FormalParameterDeclaration parameter;

    private Expression expression;

    private Statement body;

    @Override
    public FormalParameterDeclaration getParameter() {
        return this.parameter;
    }

    /**
     * Sets the loop variable declaration.
     * @param parameter the loop variable declaration
     * @throws IllegalArgumentException if {@code parameter} was {@code null}
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
     * Sets the loop target expression.
     * @param expression the loop target expression
     * @throws IllegalArgumentException if {@code expression} was {@code null}
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
     * Sets the loop body.
     * @param body the loop body
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    public void setBody(Statement body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    /**
     * Returns {@link ModelKind#ENHANCED_FOR_STATEMENT} which represents this element kind.
     * @return {@link ModelKind#ENHANCED_FOR_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ENHANCED_FOR_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitEnhancedForStatement(this, context);
    }
}
