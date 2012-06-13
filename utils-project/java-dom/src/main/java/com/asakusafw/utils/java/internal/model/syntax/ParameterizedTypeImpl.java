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

import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.ParameterizedType;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link ParameterizedType}の実装。
 */
public final class ParameterizedTypeImpl extends ModelRoot implements ParameterizedType {

    /**
     * パラメータ化されていない型。
     */
    private Type type;

    /**
     * 型引数の一覧。
     */
    private List<? extends Type> typeArguments;

    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * パラメータ化されていない型を設定する。
     * @param type
     *     パラメータ化されていない型
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     */
    public void setType(Type type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    @Override
    public List<? extends Type> getTypeArguments() {
        return this.typeArguments;
    }

    /**
     * 型引数の一覧を設定する。
     * @param typeArguments
     *     型引数の一覧
     * @throws IllegalArgumentException
     *     {@code typeArguments}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeArguments}に空が指定された場合
     */
    public void setTypeArguments(List<? extends Type> typeArguments) {
        Util.notNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notContainNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notEmpty(typeArguments, "typeArguments"); //$NON-NLS-1$
        this.typeArguments = Util.freeze(typeArguments);
    }

    /**
     * この要素の種類を表す{@link ModelKind#PARAMETERIZED_TYPE}を返す。
     * @return {@link ModelKind#PARAMETERIZED_TYPE}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.PARAMETERIZED_TYPE;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitParameterizedType(this, context);
    }
}
