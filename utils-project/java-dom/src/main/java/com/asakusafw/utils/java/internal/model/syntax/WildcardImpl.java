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

import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;
import com.asakusafw.utils.java.model.syntax.Wildcard;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;

/**
 * {@link Wildcard}の実装。
 */
public final class WildcardImpl extends ModelRoot implements Wildcard {

    /**
     * 型境界の種類。
     */
    private WildcardBoundKind boundKind;

    /**
     * 境界型。
     */
    private Type typeBound;

    @Override
    public WildcardBoundKind getBoundKind() {
        return this.boundKind;
    }

    /**
     * 型境界の種類を設定する。
     * @param boundKind
     *     型境界の種類
     * @throws IllegalArgumentException
     *     {@code boundKind}に{@code null}が指定された場合
     */
    public void setBoundKind(WildcardBoundKind boundKind) {
        Util.notNull(boundKind, "boundKind"); //$NON-NLS-1$
        this.boundKind = boundKind;
    }

    @Override
    public Type getTypeBound() {
        return this.typeBound;
    }

    /**
     * 境界型を設定する。
     * <p> 境界型が指定されない場合、引数には{@code null}を指定する。 </p>
     * @param typeBound
     *     境界型、
     *     ただし境界型が指定されない場合は{@code null}
     */
    public void setTypeBound(Type typeBound) {
        this.typeBound = typeBound;
    }

    /**
     * この要素の種類を表す{@link ModelKind#WILDCARD}を返す。
     * @return {@link ModelKind#WILDCARD}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.WILDCARD;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitWildcard(this, context);
    }
}
