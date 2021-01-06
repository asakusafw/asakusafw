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
package com.asakusafw.dmdl.semantics.type;

import java.text.MessageFormat;

import com.asakusafw.dmdl.model.AstBasicType;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.PropertyMappingKind;
import com.asakusafw.dmdl.semantics.Type;

/**
 * Basic type of properties.
 */
public class BasicType implements Type {

    private final AstBasicType originalAst;

    private final BasicTypeKind kind;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param kind the kind of this type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public BasicType(AstBasicType originalAst, BasicTypeKind kind) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.kind = kind;
    }

    @Override
    public AstBasicType getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns the kind of this type.
     * @return the kind
     */
    public BasicTypeKind getKind() {
        return kind;
    }

    @Override
    public Type map(PropertyMappingKind mapping) {
        if (mapping == null) {
            throw new IllegalArgumentException("mapping must not be null"); //$NON-NLS-1$
        }
        switch (mapping) {
        case ANY:
        case MAX:
        case MIN:
            return this;
        case COUNT:
            return new BasicType(originalAst, BasicTypeKind.LONG);
        case SUM:
            switch (kind) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
                return new BasicType(originalAst, BasicTypeKind.LONG);
            case DECIMAL:
                return new BasicType(originalAst, BasicTypeKind.DECIMAL);
            case FLOAT:
            case DOUBLE:
                return new BasicType(originalAst, BasicTypeKind.DOUBLE);
            case BOOLEAN:
            case DATE:
            case DATETIME:
            case TEXT:
                return null;
            default:
                throw new AssertionError(mapping);
            }
        default:
            throw new AssertionError(mapping);
        }
    }

    @Override
    public boolean isSame(Type other) {
        if (this == other) {
            return true;
        }
        if ((other instanceof BasicType) == false) {
            return false;
        }
        return kind == ((BasicType) other).kind;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}", //$NON-NLS-1$
                kind.name());
    }
}
