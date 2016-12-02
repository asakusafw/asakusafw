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
package com.asakusafw.vocabulary.attribute;

/**
 * Represents a buffer type of inputs.
 * @since 0.9.1
 */
public enum BufferType implements Attribute {

    /**
     * Allocates a buffer onto the Java heap, and keeps all elements on it.
     */
    HEAP,

    /**
     * Allocates a buffer onto the Java heap and temporary files.
     */
    STORED,

    /**
     * Does not allocate buffer space.
     */
    VOLATILE,
}
