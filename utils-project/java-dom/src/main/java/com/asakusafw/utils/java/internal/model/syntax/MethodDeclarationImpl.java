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
import com.asakusafw.utils.java.model.syntax.Block;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link MethodDeclaration}.
 */
public final class MethodDeclarationImpl extends ModelRoot implements MethodDeclaration {

    private Javadoc javadoc;

    private List<? extends Attribute> modifiers;

    private List<? extends TypeParameterDeclaration> typeParameters;

    private Type returnType;

    private SimpleName name;

    private List<? extends FormalParameterDeclaration> formalParameters;

    private int extraDimensions;

    private List<? extends Type> exceptionTypes;

    private Block body;

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
    public Type getReturnType() {
        return this.returnType;
    }

    /**
     * Sets the return type.
     * @param returnType the return type
     * @throws IllegalArgumentException if {@code returnType} was {@code null}
     */
    public void setReturnType(Type returnType) {
        Util.notNull(returnType, "returnType"); //$NON-NLS-1$
        this.returnType = returnType;
    }

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * Sets the method name.
     * @param name the method name
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public List<? extends FormalParameterDeclaration> getFormalParameters() {
        return this.formalParameters;
    }

    /**
     * Sets the formal parameter declarations.
     * @param formalParameters the formal parameter declarations
     * @throws IllegalArgumentException if {@code formalParameters} was {@code null}
     */
    public void setFormalParameters(List<? extends FormalParameterDeclaration> formalParameters) {
        Util.notNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        Util.notContainNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        this.formalParameters = Util.freeze(formalParameters);
    }

    @Override
    public int getExtraDimensions() {
        return this.extraDimensions;
    }

    /**
     * Sets the number of extra dimensions of the return type.
     * @param extraDimensions the number of extra dimensions of the return type
     * @throws IllegalArgumentException if {@code extraDimensions} was negative value
     */
    public void setExtraDimensions(int extraDimensions) {
        this.extraDimensions = extraDimensions;
    }

    @Override
    public List<? extends Type> getExceptionTypes() {
        return this.exceptionTypes;
    }

    /**
     * Sets the exception types.
     * @param exceptionTypes the exception types
     * @throws IllegalArgumentException if {@code exceptionTypes} was {@code null}
     */
    public void setExceptionTypes(List<? extends Type> exceptionTypes) {
        Util.notNull(exceptionTypes, "exceptionTypes"); //$NON-NLS-1$
        Util.notContainNull(exceptionTypes, "exceptionTypes"); //$NON-NLS-1$
        this.exceptionTypes = Util.freeze(exceptionTypes);
    }

    @Override
    public Block getBody() {
        return this.body;
    }

    /**
     * Sets the method body.
     * @param body the method body, or {@code null} if the method does not have a method body
     */
    public void setBody(Block body) {
        this.body = body;
    }

    /**
     * Returns {@link ModelKind#METHOD_DECLARATION} which represents this element kind.
     * @return {@link ModelKind#METHOD_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.METHOD_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitMethodDeclaration(this, context);
    }
}
