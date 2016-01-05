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

import com.asakusafw.runtime.core.Report;

/**
 * An implementation of {@link TraceReportAction} using {@link Report} API.
 * @since 0.5.1
 */
public class TraceReportAction implements TraceAction {

    private final TraceContext context;

    /**
     * Creates a new instance.
     * @param context the current context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TraceReportAction(TraceContext context) {
        this.context = context;
    }

    @Override
    public void trace(Object data) throws IOException, InterruptedException {
        Report.info(String.format("[TRACE-%04d] %s: %s", context.getSerialNumber(), context, data)); //$NON-NLS-1$
    }

    @Override
    public void close() {
        return;
    }
}
