/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.io.Closeable;
import java.io.IOException;

/**
 * An abstract superclass of session mirror.
<pre><code>
try (SessionMirror session = ...) {
    // do something
    session.complete(); // or session.abort();
}
</code></pre>
 * @since 0.2.2
 */
public abstract class SessionMirror implements Closeable {

    /**
     * Returns the current session ID.
     * @return the current session ID
     */
    public abstract String getId();

    /**
     * Completes and releases this session.
     * This session will not be reopened later.
     * @throws IOException if failed to detach from the specified session
     */
    public abstract void complete() throws IOException;

    /**
     * Aborts and releases this session.
     * This session will not be reopened later.
     * @throws IOException if failed to detach from the specified session
     */
    public abstract void abort() throws IOException;

    /**
     * Closes this session.
     * If the session was already closed, this has no effects.
     * @throws IOException if failed to close this session
     */
    @Override
    public abstract void close() throws IOException;

    /**
     * A void implementation of {@link SessionMirror}.
     * @since 0.4.0
     */
    public static final class Null extends SessionMirror {

        private final String id;

        /**
         * Creates a new instance.
         * @param id session ID
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public Null(String id) {
            if (id == null) {
                throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
            }
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void complete() {
            return;
        }

        @Override
        public void abort() {
            return;
        }

        @Override
        public void close() {
            return;
        }
    }
}
