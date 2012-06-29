/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.multidispatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.ExecutionScriptHandler;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.YaessLogger;
import com.asakusafw.yaess.core.util.PropertiesUtil;

/**
 * A Dispatcher implementation for {@link ExecutionScriptHandler}.
 * @param <T> the target script kind
 * @since 0.2.6
 */
public abstract class ExecutionScriptHandlerDispatcher<T extends ExecutionScript>
        implements ExecutionScriptHandler<T> {

    static final YaessLogger YSLOG = new YaessMultiDispatchLogger(ExecutionScriptHandlerDispatcher.class);

    static final Logger LOG = LoggerFactory.getLogger(ExecutionScriptHandlerDispatcher.class);

    private static final String LABEL_UNDEFINED = "(undefined)";

    static final String PREFIX_CONF = "conf";

    static final String KEY_DIRECTORY = PREFIX_CONF + ".directory";

    static final String KEY_SETUP = PREFIX_CONF + ".setup";

    static final String KEY_CLEANUP = PREFIX_CONF + ".cleanup";

    static final String PREFIX_DEFAULT = "default";

    static final String SUFFIX_CONF = ".properties";

    private final Class<? extends ExecutionScriptHandler<T>> handlerKind;

    private volatile String prefix;

    private volatile File confDirectory;

    private volatile Reference<Map<String, Properties>> confCache = new SoftReference<Map<String, Properties>>(null);

    private volatile Map<String, ExecutionScriptHandler<T>> delegations;

    private volatile String forceSetUp;

    private volatile String forceCleanUp;

    /**
     * Creates a new instance.
     * @param handlerKind the handler kind
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected ExecutionScriptHandlerDispatcher(Class<? extends ExecutionScriptHandler<T>> handlerKind) {
        if (handlerKind == null) {
            throw new IllegalArgumentException("handlerKind must not be null"); //$NON-NLS-1$
        }
        this.handlerKind = handlerKind;
    }

    @Override
    public void configure(ServiceProfile<?> profile) throws IOException, InterruptedException {
        this.prefix = profile.getPrefix();
        try {
            this.confDirectory = getConfDirectory(profile);
            this.delegations = getDelegations(profile);
            this.forceSetUp = profile.getConfiguration(KEY_SETUP, false, true);
            this.forceCleanUp = profile.getConfiguration(KEY_CLEANUP, false, true);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure \"{0}\" ({1})",
                    profile.getPrefix(),
                    profile.getServiceClass().getName()), e);
        }

        if (forceSetUp != null && delegations.containsKey(forceSetUp) == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to detect setUp target: \"{2}\" in {0}.{1}",
                    profile.getPrefix(),
                    KEY_SETUP,
                    forceSetUp));
        }
        if (forceCleanUp != null && delegations.containsKey(forceCleanUp) == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to detect cleanUp target: \"{2}\" in {0}.{1}",
                    profile.getPrefix(),
                    KEY_CLEANUP,
                    forceCleanUp));
        }
    }

    private File getConfDirectory(ServiceProfile<?> profile) {
        assert profile != null;
        String value = profile.getConfiguration(KEY_DIRECTORY, true, true);
        File dir = new File(value);
        if (dir.exists() == false) {
            YSLOG.info("I00001",
                    profile.getPrefix(),
                    KEY_DIRECTORY,
                    value);
        }
        return dir;
    }

    private Map<String, ExecutionScriptHandler<T>> getDelegations(
            ServiceProfile<?> profile) throws IOException, InterruptedException {
        assert profile != null;
        Map<String, String> conf = profile.getConfiguration();
        Set<String> keys = PropertiesUtil.getChildKeys(conf, "", ".");
        keys.remove(PREFIX_CONF);
        if (keys.contains(PREFIX_DEFAULT) == false) {
            throw new IOException(MessageFormat.format(
                    "Default profile for multidispatch plugin is not defined: {0}.{1}",
                    profile.getPrefix(),
                    PREFIX_DEFAULT));
        }

        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : conf.entrySet()) {
            String key = profile.getPrefix() + "." + entry.getKey();
            String value = entry.getValue();
            properties.setProperty(key, value);
        }

        Map<String, ExecutionScriptHandler<T>> results = new HashMap<String, ExecutionScriptHandler<T>>();
        for (String key : keys) {
            String subPrefix = profile.getPrefix() + "." + key;
            ServiceProfile<? extends ExecutionScriptHandler<T>> subProfile;
            try {
                subProfile = ServiceProfile.load(
                        properties,
                        subPrefix,
                        handlerKind,
                        profile.getContext());
            } catch (IllegalArgumentException e) {
                throw new IOException(MessageFormat.format(
                        "Failed to load sub component for multidispatch plugin: {0}",
                        subPrefix), e);
            }
            ExecutionScriptHandler<T> subInstance = subProfile.newInstance();
            results.put(key, subInstance);
        }
        return results;
    }

    @Override
    public String getHandlerId() {
        return prefix;
    }

    private ExecutionScriptHandler<T> resolve(
            ExecutionContext context,
            ExecutionScript script) throws IOException {
        assert context != null;
        Properties batchConf = getBatchConf(context, script);
        String key = findKey(context, script, batchConf);
        if (key != null) {
            ExecutionScriptHandler<T> target = delegations.get(key);
            if (target != null) {
                return target;
            }
            throw new IOException(MessageFormat.format(
                    "Invalid dispatch target for multidispatch plugin: "
                    + "{4} (batchId={0}, flowId={1}, phase={2}, stageId={3})",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getPhase(),
                    script == null ? LABEL_UNDEFINED : script.getId(),
                    key));
        }
        ExecutionScriptHandler<T> defaultTarget = delegations.get(PREFIX_DEFAULT);
        assert defaultTarget != null;
        return defaultTarget;
    }

    private String findKey(ExecutionContext context, ExecutionScript script, Properties batchConf) {
        if (batchConf != null) {
            for (FindPattern pattern : FindPattern.values()) {
                String key = pattern.getKey(context, script);
                if (key == null) {
                    continue;
                }
                String value = batchConf.getProperty(key);
                if (value == null) {
                    continue;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Dispatch target found (batchId={}, flowId={}, phase={}, stageId={}, key={}, value={})",
                            new Object[] {
                                context.getBatchId(),
                                context.getFlowId(),
                                context.getPhase(),
                                script == null ? LABEL_UNDEFINED : script.getId(),
                                key,
                                value,
                            }
                    );
                }
                return value;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatch target does not found (batchId={}, flowId={}, phase={}, stageId={})", new Object[] {
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getPhase().getSymbol(),
                    script == null ? LABEL_UNDEFINED : script.getId(),
            });
        }
        return null;
    }

    private Properties getBatchConf(ExecutionContext context, ExecutionScript script) throws IOException {
        assert context != null;
        Map<String, Properties> cached = confCache.get();
        if (cached != null) {
            String batchId = context.getBatchId();
            synchronized (this) {
                if (cached.containsKey(batchId)) {
                    return cached.get(batchId);
                }
            }
        }
        Properties batchConf = loadBatchConf(context, script);
        synchronized (this) {
            cached = confCache.get();
            if (cached == null) {
                cached = new HashMap<String, Properties>();
                confCache = new SoftReference<Map<String, Properties>>(cached);
            }
            cached.put(context.getBatchId(), batchConf);
        }
        return batchConf;
    }

    private Properties loadBatchConf(ExecutionContext context, ExecutionScript script) throws IOException {
        assert context != null;
        String fileName = context.getBatchId() + SUFFIX_CONF;
        File file = new File(confDirectory, fileName);
        LOG.debug("Finding multidispatch configuration file: batchId={}, file={}", context.getBatchId(), file);
        if (file.isFile() == false) {
            LOG.debug("Missing multidispatch configuration file: batchId={}, file={}", context.getBatchId(), file);
            return null;
        }
        LOG.debug("Loading multidispatch configuration file: batchId={}, file={}", context.getBatchId(), file);
        try {
            InputStream in = new FileInputStream(file);
            try {
                Properties properties = new Properties();
                properties.load(in);
                return properties;
            } finally {
                in.close();
            }
        } catch (IOException e) {
            YSLOG.error(e, "E01001",
                    context.getBatchId(),
                    file.getAbsolutePath());
            throw e;
        }
    }

    @Override
    public String getResourceId(
            ExecutionContext context,
            ExecutionScript script) throws InterruptedException, IOException {
        ExecutionScriptHandler<T> target = resolve(context, script);
        return target.getResourceId(context, script);
    }

    @Override
    public Map<String, String> getProperties(
            ExecutionContext context,
            ExecutionScript script) throws InterruptedException, IOException {
        ExecutionScriptHandler<T> target = resolve(context, script);
        return target.getProperties(context, script);
    }

    @Override
    public Map<String, String> getEnvironmentVariables(
            ExecutionContext context,
            ExecutionScript script) throws InterruptedException, IOException {
        ExecutionScriptHandler<T> target = resolve(context, script);
        return target.getEnvironmentVariables(context, script);
    }

    @Override
    public void setUp(ExecutionMonitor monitor, ExecutionContext context) throws InterruptedException, IOException {
        ExecutionScriptHandler<T> target;
        if (forceSetUp != null) {
            target = delegations.get(forceSetUp);
        } else {
            target = resolve(context, null);
        }
        assert target != null;
        YSLOG.info("I01001",
                target.getHandlerId(),
                context.getBatchId(),
                context.getFlowId(),
                context.getPhase(),
                context.getExecutionId());
        target.setUp(monitor, context);
    }

    @Override
    public void execute(
            ExecutionMonitor monitor,
            ExecutionContext context,
            T script) throws InterruptedException, IOException {
        ExecutionScriptHandler<T> target = resolve(context, script);
        assert target != null;
        YSLOG.info("I01002",
                target.getHandlerId(),
                context.getBatchId(),
                context.getFlowId(),
                context.getPhase(),
                context.getExecutionId(),
                script.getId());
        target.execute(monitor, context, script);
    }

    @Override
    public void cleanUp(ExecutionMonitor monitor, ExecutionContext context) throws InterruptedException, IOException {
        ExecutionScriptHandler<T> target;
        if (forceCleanUp != null) {
            target = delegations.get(forceCleanUp);
        } else {
            target = resolve(context, null);
        }
        assert target != null;
        YSLOG.info("I01003",
                target.getHandlerId(),
                context.getBatchId(),
                context.getFlowId(),
                context.getPhase(),
                context.getExecutionId());
        target.cleanUp(monitor, context);
    }

    private enum FindPattern {

        STAGE {
            @Override
            String getKey(ExecutionContext context, ExecutionScript script) {
                if (script == null) {
                    return null;
                }
                return MessageFormat.format(
                        "{0}.{1}.{2}",
                        context.getFlowId(),
                        context.getPhase().getSymbol(),
                        script.getId());
            }
        },

        PHASE {
            @Override
            String getKey(ExecutionContext context, ExecutionScript script) {
                return MessageFormat.format(
                        "{0}.{1}.{2}",
                        context.getFlowId(),
                        context.getPhase().getSymbol(),
                        WILDCARD);
            }
        },

        FLOW {
            @Override
            String getKey(ExecutionContext context, ExecutionScript script) {
                return MessageFormat.format(
                        "{0}.{1}",
                        context.getFlowId(),
                        WILDCARD);
            }
        },

        BATCH {
            @Override
            String getKey(ExecutionContext context, ExecutionScript script) {
                return WILDCARD;
            }
        },

        ;
        private static final String WILDCARD = "*";

        abstract String getKey(ExecutionContext context, ExecutionScript script);
    }
}
