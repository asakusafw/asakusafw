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
package com.asakusafw.runtime.trace;

import java.io.IOException;

import com.asakusafw.runtime.core.ResourceConfiguration;

/**
 * A factory class for {@link TraceAction}.
 * @since 0.5.1
 */
public interface TraceActionFactory {

    /**
     * Creates a new {@link TraceAction} for tracepoint.
     * @param configuration current resource configuration
     * @param context tracing context
     * @return the created {@link TraceAction}
     * @throws IOException if this factory failed to initialize a {@link TraceAction} object
     * @throws InterruptedException if interrupted while initializing {@link TraceAction}
     */
    TraceAction createTracepointTraceAction(
            ResourceConfiguration configuration, TraceContext context) throws IOException, InterruptedException;

    /**
     * Creates a new {@link TraceAction} for error tracing.
     * @param configuration current resource configuration
     * @return the created {@link TraceAction}
     * @throws IOException if this factory failed to initialize a {@link TraceAction} object
     * @throws InterruptedException if interrupted while initializing {@link TraceAction}
     */
    TraceAction createErrorTraceAction(ResourceConfiguration configuration) throws IOException, InterruptedException;
}
