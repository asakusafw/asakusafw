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
package com.asakusafw.utils.java.model.syntax;

import java.util.List;

/**
 * An interface which represents method and constructor references in the documentation comments.
 */
public interface DocMethod
        extends DocElement {

    /**
     * Returns the owner type.
     * @return the owner type, or {@code null} if it is not specified
     */
    Type getType();

    /**
     * Returns the target method or constructor name.
     * @return the target method or constructor name
     */
    SimpleName getName();

    /**
     * Returns the formal parameters.
     * @return the formal parameters
     */
    List<? extends DocMethodParameter> getFormalParameters();
}
