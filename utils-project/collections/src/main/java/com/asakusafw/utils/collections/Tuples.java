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
package com.asakusafw.utils.collections;

/**
 * Utilities for tuples.
 * @see Tuple2
 */
public final class Tuples {

    /**
     * Creates a new tuple.
     * @param <T1> type of first element
     * @param <T2> type of second element
     * @param first the first element
     * @param second the second element
     * @return the created instance.
     */
    public static <T1, T2> Tuple2<T1, T2> of(T1 first, T2 second) {
        return Tuple2.of(first, second);
    }

    private Tuples() {
        throw new AssertionError();
    }
}
