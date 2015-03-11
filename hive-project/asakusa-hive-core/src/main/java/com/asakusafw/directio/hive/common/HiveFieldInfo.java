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
package com.asakusafw.directio.hive.common;

import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

/**
 * Represents a table field.
 * @since 0.7.0
 */
public interface HiveFieldInfo {

    /**
     * Returns the field name.
     * @return the field name
     */
    String getFieldName();

    /**
     * Returns the field type.
     * @return the field type
     */
    TypeInfo getFieldTypeInfo();

    /**
     * Returns the field comment.
     * @return the field comment, or {@code null} if it is not set
     */
    String getFieldComment();
}
