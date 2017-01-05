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
package com.asakusafw.compiler.flow.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowGraphRewriter;
import com.asakusafw.compiler.flow.plan.FlowGraphUtil;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Logging;
import com.asakusafw.vocabulary.operator.Logging.Level;

/**
 * Removes debug level logging operators.
 */
public class LoggingFilter extends FlowCompilingEnvironment.Initialized implements FlowGraphRewriter {

    static final Logger LOG = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Phase getPhase() {
        return Phase.EARLY_OPTIMIZE;
    }

    @Override
    public boolean rewrite(FlowGraph graph) throws RewriteException {
        if (getEnvironment().getOptions().isEnableDebugLogging()) {
            LOG.debug("debug logging is enabled"); //$NON-NLS-1$
            return false;
        }
        LOG.debug("removing debug logging operators"); //$NON-NLS-1$
        return rewriteGraph(graph);
    }

    private boolean rewriteGraph(FlowGraph graph) {
        boolean modified = false;
        for (FlowElement element : FlowGraphUtil.collectElements(graph)) {
            if (element.getDescription().getKind() == FlowElementKind.FLOW_COMPONENT) {
                FlowPartDescription desc = (FlowPartDescription) element.getDescription();
                modified |= rewriteGraph(desc.getFlowGraph());
            } else if (isDebugLogging(element)) {
                LOG.debug("removing debug logging operator: {}", element); //$NON-NLS-1$
                FlowGraphUtil.skip(element);
                modified = true;
            }
        }
        return modified;
    }

    private boolean isDebugLogging(FlowElement element) {
        assert element != null;
        if (element.getDescription().getKind() != FlowElementKind.OPERATOR) {
            return false;
        }
        OperatorDescription desc = (OperatorDescription) element.getDescription();
        if (desc.getDeclaration().getAnnotationType() != Logging.class) {
            return false;
        }
        if (desc.getAttribute(Logging.Level.class) != Level.DEBUG) {
            return false;
        }
        return true;
    }

    @Override
    public Name resolve(FlowResourceDescription resource) throws RewriteException {
        return null;
    }
}
