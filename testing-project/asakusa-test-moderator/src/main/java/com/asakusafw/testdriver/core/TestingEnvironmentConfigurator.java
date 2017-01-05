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
package com.asakusafw.testdriver.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.util.hadoop.InstallationUtil;

/**
 * Configures the current testing environment.
 * <p>
 * To configure testing environment, clients can implement this
 * and put the class name in
 * {@code META-INF/services/com.asakusafw.testdriver.core.TestingEnvironmentConfigurator}.
 * </p>
 * @since 0.6.0
 */
public abstract class TestingEnvironmentConfigurator {

    static {
        InstallationUtil.verifyFrameworkVersion();
    }

    static final Logger LOG = LoggerFactory.getLogger(TestingEnvironmentConfigurator.class);

    /**
     * The system property key of enabling this feature.
     */
    public static final String KEY_ENABLE = "asakusa.testdriver.configurator"; //$NON-NLS-1$

    /**
     * The default value of {@link #KEY_ENABLE}.
     */
    public static final String DEFAULT_ENABLE = "true"; //$NON-NLS-1$

    private static boolean initialized = false;

    /**
     * Initializes the testing environment.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (isEnabled() == false) {
            LOG.info(Messages.getString("TestingEnvironmentConfigurator.infoDisabled")); //$NON-NLS-1$
            return;
        }
        LOG.debug("Loading testing environment configurators"); //$NON-NLS-1$
        Collection<TestingEnvironmentConfigurator> services = loadServices();
        for (TestingEnvironmentConfigurator service : services) {
            LOG.debug("Applying testing environment configurator: {}", service); //$NON-NLS-1$
            try {
                service.configure();
            } catch (RuntimeException e) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("TestingEnvironmentConfigurator.warnFailedToApply"), //$NON-NLS-1$
                        service.getClass().getName()), e);
            }
        }
    }

    private static Collection<TestingEnvironmentConfigurator> loadServices() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = TestingEnvironmentConfigurator.class.getClassLoader();
        }
        List<TestingEnvironmentConfigurator> results = new ArrayList<>();
        ServiceLoader<TestingEnvironmentConfigurator> loader =
                ServiceLoader.load(TestingEnvironmentConfigurator.class, classLoader);
        for (TestingEnvironmentConfigurator service : loader) {
            results.add(service);
        }
        // sort by its class name
        Collections.sort(results, (o1, o2) -> {
            String c1 = o1.getClass().getName();
            String c2 = o2.getClass().getName();
            return c1.compareTo(c2);
        });
        return results;
    }

    private static boolean isEnabled() {
        String value = System.getProperty(KEY_ENABLE, DEFAULT_ENABLE);
        return value.equals("true"); //$NON-NLS-1$
    }

    /**
     * Configures the current testing environment.
     */
    protected abstract void configure();
}
