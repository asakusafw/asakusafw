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
package com.asakusafw.runtime.io.text.driver;

/**
 * An abstract super interface of converts field contents and properties each other.
 * @param <T> the property type
 * @since 0.9.1
 */
public interface FieldAdapter<T> {

    /**
     * Clears the given property.
     * @param property the property
     */
    void clear(T property);

    /**
     * Extracts the given field content into the destination property.
     * @param contents the field content
     * @param property the destination property
     * @throws MalformedFieldException if the field content is malformed
     */
    void parse(CharSequence contents, T property);

    /**
     * Extracts the given property value into the destination field.
     * @param property the property value
     * @param output the destination field output
     */
    void emit(T property, FieldOutput output);
}
