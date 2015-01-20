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
package com.asakusafw.runtime.directio;

/**
 * Constants for {@link DirectDataSource} facility.
 * @since 0.2.5
 * @version 0.6.1
 */
public final class DirectDataSourceConstants {

    /**
     * The attribute key name of base path.
     */
    public static final String KEY_BASE_PATH = "basePath";

    /**
     * The attribute key name of resource path/pattern.
     */
    public static final String KEY_RESOURCE_PATH = "resourcePath";

    /**
     * The attribute key name of data class.
     */
    public static final String KEY_DATA_CLASS = "dataClass";

    /**
     * The attribute key name of format class.
     */
    public static final String KEY_FORMAT_CLASS = "formatClass";

    /**
     * The attribute key prefix of delete patterns.
     * @since 0.4.0
     */
    public static final String PREFIX_DELETE_PATTERN = "deletePattern.";

    /**
     * The attribute key name of whether the target input is optional.
     * The value must be {@code "true"}, {@code "false"},
     * or {@code null} ({@link #DEFAULT_OPTIONAL default value}).
     * @since 0.6.1
     */
    public static final String KEY_OPTIONAL = "optional";

    /**
     * The default value of {@link #KEY_OPTIONAL}.
     * @since 0.6.1
     */
    public static final String DEFAULT_OPTIONAL = "false";

    private DirectDataSourceConstants() {
        return;
    }
}
