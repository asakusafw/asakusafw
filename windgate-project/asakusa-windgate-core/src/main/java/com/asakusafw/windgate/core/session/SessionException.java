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
import java.text.MessageFormat;

/**
 * An exception that describes failed to attach to a session.
 * @since 0.2.2
 */
public class SessionException extends IOException {

    private static final long serialVersionUID = -819170343696459140L;

    private final String sessionId;

    private final Reason reason;

    /**
     * Creates a new instance.
     * @param sessionId target session ID
     * @param reason the reason of this exception
     * @param cause the cause of this exception (optional)
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public SessionException(String sessionId, Reason reason, Throwable cause) {
        super(buildMessage(sessionId, reason), cause);
        this.sessionId = sessionId;
        this.reason = reason;
    }

    /**
     * Returns the target session ID.
     * @return the target session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the reason of this exception.
     * @return the reason
     */
    public Reason getReason() {
        return reason;
    }

    private static String buildMessage(String sessionId, Reason reason) {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId must not be null"); //$NON-NLS-1$
        }
        if (reason == null) {
            throw new IllegalArgumentException("reason must not be null"); //$NON-NLS-1$
        }
        return MessageFormat.format(
                "{1}: Session ID={0}",
                sessionId,
                reason.getDescription());
    }

    /**
     * Creates a new instance.
     * @param sessionId target session ID
     * @param reason the reason of this exception
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public SessionException(String sessionId, Reason reason) {
        this(sessionId, reason, null);
    }

    /**
     * The reason of the exception.
     * @since 0.2.2
     */
    public enum Reason {

        /**
         * The specified session already exists.
         */
        ALREADY_EXIST("The specified session already exists"),

        /**
         * The specified session does not exist.
         */
        NOT_EXIST("The specified session does not exist"),

        /**
         * The specified session was already acquired.
         */
        ACQUIRED("The specified session was already acquired"),

        /**
         * The specified session was broken.
         */
        BROKEN("The specified session was broken"),
        ;

        private final String description;

        private Reason(String description) {
            assert description != null;
            this.description = description;
        }

        /**
         * Returns the description of this reason.
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "[{0}] {1}",
                    name(),
                    getDescription());
        }
    }
}
