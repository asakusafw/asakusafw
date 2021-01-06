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
package com.asakusafw.iterative.common;

import java.util.Map;
import java.util.Set;

/**
 * Represents a parameter set of individual rounds.
 * @since 0.8.0
 */
public interface ParameterSet {

    /**
     * Returns whether the target parameter is available in this set or not.
     * @param name the target parameter name
     * @return {@code true} if the parameter is available, otherwise {@code false}
     */
    boolean isAvailable(String name);

    /**
     * Returns the target parameter value.
     * @param name the target parameter name
     * @return the parameter value, or {@code null} if the target parameter is not available in this set
     * @see #isAvailable(String)
     */
    String get(String name);

    /**
     * Returns the all available parameter names in this set.
     * @return the all available parameter names
     */
    Set<String> getAvailable();

    /**
     * Returns all available parameters.
     * @return the map of parameter name and its value
     */
    Map<String, String> toMap();
}
