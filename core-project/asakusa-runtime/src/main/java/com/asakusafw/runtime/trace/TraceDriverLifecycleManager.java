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
package com.asakusafw.runtime.trace;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asakusafw.runtime.core.ResourceConfiguration;
import com.asakusafw.runtime.core.RuntimeResource;

/**
 * Manages {@link TraceDriver}s' life-cycle.
 * @since 0.5.1
 */
public class TraceDriverLifecycleManager implements RuntimeResource {

    /**
     * The configuration key name of the implementation class name of {@link TraceActionFactory}.
     */
    public static final String KEY_FACTORY_CLASS = TraceActionFactory.class.getName();

    /**
     * The default configuration value of the implementation class name of {@link TraceActionFactory}.
     */
    public static final String DEFAULT_FACTORY_CLASS = TraceReportActionFactory.class.getName();

    static final Log LOG = LogFactory.getLog(TraceDriverLifecycleManager.class);

    private static final ThreadLocal<TraceDriverLifecycleManager> INSTANCES =
            new ThreadLocal<TraceDriverLifecycleManager>();

    private ResourceConfiguration conf;

    private TraceActionFactory factory;

    private final Map<TraceContext, TraceAction> actions = new HashMap<TraceContext, TraceAction>();

    @Override
    public void setup(ResourceConfiguration configuration) throws IOException, InterruptedException {
        this.conf = configuration;
        this.factory = extractFactory(configuration);
        INSTANCES.set(this);
    }

    private static TraceActionFactory extractFactory(ResourceConfiguration configuration) throws IOException {
        String factoryClass = configuration.get(KEY_FACTORY_CLASS, null);
        if (factoryClass == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "The configuration key \"{0}\" is not set, we use \"{1}\"", //$NON-NLS-1$
                        KEY_FACTORY_CLASS,
                        DEFAULT_FACTORY_CLASS));
            }
            factoryClass = DEFAULT_FACTORY_CLASS;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Data-flow tracing support: {0}", //$NON-NLS-1$
                        factoryClass));
            }
        }
        try {
            return configuration.getClassLoader().loadClass(factoryClass)
                    .asSubclass(TraceActionFactory.class)
                    .newInstance();
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to initialize trace action factory: {0}",
                    factoryClass), e);
        }
    }

    @Override
    public void cleanup(ResourceConfiguration configuration) throws IOException, InterruptedException {
        INSTANCES.remove();
        conf = null;
        factory = null;
        synchronized (actions) {
            for (Map.Entry<TraceContext, TraceAction> entry : actions.entrySet()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Cleanup tracepoint: {0}", //$NON-NLS-1$
                            entry.getKey()));
                }
                try {
                    entry.getValue().close();
                } catch (Exception e) {
                    LOG.error(MessageFormat.format(
                            "Failed to cleanup tracepoint: {0}",
                            entry.getKey()), e);
                }
            }
        }
    }

    static TraceAction getAction(TraceContext context) throws IOException, InterruptedException {
        TraceDriverLifecycleManager instance = INSTANCES.get();
        if (instance == null) {
            return new TraceReportAction(context);
        } else {
            return instance.getAction0(context);
        }
    }

    private TraceAction getAction0(TraceContext context) throws IOException, InterruptedException {
        TraceAction action;
        synchronized (actions) {
            action = actions.get(context);
        }
        if (action == null) {
            action = factory.createTracepointTraceAction(conf, context);
            synchronized (action) {
                actions.put(context, action);
            }
        }
        return action;
    }

    static void error(Throwable info) throws IOException, InterruptedException {
        TraceDriverLifecycleManager instance = INSTANCES.get();
        TraceAction action = instance == null
                ? new TraceErrorReportAction() : instance.factory.createErrorTraceAction(instance.conf);
        try {
            action.trace(info);
        } finally {
            try {
                action.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
