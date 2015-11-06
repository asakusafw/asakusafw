/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

/**
 * Represents a compiled reduce actions.
 */
public class CompiledReduce {

    private final CompiledType reducerType;

    private final CompiledType combinerTypeOrNull;

    /**
     * Creates a new instance.
     * @param reducerType the target reducer type
     * @param combinerTypeOrNull the target combiner type (nullable)
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public CompiledReduce(CompiledType reducerType, CompiledType combinerTypeOrNull) {
        Precondition.checkMustNotBeNull(reducerType, "reducerType"); //$NON-NLS-1$
        this.reducerType = reducerType;
        this.combinerTypeOrNull = combinerTypeOrNull;
    }

    /**
     * Returns the target reducer type.
     * @return the target reducer type
     */
    public CompiledType getReducerType() {
        return reducerType;
    }

    /**
     * Returns the target combiner type.
     * @return the target combiner type, or {@code null} if this does not use combiner
     */
    public CompiledType getCombinerTypeOrNull() {
        return combinerTypeOrNull;
    }
}
