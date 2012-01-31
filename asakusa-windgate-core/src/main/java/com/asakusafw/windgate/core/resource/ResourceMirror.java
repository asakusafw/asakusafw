/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.windgate.core.resource;

import java.io.Closeable;
import java.io.IOException;

import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ProcessScript;

/**
 * An abstract implementation of logical data store resources.
 * @since 0.2.2
 */
public abstract class ResourceMirror implements Closeable {

    /**
     * Returns the name of this resource.
     * @return the name of this resource
     */
    public abstract String getName();

    /**
     * Returns whether this resource is transactional.
     * Transactional resources must be up to one in the profile.
     * If this is transactional resource,
     * this will be invoked {@link #onSessionCreated()} before the other resources,
     * and {@link #onSessionCompleting()} after the other resources.
     * The default implementation always returns {@code false}.
     * @return {@code true} if this resource is transactional, otherwise {@code false}
     */
    public boolean isTransactional() {
        return false;
    }

    /**
     * Invoke after the session was created.
     * This method will be invoked only once for each instance.
     * @throws IOException if failed to execute
     */
    public void onSessionCreated() throws IOException {
        return;
    }

    /**
     * Invoke before the session was completed.
     * This method will be invoked only once for each instance,
     * and only invoked after finished all gate processes corresponding to this resource.
     * If this method raises an exception, the session can not be completed.
     * @throws IOException if failed to execute
     */
    public void onSessionCompleting() throws IOException {
        return;
    }

    /**
     * Prepares this resource for the specified execution.
     * This method will be invoked only once for each instance.
     * @param script target script
     * @throws IOException if failed to prepare this resource
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public abstract void prepare(GateScript script) throws IOException;

    /**
     * Creates a new source driver for this resource.
     * This method will be invoked only after {@link #prepare(GateScript)} was completed.
     * @param <T> target data model type
     * @param script target process
     * @return the created driver
     * @throws IOException if failed to open this resource
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public abstract <T> SourceDriver<T> createSource(ProcessScript<T> script) throws IOException;

    /**
     * Creates a new drain driver for this resource.
     * This method will be invoked only after {@link #prepare(GateScript)} was completed.
     * @param <T> target data model type
     * @param script target process
     * @return the created driver
     * @throws IOException if failed to open this resource
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public abstract <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException;
}
