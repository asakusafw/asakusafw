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
package com.asakusafw.runtime.model;

/**
 * An abstract super interface of Asakusa data model classes.
 * If clients define properties in the data model classes, the target class must have following methods.
 * <ul>
 * <li> {@code get<PropertyName>Option():<PropertyType>} </li>
 * <li> {@code set<PropertyName>Option(<PropertyType>):void} </li>
 * </ul>
 * @param <T> the data model type
 * @since 0.2.0
 */
public interface DataModel<T extends DataModel<T>> {

    /**
     * Resets the properties in this to initial state.
     */
    void reset();

    /**
     * Copies the properties in the specified object into this.
     * @param other the target object
     */
    void copyFrom(T other);
}
