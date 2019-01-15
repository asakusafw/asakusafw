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
package com.asakusafw.runtime.directio;

import java.io.IOException;

/**
 * Data model format which can be fragmented.
 * Client should not implement this interface directly.
 * @param <T> the type of target data model
 * @since 0.2.6
 * @version 0.9.0
 */
public interface FragmentableDataFormat<T> extends DataFormat<T> {

    /**
     * Returns the preferred fragment size (in bytes).
     * @return the preferred fragment size, or {@code -1} as infinite
     * @throws IOException if failed to compute bytes count
     * @throws InterruptedException if interrupted
     */
    default long getPreferredFragmentSize() throws IOException, InterruptedException {
        return -1L;
    }

    /**
     * Returns the lower limit of fragment size (in bytes).
     * Each fragment must not smaller than this size.
     * @return the lower limit of fragment size, or {@code -1} as infinite
     * @throws IOException if failed to compute bytes count
     * @throws InterruptedException if interrupted
     */
    default long getMinimumFragmentSize() throws IOException, InterruptedException {
        return -1L;
    }
}
