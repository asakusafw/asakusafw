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

import com.asakusafw.utils.java.model.syntax.BasicType;
import com.asakusafw.utils.java.model.syntax.BasicTypeKind;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link BasicType}の実装。
 */
public final class BasicTypeImpl extends ModelRoot implements BasicType {

    /**
     * 基本型の種類。
     */
    private BasicTypeKind typeKind;

    @Override
    public BasicTypeKind getTypeKind() {
        return this.typeKind;
    }

    /**
     * 基本型の種類を設定する。
     * @param typeKind
     *     基本型の種類
     * @throws IllegalArgumentException
     *     {@code typeKind}に{@code null}が指定された場合
     */
    public void setTypeKind(BasicTypeKind typeKind) {
        Util.notNull(typeKind, "typeKind"); //$NON-NLS-1$
        this.typeKind = typeKind;
    }

    /**
     * この要素の種類を表す{@link ModelKind#BASIC_TYPE}を返す。
     * @return {@link ModelKind#BASIC_TYPE}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.BASIC_TYPE;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitBasicType(this, context);
    }
}
