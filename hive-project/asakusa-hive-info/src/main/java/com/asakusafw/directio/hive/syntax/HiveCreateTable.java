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
package com.asakusafw.directio.hive.syntax;

import com.asakusafw.directio.hive.info.TableInfo;

/**
 * Represents {@code CREATE TABLE} statement in Hive.
 * @since 0.8.1
 */
public interface HiveCreateTable {

    /**
     * Returns the target table information.
     * @return the target table information
     */
    TableInfo getTableInfo();

    /**
     * Returns whether the target table is external or not.
     * @return {@code true} if the target table is external, or {@code false} if not
     */
    boolean isExternal();

    /**
     * Returns whether the target table should be created only if it does not exist.
     * @return {@code true} if skip creating present table, or {@code false} if not
     */
    boolean isSkipPresentTable();

    /**
     * Returns the target database name.
     * @return the target database name, or {@code null} to create table on the context database
     */
    String getDatabaseName();

    /**
     * Returns the target table location.
     * @return the target table location, or {@code null} to create table onto the default location
     */
    String getLocation();
}
