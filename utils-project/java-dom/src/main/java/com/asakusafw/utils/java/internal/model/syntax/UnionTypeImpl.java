/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.UnionType;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link UnionType}.
 * @since 0.9.0
 */
public class UnionTypeImpl extends ModelRoot implements UnionType {

    private List<? extends Type> alternativeTypes;

    @Override
    public List<? extends Type> getAlternativeTypes() {
        return alternativeTypes;
    }

    /**
     * Sets the alternatives.
     * @param alternativeTypes the alternatives
     */
    public void setAlternativeTypes(List<? extends Type> alternativeTypes) {
        Util.notNull(alternativeTypes, "alternativeTypes"); //$NON-NLS-1$
        Util.notContainNull(alternativeTypes, "alternativeTypes"); //$NON-NLS-1$
        Util.notEmpty(alternativeTypes, "alternativeTypes"); //$NON-NLS-1$
        this.alternativeTypes = Util.freeze(alternativeTypes);
    }

    @Override
    public ModelKind getModelKind() {
        return ModelKind.UNION_TYPE;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitUnionType(this, context);
    }
}
