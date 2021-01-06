/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
 * An implementation of {@link Wildcard}.
 */
public final class WildcardImpl extends ModelRoot implements Wildcard {

    private WildcardBoundKind boundKind;

    private Type typeBound;

    @Override
    public WildcardBoundKind getBoundKind() {
        return this.boundKind;
    }

    /**
     * Sets the type bound kind.
     * @param boundKind the type bound kind
     * @throws IllegalArgumentException if {@code boundKind} was {@code null}
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
     * Sets the bound type.
     * @param typeBound the bound type, or {@code null} if this is an unbound wildcard
     */
    public void setTypeBound(Type typeBound) {
        this.typeBound = typeBound;
    }

    /**
     * Returns {@link ModelKind#WILDCARD} which represents this element kind.
     * @return {@link ModelKind#WILDCARD}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.WILDCARD;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitWildcard(this, context);
    }
}
