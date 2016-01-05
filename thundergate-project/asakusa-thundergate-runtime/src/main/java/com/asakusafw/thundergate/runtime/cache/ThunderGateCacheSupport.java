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
package com.asakusafw.thundergate.runtime.cache;

/**
 * An interface for data models which supports ThunderGate cache features.
 * @since 0.2.3
 */
public interface ThunderGateCacheSupport {

    /**
     * Returns the version of this data model <em>class</em>.
     * @return the version of this data model class
     */
    long __tgc__DataModelVersion();

    /**
     * Returns the last updated timestamp colum name for this data model <em>class</em>.
     * @return the last updated timestamp column name.
     */
    String __tgc__TimestampColumn();

    /**
     * Returns the system ID of this data model object.
     * @return the system ID of this data model object
     * @throws RuntimeException if this data model does not have the system ID
     */
    long __tgc__SystemId();

    /**
     * Returns whether this data model represents a deleted entry.
     * @return {@code true} if this represents a deleted entry, otherwise {@code false}
     */
    boolean __tgc__Deleted();
}
