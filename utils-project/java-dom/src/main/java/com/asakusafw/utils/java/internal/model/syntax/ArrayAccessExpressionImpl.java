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

import com.asakusafw.utils.java.model.syntax.ArrayAccessExpression;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link ArrayAccessExpression}の実装。
 */
public final class ArrayAccessExpressionImpl extends ModelRoot implements ArrayAccessExpression {

    /**
     * 配列式。
     */
    private Expression array;

    /**
     * 添え字式。
     */
    private Expression index;

    @Override
    public Expression getArray() {
        return this.array;
    }

    /**
     * 配列式を設定する。
     * @param array
     *     配列式
     * @throws IllegalArgumentException
     *     {@code array}に{@code null}が指定された場合
     */
    public void setArray(Expression array) {
        Util.notNull(array, "array"); //$NON-NLS-1$
        this.array = array;
    }

    @Override
    public Expression getIndex() {
        return this.index;
    }

    /**
     * 添え字式を設定する。
     * @param index
     *     添え字式
     * @throws IllegalArgumentException
     *     {@code index}に{@code null}が指定された場合
     */
    public void setIndex(Expression index) {
        Util.notNull(index, "index"); //$NON-NLS-1$
        this.index = index;
    }

    /**
     * この要素の種類を表す{@link ModelKind#ARRAY_ACCESS_EXPRESSION}を返す。
     * @return {@link ModelKind#ARRAY_ACCESS_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ARRAY_ACCESS_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitArrayAccessExpression(this, context);
    }
}
