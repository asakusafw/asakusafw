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
package com.asakusafw.compiler.batch;


import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.batch.WorkDescription;

/**
 * Represents a workflow structure for each batch.
 */
public class Workflow {

    private final BatchDescription description;

    private final Graph<Unit> graph;

    /**
     * Creates a new instance.
     * @param description the batch description
     * @param graph the dependency graph of unit-of-works
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public Workflow(BatchDescription description, Graph<Unit> graph) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        this.description = description;
        this.graph = graph;
    }

    /**
     * Returns the target batch description.
     * @return the target batch description
     */
    public BatchDescription getDescription() {
        return description;
    }

    /**
     * Returns the dependency graph of unit-of-works in this batch.
     * @return the dependency graph
     */
    public Graph<Unit> getGraph() {
        return graph;
    }

    /**
     * Represents a unit-of-work in the {@link Workflow}.
     */
    public static class Unit {

        private final WorkDescription description;

        private boolean isProcessed;

        private Object processed;

        /**
         * Creates a new instance.
         * @param description description of the target unit-of-work
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Unit(WorkDescription description) {
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            this.description = description;
        }

        /**
         * Returns the description of this unit-of-work.
         * @return the description
         */
        public WorkDescription getDescription() {
            return description;
        }

        /**
         * Returns the processed information.
         * @return the processed information, or {@code null} if there is no such information for this
         * @throws IllegalStateException if processing this has not been completed
         */
        public Object getProcessed() {
            if (isProcessed == false) {
                throw new IllegalStateException();
            }
            return processed;
        }

        /**
         * Sets the processed information.
         * @param result the processed information, or {@code null} if there is no such information for this
         * @throws IllegalStateException if processing this has been already completed
         */
        public void setProcessed(Object result) {
            if (isProcessed) {
                throw new IllegalStateException();
            }
            isProcessed = true;
            this.processed = result;
        }
    }
}
