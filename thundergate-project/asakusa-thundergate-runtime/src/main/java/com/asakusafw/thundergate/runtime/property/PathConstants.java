/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.thundergate.runtime.property;

/**
 * Holds script paths.
 * @since 0.4.0
 */
public final class PathConstants {

    /**
     * Relative path to the importer script (from framework installation home).
     */
    public static final String PATH_IMPORTER = "bulkloader/libexec/importer.sh";

    /**
     * Relative path to the exporter script (from framework installation home).
     */
    public static final String PATH_EXPORTER = "bulkloader/libexec/exporter.sh";

    /**
     * Relative path to the finalizer script (from framework installation home).
     */
    public static final String PATH_FINALIZER = "bulkloader/libexec/finalizer.sh";

    /**
     * Relative path to the finalizer script (from framework installation home).
     */
    public static final String PATH_CACHE_FINALIZER = "bulkloader/bin/release-cache-lock.sh";

    private PathConstants() {
        return;
    }
}
