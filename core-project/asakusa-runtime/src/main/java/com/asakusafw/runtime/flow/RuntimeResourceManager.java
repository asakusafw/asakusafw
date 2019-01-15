/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;

import com.asakusafw.runtime.core.HadoopConfiguration;
import com.asakusafw.runtime.core.legacy.RuntimeResource;

/**
 * Manages lifecycle of {@link RuntimeResource} objects.
 * @since 0.1.0
 * @version 0.7.3
 */
public class RuntimeResourceManager extends Configured {

    static final Log LOG = LogFactory.getLog(RuntimeResourceManager.class);

    /**
     * The standard name of the framework configuration file.
     */
    public static final String CONFIGURATION_FILE_NAME = "asakusa-resources.xml"; //$NON-NLS-1$

    /**
     * The path to configuration file (relative from $ASAKUSA_HOME).
     * @since 0.2.5
     */
    public static final String CONFIGURATION_FILE_PATH = "core/conf/" + CONFIGURATION_FILE_NAME; //$NON-NLS-1$

    private final HadoopConfiguration configuration;

    private List<RuntimeResource> resources;

    /**
     * Creates a new instance.
     * @param configuration the current configuration
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public RuntimeResourceManager(Configuration configuration) {
        super(configuration);
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.configuration = new HadoopConfiguration(configuration);
        this.resources = Collections.emptyList();
    }

    /**
     * Initializes the managed resources.
     * @throws IOException if failed to initialize resources
     * @throws InterruptedException if interrupted while initializing resources
     * @throws IllegalArgumentException if the configuration is something wrong
     * @throws IllegalStateException if the current lifecycle is something wrong
     */
    public void setup() throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading runtime plugins"); //$NON-NLS-1$
        }
        List<? extends RuntimeResource> loaded = load();
        this.resources = new ArrayList<>();
        for (RuntimeResource resource : loaded) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Activating runtime plugin: {0}", //$NON-NLS-1$
                        resource.getClass().getName()));
            }
            resource.setup(configuration);
            resources.add(resource);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Loaded {0} runtime plugins", //$NON-NLS-1$
                    resources.size()));
        }
    }

    /**
     * Finalizes and releases the managed resources.
     * @throws IOException if failed to finalize resources
     * @throws InterruptedException if interrupted while initializing resources
     * @throws IllegalArgumentException if the configuration is something wrong
     * @throws IllegalStateException if the current lifecycle is something wrong
     */
    public void cleanup() throws IOException, InterruptedException {
        int count = resources.size();
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Unloading {0} runtime plugins", //$NON-NLS-1$
                    count));
        }
        try {
            for (RuntimeResource resource : resources) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Deactivating runtime plugin: {0}", //$NON-NLS-1$
                            resource.getClass().getName()));
                }
                resource.cleanup(configuration);
            }
        } finally {
            this.resources = Collections.emptyList();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Unloaded {0} runtime plugins", //$NON-NLS-1$
                    count));
        }
    }

    /**
     * Loads the available resources.
     * In this implementation, the method collects service information from
     * {@code META-INF/services/com.asakusafw.runtime.core.RuntimeResource}
     * and creates {@link RuntimeResource} implementations on them via SPI.
     * @return the loaded resources
     * @throws IOException if failed to load the resources
     */
    protected List<RuntimeResource> load() throws IOException {
        List<RuntimeResource> results = new ArrayList<>();
        ClassLoader loader = configuration.getClassLoader();
        try {
            for (RuntimeResource resource : ServiceLoader.load(RuntimeResource.class, loader)) {
                if (resource instanceof Configurable) {
                    ((Configurable) resource).setConf(configuration.getConf());
                }
                results.add(resource);
            }
        } catch (RuntimeException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to load resources ({0})",
                    RuntimeResource.class.getName()),
                    e);
        }
        return results;
    }
}
