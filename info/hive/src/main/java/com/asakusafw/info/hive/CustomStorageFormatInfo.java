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

import java.text.MessageFormat;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents custom storage format that using Hadoop input/output format.
 * Format category must be {@link com.asakusafw.info.hive.StorageFormatInfo.Category#BUILTIN}.
 * @since 0.8.1
 */
public class CustomStorageFormatInfo implements StorageFormatInfo {

    private static final String K_INPUT_FORMAT = "input";

    private static final String K_OUTPUT_FORMAT = "output";

    private final String inputFormatClass;

    private final String outputFormatClass;

    /**
     * Creates a new instance.
     * @param inputFormatClass the input format class name
     * @param outputFormatClass the output format class name
     */
    public CustomStorageFormatInfo(String inputFormatClass, String outputFormatClass) {
        this.inputFormatClass = inputFormatClass;
        this.outputFormatClass = outputFormatClass;
    }

    @JsonCreator
    CustomStorageFormatInfo(
            @JsonProperty(value = "kind", required = true) FormatKind kind,
            @JsonProperty(value = K_INPUT_FORMAT, required = true) String inputFormatClass,
            @JsonProperty(value = K_OUTPUT_FORMAT, required = true) String outputFormatClass) {
        this(inputFormatClass, outputFormatClass);
        if (kind.getCategory() != Category.CUSTOM) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported custom storage format: {0}",
                    kind));
        }
    }

    @Override
    public FormatKind getFormatKind() {
        return FormatKind.CUSTOM;
    }

    /**
     * Returns the input format class name.
     * @return the input format class name
     */
    @JsonProperty(K_INPUT_FORMAT)
    public String getInputFormatClass() {
        return inputFormatClass;
    }

    /**
     * Returns the output format class name.
     * @return the output format class name
     */
    @JsonProperty(K_OUTPUT_FORMAT)
    public String getOutputFormatClass() {
        return outputFormatClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(inputFormatClass);
        result = prime * result + Objects.hashCode(outputFormatClass);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CustomStorageFormatInfo other = (CustomStorageFormatInfo) obj;
        if (Objects.equals(inputFormatClass, other.inputFormatClass) == false) {
            return false;
        }
        if (Objects.equals(outputFormatClass, other.outputFormatClass) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "%s(input=\"%s\", output=\"%s\")",
                getFormatKind().toString(),
                getInputFormatClass(),
                getOutputFormatClass());
    }
}
