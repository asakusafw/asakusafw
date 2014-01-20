/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.yaess.paralleljob;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.basic.JobExecutor;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionScriptHandler;
import com.asakusafw.yaess.core.Job;
import com.asakusafw.yaess.core.VariableResolver;
import com.asakusafw.yaess.core.YaessLogger;
import com.asakusafw.yaess.core.util.PropertiesUtil;

/**
 * An implementation of {@link ParallelJobExecutor} which use multiple threads for each resource.
 * @since 0.2.3
 */
public class ParallelJobExecutor implements JobExecutor {

    static final YaessLogger YSLOG = new YaessParallelJobLogger(ParallelJobExecutor.class);

    static final Logger LOG = LoggerFactory.getLogger(ParallelJobExecutor.class);

    /**
     * The key prefix of multiplexity configuration each resources.
     */
    public static final String KEY_PARALLEL_PREFIX = "parallel.";

    /**
     * The default resource name.
     */
    public static final String DEFAULT_RESOURCE_ID = ExecutionScriptHandler.DEFAULT_RESOURCE_ID;

    private final ExecutorService defaultExecutor;

    private final Map<String, ExecutorService> resourceExecutors;

    /**
     * Creates a new instance.
     * @param defaultResuorce default resource multiplexity
     * @param threadConfig each resource multiplexity
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ParallelJobExecutor(int defaultResuorce, Map<String, Integer> threadConfig) {
        if (defaultResuorce <= 0) {
            throw new IllegalArgumentException("defaultResurce must be > 0"); //$NON-NLS-1$
        }
        if (threadConfig == null) {
            throw new IllegalArgumentException("threadConfig must not be null"); //$NON-NLS-1$
        }
        this.defaultExecutor = Executors.newFixedThreadPool(defaultResuorce, new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(MessageFormat.format(
                        "ParallelJobScheduler-{0}",
                        String.valueOf(count.incrementAndGet())));
                return thread;
            }
        });
        HashMap<String, ExecutorService> map = new HashMap<String, ExecutorService>();
        for (Map.Entry<String, Integer> entry : threadConfig.entrySet()) {
            final String name = entry.getKey();
            Integer value = entry.getValue();
            if (value == null || value < 0) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "thread config must be > 0: key={0}, value={1}",
                        name,
                        value));
            }
            map.put(name, Executors.newFixedThreadPool(value, new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger();
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName(MessageFormat.format(
                            "ParallelJobScheduler-{0}-{1}",
                            name,
                            String.valueOf(count.incrementAndGet())));
                    return thread;
                }
            }));
        }
        map.put(DEFAULT_RESOURCE_ID, defaultExecutor);
        resourceExecutors = Collections.unmodifiableMap(map);
    }

    /**
     * Extracts multiplexity profiles from configuration and returns a related executor.
     * This operation extracts following entries from {@code configuration}:
     * <ul>
     * <li> {@link #KEY_PARALLEL_PREFIX parallel.<resource-name>} -  </li>
     * </ul>
     * Profiles must be contain {@link #DEFAULT_RESOURCE_ID parallel.default}.
     * @param servicePrefix prefix of configuration keys
     * @param configuration target configuration
     * @param variables variable resolver
     * @return the created executor
     * @throws IllegalArgumentException if configuration is invalid
     */
    public static ParallelJobExecutor extract(
            String servicePrefix,
            Map<String, String> configuration,
            VariableResolver variables) {
        if (servicePrefix == null) {
            throw new IllegalArgumentException("servicePrefix must not be null"); //$NON-NLS-1$
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (variables == null) {
            throw new IllegalArgumentException("variables must not be null"); //$NON-NLS-1$
        }
        NavigableMap<String, String> segment = PropertiesUtil.createPrefixMap(configuration, KEY_PARALLEL_PREFIX);
        Map<String, Integer> conf = new HashMap<String, Integer>();
        for (Map.Entry<String, String> entry : segment.entrySet()) {
            String name = entry.getKey();
            String valueString = entry.getValue();
            try {
                valueString = variables.replace(valueString, true);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Failed to resolve the profile \"{0}\": {1}",
                        servicePrefix + '.' + name,
                        valueString), e);
            }
            Integer value;
            try {
                value = Integer.valueOf(valueString);
            } catch (NumberFormatException e) {
                value = null;
            }
            if (value == null || value <= 0) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Parallel configuration \"{0}\" must be > 0: {1}",
                        servicePrefix + '.' + KEY_PARALLEL_PREFIX + name,
                        valueString));
            }
            conf.put(name, value);
        }
        LOG.debug("ParallelJobExecutor: {}", conf);
        Integer defaultValue = conf.remove(DEFAULT_RESOURCE_ID);
        if (defaultValue == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Default parallel configuration \"{0}\" is not defined",
                    servicePrefix + '.' + KEY_PARALLEL_PREFIX + DEFAULT_RESOURCE_ID));
        }
        return new ParallelJobExecutor(defaultValue, conf);
    }

    @Override
    public Executing submit(
            ExecutionMonitor monitor,
            ExecutionContext context,
            Job job,
            BlockingQueue<Executing> doneQueue) throws InterruptedException, IOException {
        if (monitor == null) {
            throw new IllegalArgumentException("monitor must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        String resourceId = job.getResourceId(context);
        ExecutorService executor = resourceExecutors.get(resourceId);
        if (executor == null) {
            YSLOG.warn("W01001",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getExecutionId(),
                    context.getPhase(),
                    job.getJobLabel(),
                    job.getServiceLabel(),
                    resourceId);
            LOG.debug("Resource {} is not defined: {}", resourceId, job.getId());
            executor = defaultExecutor;
        } else {
            YSLOG.info("I01001",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getExecutionId(),
                    context.getPhase(),
                    job.getJobLabel(),
                    job.getServiceLabel(),
                    resourceId);
        }
        Executing executing = new Executing(monitor, context, job, doneQueue);
        executor.execute(executing);
        return executing;
    }
}
