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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.BatchCompilingEnvironment;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.WorkflowProcessor;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.batch.WorkDescription;

/**
 * Aggregates repositories of {@link WorkflowProcessor} using Service Provider Interface.
 */
public class SpiWorkflowProcessorRepository
        extends BatchCompilingEnvironment.Initialized
        implements WorkflowProcessor.Repository {

    static final Logger LOG = LoggerFactory.getLogger(SpiWorkflowProcessorRepository.class);

    private Map<WorkflowProcessor, Set<WorkDescriptionProcessor<?>>> processors;

    private Map<Class<? extends WorkDescription>, WorkDescriptionProcessor<?>> descriptionProcessors;

    @Override
    protected void doInitialize() {
        LOG.debug("loading workflow processor plug-ins"); //$NON-NLS-1$
        Iterable<? extends WorkflowProcessor> services = loadServices();
        List<WorkflowProcessor> procs = new ArrayList<>();
        for (WorkflowProcessor proc : services) {
            proc.initialize(getEnvironment());
            procs.add(proc);
            LOG.debug("activating workflow processor plug-in: {}", proc.getClass().getName()); //$NON-NLS-1$
        }

        Map<Class<? extends WorkDescriptionProcessor<?>>, WorkDescriptionProcessor<?>> saw = new HashMap<>();
        descriptionProcessors = new HashMap<>();
        for (WorkflowProcessor proc : procs) {
            proc.initialize(getEnvironment());
            for (Class<? extends WorkDescriptionProcessor<?>> type
                    : proc.getDescriptionProcessors()) {
                if (saw.containsKey(type)) {
                    continue;
                }
                saw.put(type, null);
                WorkDescriptionProcessor<?> dproc = newInstance(type);
                if (dproc == null) {
                    continue;
                }
                dproc.initialize(getEnvironment());
                saw.put(type, dproc);
                Class<? extends WorkDescription> target = dproc.getTargetType();
                descriptionProcessors.put(target, dproc);
            }
        }

        processors = new HashMap<>();
        for (WorkflowProcessor proc : procs) {
            Set<WorkDescriptionProcessor<?>> subProcs = new HashSet<>();
            for (Class<? extends WorkDescriptionProcessor<?>> subProcClass
                    : proc.getDescriptionProcessors()) {
                WorkDescriptionProcessor<?> subProc = saw.get(subProcClass);
                if (subProc != null) {
                    subProcs.add(subProc);
                }
            }
            processors.put(proc, subProcs);
        }
    }

    /**
     * Returns the available {@link WorkflowProcessor} instances in this environment.
     * @return the available {@link WorkflowProcessor} instances
     */
    protected Iterable<? extends WorkflowProcessor> loadServices() {
        Iterable<WorkflowProcessor> services = ServiceLoader.load(
                WorkflowProcessor.class,
                getEnvironment().getConfiguration().getServiceClassLoader());
        return services;
    }

    private <T extends WorkDescriptionProcessor<?>> T newInstance(Class<T> type) {
        try {
            return type.newInstance();
        } catch (Exception e) {
            getEnvironment().error(
                    Messages.getString("SpiWorkflowProcessorRepository.errorFailedToInitialize"), //$NON-NLS-1$
                    type.getName());
            return null;
        }
    }

    @Override
    public Set<WorkflowProcessor> findWorkflowProcessors(
            Set<? extends WorkDescription> descriptions) {
        Precondition.checkMustNotBeNull(descriptions, "descriptions"); //$NON-NLS-1$
        if (descriptions.isEmpty()) {
            return Collections.emptySet();
        }
        Set<WorkDescriptionProcessor<?>> procs = new HashSet<>();
        for (WorkDescription desc : descriptions) {
            WorkDescriptionProcessor<?> proc = findDescriptionProcessor(desc);
            if (proc == null) {
                return Collections.emptySet();
            }
            procs.add(proc);
        }
        Set<WorkflowProcessor> results = new HashSet<>();
        for (Map.Entry<WorkflowProcessor, Set<WorkDescriptionProcessor<?>>> entry
                : processors.entrySet()) {
            if (entry.getValue().containsAll(procs)) {
                results.add(entry.getKey());
            }
        }
        return results;
    }

    @Override
    public WorkDescriptionProcessor<?> findDescriptionProcessor(
            WorkDescription workDescription) {
        Precondition.checkMustNotBeNull(workDescription, "workDescription"); //$NON-NLS-1$
        Class<? extends WorkDescription> aClass = workDescription.getClass();
        Class<?> current = aClass;
        while (current != null) {
            WorkDescriptionProcessor<?> proc = descriptionProcessors.get(current);
            if (proc != null) {
                if (current != aClass) {
                    descriptionProcessors.put(aClass, proc);
                }
                return proc;
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
