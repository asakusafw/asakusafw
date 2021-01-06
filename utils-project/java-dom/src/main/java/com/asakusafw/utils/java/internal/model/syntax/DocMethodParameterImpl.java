/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.DocMethodParameter;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link DocMethodParameter}.
 */
public final class DocMethodParameterImpl extends ModelRoot implements DocMethodParameter {

    private Type type;

    private SimpleName name;

    private boolean variableArity;

    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * Sets the parameter type.
     * @param type the parameter type
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    public void setType(Type type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * Sets the parameter name.
     * @param name the parameter name, or {@code null} if it is not specified
     */
    public void setName(SimpleName name) {
        this.name = name;
    }

    @Override
    public boolean isVariableArity() {
        return this.variableArity;
    }

    /**
     * Sets whether this parameter is variable arity or not.
     * @param variableArity {@code true} if this parameter is variable arity, otherwise {@code false}
     */
    public void setVariableArity(boolean variableArity) {
        this.variableArity = variableArity;
    }

    /**
     * Returns {@link ModelKind#DOC_METHOD_PARAMETER} which represents this element kind.
     * @return {@link ModelKind#DOC_METHOD_PARAMETER}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.DOC_METHOD_PARAMETER;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitDocMethodParameter(this, context);
    }
}
