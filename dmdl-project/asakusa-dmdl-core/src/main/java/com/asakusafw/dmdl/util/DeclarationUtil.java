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
package com.asakusafw.dmdl.util;

import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;

/**
 * Utilities for declarations.
 * @since 0.7.0
 */
public final class DeclarationUtil {

    private DeclarationUtil() {
        return;
    }

    /**
     * Returns whether the type has the specified kind or not.
     * @param type the type
     * @param kind the type kind
     * @return {@code true} if the type has its kind, otherwise {@code false}
     */
    public static boolean isType(Type type, BasicTypeKind kind) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        if (type instanceof BasicType) {
            return ((BasicType) type).getKind() == kind;
        }
        return false;
    }
}
