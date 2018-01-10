/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
 * Represents a resource.
 * @since 0.4.0
 */
public class ResourceInfo {

    private final String id;

    private final String path;

    private final boolean directory;

    /**
     * Creates a new instance.
     * @param id the container datasource ID
     * @param path the resource real path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ResourceInfo(String id, String path) {
        this(id, path, false);
    }

    /**
     * Creates a new instance.
     * @param id the container datasource ID
     * @param path the resource real path
     * @param directory target directory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ResourceInfo(String id, String path, boolean directory) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        this.id = id;
        this.path = path;
        this.directory = directory;
    }

    /**
     * Returns the container datasource ID.
     * @return the container datasource ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the real path.
     * @return the real path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the directory.
     * @return the directory.
     */
    public boolean isDirectory() {
        return directory;
    }
}
