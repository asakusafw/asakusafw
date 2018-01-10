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
package com.asakusafw.yaess.core.task;

import java.io.IOException;
import java.util.Set;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.ExecutionScriptHandler;
import com.asakusafw.yaess.core.Job;

/**
 * Job to execute a script.
 * @param <T> kind of script
 * @since 0.2.3
 */
public class ScriptJob<T extends ExecutionScript> extends Job {

    private final ExecutionScriptHandler<? super T> handler;

    private final T script;

    /**
     * Creates a new instance.
     * @param script target script
     * @param handler the related script handler
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ScriptJob(T script, ExecutionScriptHandler<? super T> handler) {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null"); //$NON-NLS-1$
        }
        this.handler = handler;
        this.script = script;
    }

    @Override
    public void execute(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        handler.execute(monitor, context, script);
    }

    @Override
    public String getServiceLabel() {
        return handler.getHandlerId();
    }

    @Override
    public String getJobLabel() {
        return script.getId();
    }

    @Override
    public String getId() {
        return script.getId();
    }

    @Override
    public String getTrackingId(ExecutionContext context) {
        return computeTrackingId(context, script);
    }

    @Override
    public Set<String> getBlockerIds() {
        return script.getBlockerIds();
    }

    @Override
    public String getResourceId(ExecutionContext context) throws InterruptedException, IOException {
        return handler.getResourceId(context, script);
    }
}
