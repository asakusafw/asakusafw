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
package com.asakusafw.trace.model;

import java.util.Collections;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a setting of tracing.
 * @since 0.5.1
 */
public class TraceSetting {

    @SerializedName("point")
    private Tracepoint tracepoint;

    @SerializedName("mode")
    private Mode mode;

    @SerializedName("attributes")
    private Map<String, String> attributes = Collections.emptyMap();

    TraceSetting() {
        return;
    }

    /**
     * Creates a new instance.
     * @param tracepoint target tracepoint
     * @param mode trace mode
     * @param attributes the extra attributes
     */
    public TraceSetting(Tracepoint tracepoint, Mode mode, Map<String, String> attributes) {
        this.tracepoint = tracepoint;
        this.mode = mode;
        this.attributes = attributes;
    }

    /**
     * Returns the tracepoint.
     * @return the point
     */
    public Tracepoint getTracepoint() {
        return tracepoint;
    }

    /**
     * Returns the trigger mode.
     * @return the mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Returns the extra-attributes.
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Represents trace mode.
     * @since 0.5.1
     */
    public enum Mode {

        /**
         * Fire strictly.
         */
        STRICT,

        /**
         * Fire in-ordered.
         *
         */
        IN_ORDER,

        /**
         * Fire out-of-ordered.
         */
        OUT_OF_ORDER,
    }
}
