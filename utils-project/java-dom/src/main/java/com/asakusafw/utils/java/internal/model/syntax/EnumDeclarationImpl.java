/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import com.asakusafw.utils.java.model.syntax.EnumConstantDeclaration;
import com.asakusafw.utils.java.model.syntax.EnumDeclaration;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link EnumDeclaration}の実装。
 */
public final class EnumDeclarationImpl extends ModelRoot implements EnumDeclaration {

    /**
     * ドキュメンテーションコメント。
     */
    private Javadoc javadoc;

    /**
     * 修飾子および注釈の一覧。
     */
    private List<? extends Attribute> modifiers;

    /**
     * 型の単純名。
     */
    private SimpleName name;

    /**
     * 親インターフェースの一覧。
     */
    private List<? extends Type> superInterfaceTypes;

    /**
     * 列挙定数の一覧。
     */
    private List<? extends EnumConstantDeclaration> constantDeclarations;

    /**
     * メンバの一覧。
     */
    private List<? extends TypeBodyDeclaration> bodyDeclarations;

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
    public SimpleName getName() {
        return this.name;
    }

    /**
     * 型の単純名を設定する。
     * @param name
     *     型の単純名
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public List<? extends Type> getSuperInterfaceTypes() {
        return this.superInterfaceTypes;
    }

    /**
     * 親インターフェースの一覧を設定する。
     * <p> 親インターフェースが一つも宣言されない場合、引数には空を指定する。 </p>
     * @param superInterfaceTypes
     *     親インターフェースの一覧
     * @throws IllegalArgumentException
     *     {@code superInterfaceTypes}に{@code null}が指定された場合
     */
    public void setSuperInterfaceTypes(List<? extends Type> superInterfaceTypes) {
        Util.notNull(superInterfaceTypes, "superInterfaceTypes"); //$NON-NLS-1$
        Util.notContainNull(superInterfaceTypes, "superInterfaceTypes"); //$NON-NLS-1$
        this.superInterfaceTypes = Util.freeze(superInterfaceTypes);
    }

    @Override
    public List<? extends EnumConstantDeclaration> getConstantDeclarations() {
        return this.constantDeclarations;
    }

    /**
     * 列挙定数の一覧を設定する。
     * <p> 列挙定数が一つも宣言されない場合、引数には空を指定する。 </p>
     * @param constantDeclarations
     *     列挙定数の一覧
     * @throws IllegalArgumentException
     *     {@code constantDeclarations}に{@code null}が指定された場合
     */
    public void setConstantDeclarations(List<? extends EnumConstantDeclaration> constantDeclarations) {
        Util.notNull(constantDeclarations, "constantDeclarations"); //$NON-NLS-1$
        Util.notContainNull(constantDeclarations, "constantDeclarations"); //$NON-NLS-1$
        this.constantDeclarations = Util.freeze(constantDeclarations);
    }

    @Override
    public List<? extends TypeBodyDeclaration> getBodyDeclarations() {
        return this.bodyDeclarations;
    }

    /**
     * メンバの一覧を設定する。
     * <p> メンバが一つも宣言されない場合、引数には空を指定する。 </p>
     * @param bodyDeclarations
     *     メンバの一覧
     * @throws IllegalArgumentException
     *     {@code bodyDeclarations}に{@code null}が指定された場合
     */
    public void setBodyDeclarations(List<? extends TypeBodyDeclaration> bodyDeclarations) {
        Util.notNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        Util.notContainNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        this.bodyDeclarations = Util.freeze(bodyDeclarations);
    }

    /**
     * この要素の種類を表す{@link ModelKind#ENUM_DECLARATION}を返す。
     * @return {@link ModelKind#ENUM_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ENUM_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitEnumDeclaration(this, context);
    }
}
