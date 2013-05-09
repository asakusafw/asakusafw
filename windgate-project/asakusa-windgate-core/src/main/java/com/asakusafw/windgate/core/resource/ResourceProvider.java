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
package com.asakusafw.windgate.core.resource;

import java.io.IOException;
import java.text.MessageFormat;

import com.asakusafw.windgate.core.BaseProvider;
import com.asakusafw.windgate.core.ParameterList;

/**
 * An abstract super class of resource factory.
 * Clients can inherit this class to provide new data sources.
 * Each subclass must provide a public constructor with no parameters.
 * @since 0.2.2
 */
public abstract class ResourceProvider extends BaseProvider<ResourceProfile> {

    /**
     * Provides a new {@link ResourceMirror}.
     * @param sessionId the current session ID
     * @param arguments arguments (key and value pairs)
     * @return the created {@link ResourceMirror}
     * @throws IOException if failed to create the specified {@link ResourceMirror}
     */
    public abstract ResourceMirror create(String sessionId, ParameterList arguments) throws IOException;

    /**
     * Aborts the specified session corresponding to this resouce.
     * The default implementation does nothing.
     * @param sessionId the target session ID
     * @throws IOException if failed to abort the resource
     */
    public void abort(String sessionId) throws IOException {
        return;
    }

    /**
     * Aborts all sessions corresponding to this resouce.
     * The default implementation does nothing.
     * @throws IOException if failed to abort the resource
     */
    public void abortAll() throws IOException {
        return;
    }

    /**
     * Provides a new {@link ResourceManipulator}.
     * @param arguments arguments (key and value pairs)
     * @return the created {@link ResourceManipulator}
     * @throws IOException if failed to create a {@link ResourceManipulator},
     *     or this resource does not support manipulation
     */
    public ResourceManipulator createManipulator(ParameterList arguments) throws IOException {
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        throw new IOException(MessageFormat.format(
                "This resource does not support manipulation: {0}",
                getClass().getName()));
    }
}
