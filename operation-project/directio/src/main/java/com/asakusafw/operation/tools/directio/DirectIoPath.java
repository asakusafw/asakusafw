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
package com.asakusafw.operation.tools.directio;

import java.util.Optional;

import com.asakusafw.runtime.directio.FilePattern;

/**
 * Represents a path of Direct I/O.
 * @since 0.10.0
 */
public class DirectIoPath {

    private final DataSourceInfo source;

    private final BasePath basePath;

    private final FilePattern resourcePattern;

    /**
     * Creates a new instance.
     * @param source the container data source
     * @param basePath the base path
     * @param resourcePattern the resource pattern (nullable)
     */
    public DirectIoPath(
            DataSourceInfo source,
            BasePath basePath,
            FilePattern resourcePattern) {
        this.source = source;
        this.basePath = basePath;
        this.resourcePattern = resourcePattern;
    }

    /**
     * Returns whether this represents the root path.
     * @return {@code true} if this represents the root path
     */
    public boolean isRoot() {
        return basePath.isRoot() && resourcePattern == null;
    }

    /**
     * Returns the container data source information.
     * @return data source information
     */
    public DataSourceInfo getSource() {
        return source;
    }

    /**
     * Returns the base path including the container data source path.
     * @return the base path
     */
    public BasePath getBasePath() {
        return basePath;
    }

    /**
     * Returns the resource pattern.
     * @return the resource pattern
     */
    public Optional<FilePattern> getResourcePattern() {
        return Optional.ofNullable(resourcePattern);
    }

    /**
     * Returns the container path.
     * @return the container path
     */
    public BasePath getContainerPath() {
        return getSource().getPath();
    }

    /**
     * Returns the component base path.
     * @return the component base path
     */
    public BasePath getComponentPath() {
        return getContainerPath().relativise(getBasePath());
    }

    /**
     * Returns whether this represents the root path of the container.
     * @return {@code true} if this represents the root path
     */
    public boolean isComponentRoot() {
        return getComponentPath().isRoot() && resourcePattern == null;
    }

    /**
     * Returns the bare path.
     * @return the bare path
     */
    public String getBarePath() {
        return getResourcePattern()
                .map(it -> getSource().getEntity().path(getComponentPath().getPathString(), it))
                .orElseGet(() -> getSource().getEntity().path(getComponentPath().getPathString()));
    }

    @Override
    public String toString() {
        return getResourcePattern()
                .map(getBasePath()::resolve)
                .map(it -> it.toString())
                .orElseGet(() -> getBasePath().toString());
    }
}
