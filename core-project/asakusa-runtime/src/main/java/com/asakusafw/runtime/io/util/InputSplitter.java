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
package com.asakusafw.runtime.io.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Splits {@link InputStream}.
 * @since 0.10.3
 */
@FunctionalInterface
public interface InputSplitter {

    /**
     * Trims {@link InputStream} to provide the correct split data.
     * @param input the input, which skipped leading data until the offset position
     * @param offset the current input offset in bytes
     * @param splitSize the preferred split size in bytes (may be larger than the input)
     * @return the trimmed {@link InputStream}
     * @throws IOException if I/O error was occurred while trimming the input
     */
    InputStream trim(InputStream input, long offset, long splitSize) throws IOException;

    /**
     * Returns the preferred split size.
     * @return the preferred split size in bytes
     */
    default long getPreferredSize() {
        return -1L;
    }

    /**
     * Returns the lower limit of split size.
     * The each fragment must have size greater than or equal to the returned value.
     * @return the lower limit of split size in bytes
     */
    default long getLowerLimitSize() {
        return 1L;
    }
}
