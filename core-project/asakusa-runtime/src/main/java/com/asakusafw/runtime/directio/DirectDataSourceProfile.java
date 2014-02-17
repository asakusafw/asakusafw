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
package com.asakusafw.runtime.directio;

import java.util.Map;

/**
 * Profile for {@link DirectDataSource}.
 * @since 0.2.5
 */
public class DirectDataSourceProfile {

    private final String id;

    private final Class<? extends AbstractDirectDataSource> targetClass;

    private final String path;

    private final Map<String, String> attributes;

    /**
     * Creates a new instance.
     * @param id target ID
     * @param targetClass target class
     * @param path the path of this data source
     * @param attributes data source configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DirectDataSourceProfile(
            String id,
            Class<? extends AbstractDirectDataSource> targetClass,
            String path,
            Map<String, String> attributes) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        if (targetClass == null) {
            throw new IllegalArgumentException("targetClass must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        this.id = id;
        this.targetClass = targetClass;
        this.path = path;
        this.attributes = attributes;
    }

    /**
     * Returns the target ID.
     * @return the target ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the target data source class.
     * @return the target class
     */
    public Class<? extends AbstractDirectDataSource> getTargetClass() {
        return targetClass;
    }

    /**
     * Returns the path of the target data source is mapped on.
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the attributes of the target data source.
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
