/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.util.Collections;
import java.util.Set;

import com.asakusafw.yaess.core.ExecutionScriptHandler;
import com.asakusafw.yaess.core.Job;

/**
 * An abstract super class of {@link Job} implementation for each handler's lifecycle event.
 * @since 0.2.3
 */
public abstract class HandlerLifecycleJob extends Job {

    /**
     * The target handler.
     */
    protected final ExecutionScriptHandler<?> handler;

    /**
     * Creates a new instance.
     * @param handler target handler
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HandlerLifecycleJob(ExecutionScriptHandler<?> handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null"); //$NON-NLS-1$
        }
        this.handler = handler;
    }

    @Override
    public String getId() {
        return handler.getHandlerId();
    }

    @Override
    public Set<String> getBlockerIds() {
        return Collections.emptySet();
    }

    @Override
    public String getResourceId() {
        return handler.getResourceId();
    }
}
