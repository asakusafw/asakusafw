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
package com.asakusafw.workflow.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Deletes files.
 * @since 0.10.0
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.MINIMAL_CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class",
        visible = false
)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        scope = BatchInfo.class,
        property = "oid"
)
public interface DeleteTaskInfo extends TaskInfo {

    /**
     * The default {@link #getModuleName() module name}.
     */
    String DEFAULT_MODULE_NAME = "delete"; //$NON-NLS-1$

    /**
     * Returns the path kind.
     * @return the path kind
     */
    PathKind getPathKind();

    /**
     * Returns the path expression of the target files.
     * @return the delete path expression
     * @see #getPathKind()
     */
    String getPath();

    /**
     * Represents kinds of path of {@link DeleteTaskInfo}.
     * @since 0.10.0
     */
    enum PathKind {

        /**
         * Deletes files on the local file system.
         */
        LOCAL_FILE_SYSTEM,

        /**
         * Deletes files on Hadoop file system.
         */
        HADOOP_FILE_SYSTEM,
    }
}
