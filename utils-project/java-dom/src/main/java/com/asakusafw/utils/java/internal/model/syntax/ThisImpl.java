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
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.This;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link This}の実装。
 */
public final class ThisImpl extends ModelRoot implements This {

    /**
     * 型限定子。
     */
    private NamedType qualifier;

    @Override
    public NamedType getQualifier() {
        return this.qualifier;
    }

    /**
     * 型限定子を設定する。
     * <p> 限定子が指定されない場合、引数には{@code null}を指定する。 </p>
     * @param qualifier
     *     型限定子、
     *     ただし限定子が指定されない場合は{@code null}
     */
    public void setQualifier(NamedType qualifier) {
        this.qualifier = qualifier;
    }

    /**
     * この要素の種類を表す{@link ModelKind#THIS}を返す。
     * @return {@link ModelKind#THIS}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.THIS;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitThis(this, context);
    }
}
