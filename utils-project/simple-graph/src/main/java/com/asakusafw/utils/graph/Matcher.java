/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.utils.graph;

/**
 * An interface for evaluating whether the value satisfies some conditions.
 * @param <T> the value type
 */
public interface Matcher<T> {

    /**
     * Returns whether the target value satisfies this matcher's condition.
     * @param object the target value
     * @return {@code true} if the target value satisfies this matcher's condition, otherwise {@code false}
     */
    boolean matches(T object);
}
