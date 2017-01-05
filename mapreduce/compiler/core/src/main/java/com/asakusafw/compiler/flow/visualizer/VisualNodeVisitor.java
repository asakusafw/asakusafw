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
package com.asakusafw.compiler.flow.visualizer;

/**
 * A visitor for {@link VisualNode}.
 * Methods in this implementation always returns {@code null}.
 * @param <C> type of visitor context
 * @param <R> type of visitor result
 * @param <E> type of visitor exception
 * @see VisualNode#accept(VisualNodeVisitor, Object)
 */
public abstract class VisualNodeVisitor<R, C, E extends Throwable> {

    /**
     * Processes {@link VisualGraph}.
     * @param node the target node
     * @param context the current context (nullable)
     * @return the result of this invocation
     * @throws E if error occurred while processing the target node
     */
    protected R visitGraph(C context, VisualGraph node) throws E {
        return null;
    }

    /**
     * Processes {@link VisualBlock}.
     * @param node the target node
     * @param context the current context (nullable)
     * @return the result of this invocation
     * @throws E if error occurred while processing the target node
     */
    protected R visitBlock(C context, VisualBlock node) throws E {
        return null;
    }

    /**
     * Processes {@link VisualFlowPart}.
     * @param node the target node
     * @param context the current context (nullable)
     * @return the result of this invocation
     * @throws E if error occurred while processing the target node
     */
    protected R visitFlowPart(C context, VisualFlowPart node) throws E {
        return null;
    }

    /**
     * Processes {@link VisualElement}.
     * @param node the target node
     * @param context the current context (nullable)
     * @return the result of this invocation
     * @throws E if error occurred while processing the target node
     */
    protected R visitElement(C context, VisualElement node) throws E {
        return null;
    }

    /**
     * Processes {@link VisualLabel}.
     * @param node the target node
     * @param context the current context (nullable)
     * @return the result of this invocation
     * @throws E if error occurred while processing the target node
     */
    protected R visitLabel(C context, VisualLabel node) throws E {
        return null;
    }
}
