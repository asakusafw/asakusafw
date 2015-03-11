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
package com.asakusafw.dmdl.java.emitter;

import java.text.MessageFormat;

/**
 * Constants about Java names.
 * @since 0.2.0
 * @version 0.7.1
 */
public final class NameConstants {

    /**
     * The default namespace.
     * <p>
     * This used if <code>&#64;namespace</code> was not specified for any models.
     * </p>
     */
    public static final String DEFAULT_NAMESPACE = "dmdl"; //$NON-NLS-1$

    /**
     * The package name fragment for data models.
     */
    public static final String CATEGORY_DATA_MODEL = "model"; //$NON-NLS-1$

    /**
     * The package name fragment for data model input/output.
     */
    public static final String CATEGORY_IO = "io"; //$NON-NLS-1$

    /**
     * The simple class name pattern for data models.
     * <p>
     * This pattern is written in {@link MessageFormat#format(String, Object...)}
     * </p>
     */
    public static final String PATTERN_DATA_MODEL = "{0}"; //$NON-NLS-1$

    /**
     * The property getter name suffix.
     * @since 0.7.1
     */
    public static final String PROPERTY_GETTER_SUFFIX = "option"; //$NON-NLS-1$

    private NameConstants() {
        return;
    }
}
