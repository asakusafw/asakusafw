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
package com.asakusafw.utils.gradle;

import java.util.function.Consumer;

/**
 * {@link Consumer} with exception.
 * @since 0.10.0
 * @param <T> the acceptable value type
 * @param <E> the exception type
 */
@FunctionalInterface
public interface TryConsumer<T, E extends Throwable> {

    /**
     * Accepts a value and process it.
     * @param value the value
     * @throws E if exception was occurred
     */
    void accept(T value) throws E;
}