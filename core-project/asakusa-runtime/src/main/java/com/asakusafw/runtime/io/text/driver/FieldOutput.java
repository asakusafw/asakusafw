/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import java.util.Collection;

/**
 * An output target of {@link FieldAdapter}.
 * @since 0.9.1
 */
public interface FieldOutput {

    /**
     * Returns the output contents.
     * @return the output contents, or {@code null} if this output was {@link #putNull() set as null}
     */
    CharSequence get();

    /**
     * Returns the options of this output.
     * @return the options
     */
    Collection<? extends Option> getOptions();

    /**
     * Sets this field as {@code NULL}.
     * @throws IllegalStateException if this field already has any other contents
     */
    void putNull();

    /**
     * Adds a content into this output.
     * @param contents the content
     * @param start the beginning index (inclusive)
     * @param end the ending index (exclusive)
     * @return this
     * @throws IllegalStateException if this field was already {@link #putNull() set as null}
     */
    FieldOutput put(CharSequence contents, int start, int end);

    /**
     * Adds a content into this output.
     * @param contents the content
     * @return this
     * @throws IllegalStateException if this field was already {@link #putNull() set as null}
     */
    default FieldOutput put(CharSequence contents) {
        return put(contents, 0, contents.length());
    }

    /**
     * Adds an option of this output.
     * @param option the options
     * @return this
     */
    FieldOutput addOption(Option option);

    /**
     * Adds options of this output.
     * @param options the options
     * @return this
     */
    default FieldOutput addOptions(Option... options) {
        for (Option option : options) {
            addOption(option);
        }
        return this;
    }

    /**
     * Acquires the internal buffer.
     * @return the internal buffer, always empty
     * @throws IllegalStateException if this field already has some content (optional)
     */
    default StringBuilder acquireBuffer() {
        return new StringBuilder();
    }

    /**
     * Releases the {@link #acquireBuffer() acquired} buffer.
     * @param acquired the buffer to release
     * @throws IllegalStateException if the given buffer is not acquired from this output
     */
    default void releaseBuffer(StringBuilder acquired) {
        put(acquired);
    }

    /**
     * Represents an option of {@link FieldOutput}.
     * @since 0.9.1
     */
    interface Option {

        /**
         * Returns the option type.
         * @return the option type
         */
        default Class<? extends Option> getDeclaringClass() {
            return getClass();
        }
    }
}