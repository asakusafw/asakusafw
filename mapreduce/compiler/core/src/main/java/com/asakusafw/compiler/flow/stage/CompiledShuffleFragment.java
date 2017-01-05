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

/**
 * A compiled model of shuffle fragment.
 */
public class CompiledShuffleFragment {

    private final CompiledType mapOutputType;

    private final CompiledType combineOutputType;

    /**
     * Creates a new instance.
     * @param mapOutput the output type of map action
     * @param combineOutput the output type of combine action
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public CompiledShuffleFragment(CompiledType mapOutput, CompiledType combineOutput) {
        Precondition.checkMustNotBeNull(mapOutput, "mapOutput"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(combineOutput, "combineOutput"); //$NON-NLS-1$
        this.mapOutputType = mapOutput;
        this.combineOutputType = combineOutput;
    }

    /**
     * Returns the output type of map action.
     * @return the output type of map action
     */
    public CompiledType getMapOutputType() {
        return mapOutputType;
    }

    /**
     * Returns the output type of combine action.
     * @return the output type of combine action
     */
    public CompiledType getCombineOutputType() {
        return combineOutputType;
    }
}
