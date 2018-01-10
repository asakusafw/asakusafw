/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import com.asakusafw.utils.java.model.syntax.MethodInvocationExpression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link MethodInvocationExpression}.
 */
public final class MethodInvocationExpressionImpl extends ModelRoot implements MethodInvocationExpression {

    private Expression qualifier;

    private List<? extends Type> typeArguments;

    private SimpleName name;

    private List<? extends Expression> arguments;

    @Override
    public Expression getQualifier() {
        return this.qualifier;
    }

    /**
     * Sets the qualifier expression or type.
     * @param qualifier the qualifier expression or type, or {@code null} if this represents a simple method invocation
     */
    public void setQualifier(Expression qualifier) {
        this.qualifier = qualifier;
    }

    @Override
    public List<? extends Type> getTypeArguments() {
        return this.typeArguments;
    }

    /**
     * Sets the type arguments.
     * @param typeArguments the type arguments
     * @throws IllegalArgumentException if {@code typeArguments} was {@code null}
     */
    public void setTypeArguments(List<? extends Type> typeArguments) {
        Util.notNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notContainNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        this.typeArguments = Util.freeze(typeArguments);
    }

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * Sets the target method name.
     * @param name the target method name
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public List<? extends Expression> getArguments() {
        return this.arguments;
    }

    /**
     * Sets the actual arguments.
     * @param arguments the actual arguments
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    public void setArguments(List<? extends Expression> arguments) {
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        Util.notContainNull(arguments, "arguments"); //$NON-NLS-1$
        this.arguments = Util.freeze(arguments);
    }

    /**
     * Returns {@link ModelKind#METHOD_INVOCATION_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#METHOD_INVOCATION_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.METHOD_INVOCATION_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitMethodInvocationExpression(this, context);
    }
}
