/**
 * Copyright 2013 Asakusa Framework Team.
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
package com.asakusafw.runtime.trace;

import java.io.Closeable;
import java.io.IOException;

/**
 * An abstract super interface of actions handling trace events.
 * @since 0.5.1
 */
public interface TraceAction extends Closeable {

    /**
     * Handles trace event.
     * @param data trace data (is usually target data-model for tracepoints or exception information for errors)
     * @throws IOException if handling failed by I/O error
     * @throws InterruptedException if interrupted while handling the event
     */
    void trace(Object data) throws IOException, InterruptedException;
}
