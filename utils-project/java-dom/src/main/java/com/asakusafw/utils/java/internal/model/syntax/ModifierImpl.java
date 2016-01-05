/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import com.asakusafw.utils.java.model.syntax.Modifier;
import com.asakusafw.utils.java.model.syntax.ModifierKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link Modifier}.
 */
public final class ModifierImpl extends ModelRoot implements Modifier {

    private ModifierKind modifierKind;

    @Override
    public ModifierKind getModifierKind() {
        return this.modifierKind;
    }

    /**
     * Sets the modifier kind.
     * @param modifierKind the modifier kind
     * @throws IllegalArgumentException if {@code modifierKind} was {@code null}
     */
    public void setModifierKind(ModifierKind modifierKind) {
        Util.notNull(modifierKind, "modifierKind"); //$NON-NLS-1$
        this.modifierKind = modifierKind;
    }

    /**
     * Returns {@link ModelKind#MODIFIER} which represents this element kind.
     * @return {@link ModelKind#MODIFIER}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.MODIFIER;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitModifier(this, context);
    }
}
