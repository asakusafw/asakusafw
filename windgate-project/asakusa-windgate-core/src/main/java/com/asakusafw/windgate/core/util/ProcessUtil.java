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
package com.asakusafw.windgate.core.util;

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.WindGateCoreLogger;
import com.asakusafw.windgate.core.WindGateLogger;

/**
 * Common utilities for processes.
 * @since 0.2.2
 */
public final class ProcessUtil {

    static final WindGateLogger WGLOG = new WindGateCoreLogger(ProcessUtil.class);

    static final Logger LOG = LoggerFactory.getLogger(ProcessUtil.class);

    /**
     * Creates a new data model object for the specified process.
     * @param <T> type of data model object
     * @param resourceName target resource name
     * @param script target script
     * @return the created instance
     * @throws IOException if failed to create a new instance
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <T> T newDataModel(
            String resourceName,
            ProcessScript<T> script) throws IOException {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName must not be null"); //$NON-NLS-1$
        }
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        Class<T> dataClass = script.getDataClass();
        LOG.debug("Creating data model object: {} (resource={}, process={})", new Object[] {
                dataClass.getName(),
                resourceName,
                script.getName(),
        });
        try {
            T object = dataClass.newInstance();
            return object;
        } catch (Exception e) {
            WGLOG.error(e, "E05004",
                    resourceName,
                    script.getName(),
                    dataClass.getName());
            throw new IOException(MessageFormat.format(
                    "Failed to create a new instance: {2} (resource={0}, process={1})",
                    resourceName,
                    script.getName(),
                    dataClass.getName()), e);
        }
    }

    private ProcessUtil() {
        return;
    }
}
