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

import com.asakusafw.utils.java.model.syntax.ClassLiteral;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link ClassLiteral}.
 */
public final class ClassLiteralImpl extends ModelRoot implements ClassLiteral {

    private Type type;

    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * Sets the target type.
     * @param type the target type
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    public void setType(Type type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    /**
     * Returns {@link ModelKind#CLASS_LITERAL} which represents this element kind.
     * @return {@link ModelKind#CLASS_LITERAL}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.CLASS_LITERAL;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitClassLiteral(this, context);
    }
}
