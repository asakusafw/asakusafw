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
package com.asakusafw.directio.hive.ql;

import com.asakusafw.directio.hive.common.HiveTableInfo;

/**
 * Represents {@code CREATE TABLE} statement in Hive.
 */
public class SimpleCreateTable implements HiveCreateTable {

    private final HiveTableInfo tableInfo;

    private boolean external;

    private boolean skipPresentTable;

    private String databaseName;

    private String location;

    /**
     * Creates a new instance.
     * @param tableInfo the target table
     */
    public SimpleCreateTable(HiveTableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    @Override
    public HiveTableInfo getTableInfo() {
        return tableInfo;
    }

    @Override
    public boolean isExternal() {
        return external;
    }

    @Override
    public boolean isSkipPresentTable() {
        return skipPresentTable;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getLocation() {
        return location;
    }

    /**
     * Sets whether the target table is external or not.
     * @param value the value
     * @return this
     */
    public SimpleCreateTable withExternal(boolean value) {
        this.external = value;
        return this;
    }

    /**
     * Sets whether the target table should be created only if it does not exist.
     * @param value the value
     * @return this
     */
    public SimpleCreateTable withSkipPresentTable(boolean value) {
        this.skipPresentTable = value;
        return this;
    }

    /**
     * Sets the target database name.
     * @param value the value
     * @return this
     */
    public SimpleCreateTable withDatabaseName(String value) {
        this.databaseName = value;
        return this;
    }

    /**
     * Sets the target table location.
     * @param value the value
     * @return this
     */
    public SimpleCreateTable withLocation(String value) {
        this.location = value;
        return this;
    }
}
