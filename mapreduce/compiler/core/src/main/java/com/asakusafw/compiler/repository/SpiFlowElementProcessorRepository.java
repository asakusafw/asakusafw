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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.LinePartProcessor;
import com.asakusafw.compiler.flow.LineProcessor;
import com.asakusafw.compiler.flow.RendezvousProcessor;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;

/**
 * Aggregates repositories of {@link FlowElementProcessor} using Service Provider Interface.
 */
public class SpiFlowElementProcessorRepository
        extends FlowCompilingEnvironment.Initialized
        implements FlowElementProcessor.Repository {

    static final Logger LOG = LoggerFactory.getLogger(SpiFlowElementProcessorRepository.class);

    private LinePartProcessor emptyProcessor;

    private Map<Class<? extends Annotation>, LineProcessor> lines;

    private Map<Class<? extends Annotation>, RendezvousProcessor> rendezvouses;

    @Override
    protected void doInitialize() {
        LOG.debug("loading flow element processor plug-ins"); //$NON-NLS-1$
        this.emptyProcessor = new LinePartProcessor.Nop();
        this.lines = new HashMap<>();
        this.rendezvouses = new HashMap<>();
        emptyProcessor.initialize(getEnvironment());
        Map<Class<?>, FlowElementProcessor> saw = new HashMap<>();
        ServiceLoader<FlowElementProcessor> services = ServiceLoader.load(
                FlowElementProcessor.class,
                getEnvironment().getServiceClassLoader());
        for (FlowElementProcessor proc : services) {
            proc.initialize(getEnvironment());
            Class<? extends Annotation> targetType = proc.getTargetAnnotationType();
            if (saw.containsKey(targetType)) {
                getEnvironment().error(
                        Messages.getString("SpiFlowElementProcessorRepository.errorConflictProcessor"), //$NON-NLS-1$
                        targetType.getName(),
                        saw.get(targetType).getClass().getName(),
                        proc.getClass().getName());
                continue;
            }
            LOG.debug("found flow element processor: {} ({})", //$NON-NLS-1$
                    targetType.getName(),
                    proc.getClass().getName());
            saw.put(targetType, proc);
            switch (proc.getKind()) {
            case LINE_PART:
            case LINE_END:
                lines.put(targetType, (LineProcessor) proc);
                break;

            case RENDEZVOUS:
                rendezvouses.put(targetType, (RendezvousProcessor) proc);
                break;

            default:
                throw new AssertionError(proc.getKind());
            }
        }
    }

    @Override
    public LinePartProcessor getEmptyProcessor() {
        return emptyProcessor;
    }

    @Override
    public FlowElementProcessor findProcessor(FlowElementDescription description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$

        LineProcessor lineProc = findLineProcessor(description);
        if (lineProc != null) {
            return lineProc;
        }

        RendezvousProcessor rendProc = findRendezvousProcessor(description);
        if (rendProc != null) {
            return rendProc;
        }

        return null;
    }

    @Override
    public LineProcessor findLineProcessor(FlowElementDescription description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        if (description.getKind() != FlowElementKind.OPERATOR) {
            return null;
        }

        OperatorDescription op = (OperatorDescription) description;
        return lines.get(op.getDeclaration().getAnnotationType());
    }

    @Override
    public RendezvousProcessor findRendezvousProcessor(FlowElementDescription description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        if (description.getKind() != FlowElementKind.OPERATOR) {
            return null;
        }

        OperatorDescription op = (OperatorDescription) description;
        return rendezvouses.get(op.getDeclaration().getAnnotationType());
    }
}
