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
package com.asakusafw.windgate.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.resource.ResourceProvider;
import com.asakusafw.windgate.core.session.SessionException;
import com.asakusafw.windgate.core.session.SessionException.Reason;
import com.asakusafw.windgate.core.session.SessionMirror;
import com.asakusafw.windgate.core.session.SessionProfile;
import com.asakusafw.windgate.core.session.SessionProvider;

/**
 * Aborts a session or sessions.
 * @since 0.2.3
 */
public class AbortTask {

    private final String sessionId;

    private final SessionProvider sessionProvider;

    private final List<ResourceProvider> resourceProviders;

    /**
     * Creates a new instance.
     * @param profile the target profile
     * @param sessionId the target session ID to abort, or {@code null} if aborts all sessions
     * @throws IOException if failed to initialize the task
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public AbortTask(GateProfile profile, String sessionId) throws IOException {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        this.sessionId = sessionId;
        this.sessionProvider = loadSessionProvider(profile.getSession());
        this.resourceProviders = loadResourceProviders(profile.getResources());
    }

    private SessionProvider loadSessionProvider(SessionProfile session) throws IOException {
        assert session != null;
        // TODO logging
        SessionProvider result = session.createProvider();
        return result;
    }

    private List<ResourceProvider> loadResourceProviders(
            List<ResourceProfile> resources) throws IOException {
        assert resources != null;
        List<ResourceProvider> results = new ArrayList<ResourceProvider>();
        for (ResourceProfile profile : resources) {
            // TODO logging
            ResourceProvider provider = profile.createProvider();
            results.add(provider);
        }
        return results;
    }

    /**
     * Executes WindGate process.
     * @throws IOException if failed
     * @throws InterruptedException if interrupted
     */
    public void execute() throws IOException, InterruptedException {
        if (sessionId != null) {
            doAbortSingle(sessionId);
        } else {
            int failureCount = 0;
            for (String sid : sessionProvider.getCreatedIds()) {
                try {
                    doAbortSingle(sid);
                } catch (IOException e) {
                    failureCount++;
                    // TODO warn
                }
            }
            if (failureCount > 0) {
                throw new IOException("Failed to abort some sessions");
            }
        }
    }

    private boolean doAbortSingle(String targetSessionId) throws IOException {
        assert targetSessionId != null;
        SessionMirror session;
        try {
            session = sessionProvider.open(targetSessionId);
        } catch (SessionException e) {
            if (e.getReason() == Reason.NOT_EXIST) {
                // it seems that the session was already completed/aborted.
                return false;
            }
            throw e;
        }
        try {
            int failureCount = 0;
            for (ResourceProvider provider : resourceProviders) {
                // TODO logging
                try {
                    provider.abort(session.getId());
                } catch (IOException e) {
                    failureCount++;
                    // TODO logging
                }
            }
            if (failureCount > 0) {
                throw new IOException(MessageFormat.format(
                        "Failed to abort some resources for the session \"{0}\"",
                        targetSessionId));
            }
            session.abort();
            return true;
        } finally {
            try {
                session.close();
            } catch (IOException e) {
                // TODO warn
            }
        }
    }
}
