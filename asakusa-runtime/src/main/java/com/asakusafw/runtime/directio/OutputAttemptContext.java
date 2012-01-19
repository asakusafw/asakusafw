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
package com.asakusafw.runtime.directio;

/**
 * Represents a context for output contents to attempt area.
 * @since 0.2.5
 */
public final class OutputAttemptContext {

    private final String transactionId;

    private final String attemptId;

    private final String outputId;

    /**
     * Creates a new instance.
     * @param transactionId current transaction ID
     * @param attemptId current attempt ID
     * @param outputId current output ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public OutputAttemptContext(String transactionId, String attemptId, String outputId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("transactionId must not be null"); //$NON-NLS-1$
        }
        if (attemptId == null) {
            throw new IllegalArgumentException("attemptId must not be null"); //$NON-NLS-1$
        }
        if (outputId == null) {
            throw new IllegalArgumentException("outputId must not be null"); //$NON-NLS-1$
        }
        this.transactionId = transactionId;
        this.attemptId = attemptId;
        this.outputId = outputId;
    }

    /**
     * Returns the corresponded transaction context.
     * @return the transaction context
     */
    public OutputTransactionContext getTransactionContext() {
        return new OutputTransactionContext(transactionId, outputId);
    }

    /**
     * Returns the transaction ID corresponding to this output.
     * @return the transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Returns the attempt ID corresponding to this output.
     * @return the attempt ID
     */
    public String getAttemptId() {
        return attemptId;
    }

    /**
     * Returns the output ID corresponding to this output.
     * @return the output ID
     */
    public String getOutputId() {
        return outputId;
    }
}
