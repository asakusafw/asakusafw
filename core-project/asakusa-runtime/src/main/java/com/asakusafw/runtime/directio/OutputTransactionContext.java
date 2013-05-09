/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
 * Represents a context for output contents to staging area.
 * @since 0.2.5
 */
public final class OutputTransactionContext {

    private final String transactionId;

    private final String outputId;

    private final Counter counter;

    /**
     * Creates a new instance.
     * @param transactionId current transaction ID
     * @param outputId current output ID
     * @param counter operation counter for this context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public OutputTransactionContext(String transactionId, String outputId, Counter counter) {
        if (transactionId == null) {
            throw new IllegalArgumentException("transactionId must not be null"); //$NON-NLS-1$
        }
        if (outputId == null) {
            throw new IllegalArgumentException("outputId must not be null"); //$NON-NLS-1$
        }
        if (counter == null) {
            throw new IllegalArgumentException("counter must not be null"); //$NON-NLS-1$
        }
        this.transactionId = transactionId;
        this.outputId = outputId;
        this.counter = counter;
    }

    /**
     * Returns the transaction ID corresponding to this output.
     * @return the transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Returns the output ID corresponding to this output.
     * @return the output ID
     */
    public String getOutputId() {
        return outputId;
    }

    /**
     * Returns the operation counter.
     * @return the counter
     */
    public Counter getCounter() {
        return counter;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OutputTransactionContext [transactionId=");
        builder.append(transactionId);
        builder.append(", outputId=");
        builder.append(outputId);
        builder.append("]");
        return builder.toString();
    }
}
