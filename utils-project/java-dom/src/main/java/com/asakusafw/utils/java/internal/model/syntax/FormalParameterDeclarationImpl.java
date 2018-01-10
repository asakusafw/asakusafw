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

import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link FormalParameterDeclaration}.
 */
public final class FormalParameterDeclarationImpl extends ModelRoot implements FormalParameterDeclaration {

    private List<? extends Attribute> modifiers;

    private Type type;

    private boolean variableArity;

    private SimpleName name;

    private int extraDimensions;

    @Override
    public List<? extends Attribute> getModifiers() {
        return this.modifiers;
    }

    /**
     * Sets the modifiers and annotations.
     * @param modifiers the modifiers and annotations
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     */
    public void setModifiers(List<? extends Attribute> modifiers) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        this.modifiers = Util.freeze(modifiers);
    }

    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * Sets the variable type.
     * @param type the variable type
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    public void setType(Type type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    @Override
    public boolean isVariableArity() {
        return this.variableArity;
    }

    /**
     * Sets whether the parameter is variable arity or not.
     * @param variableArity {@code true} if the parameter is variable arity, otherwise {@code false}
     */
    public void setVariableArity(boolean variableArity) {
        this.variableArity = variableArity;
    }

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * Sets the parameter name.
     * @param name the parameter name
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

    /**
     * Returns {@link ModelKind#FORMAL_PARAMETER_DECLARATION} which represents this element kind.
     * @return {@link ModelKind#FORMAL_PARAMETER_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.FORMAL_PARAMETER_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitFormalParameterDeclaration(this, context);
    }
}
