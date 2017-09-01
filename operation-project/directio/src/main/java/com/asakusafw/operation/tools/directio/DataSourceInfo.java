/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import com.asakusafw.runtime.directio.DirectDataSource;

/**
 * Represents Direct I/O data source information.
 * @since 0.10.0
 */
public class DataSourceInfo {

    private final String id;

    private final DirectDataSource entity;

    private final BasePath path;

    /**
     * Creates a new instance.
     * @param id the data source ID
     * @param entity the data source entity
     * @param path the data source path
     */
    public DataSourceInfo(String id, DirectDataSource entity, BasePath path) {
        this.id = id;
        this.entity = entity;
        this.path = path;
    }

    /**
     * Returns the data source ID.
     * @return the data source ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the data source.
     * @return the data source
     */
    public DirectDataSource getEntity() {
        return entity;
    }

    /**
     * Returns the data source path.
     * @return the data source path
     */
    public BasePath getPath() {
        return path;
    }
}
