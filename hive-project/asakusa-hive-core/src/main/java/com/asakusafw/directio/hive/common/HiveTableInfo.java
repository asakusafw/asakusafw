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
package com.asakusafw.directio.hive.common;

import java.util.List;
import java.util.Map;

/**
 * Represents a Hive table meta data.
 * @since 0.7.0
 */
public interface HiveTableInfo {

    /**
     * Returns the target data model class.
     * @return the target data model class, or {@code null} if it was not set
     */
    Class<?> getDataModelClass();

    /**
     * Returns the recommended table name.
     * @return the recommended table name
     */
    String getTableName();

    /**
     * Returns the field information in the target table.
     * @return the field information
     */
    List<? extends HiveFieldInfo> getFields();

    /**
     * Returns the table comment.
     * @return the table comment, or {@code null} if it is not set
     */
    String getTableComment();

    /**
     * Returns the row format information.
     * @return the row format information, or {@code null} if it is not set
     */
    RowFormatInfo getRowFormat();

    /**
     * Returns the table format name.
     * @return the table format name
     */
    String getFormatName();

    /**
     * Returns the table properties.
     * @return the table properties, or an empty map if this table has no extra properties
     */
    Map<String, String> getTableProperties();
}
