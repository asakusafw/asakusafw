/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.yaess.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A script describes each batch structure.
 * @since 0.2.3
 * @version 0.4.0
 */
public class BatchScript {

    /**
     * A configuration key name of IDs.
     */
    public static final String KEY_ID = "batch.id";

    /**
     * A configuration key name of script version.
     */
    public static final String KEY_VERSION = "batch.version";

    /**
     * A configuration key name of verification code.
     */
    public static final String KEY_VERIFICATION_CODE = "batch.buildId";

    /**
     * Current version.
     */
    public static final String VERSION = "0.2";

    private final String id;

    private final String buildId;

    private final SortedMap<String, FlowScript> flows;

    /**
     * Creates a new instance.
     * @param id ID of this batch
     * @param flows member flows
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public BatchScript(String id, Collection<FlowScript> flows) {
        this(id, null, flows);
    }

    /**
     * Creates a new instance.
     * @param batchId ID of this batch
     * @param buildId the application verification code, or {@code null} if not defined
     * @param flows member flows
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.4.0
     */
    public BatchScript(String batchId, String buildId, Collection<FlowScript> flows) {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        if (flows == null) {
            throw new IllegalArgumentException("flows must not be null"); //$NON-NLS-1$
        }
        this.id = batchId;
        this.buildId = buildId;
        SortedMap<String, FlowScript> map = new TreeMap<String, FlowScript>();
        for (FlowScript flow : flows) {
            map.put(flow.getId(), flow);
        }
        this.flows = Collections.unmodifiableSortedMap(map);
    }

    /**
     * Returns the ID of this batch.
     * @return the batch ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the build ID of this batch.
     * @return the build ID, or {@code null} if not defined
     */
    public String getBuildId() {
        return buildId;
    }

    /**
     * Returns a {@link FlowScript} which has the specified flow ID.
     * @param flowId target flow ID
     * @return the related {@link FlowScript}, or {@code null} if not found
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowScript findFlow(String flowId) {
        if (flowId == null) {
            throw new IllegalArgumentException("flowId must not be null"); //$NON-NLS-1$
        }
        return flows.get(flowId);
    }

    /**
     * Returns all {@link FlowScript}s defined in this batch.
     * @return {@link FlowScript}s
     */
    public List<FlowScript> getAllFlows() {
        LinkedList<FlowScript> work = new LinkedList<FlowScript>(flows.values());
        Set<String> blockerIds = new HashSet<String>(flows.keySet());
        // tiny topological sort
        List<FlowScript> results = new ArrayList<FlowScript>();
        while (work.isEmpty() == false) {
            boolean worked = false;
            for (Iterator<FlowScript> iter = work.iterator(); iter.hasNext();) {
                FlowScript script = iter.next();
                boolean blocked = false;
                for (String blockerId : script.getBlockerIds()) {
                    if (blockerIds.contains(blockerId)) {
                        blocked = true;
                        break;
                    }
                }
                if (blocked == false) {
                    iter.remove();
                    blockerIds.remove(script.getId());
                    results.add(script);
                }
            }
            // avoid deadlocking
            if (worked == false) {
                results.addAll(work);
                work.clear();
            }
        }
        return results;
    }

    /**
     * Loads a {@link BatchScript}.
     * @param properties source properties
     * @return the loaded script
     * @throws IllegalArgumentException if script is invalid, or some parameters were {@code null}
     */
    public static BatchScript load(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        String version = properties.getProperty(KEY_VERSION);
        if (VERSION.equals(version) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unsupported script version: {0}",
                    version));
        }
        String batchId = properties.getProperty(KEY_ID);
        String verificationCode = properties.getProperty(KEY_VERIFICATION_CODE);
        Set<String> flowIds = FlowScript.extractFlowIds(properties);
        List<FlowScript> flowScripts = new ArrayList<FlowScript>();
        for (String flowId : flowIds) {
            FlowScript flowScript = FlowScript.load(properties, flowId);
            flowScripts.add(flowScript);
        }
        return new BatchScript(batchId, verificationCode, flowScripts);
    }
}
