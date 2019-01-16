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
package com.asakusafw.testdriver.configurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import com.asakusafw.runtime.workaround.snappyjava.MacSnappyJavaWorkaround;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;

/**
 * Configures testing environment for snappy.
 * @since 0.7.0
 */
public class SnappyConfigurator extends TestingEnvironmentConfigurator {

    static final Logger LOG = LoggerFactory.getLogger(SnappyConfigurator.class);

    /**
     * The system property key of enabling this feature.
     */
    public static final String KEY_FEATURE_ENABLE = KEY_ENABLE + ".snappy"; //$NON-NLS-1$

    /**
     * The default value of {@link #KEY_FEATURE_ENABLE}.
     */
    public static final String DEFAULT_FEATURE_ENABLE = "true"; //$NON-NLS-1$

    @Override
    protected void configure() {
        if (isEnabled() == false) {
            LOG.debug("snappy configurator is disabled."); //$NON-NLS-1$
            return;
        }
        // Preloads native snappy library.
        MacSnappyJavaWorkaround.install();
        Snappy.getNativeLibraryVersion();
    }

    private boolean isEnabled() {
        String value = System.getProperty(KEY_FEATURE_ENABLE, DEFAULT_FEATURE_ENABLE);
        return value.equals("true"); //$NON-NLS-1$
    }

}
