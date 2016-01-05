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
package com.asakusafw.yaess.tools.log.util;

/**
 * Filters objects.
 * @param <T> the target object type
 * @since 0.6.2
 */
public interface Filter<T> {

    /**
     * Accepts any objects.
     */
    Filter<Object> THROUGH = new Filter<Object>() {
        @Override
        public boolean accepts(Object value) {
            return true;
        }
    };

    /**
     * Returns whether this filter accepts the target object or not.
     * @param value the target object
     * @return {@code true} if this accepts the object, otherwise {@code false}
     */
    boolean accepts(T value);
}
