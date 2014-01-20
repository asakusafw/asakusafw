/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.windgate.core.session;

import java.io.IOException;
import java.util.List;

import com.asakusafw.windgate.core.BaseProvider;

/**
 * An abstract super class of session provider.
 * Clients can inherit this class to provide a session implementation.
 * Each subclass must provide a public constructor with no parameters.
 * @since 0.2.2
 */
public abstract class SessionProvider extends BaseProvider<SessionProfile> {

    /**
     * Returns session IDs which are created and not completed/aborted.
     * Note that the returned IDs may represent broken sessions.
     * @return session IDs
     * @throws IOException if failed to list IDs
     */
    public abstract List<String> getCreatedIds() throws IOException;

    /**
     * Creates a new session and acquires the session with the specified session ID.
     * @param id target session ID
     * @return the created session
     * @throws SessionException if session was invalid state
     * @throws IOException if failed to create a new session
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public abstract SessionMirror create(String id) throws SessionException, IOException;

    /**
     * Opens and acquires a created session with the specified session ID.
     * @param id target session ID
     * @return the opened session
     * @throws SessionException if session was invalid state
     * @throws IOException if failed to open the session
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public abstract SessionMirror open(String id) throws SessionException, IOException;

    /**
     * Deletes a session with the specified session ID.
     * Even if the target session is broken, this will force delete it.
     * @param id target session ID
     * @throws SessionException if session was invalid state, except the session was broken
     * @throws IOException if failed to delete the session
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public abstract void delete(String id) throws SessionException, IOException;
}
