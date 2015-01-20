/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
 * {@link FieldDeclaration}の実装。
 */
public final class FieldDeclarationImpl extends ModelRoot implements FieldDeclaration {

    /**
     * ドキュメンテーションコメント。
     */
    private Javadoc javadoc;

    /**
     * 修飾子および注釈の一覧。
     */
    private List<? extends Attribute> modifiers;

    /**
     * フィールドの型。
     */
    private Type type;

    /**
     * 宣言するフィールドの一覧。
     */
    private List<? extends VariableDeclarator> variableDeclarators;

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
    public Type getType() {
        return this.type;
    }

    /**
     * フィールドの型を設定する。
     * @param type
     *     フィールドの型
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
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
     * 宣言するフィールドの一覧を設定する。
     * @param variableDeclarators
     *     宣言するフィールドの一覧
     * @throws IllegalArgumentException
     *     {@code variableDeclarators}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code variableDeclarators}に空が指定された場合
     */
    public void setVariableDeclarators(List<? extends VariableDeclarator> variableDeclarators) {
        Util.notNull(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        Util.notContainNull(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        Util.notEmpty(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        this.variableDeclarators = Util.freeze(variableDeclarators);
    }

    /**
     * この要素の種類を表す{@link ModelKind#FIELD_DECLARATION}を返す。
     * @return {@link ModelKind#FIELD_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.FIELD_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitFieldDeclaration(this, context);
    }
}
