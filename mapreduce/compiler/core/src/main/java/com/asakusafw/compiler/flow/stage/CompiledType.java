/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.stage;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Represents a compiled class or interface.
 */
public class CompiledType {

    private final Name qualifiedName;

    /**
     * Creates a new instance.
     * @param qualifiedName the qualified name of the compiled type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public CompiledType(Name qualifiedName) {
        Precondition.checkMustNotBeNull(qualifiedName, "qualifiedName"); //$NON-NLS-1$
        this.qualifiedName = qualifiedName;
    }

    /**
     * Returns the qualified name of the compiled type.
     * @return the qualified class name
     */
    public Name getQualifiedName() {
        return qualifiedName;
    }
}
