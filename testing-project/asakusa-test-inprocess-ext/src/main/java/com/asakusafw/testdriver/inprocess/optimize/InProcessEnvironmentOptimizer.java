/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.testdriver.inprocess.optimize;

import java.text.MessageFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.stage.inprocess.InProcessStageConfigurator;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;
import com.asakusafw.testdriver.inprocess.InProcessJobExecutor;

/**
 * Configures in-process testing environment for optimizing tests for small jobs.
 * @since 0.7.1
 */
public class InProcessEnvironmentOptimizer extends TestingEnvironmentConfigurator {

    static final Logger LOG = LoggerFactory.getLogger(InProcessEnvironmentOptimizer.class);

    /**
     * The system property key of enabling this feature.
     */
    public static final String KEY_FEATURE_ENABLE = KEY_ENABLE + ".inprocess.optimize"; //$NON-NLS-1$

    /**
     * The default value of {@link #KEY_FEATURE_ENABLE}.
     */
    public static final String DEFAULT_FEATURE_ENABLE = "true"; //$NON-NLS-1$

    @Override
    protected void configure() {
        if (isEnabled() == false) {
            LOG.debug("disabled in-process testing environment optimizer for small jobs."); //$NON-NLS-1$
            return;
        }
        InProcessJobExecutor.Settings settings = InProcessJobExecutor.getGlobalSettings();
        synchronized (settings) {
            Map<String, String> properties = settings.getProperties();
            if (checkProperty(properties, InProcessStageConfigurator.KEY_FORCE)) {
                LOG.info("installing test driver features for optimizing in-process testing environment.");
                properties.put(InProcessStageConfigurator.KEY_FORCE, String.valueOf(true));
            }
        }
    }

    private boolean isEnabled() {
        String value = System.getProperty(KEY_FEATURE_ENABLE, DEFAULT_FEATURE_ENABLE);
        return value.equals("true"); //$NON-NLS-1$
    }

    private boolean checkProperty(Map<String, String> properties, String key) {
        if (properties.containsKey(key)) {
            LOG.warn(MessageFormat.format(
                    "The system property {0} is already set: {1}",
                    key,
                    properties.get(key)));
            return false;
        }
        return true;
    }
}
