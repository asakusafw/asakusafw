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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.resource.ResourceProvider;
import com.asakusafw.windgate.core.session.SessionException;
import com.asakusafw.windgate.core.session.SessionException.Reason;
import com.asakusafw.windgate.core.session.SessionMirror;
import com.asakusafw.windgate.core.session.SessionProfile;
import com.asakusafw.windgate.core.session.SessionProvider;

/**
 * Aborts a session or sessions.
 * @since 0.2.2
 */
public class AbortTask {

    static final WindGateLogger WGLOG = new WindGateCoreLogger(AbortTask.class);

    static final Logger LOG = LoggerFactory.getLogger(AbortTask.class);

    private final GateProfile profile;

    private final String sessionId;

    private final SessionProvider sessionProvider;

    private final Map<String, ResourceProvider> resourceProviders;

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
        this.profile = profile;
        this.sessionId = sessionId;
        this.sessionProvider = loadSessionProvider(profile.getSession());
        this.resourceProviders = loadResourceProviders(profile.getResources());
    }

    private SessionProvider loadSessionProvider(SessionProfile session) throws IOException {
        assert session != null;
        LOG.debug("Loading session provider: {}",
                session.getProviderClass().getName());
        SessionProvider result = session.createProvider();
        return result;
    }

    private Map<String, ResourceProvider> loadResourceProviders(
            List<ResourceProfile> resources) throws IOException {
        assert resources != null;
        Map<String, ResourceProvider> results = new TreeMap<String, ResourceProvider>();
        for (ResourceProfile resourceProfile : resources) {
            LOG.debug("Loading resource provider \"{}\": {}",
                    resourceProfile.getName(),
                    resourceProfile.getProviderClass().getName());
            ResourceProvider provider = resourceProfile.createProvider();
            results.put(resourceProfile.getName(), provider);
        }
        return results;
    }

    /**
     * Executes WindGate process.
     * @throws IOException if failed
     * @throws InterruptedException if interrupted
     */
    public void execute() throws IOException, InterruptedException {
        WGLOG.info("I01000",
                sessionId,
                profile.getName());
        if (sessionId != null) {
            doAbortSingle(sessionId);
        } else {
            int failureCount = 0;
            for (String sid : sessionProvider.getCreatedIds()) {
                try {
                    doAbortSingle(sid);
                } catch (IOException e) {
                    failureCount++;
                    WGLOG.warn(e, "W01001",
                            sid,
                            profile.getName());
                }
            }
            if (failureCount > 0) {
                throw new IOException("Failed to abort some sessions");
            }
        }
        WGLOG.info("I01999",
                sessionId,
                profile.getName());
    }

    private boolean doAbortSingle(String targetSessionId) throws IOException {
        assert targetSessionId != null;
        SessionMirror session;
        try {
            WGLOG.info("I01001",
                    targetSessionId,
                    profile.getName());
            session = sessionProvider.open(targetSessionId);
        } catch (SessionException e) {
            if (e.getReason() == Reason.NOT_EXIST) {
                WGLOG.info("I01002",
                        targetSessionId,
                        profile.getName());
                return false;
            }
            throw e;
        }
        try {
            WGLOG.info("I01003",
                    targetSessionId,
                    profile.getName());
            int failureCount = 0;
            for (Map.Entry<String, ResourceProvider> entry : resourceProviders.entrySet()) {
                String name = entry.getKey();
                ResourceProvider provider = entry.getValue();
                LOG.debug("Attempting to abort resource {} (session={})",
                        name,
                        targetSessionId);
                try {
                    provider.abort(session.getId());
                } catch (IOException e) {
                    failureCount++;
                    WGLOG.warn(e, "W01002",
                            targetSessionId,
                            profile.getName(),
                            name);
                }
            }
            if (failureCount > 0) {
                throw new IOException(MessageFormat.format(
                        "Failed to abort some resources for the session \"{0}\"",
                        targetSessionId));
            }
            WGLOG.info("I01004",
                    targetSessionId,
                    profile.getName());
            session.abort();
            return true;
        } finally {
            try {
                session.close();
            } catch (IOException e) {
                WGLOG.warn(e, "W01003",
                        targetSessionId,
                        profile.getName());
            }
        }
    }
}
