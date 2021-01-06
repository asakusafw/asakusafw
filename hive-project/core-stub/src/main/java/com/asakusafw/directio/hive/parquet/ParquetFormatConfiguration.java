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
package com.asakusafw.directio.hive.parquet;

import java.util.Optional;

import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelMapping.FieldMappingStrategy;

/**
 * Represents the parquet file format configurations.
 * @since 0.7.0
 * @version 0.10.3
 */
public class ParquetFormatConfiguration {

    private FieldMappingStrategy fieldMappingStrategy = FieldMappingStrategy.POSITION;

    private ExceptionHandlingStrategy onMissingSource = ExceptionHandlingStrategy.LOGGING;

    private ExceptionHandlingStrategy onMissingTarget = ExceptionHandlingStrategy.LOGGING;

    private ExceptionHandlingStrategy onIncompatibleType = ExceptionHandlingStrategy.FAIL;

    private String compressionCodecName;

    private Integer blockSize;

    private Integer dataPageSize;

    private Integer dictionaryPageSize;

    private Boolean enableDictionary;

    private Boolean enableValidation;

    private String writerVersion;

    /**
     * Clears all properties.
     * @return this
     */
    public ParquetFormatConfiguration clear() {
        fieldMappingStrategy = null;
        onMissingSource = null;
        onMissingTarget = null;
        onIncompatibleType = null;
        compressionCodecName = null;
        blockSize = null;
        dataPageSize = null;
        dictionaryPageSize = null;
        enableDictionary = null;
        enableValidation = null;
        writerVersion = null;
        return this;
    }

    /**
     * Sets the field mapping strategy.
     * @param value the strategy
     * @return this
     */
    public ParquetFormatConfiguration withFieldMappingStrategy(FieldMappingStrategy value) {
        this.fieldMappingStrategy = value;
        return this;
    }

    /**
     * Sets the exception handling strategy for missing source fields.
     * @param value the strategy
     * @return this
     */
    public ParquetFormatConfiguration withOnMissingSource(ExceptionHandlingStrategy value) {
        this.onMissingSource = value;
        return this;
    }

    /**
     * Sets the exception handling strategy for missing target fields.
     * @param value the strategy
     * @return this
     */
    public ParquetFormatConfiguration withOnMissingTarget(ExceptionHandlingStrategy value) {
        this.onMissingTarget = value;
        return this;
    }

    /**
     * Sets the exception handling strategy for incompatible field type.
     * @param value the strategy
     * @return this
     */
    public ParquetFormatConfiguration withOnIncompatibleType(ExceptionHandlingStrategy value) {
        this.onIncompatibleType = value;
        return this;
    }

    /**
     * Sets the compression codec name.
     * @param value the value
     * @return this
     */
    public ParquetFormatConfiguration withCompressionCodecName(String value) {
        this.compressionCodecName = value;
        return this;
    }

    /**
     * Sets the block size (in bytes).
     * @param value the value
     * @return this
     */
    public ParquetFormatConfiguration withBlockSize(Integer value) {
        this.blockSize = value;
        return this;
    }

    /**
     * Sets the data page size (in bytes).
     * @param value the value
     * @return this
     */
    public ParquetFormatConfiguration withDataPageSize(Integer value) {
        this.dataPageSize = value;
        return this;
    }

    /**
     * Sets the dictionary page size (in bytes).
     * @param value the value
     * @return this
     */
    public ParquetFormatConfiguration withDictionaryPageSize(Integer value) {
        this.dictionaryPageSize = value;
        return this;
    }

    /**
     * Sets whether the dictionary is enabled or not.
     * @param value the value
     * @return this
     */
    public ParquetFormatConfiguration withEnableDictionary(Boolean value) {
        this.enableDictionary = value;
        return this;
    }

    /**
     * Sets whether the schema validation is enabled or not.
     * @param value the value
     * @return this
     */
    public ParquetFormatConfiguration withEnableValidation(Boolean value) {
        this.enableValidation = value;
        return this;
    }

