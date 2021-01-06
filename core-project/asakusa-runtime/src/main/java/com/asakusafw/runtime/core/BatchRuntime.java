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
package com.asakusafw.runtime.core;

import java.text.MessageFormat;

/**
 * Provides runtime structural information.
 * @since 0.1.0
 * @version 0.4.1
 */
public final class BatchRuntime {

    /**
     * Major version.
     */
    public static final int VERSION_MAJOR = 4;

    /**
     * Minor version.
     */
    public static final int VERSION_MINOR = 1;

    /**
     * Checks runtime version and raises an exception if the version is inconsistent.
     * @param major the major version
     * @param minor the minor version
     * @throws IllegalStateException if versions are inconsistent
     */
    public static void require(int major, int minor) {
        if (major != VERSION_MAJOR || minor != VERSION_MINOR) {
            throw new IllegalStateException(MessageFormat.format(
                    "Inconsistent version: runtime-version={0}, buildtime-version={1}",
                    toString(VERSION_MAJOR, VERSION_MINOR),
                    toString(major, minor)));
        }
    }

    /**
     * Returns the version label.
     * @return the version label
     */
    public static String getLabel() {
        return toString(VERSION_MAJOR, VERSION_MINOR);
    }

    private static String toString(int major, int minor) {
        return MessageFormat.format("{0}.{1}", String.valueOf(major), String.valueOf(minor)); //$NON-NLS-1$
    }

    private BatchRuntime() {
        return;
    }
}
