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

import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.VariableDeclarator;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link FieldDeclaration}.
 */
public final class FieldDeclarationImpl extends ModelRoot implements FieldDeclaration {

    private Javadoc javadoc;

    private List<? extends Attribute> modifiers;

    private Type type;

    private List<? extends VariableDeclarator> variableDeclarators;

    @Override
    public Javadoc getJavadoc() {
        return this.javadoc;
    }

    /**
     * Sets the documentation comment.
     * @param javadoc the documentation comment, or {@code null} if it is not specified
     */
    public void setJavadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
    }

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
     * Sets the field type.
     * @param type the field type
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    public void setType(Type type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    @Override
    public List<? extends VariableDeclarator> getVariableDeclarators() {
        return this.variableDeclarators;
    }

    /**
     * Sets the field variable declarators.
     * @param variableDeclarators the field variable declarators
     * @throws IllegalArgumentException if {@code variableDeclarators} was {@code null}
     * @throws IllegalArgumentException if {@code variableDeclarators} was empty
     */
    public void setVariableDeclarators(List<? extends VariableDeclarator> variableDeclarators) {
        Util.notNull(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        Util.notContainNull(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        Util.notEmpty(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        this.variableDeclarators = Util.freeze(variableDeclarators);
    }

    /**
     * Returns {@link ModelKind#FIELD_DECLARATION} which represents this element kind.
     * @return {@link ModelKind#FIELD_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.FIELD_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitFieldDeclaration(this, context);
    }
}
