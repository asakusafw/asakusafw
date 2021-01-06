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
package com.asakusafw.runtime.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * An abstract interface of reading data-sets.
 * @param <T> the data model type
 */
public interface ModelInput<T> extends Closeable {

    /**
     * Reads contents and write them into the target data model object.
     * The target data model object will be changed even if the next data does not exist.
     * @param model the target data model object
     * @return {@code true} if the next data was successfully read, or {@code false} if there is no any more
     * @throws IOException if error occurred while reading the next data
     */
    boolean readTo(T model) throws IOException;
}
