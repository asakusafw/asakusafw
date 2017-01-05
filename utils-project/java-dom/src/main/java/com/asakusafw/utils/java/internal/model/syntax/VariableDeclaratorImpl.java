/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.VariableDeclarator;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link VariableDeclarator}.
 */
public final class VariableDeclaratorImpl extends ModelRoot implements VariableDeclarator {

    private SimpleName name;

    private int extraDimensions;

    private Expression initializer;

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * Sets the variable name.
     * @param name the variable name
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public int getExtraDimensions() {
        return this.extraDimensions;
    }

    /**
     * Sets the extra variable dimensions.
     * @param extraDimensions the extra variable dimensions
     * @throws IllegalArgumentException if {@code extraDimensions} was negative value
     */
    public void setExtraDimensions(int extraDimensions) {
        this.extraDimensions = extraDimensions;
    }

    @Override
    public Expression getInitializer() {
        return this.initializer;
    }

    /**
     * Sets the variable initializer expression.
     * @param initializer the variable initializer expression, or {@code null} if it is not specified
     */
    public void setInitializer(Expression initializer) {
        this.initializer = initializer;
    }

    /**
     * Returns {@link ModelKind#VARIABLE_DECLARATOR} which represents this element kind.
     * @return {@link ModelKind#VARIABLE_DECLARATOR}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.VARIABLE_DECLARATOR;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitVariableDeclarator(this, context);
    }
}
