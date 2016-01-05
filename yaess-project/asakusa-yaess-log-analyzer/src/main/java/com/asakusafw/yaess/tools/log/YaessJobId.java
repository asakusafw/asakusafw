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
package com.asakusafw.yaess.tools.log;

/**
 * Represents a YAESS Job identifier.
 * @since 0.6.2
 */
public class YaessJobId {

    private String batchId;

    private String flowId;

    private String executionId;

    private String phase;

    private String jobId;

    private String serviceId;

    private String trackingId;

    /**
     * Creates a new instance.
     */
    public YaessJobId() {
        return;
    }

    /**
     * Returns the batch ID.
     * @return the batch ID, or {@code null} if it is unknown
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Sets the batch ID.
     * @param id the ID
     */
    public void setBatchId(String id) {
        this.batchId = id;
    }

    /**
     * Returns the flow ID.
     * @return the flow ID, or {@code null} if it is unknown
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * Sets the flow ID.
     * @param id the ID
     */
    public void setFlowId(String id) {
        this.flowId = id;
    }

    /**
     * Returns the execution ID.
     * @return the execution ID, or {@code null} if it is unknown
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * Sets the execution ID.
     * @param id the ID
     */
    public void setExecutionId(String id) {
        this.executionId = id;
    }

    /**
     * Returns the phase name.
     * @return the phase name, or {@code null} if it is unknown
     */
    public String getPhase() {
        return phase;
    }

    /**
     * Sets the phase name.
     * @param name the name
     */
    public void setPhase(String name) {
        this.phase = name;
    }

    /**
     * Returns the local job ID (similar to {@literal "stage name"}).
     * @return the local job ID, or {@code null} if it is unknown
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Sets the local job ID.
     * @param id the ID
     */
    public void setJobId(String id) {
        this.jobId = id;
    }

    /**
     * Returns the service ID (similar to {@literal "profile name"}).
     * @return the service ID, or {@code null} if it is unknown
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the service ID.
     * @param id the ID
     */
    public void setServiceId(String id) {
        this.serviceId = id;
    }

    /**
     * Returns the tracking ID.
     * @return the tracking ID, or {@code null} if it is unknown
     */
    public String getTrackingId() {
        return trackingId;
    }

    /**
     * Sets the tracking ID.
     * @param id the ID
     */
    public void setTrackingId(String id) {
        this.trackingId = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((batchId == null) ? 0 : batchId.hashCode());
        result = prime * result + ((executionId == null) ? 0 : executionId.hashCode());
        result = prime * result + ((flowId == null) ? 0 : flowId.hashCode());
        result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
        result = prime * result + ((phase == null) ? 0 : phase.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        result = prime * result + ((trackingId == null) ? 0 : trackingId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        YaessJobId other = (YaessJobId) obj;
        if (batchId == null) {
            if (other.batchId != null) {
                return false;
            }
        } else if (!batchId.equals(other.batchId)) {
            return false;
        }
        if (executionId == null) {
            if (other.executionId != null) {
                return false;
            }
        } else if (!executionId.equals(other.executionId)) {
            return false;
        }
        if (flowId == null) {
            if (other.flowId != null) {
                return false;
            }
        } else if (!flowId.equals(other.flowId)) {
            return false;
        }
        if (jobId == null) {
            if (other.jobId != null) {
                return false;
            }
        } else if (!jobId.equals(other.jobId)) {
            return false;
        }
        if (phase == null) {
            if (other.phase != null) {
                return false;
            }
        } else if (!phase.equals(other.phase)) {
            return false;
        }
        if (serviceId == null) {
            if (other.serviceId != null) {
                return false;
            }
        } else if (!serviceId.equals(other.serviceId)) {
            return false;
        }
        if (trackingId == null) {
            if (other.trackingId != null) {
                return false;
            }
        } else if (!trackingId.equals(other.trackingId)) {
            return false;
        }
        return true;
    }
}
