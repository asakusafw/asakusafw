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
package com.asakusafw.dmdl.semantics;

/**
 * Property types.
 */
public interface Type extends Element {

    /**
     * Returns the mapped result type.
     * @param mapping the mapping function
     * @return the mapped type, or {@code null} if mapping is not defined
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    Type map(PropertyMappingKind mapping);

    /**
     * Compare to other type, and returns {@code true} iff both type are same.
     * @param other the type compared to
     * @return {@code true} iff this and the other type are same
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    boolean isSame(Type other);
}
