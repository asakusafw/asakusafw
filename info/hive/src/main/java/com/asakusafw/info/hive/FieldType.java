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
package com.asakusafw.info.hive;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * An abstract super interface of Hive field types.
 * @since 0.8.1
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "name",
        visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PlainType.class, name = "TINYINT"),
    @JsonSubTypes.Type(value = PlainType.class, name = "SMALLINT"),
    @JsonSubTypes.Type(value = PlainType.class, name = "INT"),
    @JsonSubTypes.Type(value = PlainType.class, name = "BIGINT"),
    @JsonSubTypes.Type(value = PlainType.class, name = "FLOAT"),
    @JsonSubTypes.Type(value = PlainType.class, name = "DOUBLE"),
    @JsonSubTypes.Type(value = PlainType.class, name = "BOOLEAN"),
    @JsonSubTypes.Type(value = PlainType.class, name = "STRING"),
    @JsonSubTypes.Type(value = PlainType.class, name = "BINARY"),
    @JsonSubTypes.Type(value = PlainType.class, name = "DATE"),
    @JsonSubTypes.Type(value = PlainType.class, name = "TIMESTAMP"),
    @JsonSubTypes.Type(value = SequenceType.class, name = "CHAR"),
    @JsonSubTypes.Type(value = SequenceType.class, name = "VARCHAR"),
    @JsonSubTypes.Type(value = DecimalType.class, name = "DECIMAL"),
    @JsonSubTypes.Type(value = ArrayType.class, name = "ARRAY"),
    @JsonSubTypes.Type(value = MapType.class, name = "MAP"),
    @JsonSubTypes.Type(value = StructType.class, name = "STRUCT"),
    @JsonSubTypes.Type(value = UnionType.class, name = "UNION")
})
public interface FieldType {

    /**
     * Returns the type name.
     * @return the type name
     */
    @JsonProperty("name")
    TypeName getTypeName();

    /**
     * Returns the qualified type name.
     * @return the qualified type name
     */
    @JsonIgnore
    String getQualifiedName();

    /**
     * Represents a type category.
     * @since 0.8.1
     */
    enum Category {

        /**
         * Plain (non-decorated) types.
         */
        PLAIN,

        /**
         * Sequence types with fixed length.
         */
        SEQUENCE,

        /**
         * Decimal types.
         */
        DECIMAL,

        /**
         * Array types.
         */
        ARRAY,

        /**
         * Map types.
         */
        MAP,

        /**
         * Struct types.
         */
        STRUCT,

        /**
         * Union types.
         */
        UNION,
        ;
    }

    /**
     * Represents a type name.
     * @since 0.8.1
     */
    enum TypeName {

        /**
         * 8-bit signed integer.
         */
        TINYINT(Category.PLAIN),

        /**
         * 16-bit signed integer.
         */
        SMALLINT(Category.PLAIN),

        /**
         * 32-bit signed integer.
         */
        INT(Category.PLAIN),

        /**
         * 64-bit signed integer.
         */
        BIGINT(Category.PLAIN),

        /**
         * 32-bit floating point number.
         */
        FLOAT(Category.PLAIN),

        /**
         * 64-bit floating point number.
         */
        DOUBLE(Category.PLAIN),

        /**
         * boolean.
         */
        BOOLEAN(Category.PLAIN),

        /**
         * character string.
         */
        STRING(Category.PLAIN),

        /**
         * binary string.
         */
        BINARY(Category.PLAIN),

        /**
         * date.
         */
        DATE(Category.PLAIN),

        /**
         * timestamp.
         */
        TIMESTAMP(Category.PLAIN),

        /**
         * character sequence with maximum length.
         */
        VARCHAR(Category.SEQUENCE),

        /**
         * character sequence with fixed length.
         */
        CHAR(Category.SEQUENCE),

        /**
         * fixed point decimal.
         */
        DECIMAL(Category.DECIMAL),

        /**
         * array.
         */
        ARRAY(Category.ARRAY),

        /**
         * map.
         */
        MAP(Category.MAP),

        /**
         * struct.
         */
        STRUCT(Category.STRUCT),

        /**
         * union.
         */
        UNION(Category.UNION),
        ;

        private final Category category;

        TypeName(Category category) {
            this.category = category;
        }

        /**
         * Returns the type category.
         * @return the type category
         */
        public Category getCategory() {
            return category;
        }
    }
}
