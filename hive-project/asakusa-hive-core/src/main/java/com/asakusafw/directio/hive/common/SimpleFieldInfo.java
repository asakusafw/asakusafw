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
package com.asakusafw.directio.hive.common;

import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

/**
 * A simple implementation of {@link HiveFieldInfo}.
 */
public class SimpleFieldInfo implements HiveFieldInfo {

    private final String fieldName;

    private final TypeInfo fieldTypeInfo;

    private String fieldComment;

    /**
     * Creates a new instance.
     * @param fieldName the target field name
     * @param fieldTypeInfo the target field type
     */
    public SimpleFieldInfo(String fieldName, TypeInfo fieldTypeInfo) {
        this.fieldName = fieldName;
        this.fieldTypeInfo = fieldTypeInfo;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public TypeInfo getFieldTypeInfo() {
        return fieldTypeInfo;
    }

    @Override
    public String getFieldComment() {
        return fieldComment;
    }

    /**
     * Sets the target field comment.
     * @param value the comment text
     * @return this
     */
    public SimpleFieldInfo withFieldComment(String value) {
        this.fieldComment = value;
        return this;
    }
}
