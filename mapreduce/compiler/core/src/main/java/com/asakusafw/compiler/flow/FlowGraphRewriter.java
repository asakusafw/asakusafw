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
package com.asakusafw.compiler.flow;

import java.util.List;

import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;

/**
 * An abstract super interface of rewriting flow graphs.
 * <p>
 * Adding flow graph rewriter to Flow DSL compiler, clients can implement this and put the class name in
 * {@code META-INF/services/com.asakusafw.compiler.flow.FlowGraphRewriter}.
 * </p>
 * @since 0.1.0
 * @version 0.5.1
 */
public interface FlowGraphRewriter extends FlowCompilingEnvironment.Initializable {

    /**
     * Returns the phase when the compiler activates this rewriter.
     * @return the activating phase
     * @since 0.5.1
     */
    Phase getPhase();

    /**
     * Rewrites the target flow graph.
     * @param graph the target flow graph
     * @return {@code true} if this was actually rewrite the target graph, otherwise {@code false}
     * @throws RewriteException if error occurred while rewriting the flow graph
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    boolean rewrite(FlowGraph graph) throws RewriteException;

    /**
     * Returns the target external resource and returns the qualified class name of the corresponding
     * {@link FlowResourceDescription}.
     * @param resource the target resource description
     * @return the target class name, or {@code null} if this does not handle the target resource
     * @throws RewriteException if error occurred while resolving the description
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    Name resolve(FlowResourceDescription resource) throws RewriteException;

    /**
     * Represents a rewrite phase.
     * @since 0.5.1
     */
    enum Phase {

        /**
         * Rewrite flow-graph for debugging.
         */
        EARLY_DEBUG,

        /**
         * Rewrite flow-graph for debugging.
         */
        LATER_DEBUG,

        /**
         * Rewrite flow-graph for optimization.
         */
        EARLY_OPTIMIZE,

        /**
         * Rewrite flow-graph for optimization.
         */
        LATER_OPTIMIZE,
    }

    /**
     * An exception which is occurred when rewriting flow graphs.
     */
    class RewriteException extends Exception {

        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance.
         */
        public RewriteException() {
            super();
        }

        /**
         * Creates a new instance.
         * @param message the exception message (nullable)
         * @param cause the original cause (nullable)
         */
        public RewriteException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Creates a new instance.
         * @param message the exception message (nullable)
         */
        public RewriteException(String message) {
            super(message);
        }

        /**
         * Creates a new instance.
         * @param cause the original cause (nullable)
         */
        public RewriteException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * An abstract super interface of repository for {@link FlowGraphRewriter}.
     */
    interface Repository extends FlowCompilingEnvironment.Initializable {

        List<FlowGraphRewriter> getRewriters();
    }
}
