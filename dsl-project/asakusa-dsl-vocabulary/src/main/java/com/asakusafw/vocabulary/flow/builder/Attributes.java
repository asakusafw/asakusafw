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
package com.asakusafw.vocabulary.flow.builder;

import java.util.Arrays;

import com.asakusafw.vocabulary.flow.graph.OperatorHelper;

/**
 * Utilities about flow element attributes.
 * @since 0.9.0
 */
public final class Attributes {

    private Attributes() {
        return;
    }

    /**
     * Creates a new {@link KeyInfo} object.
     * @return the created object
     */
    public static KeyInfo key() {
        return new KeyInfo();
    }

    /**
     * Creates a new {@link ExternInfo} object.
     * @param name the external port name
     * @param description the external port description class
     * @return the created object
     */
    public static ExternInfo extern(String name, Class<?> description) {
        return new ExternInfo(name, description);
    }

    /**
     * Creates a new {@link OperatorHelper} object.
     * @param name the support method name
     * @param parameterTypes the support method parameter types
     * @return the created object
     */
    public static OperatorHelper support(String name, Class<?>... parameterTypes) {
        return new OperatorHelper(name, Arrays.asList(parameterTypes));
    }
}
