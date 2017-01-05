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
package com.asakusafw.compiler.tool.batchspec;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Specification of batch.
 * @since 0.5.0
 */
public class BatchSpec {

    @SerializedName("id")
    private String id;

    @SerializedName("comment")
    private String comment;

    @SerializedName("strict")
    private boolean strict;

    @SerializedName("parameters")
    private List<Parameter> parameters = Collections.emptyList();

    BatchSpec() {
        return;
    }

    /**
     * Creates a new instance.
     * @param id the target batch ID
     */
    public BatchSpec(String id) {
        this(id, null, false, Collections.emptyList());
    }

    /**
     * Creates a new instance.
     * @param id the target batch ID
     * @param comment comment for the batch (nullable)
     * @param strict {@code true} if extra parameters are NOT permitted, otherwise {@code false}
     * @param parameters the batch parameters
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public BatchSpec(String id, String comment, boolean strict, List<Parameter> parameters) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters must not be null"); //$NON-NLS-1$
        }
        this.id = id;
        this.comment = comment;
        this.strict = strict;
        this.parameters = parameters;
    }

    /**
     * Returns the target batch ID.
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the batch comment.
     * @return the comment, or {@code null} if it was not defined
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns whether or not the optional parameters are restricted.
     * @return {@code true} not to permit extra parameters, otherwise {@code false}
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * Returns the parameters.
     * @return the parameters
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Represents a batch parameter.
     * @since 0.5.0
     */
    public static class Parameter {

        @SerializedName("key")
        private String key;

        @SerializedName("comment")
        private String comment;

        @SerializedName("required")
        private boolean required;

        @SerializedName("pattern")
        private String pattern;

        Parameter() {
            return;
        }

        /**
         * Creates a new instance.
         * @param key the parameter key
         * @param comment the parameter comment (nullable)
         * @param required {@code true} iff the parameter is required
         * @param pattern the parameter value pattern in regex
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public Parameter(String key, String comment, boolean required, String pattern) {
            if (key == null) {
                throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
            }
            if (pattern == null) {
                throw new IllegalArgumentException("pattern must not be null"); //$NON-NLS-1$
            }
            this.key = key;
            this.comment = comment;
            this.required = required;
            this.pattern = pattern;
        }

        /**
         * Returns the key of the target parameter.
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * Returns a comment for this parameter.
         * @return a comment, or {@code null} if it is not defined
         */
        public String getComment() {
            return comment;
        }

        /**
         * Returns whether or not this parameter is required.
         * @return {@code true} if this is required, otherwise {@code false}
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * The parameter value pattern in regex.
         * @return the pattern string
         */
        public String getPattern() {
            return pattern;
        }
    }
}
