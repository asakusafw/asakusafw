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

import com.asakusafw.utils.java.model.syntax.ArrayType;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link ArrayType}の実装。
 */
public final class ArrayTypeImpl extends ModelRoot implements ArrayType {

    /**
     * 要素型。
     */
    private Type componentType;

    @Override
    public Type getComponentType() {
        return this.componentType;
    }

    /**
     * 要素型を設定する。
     * @param componentType
     *     要素型
     * @throws IllegalArgumentException
     *     {@code componentType}に{@code null}が指定された場合
     */
    public void setComponentType(Type componentType) {
        Util.notNull(componentType, "componentType"); //$NON-NLS-1$
        this.componentType = componentType;
    }

    /**
     * この要素の種類を表す{@link ModelKind#ARRAY_TYPE}を返す。
     * @return {@link ModelKind#ARRAY_TYPE}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ARRAY_TYPE;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitArrayType(this, context);
    }
}
