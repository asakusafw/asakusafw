/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.jobqueue.client;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a job status to be received from remote server.
 * @since 0.2.6
 */
public class JobStatus {

    @SerializedName("status")
    private Kind kind;

    @SerializedName("jrid")
    private String jobId;

    private Integer exitCode;

    @SerializedName("errorCode")
    private String errorCode;

    @SerializedName("errorMessage")
    private String errorMessage;

    /**
     * Returns the kind of this status.
     * @return the status kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Sets the kind of this status.
     * @param kind the status kind
     */
    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /**
     * Returns the target job ID on current operation.
     * @return the target job ID, or undefined if the status kind is {@link JobStatus.Kind#ERROR}
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Sets the target job ID on current operation.
     * @param jobId the job ID
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    /**
     * Returns the exit code of target job.
     * @return the exit code, or undefined if the status kind is NOT {@link JobStatus.Kind#COMPLETED}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Integer getExitCode() {
        return exitCode;
    }

    /**
     * Sets the exit code of target job.
     * @param exitCode the code
     */
    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Returns the error code for target job.
     * @return the error code, or undefined if the target job has no errors
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the error code for target job.
     * @param errorCode the code
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Returns the error message for target job.
     * @return the error message, or undefined if the target job has no errors
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message for target job.
     * @param message the message
     */
    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }

    /**
     * Represent a kind of status.
     * @since 0.2.6
     */
    public enum Kind {

        /**
         * Job is initialized (initial state).
         */
        INITIALIZED("initialized"),

        /**
         * Job is submitted but is waiting for execution.
         */
        WAITING("waiting"),

        /**
         * Job is submitted and running.
         */
        RUNNING("running"),

        /**
         * Job is completed (final state).
         */
        COMPLETED("completed"),

        /**
         * Job is aborted (final state).
         */
        ERROR("error"),
        ;

        private final String symbol;

        private Kind(String symbol) {
            assert symbol != null;
            this.symbol = symbol;
        }

        /**
         * Returns the symbol of this kind.
         * @return the symbol
         */
        public String getSymbol() {
            return symbol;
        }

        /**
         * Returns the corresponded kind about the symbol.
         * @param string target symbol
         * @return the corresponded kind, of {@code null} if no such a kind
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public static Kind findFromSymbol(String string) {
            if (string == null) {
                throw new IllegalArgumentException("string must not be null"); //$NON-NLS-1$
            }
            for (Kind kind : values()) {
                if (kind.getSymbol().equals(string)) {
                    return kind;
                }
            }
            return null;
        }
    }
}
