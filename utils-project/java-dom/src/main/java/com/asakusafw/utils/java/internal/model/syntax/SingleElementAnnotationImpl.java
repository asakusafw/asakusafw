/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SingleElementAnnotation;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link SingleElementAnnotation}の実装。
 */
public final class SingleElementAnnotationImpl extends ModelRoot implements SingleElementAnnotation {

    /**
     * 注釈の型。
     */
    private NamedType type;

    /**
     * {@code value}要素値の式。
     */
    private Expression expression;

    @Override
    public NamedType getType() {
        return this.type;
    }

    /**
     * 注釈の型を設定する。
     * @param type
     *     注釈の型
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     */
    public void setType(NamedType type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    @Override
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * {@code value}要素値の式を設定する。
     * @param expression
     *     {@code value}要素値の式
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    public void setExpression(Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        this.expression = expression;
    }

    /**
     * この要素の種類を表す{@link ModelKind#SINGLE_ELEMENT_ANNOTATION}を返す。
     * @return {@link ModelKind#SINGLE_ELEMENT_ANNOTATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.SINGLE_ELEMENT_ANNOTATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitSingleElementAnnotation(this, context);
    }
}
