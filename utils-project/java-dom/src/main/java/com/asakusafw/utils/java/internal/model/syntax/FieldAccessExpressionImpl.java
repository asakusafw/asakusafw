/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import com.asakusafw.utils.java.model.syntax.FieldAccessExpression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link FieldAccessExpression}.
 */
public final class FieldAccessExpressionImpl extends ModelRoot implements FieldAccessExpression {

    private Expression qualifier;

    private SimpleName name;

    @Override
    public Expression getQualifier() {
        return this.qualifier;
    }

    /**
     * Sets the qualifier expression.
     * @param qualifier the qualifier expression
     * @throws IllegalArgumentException if {@code qualifier} was {@code null}
     */
    public void setQualifier(Expression qualifier) {
        Util.notNull(qualifier, "qualifier"); //$NON-NLS-1$
        this.qualifier = qualifier;
    }

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * Sets the field name.
     * @param name the field name
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    /**
     * Returns {@link ModelKind#FIELD_ACCESS_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#FIELD_ACCESS_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.FIELD_ACCESS_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitFieldAccessExpression(this, context);
    }
}
