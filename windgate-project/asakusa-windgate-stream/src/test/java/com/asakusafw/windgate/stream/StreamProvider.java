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
package com.asakusafw.windgate.stream;

import java.io.IOException;

/**
 * Provides streams.
 * @param <T> stream type
 */
public interface StreamProvider<T> {

    /**
     * Returns the description of the target stream.
     * @return the description of the target stream
     */
    String getDescription();

    /**
     * Creates a new stream.
     * @return the opened stream
     * @throws IOException if failed to open stream
     */
    T open() throws IOException;
}
