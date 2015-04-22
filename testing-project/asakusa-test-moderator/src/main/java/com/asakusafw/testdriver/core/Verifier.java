/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Result verifier.
 * @since 0.2.3
 * @see Verifier.Validatable
 */
public interface Verifier extends Closeable {

    /**
     * Verifies the result data.
     * @param results result data
     * @return the detected differences (will be trimmed)
     * @throws IOException if failed to
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    List<Difference> verify(DataModelSource results) throws IOException;

    /**
     * An extension interface for {@link Verifier}.
     * @since 0.7.0
     */
    public interface Validatable {

        /**
         * Validates this object.
         * Clients should dispose this object after the invoking this method
         * and must not invoke {@link Verifier#verify(DataModelSource)}.
         * @throws IOException if validation was failed
         */
        void validate() throws IOException;
    }
}
