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
package com.asakusafw.compiler.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowGraphRewriter;

/**
 * Aggregates repositories of {@link FlowGraphRewriter} using Service Provider Interface.
 */
public class SpiFlowGraphRewriterRepository
        extends FlowCompilingEnvironment.Initialized
        implements FlowGraphRewriter.Repository {

    static final Logger LOG = LoggerFactory.getLogger(SpiFlowGraphRewriterRepository.class);

    private List<FlowGraphRewriter> rewriters;

    @Override
    protected void doInitialize() {
        LOG.debug("loading flow graph rewriting plug-ins"); //$NON-NLS-1$
        this.rewriters = new ArrayList<>();
        ServiceLoader<FlowGraphRewriter> services = ServiceLoader.load(
                FlowGraphRewriter.class,
                getEnvironment().getServiceClassLoader());
        for (FlowGraphRewriter rewriter : services) {
            rewriter.initialize(getEnvironment());
            LOG.debug("found flow graph rewriting plug-in: {}", rewriter.getClass().getName()); //$NON-NLS-1$
            rewriters.add(rewriter);
        }
    }

    @Override
    public List<FlowGraphRewriter> getRewriters() {
        return rewriters;
    }
}
