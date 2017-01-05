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
package com.asakusafw.utils.collections;

/**
 * Represents a tuple with 2 elements.
 * @param <T1> type of first element
 * @param <T2> type of second element
 */
public class Tuple2<T1, T2> {

    /**
     * The first element.
     */
    public final T1 first;

    /**
     * The second element.
     */
    public final T2 second;

    /**
     * Creates a new instance.
     * @param first the first element
     * @param second the second element
     */
    public Tuple2(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Creates a new instance.
     * @param <T1> type of first element
     * @param <T2> type of second element
     * @param first the first element
     * @param second the second element
     * @return the created instance.
     */
    public static <T1, T2> Tuple2<T1, T2> of(T1 first, T2 second) {
        return new Tuple2<>(first, second);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Tuple2<?, ?> other = (Tuple2<?, ?>) obj;
        if (first == null) {
            if (other.first != null) {
                return false;
            }
        } else if (first.equals(other.first) == false) {
            return false;
        }
        if (second == null) {
            if (other.second != null) {
                return false;
            }
        } else if (second.equals(other.second) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", first, second); //$NON-NLS-1$
    }
}
