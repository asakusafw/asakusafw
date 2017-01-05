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

import java.util.List;

import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.ClassBody;
import com.asakusafw.utils.java.model.syntax.EnumConstantDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link EnumConstantDeclaration}.
 */
public final class EnumConstantDeclarationImpl extends ModelRoot implements EnumConstantDeclaration {

    private Javadoc javadoc;

    private List<? extends Attribute> modifiers;

    private SimpleName name;

    private List<? extends Expression> arguments;

    private ClassBody body;

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
    public SimpleName getName() {
        return this.name;
    }

    /**
     * Sets the enum constant name.
     * @param name the enum constant name
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
     * Sets the constructor arguments.
     * @param arguments the constructor arguments
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
     * Sets the class body.
     * @param body the class body, or {@code null} if it is not specified
     */
    public void setBody(ClassBody body) {
        this.body = body;
    }

    /**
     * Returns {@link ModelKind#ENUM_CONSTANT_DECLARATION} which represents this element kind.
     * @return {@link ModelKind#ENUM_CONSTANT_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ENUM_CONSTANT_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitEnumConstantDeclaration(this, context);
    }
}
