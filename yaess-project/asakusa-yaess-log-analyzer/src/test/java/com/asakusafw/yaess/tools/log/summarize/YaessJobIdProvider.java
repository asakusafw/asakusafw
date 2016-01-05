/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.yaess.tools.log.summarize;

import com.asakusafw.yaess.tools.log.YaessJobId;

/**
 * Provides {@link YaessJobId}.
 */
public class YaessJobIdProvider {

    private YaessJobId id;

    /**
     * Creates a {@link YaessJobId}.
     * @return a job ID
     */
    protected YaessJobId createId() {
        return new YaessJobId();
    }

    /**
     * Returns the original ID.
     * @return the original ID
     */
    public final YaessJobId id() {
        if (id == null) {
            id = createId();
            if (id == null) {
                throw new IllegalStateException();
            }
        }
        return id;
    }

    /**
     * Returns a copy of the original ID.
     * @return a copy
     */
    public YaessJobId copy() {
        YaessJobId copy = new YaessJobId();
        copy.setBatchId(id().getBatchId());
        copy.setFlowId(id().getFlowId());
        copy.setJobId(id().getJobId());
        copy.setPhase(id().getPhase());
        copy.setExecutionId(id().getExecutionId());
        copy.setServiceId(id().getServiceId());
        copy.setTrackingId(id().getTrackingId());
        return copy;
    }
}
