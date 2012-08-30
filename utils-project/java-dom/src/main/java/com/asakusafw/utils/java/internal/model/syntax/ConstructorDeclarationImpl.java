/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link ConstructorDeclaration}の実装。
 */
public final class ConstructorDeclarationImpl extends ModelRoot implements ConstructorDeclaration {

    /**
     * ドキュメンテーションコメント。
     */
    private Javadoc javadoc;

    /**
     * 修飾子および注釈の一覧。
     */
    private List<? extends Attribute> modifiers;

    /**
     * 型引数宣言の一覧。
     */
    private List<? extends TypeParameterDeclaration> typeParameters;

    /**
     * メソッドまたはコンストラクタの名前。
     */
    private SimpleName name;

    /**
     * 仮引数宣言の一覧。
     */
    private List<? extends FormalParameterDeclaration> formalParameters;

    /**
     * 例外型宣言の一覧。
     */
    private List<? extends Type> exceptionTypes;

    /**
     * コンストラクタ本体。
     */
    private Block body;

    @Override
    public Javadoc getJavadoc() {
        return this.javadoc;
    }

    /**
     * ドキュメンテーションコメントを設定する。
     * <p> ドキュメンテーションコメントが存在しない場合、引数には{@code null}を指定する。 </p>
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     */
    public void setJavadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
    }

    @Override
    public List<? extends Attribute> getModifiers() {
        return this.modifiers;
    }

    /**
     * 修飾子および注釈の一覧を設定する。
     * <p> 修飾子または注釈が一つも宣言されない場合、引数には空を指定する。 </p>
     * @param modifiers
     *     修飾子および注釈の一覧
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
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
     * 型引数宣言の一覧を設定する。
     * <p> 型引数が一つも宣言されない場合、引数には空を指定する。 </p>
     * @param typeParameters
     *     型引数宣言の一覧
     * @throws IllegalArgumentException
     *     {@code typeParameters}に{@code null}が指定された場合
     */
    public void setTypeParameters(List<? extends TypeParameterDeclaration> typeParameters) {
        Util.notNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        Util.notContainNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        this.typeParameters = Util.freeze(typeParameters);
    }

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * メソッドまたはコンストラクタの名前を設定する。
     * @param name
     *     メソッドまたはコンストラクタの名前
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
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
     * 仮引数宣言の一覧を設定する。
     * <p> 仮引数が一つも宣言されない場合、引数には空を指定する。 </p>
     * @param formalParameters
     *     仮引数宣言の一覧
     * @throws IllegalArgumentException
     *     {@code formalParameters}に{@code null}が指定された場合
     */
    public void setFormalParameters(List<? extends FormalParameterDeclaration> formalParameters) {
        Util.notNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        Util.notContainNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        this.formalParameters = Util.freeze(formalParameters);
    }

    @Override
    public List<? extends Type> getExceptionTypes() {
        return this.exceptionTypes;
    }

    /**
     * 例外型宣言の一覧を設定する。
     * <p> 例外型が一つも宣言されない場合、引数には空を指定する。 </p>
     * @param exceptionTypes
     *     例外型宣言の一覧
     * @throws IllegalArgumentException
     *     {@code exceptionTypes}に{@code null}が指定された場合
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
     * コンストラクタ本体を設定する。
     * @param body
     *     コンストラクタ本体
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    public void setBody(Block body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    /**
     * この要素の種類を表す{@link ModelKind#CONSTRUCTOR_DECLARATION}を返す。
     * @return {@link ModelKind#CONSTRUCTOR_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.CONSTRUCTOR_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitConstructorDeclaration(this, context);
    }
}
