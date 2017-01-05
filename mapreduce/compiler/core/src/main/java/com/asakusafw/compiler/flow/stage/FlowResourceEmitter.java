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
package com.asakusafw.compiler.flow.stage;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowGraphRewriter;
import com.asakusafw.compiler.flow.FlowGraphRewriter.RewriteException;
import com.asakusafw.runtime.flow.FlowResource;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;

/**
 * Compiles {@link FlowResourceDescription} and provides the corresponding {@link FlowResource}.
 */
public class FlowResourceEmitter {

    static final Logger LOG = LoggerFactory.getLogger(FlowResourceEmitter.class);

    private final FlowCompilingEnvironment environment;

    private final List<FlowGraphRewriter> rewriters;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public FlowResourceEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
        this.rewriters = environment.getGraphRewriters().getRewriters();
    }

    /**
     * Compiles a set of {@link FlowResourceDescription}.
     * @param resources the target descriptions
     * @return a mapping of {@link FlowResourceDescription} and its compiled type
     * @throws IOException if error was occurred while creating the class
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Map<FlowResourceDescription, CompiledType> emit(Set<FlowResourceDescription> resources) throws IOException {
        Precondition.checkMustNotBeNull(resources, "resources"); //$NON-NLS-1$
        Map<FlowResourceDescription, CompiledType> results = new HashMap<>();
        for (FlowResourceDescription resource : resources) {
            Name compiled = compile(resource);
            if (compiled != null) {
                results.put(resource, new CompiledType(compiled));
            }
        }
        return results;
    }

    private Name compile(FlowResourceDescription resource) {
        assert resource != null;
        for (FlowGraphRewriter rewriter : rewriters) {
            try {
                Name compiled = rewriter.resolve(resource);
                if (compiled != null) {
                    LOG.debug("compiled resource: {} ({})", resource, compiled); //$NON-NLS-1$
                    return compiled;
                }
            } catch (RewriteException e) {
                environment.error(
                        Messages.getString("FlowResourceEmitter.errorFailedToCompile") //$NON-NLS-1$
                        + ": {0}", //$NON-NLS-1$
                        resource,
                        e.getMessage());
                LOG.error(MessageFormat.format(
                        Messages.getString("FlowResourceEmitter.errorFailedToCompile"), //$NON-NLS-1$
                        resource),
                        e);
            }
        }
        environment.error(
                Messages.getString("FlowResourceEmitter.errorMissingProcessor"), //$NON-NLS-1$
                resource);
        return null;
    }
}
