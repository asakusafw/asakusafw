/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.yaess.core.task;

import java.io.IOException;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionScriptHandler;

/**
 * A job to setup each {@link ExecutionScriptHandler}.
 * @since 0.2.3
 */
public class SetupJob extends HandlerLifecycleJob {

    private static final String JOB_ID = "(setup)";

    /**
     * Creates a new instance.
     * @param handler target handler
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SetupJob(ExecutionScriptHandler<?> handler) {
        super(handler);
    }

    @Override
    public void execute(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        handler.setUp(monitor, context);
    }

    @Override
    public String getServiceLabel() {
        return handler.getHandlerId();
    }

    @Override
    public String getJobLabel() {
        return JOB_ID;
    }
}
