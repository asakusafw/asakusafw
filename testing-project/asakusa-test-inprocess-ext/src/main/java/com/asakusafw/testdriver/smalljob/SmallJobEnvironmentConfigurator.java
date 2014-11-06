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
package com.asakusafw.testdriver.smalljob;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.smalljob.SmallJobRunner;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;
import com.asakusafw.testdriver.inprocess.EmulatorUtils;

/**
 * Configures testing environment for optimizing tests for small jobs.
 * @since 0.7.1
 */
public class SmallJobEnvironmentConfigurator extends TestingEnvironmentConfigurator {

    static final Logger LOG = LoggerFactory.getLogger(SmallJobEnvironmentConfigurator.class);

    /**
     * The system property key of enabling this feature.
     */
    public static final String KEY_FEATURE_ENABLE = KEY_ENABLE + ".smalljob";

    /**
     * The default value of {@link #KEY_FEATURE_ENABLE}.
     */
    public static final String DEFAULT_FEATURE_ENABLE = "true";

    @Override
    protected void configure() {
        if (isEnabled() == false) {
            LOG.debug("small jobs testing environment configurator is disabled.");
            return;
        }
        if (checkProperty(EmulatorUtils.KEY_JOB_RUNNER_CLASS) == false) {
            return;
        }
        LOG.info("Installing test driver features for small jobs testing environment.");
        System.setProperty(EmulatorUtils.KEY_JOB_RUNNER_CLASS, SmallJobRunner.class.getName());
    }

    private boolean isEnabled() {
        String value = System.getProperty(KEY_FEATURE_ENABLE, DEFAULT_FEATURE_ENABLE);
        return value.equals("true");
    }

    private boolean checkProperty(String key) {
        String value = System.getProperty(key);
        if (value != null) {
            LOG.warn(MessageFormat.format(
                    "The system property {0} is already set: {1}",
                    key,
                    value));
            return false;
        }
        return true;
    }
}
