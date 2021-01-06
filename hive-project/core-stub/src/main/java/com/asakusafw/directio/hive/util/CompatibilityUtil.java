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
package com.asakusafw.directio.hive.util;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.OptionalInt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hive.common.HiveVersionAnnotation;

/**
 * utilities about Hive v1/v2 compatibilities.
 * @since 0.10.3
 */
public final class CompatibilityUtil {

    static final Log LOG = LogFactory.getLog(CompatibilityUtil.class);

    private static HiveVersionAnnotation version;
    static {
        version = Optional.ofNullable(HiveVersionAnnotation.class.getPackage())
                .flatMap(it -> Optional.ofNullable(it.getAnnotation(HiveVersionAnnotation.class)))
                .orElse(null);
    }

    private CompatibilityUtil() {
        return;
    }

    /**
     * returns the major version number of Hive.
     * @return the major version, or {@code empty} if it is not sure
     */
    public static OptionalInt getHiveMajorVersion() {
        if (version == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("missing Hive version info");
            }
            return OptionalInt.empty();
        }
        // x.y.z
        String shortVersion = version.shortVersion();
        if (LOG.isTraceEnabled()) {
            LOG.trace(MessageFormat.format("Hive version: {0}", shortVersion));
        }
        int firstDot = shortVersion.indexOf('.');
        if (firstDot < 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("cannot extract Hive major version: {0}", shortVersion));
            }
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(shortVersion.substring(0, firstDot).trim()));
        } catch (NumberFormatException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("cannot extract Hive major version: {0}", shortVersion), e);
            }
            return OptionalInt.empty();
        }
    }
}
