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

import com.asakusafw.utils.java.model.syntax.AnnotationElement;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link AnnotationElement}の実装。
 */
public final class AnnotationElementImpl extends ModelRoot implements AnnotationElement {

    /**
     * 注釈要素の名前。
     */
    private SimpleName name;

    /**
     * 注釈要素値の式。
     */
    private Expression expression;

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
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 注釈要素値の式を設定する。
     * @param expression
     *     注釈要素値の式
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    public void setExpression(Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        this.expression = expression;
    }

    /**
     * この要素の種類を表す{@link ModelKind#ANNOTATION_ELEMENT}を返す。
     * @return {@link ModelKind#ANNOTATION_ELEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ANNOTATION_ELEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitAnnotationElement(this, context);
    }
}
