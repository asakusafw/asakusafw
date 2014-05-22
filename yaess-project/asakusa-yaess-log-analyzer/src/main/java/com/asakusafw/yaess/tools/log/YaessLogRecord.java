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
package com.asakusafw.yaess.tools.log;

/**
 * Represents a YAESS log record.
 * @since 0.6.2
 */
public class YaessLogRecord {

    private long time = -1L;

    private String code;

    private YaessJobId jobId;

    /**
     * Creates a new empty instance.
     */
    public YaessLogRecord() {
        return;
    }

    /**
     * Returns the record time.
     * @return the unit time, or {@code -1L} if it is not defined
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets the record time.
     * @param time the unit time
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Returns the log code.
     * @return the log code, or {@code null} if it is not defined
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the log code.
     * @param code the log code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Returns the job ID.
     * @return the job ID, or {@code null} if it is not defined
     */
    public YaessJobId getJobId() {
        return jobId;
    }

    /**
     * Sets the job ID.
     * @param jobId the job ID
     */
    public void setJobId(YaessJobId jobId) {
        this.jobId = jobId;
    }
}
