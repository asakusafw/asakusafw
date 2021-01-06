/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.util;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utilities for {@link ByteArrayComparator}.
 * @since 0.8.0
 */
public final class ByteArrayComparators {

    static final Log LOG = LogFactory.getLog(ByteArrayComparators.class);

    private static final ByteArrayComparator DEFAULT = new BasicByteArrayComparator();

    private static final String KEY_COMPARATOR = ByteArrayComparator.class.getName();

    private static final ByteArrayComparator INSTANCE;
    static {
        // search for the best ByteArrayComparator for the current platform
        ByteArrayComparator result = DEFAULT;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ByteArrayComparator.class.getClassLoader();
        }
        String implementation = System.getProperty(KEY_COMPARATOR);
        if (implementation != null) {
            try {
                Class<?> loaded = Class.forName(implementation, false, cl);
                result = (ByteArrayComparator) loaded.newInstance();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "using a custom byte array comparator: {0}",
                            result));
                }
            } catch (ClassCastException | ReflectiveOperationException e) {
                LOG.warn(MessageFormat.format(
                        "failed to load a byte array comparator implementation: {0}",
                        implementation), e);
            }
        } else {
            // for handling errors, use old-style for statement
            ServiceLoader<ByteArrayComparator> services = ServiceLoader.load(ByteArrayComparator.class, cl);
            for (Iterator<ByteArrayComparator> iter = services.iterator(); iter.hasNext();) {
                try {
                    result = iter.next();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(MessageFormat.format(
                                "using a custom byte array comparator: {0}",
                                result));
                    }
                    // always use the first available one
                    break;
                } catch (ServiceConfigurationError e) {
                    LOG.warn("error occurred while loading a byte comparator implementation", e);
                }
            }
        }
        INSTANCE = result;
    }

    /**
     * Returns the system {@link ByteArrayComparator} instance.
     * @return the instance
     */
    public static ByteArrayComparator getInstance() {
        return INSTANCE;
    }

    private ByteArrayComparators() {
        return;
    }
}
