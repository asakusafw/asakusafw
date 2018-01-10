/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents a storage format ({@code STORED AS ...}) in Hive.
 * @since 0.8.1
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "kind",
        visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BuiltinStorageFormatInfo.class, name = "SEQUENCEFILE"),
    @JsonSubTypes.Type(value = BuiltinStorageFormatInfo.class, name = "TEXTFILE"),
    @JsonSubTypes.Type(value = BuiltinStorageFormatInfo.class, name = "RCFILE"),
    @JsonSubTypes.Type(value = BuiltinStorageFormatInfo.class, name = "ORC"),
    @JsonSubTypes.Type(value = BuiltinStorageFormatInfo.class, name = "PARQUET"),
    @JsonSubTypes.Type(value = BuiltinStorageFormatInfo.class, name = "AVRO"),
    @JsonSubTypes.Type(value = CustomStorageFormatInfo.class, name = "CUSTOM"),
})
public interface StorageFormatInfo {

    /**
     * Returns the storage format kind.
     * @return the format kind
     */
    @JsonProperty("kind")
    FormatKind getFormatKind();

    /**
     * Represents a storage format category.
     * @since 0.8.1
     */
    enum Category {

        /**
         * Built-in storage formats.
         */
        BUILTIN,

        /**
         * Custom storage formats.
         */
        CUSTOM,
    }

    /**
     * Represents a storage format kind.
     * @since 0.8.1
     */
    enum FormatKind {

        /**
         * Hadoop sequence files.
         */
        SEQUENCEFILE(Category.BUILTIN),

        /**
         * Text files.
         */
        TEXTFILE(Category.BUILTIN),

        /**
         * Hadoop RC files.
         */
        RCFILE(Category.BUILTIN),

        /**
         * ORC files.
         */
        ORC(Category.BUILTIN),

        /**
         * Parquet files.
         */
        PARQUET(Category.BUILTIN),

        /**
         * Avro formatted files.
         */
        AVRO(Category.BUILTIN),

        /**
         * Using custom Input/Output format.
         */
        CUSTOM(Category.CUSTOM),
        ;

        private final Category category;

        FormatKind(Category category) {
            this.category = category;
        }

        /**
         * Returns the category.
         * @return the category
         */
        public Category getCategory() {
            return category;
        }
    }
}
