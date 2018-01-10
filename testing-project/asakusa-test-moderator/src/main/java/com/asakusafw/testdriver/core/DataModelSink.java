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
package com.asakusafw.testdriver.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * Sink channel of model objects.
 * @since 0.2.3
 */
public interface DataModelSink extends Closeable {

    /**
     * Puts a reflection of data model object into this channel.
     * @param model target data model object
     * @throws IOException if failed to put the object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    void put(DataModelReflection model) throws IOException;
}
