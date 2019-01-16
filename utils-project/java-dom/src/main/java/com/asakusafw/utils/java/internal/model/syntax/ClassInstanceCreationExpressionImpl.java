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

import java.util.List;

import com.asakusafw.utils.java.model.syntax.ClassBody;
import com.asakusafw.utils.java.model.syntax.ClassInstanceCreationExpression;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link ClassInstanceCreationExpression}.
 */
public final class ClassInstanceCreationExpressionImpl extends ModelRoot implements ClassInstanceCreationExpression {

    private Expression qualifier;

    private List<? extends Type> typeArguments;

    private Type type;

    private List<? extends Expression> arguments;

    private ClassBody body;

    @Override
    public Expression getQualifier() {
        return this.qualifier;
    }

    /**
     * Sets the qualifier expression.
     * @param qualifier the qualifier expression, or {@code null} if it is not specified
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
    public Type getType() {
        return this.type;
    }

    /**
     * Sets the target type.
     * @param type the target type
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    public void setType(Type type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
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

    @Override
    public ClassBody getBody() {
        return this.body;
    }

    /**
     * Sets the anonymous class body.
     * @param body the anonymous class body, or {@code null} if the target is not an anonymous class
     */
    public void setBody(ClassBody body) {
        this.body = body;
    }

    /**
     * Returns {@link ModelKind#CLASS_INSTANCE_CREATION_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#CLASS_INSTANCE_CREATION_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.CLASS_INSTANCE_CREATION_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitClassInstanceCreationExpression(this, context);
    }
}
