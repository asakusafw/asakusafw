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

import com.asakusafw.utils.java.model.syntax.ArrayCreationExpression;
import com.asakusafw.utils.java.model.syntax.ArrayInitializer;
import com.asakusafw.utils.java.model.syntax.ArrayType;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link ArrayCreationExpression}の実装。
 */
public final class ArrayCreationExpressionImpl extends ModelRoot implements ArrayCreationExpression {

    /**
     * 生成する配列の型。
     */
    private ArrayType type;

    /**
     * 要素数指定式。
     */
    private List<? extends Expression> dimensionExpressions;

    /**
     * 配列初期化子。
     */
    private ArrayInitializer arrayInitializer;

    @Override
    public ArrayType getType() {
        return this.type;
    }

    /**
     * 生成する配列の型を設定する。
     * @param type
     *     生成する配列の型
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     */
    public void setType(ArrayType type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    @Override
    public List<? extends Expression> getDimensionExpressions() {
        return this.dimensionExpressions;
    }

    /**
     * 要素数指定式を設定する。
     * <p> 次元ごとの要素数が一つも指定されない場合、引数には空を指定する。 </p>
     * @param dimensionExpressions
     *     要素数指定式
     * @throws IllegalArgumentException
     *     {@code dimensionExpressions}に{@code null}が指定された場合
     */
    public void setDimensionExpressions(List<? extends Expression> dimensionExpressions) {
        Util.notNull(dimensionExpressions, "dimensionExpressions"); //$NON-NLS-1$
        Util.notContainNull(dimensionExpressions, "dimensionExpressions"); //$NON-NLS-1$
        this.dimensionExpressions = Util.freeze(dimensionExpressions);
    }

    @Override
    public ArrayInitializer getArrayInitializer() {
        return this.arrayInitializer;
    }

    /**
     * 配列初期化子を設定する。
     * <p> 配列初期化子が指定されない場合、引数には{@code null}を指定する。 </p>
     * @param arrayInitializer
     *     配列初期化子、
     *     ただし配列初期化子が指定されない場合は{@code null}
     */
    public void setArrayInitializer(ArrayInitializer arrayInitializer) {
        this.arrayInitializer = arrayInitializer;
    }

    /**
     * この要素の種類を表す{@link ModelKind#ARRAY_CREATION_EXPRESSION}を返す。
     * @return {@link ModelKind#ARRAY_CREATION_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ARRAY_CREATION_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitArrayCreationExpression(this, context);
    }
}