    /**
     * Sets the writer version.
     * @param value the value
     * @return this
     */
    public ParquetFormatConfiguration withWriterVersion(String value) {
        this.writerVersion = value;
        return this;
    }

    /**
     * Returns the field mapping strategy.
     * @return the field mapping strategy
     */
    public FieldMappingStrategy getFieldMappingStrategy() {
        return fieldMappingStrategy;
    }

    /**
     * Returns the exception handling strategy for missing source fields.
     * @return the exception handling strategy
     */
    public ExceptionHandlingStrategy getOnMissingSource() {
        return onMissingSource;
    }

    /**
     * Returns the exception handling strategy for missing target fields.
     * @return the exception handling strategy
     */
    public ExceptionHandlingStrategy getOnMissingTarget() {
        return onMissingTarget;
    }

    /**
     * Returns the exception handling strategy for incompatible field type.
     * @return the exception handling strategy
     */
    public ExceptionHandlingStrategy getOnIncompatibleType() {
        return onIncompatibleType;
    }

    /**
     * Returns the compression codec name.
     * @return the compression codec name
     */
    public String getCompressionCodecName() {
        return compressionCodecName;
    }

    /**
     * Returns the compression codec name.
     * @param type the type
     * @param <T> the type
     * @return the compression codec name
     * @since 0.10.3
     */
    public <T extends Enum<T>> T getCompressionCodecName(Class<T> type) {
        return Optional.ofNullable(getCompressionCodecName())
                .map(it -> Enum.valueOf(type, it))
                .orElse(null);
    }

    /**
     * Returns the block size (in bytes).
     * @return the block size
     */
    public Integer getBlockSize() {
        return blockSize;
    }

    /**
     * Returns the data page size (in bytes).
     * @return the data page size
     */
    public Integer getDataPageSize() {
        return dataPageSize;
    }

    /**
     * Returns the dictionary page size (in bytes).
     * @return the dictionary page size
     */
    public Integer getDictionaryPageSize() {
        return dictionaryPageSize;
    }

    /**
     * Returns whether the dictionary is enabled or not.
     * @return {@code true} if enabled, otherwise {@code false}
     */
    public Boolean getEnableDictionary() {
        return enableDictionary;
    }

    /**
     * Returns whether the schema validation is enabled or not.
     * @return {@code true} if enabled, otherwise {@code false}
     */
    public Boolean getEnableValidation() {
        return enableValidation;
    }

    /**
     * Returns the writer version.
     * @return the writer version, or {@code null}
     */
    public String getWriterVersion() {
        return writerVersion;
    }

    /**
     * Returns the writer version.
     * @param type the type
     * @param <T> the type
     * @return the writer version, or {@code null}
     * @since 0.10.3
     */
    public <T extends Enum<T>> T getWriterVersion(Class<T> type) {
        return Optional.ofNullable(getWriterVersion())
                .map(it -> Enum.valueOf(type, it))
                .orElse(null);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ParquetFormatConfiguration [fieldMappingStrategy="); //$NON-NLS-1$
        builder.append(fieldMappingStrategy);
        builder.append(", onMissingSource="); //$NON-NLS-1$
        builder.append(onMissingSource);
        builder.append(", onMissingTarget="); //$NON-NLS-1$
        builder.append(onMissingTarget);
        builder.append(", onIncompatibleType="); //$NON-NLS-1$
        builder.append(onIncompatibleType);
        builder.append(", compressionCodecName="); //$NON-NLS-1$
        builder.append(compressionCodecName);
        builder.append(", blockSize="); //$NON-NLS-1$
        builder.append(blockSize);
        builder.append(", dataPageSize="); //$NON-NLS-1$
        builder.append(dataPageSize);
        builder.append(", dictionaryPageSize="); //$NON-NLS-1$
        builder.append(dictionaryPageSize);
        builder.append(", enableDictionary="); //$NON-NLS-1$
        builder.append(enableDictionary);
        builder.append(", enableValidation="); //$NON-NLS-1$
        builder.append(enableValidation);
        builder.append(", writerVersion="); //$NON-NLS-1$
        builder.append(writerVersion);
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }
}
