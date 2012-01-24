/**
 * Copyright 2012 Asakusa Framework Team.
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
 */
public final class DirectDataSourceConstants {

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
     * The attribute key name of local tempdir.
     */
    public static final String KEY_LOCAL_TEMPDIR = "com.asakusafw.output.local.tempdir";

    private DirectDataSourceConstants() {
        return;
    }
}
