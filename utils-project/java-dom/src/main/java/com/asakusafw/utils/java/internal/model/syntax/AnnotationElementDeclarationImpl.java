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

import com.asakusafw.utils.java.model.syntax.AnnotationElementDeclaration;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link AnnotationElementDeclaration}の実装。
 */
public final class AnnotationElementDeclarationImpl extends ModelRoot implements AnnotationElementDeclaration {

    /**
     * ドキュメンテーションコメント。
     */
    private Javadoc javadoc;

    /**
     * 修飾子および注釈の一覧。
     */
    private List<? extends Attribute> modifiers;

    /**
     * 注釈要素の型。
     */
    private Type type;

    /**
     * 注釈要素の名前。
     */
    private SimpleName name;

    /**
     * 注釈要素の規定値。
     */
    private Expression defaultExpression;

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
     * 注釈要素の型を設定する。
     * @param type
     *     注釈要素の型
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
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
     * 注釈要素の名前を設定する。
     * @param name
     *     注釈要素の名前
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public Expression getDefaultExpression() {
        return this.defaultExpression;
    }

    /**
     * 注釈要素の規定値を設定する。
     * <p> 規定値が存在しない場合、引数には{@code null}を指定する。 </p>
     * @param defaultExpression
     *     注釈要素の規定値、
     *     ただし規定値が存在しない場合は{@code null}
     */
    public void setDefaultExpression(Expression defaultExpression) {
        this.defaultExpression = defaultExpression;
    }

    /**
     * この要素の種類を表す{@link ModelKind#ANNOTATION_ELEMENT_DECLARATION}を返す。
     * @return {@link ModelKind#ANNOTATION_ELEMENT_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ANNOTATION_ELEMENT_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitAnnotationElementDeclaration(this, context);
    }
}
