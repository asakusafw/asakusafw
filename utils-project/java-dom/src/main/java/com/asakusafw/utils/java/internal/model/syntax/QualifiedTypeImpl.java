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

import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.QualifiedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link QualifiedType}の実装。
 */
public final class QualifiedTypeImpl extends ModelRoot implements QualifiedType {

    /**
     * 型限定子。
     */
    private Type qualifier;

    /**
     * 型の単純名。
     */
    private SimpleName simpleName;

    @Override
    public Type getQualifier() {
        return this.qualifier;
    }

    /**
     * 型限定子を設定する。
     * @param qualifier
     *     型限定子
     * @throws IllegalArgumentException
     *     {@code qualifier}に{@code null}が指定された場合
     */
    public void setQualifier(Type qualifier) {
        Util.notNull(qualifier, "qualifier"); //$NON-NLS-1$
        this.qualifier = qualifier;
    }

    @Override
    public SimpleName getSimpleName() {
        return this.simpleName;
    }

    /**
     * 型の単純名を設定する。
     * @param simpleName
     *     型の単純名
     * @throws IllegalArgumentException
     *     {@code simpleName}に{@code null}が指定された場合
     */
    public void setSimpleName(SimpleName simpleName) {
        Util.notNull(simpleName, "simpleName"); //$NON-NLS-1$
        this.simpleName = simpleName;
    }

    /**
     * この要素の種類を表す{@link ModelKind#QUALIFIED_TYPE}を返す。
     * @return {@link ModelKind#QUALIFIED_TYPE}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.QUALIFIED_TYPE;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitQualifiedType(this, context);
    }
}
