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
import com.asakusafw.utils.java.model.syntax.InterfaceDeclaration;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link InterfaceDeclaration}.
 */
public final class InterfaceDeclarationImpl extends ModelRoot implements InterfaceDeclaration {

    private Javadoc javadoc;

    private List<? extends Attribute> modifiers;

    private SimpleName name;

    private List<? extends TypeParameterDeclaration> typeParameters;

    private List<? extends Type> superInterfaceTypes;

    private List<? extends TypeBodyDeclaration> bodyDeclarations;

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
     * Sets the simple type name.
     * @param name the simple type name
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public List<? extends TypeParameterDeclaration> getTypeParameters() {
        return this.typeParameters;
    }

    /**
     * Sets the type parameter declarations.
     * @param typeParameters the type parameter declarations
     * @throws IllegalArgumentException if {@code typeParameters} was {@code null}
     */
    public void setTypeParameters(List<? extends TypeParameterDeclaration> typeParameters) {
        Util.notNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        Util.notContainNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        this.typeParameters = Util.freeze(typeParameters);
    }

    @Override
    public List<? extends Type> getSuperInterfaceTypes() {
        return this.superInterfaceTypes;
    }

    /**
     * Sets the super interface types.
     * @param superInterfaceTypes the super interface types
     * @throws IllegalArgumentException if {@code superInterfaceTypes} was {@code null}
     */
    public void setSuperInterfaceTypes(List<? extends Type> superInterfaceTypes) {
        Util.notNull(superInterfaceTypes, "superInterfaceTypes"); //$NON-NLS-1$
        Util.notContainNull(superInterfaceTypes, "superInterfaceTypes"); //$NON-NLS-1$
        this.superInterfaceTypes = Util.freeze(superInterfaceTypes);
    }

    @Override
    public List<? extends TypeBodyDeclaration> getBodyDeclarations() {
        return this.bodyDeclarations;
    }

    /**
     * Sets the type member declarations.
     * @param bodyDeclarations the type member declarations
     * @throws IllegalArgumentException if {@code bodyDeclarations} was {@code null}
     */
    public void setBodyDeclarations(List<? extends TypeBodyDeclaration> bodyDeclarations) {
        Util.notNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        Util.notContainNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        this.bodyDeclarations = Util.freeze(bodyDeclarations);
    }

    /**
     * Returns {@link ModelKind#INTERFACE_DECLARATION} which represents this element kind.
     * @return {@link ModelKind#INTERFACE_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.INTERFACE_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitInterfaceDeclaration(this, context);
    }
}
