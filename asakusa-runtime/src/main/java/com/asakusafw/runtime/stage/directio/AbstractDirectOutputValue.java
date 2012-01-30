/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.directio;

import com.asakusafw.runtime.io.util.WritableUnion;

/**
 * An abstract implementation of value object in direct output stages.
 * @since 0.2.5
 */
public abstract class AbstractDirectOutputValue extends WritableUnion {

    /**
     * Creates a new instance.
     * @param specs spec objects
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected AbstractDirectOutputValue(DirectOutputSpec... specs) {
        super(DirectOutputSpec.getValueTypes(specs));
    }
}
