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
package com.asakusafw.directio.hive.orc;

import org.apache.hadoop.hive.ql.io.orc.CompressionKind;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;

import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelMapping.FieldMappingStrategy;

/**
 * Represents the ORCFile format configurations.
 * @since 0.7.0
 */
public class OrcFormatConfiguration {

    private FieldMappingStrategy fieldMappingStrategy = FieldMappingStrategy.POSITION;

    private ExceptionHandlingStrategy onMissingSource = ExceptionHandlingStrategy.LOGGING;

    private ExceptionHandlingStrategy onMissingTarget = ExceptionHandlingStrategy.LOGGING;

    private ExceptionHandlingStrategy onIncompatibleType = ExceptionHandlingStrategy.FAIL;

    private OrcFile.Version formatVersion;

    private CompressionKind compressionKind = CompressionKind.SNAPPY;

    private Long stripeSize = 64L * 1024 * 1024;

    /**
     * Clears all properties.
     * @return this
     */
    public OrcFormatConfiguration clear() {
        fieldMappingStrategy = null;
        onMissingSource = null;
        onMissingTarget = null;
        onIncompatibleType = null;
        formatVersion = null;
        compressionKind = null;
        stripeSize = null;
        return this;
    }

    /**
     * Sets the field mapping strategy.
     * @param value the strategy
     * @return this
     */
    public OrcFormatConfiguration withFieldMappingStrategy(FieldMappingStrategy value) {
        this.fieldMappingStrategy = value;
        return this;
    }

    /**
     * Sets the exception handling strategy for missing source fields.
     * @param value the strategy
     * @return this
     */
    public OrcFormatConfiguration withOnMissingSource(ExceptionHandlingStrategy value) {
        this.onMissingSource = value;
        return this;
    }

    /**
     * Sets the exception handling strategy for missing target fields.
     * @param value the strategy
     * @return this
     */
    public OrcFormatConfiguration withOnMissingTarget(ExceptionHandlingStrategy value) {
        this.onMissingTarget = value;
        return this;
    }

    /**
     * Sets the exception handling strategy for incompatible field type.
     * @param value the strategy
     * @return this
     */
    public OrcFormatConfiguration withOnIncompatibleType(ExceptionHandlingStrategy value) {
        this.onIncompatibleType = value;
        return this;
    }

    /**
     * Sets the ORCFile format version.
     * @param value the version
     * @return this
     */
    public OrcFormatConfiguration withFormatVersion(OrcFile.Version value) {
        this.formatVersion = value;
        return this;
    }

    /**
     * Sets the ORCFile compression kind.
     * @param value the compression kind
     * @return this
     */
    public OrcFormatConfiguration withCompressionKind(CompressionKind value) {
        this.compressionKind = value;
        return this;
    }

    /**
     * Sets the ORCFile stripe size (in bytes).
     * @param value the stripe size
     * @return this
     */
    public OrcFormatConfiguration withStripeSize(Long value) {
        this.stripeSize = value;
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
     * Returns the ORCFile format version.
     * @return the format version, or {@code null} if use system default value
     */
    public OrcFile.Version getFormatVersion() {
        return formatVersion;
    }

    /**
     * Returns the ORCFile compression kind.
     * @return the compression kind, or {@code null} if use system default value
     */
    public CompressionKind getCompressionKind() {
        return compressionKind;
    }

    /**
     * Returns the ORCFile stripe size (in bytes).
     * @return the stripe size, or {@code null} if use system default value
     */
    public Long getStripeSize() {
        return stripeSize;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OrcFormatConfiguration [fieldMappingStrategy="); //$NON-NLS-1$
        builder.append(fieldMappingStrategy);
        builder.append(", onMissingSource="); //$NON-NLS-1$
        builder.append(onMissingSource);
        builder.append(", onMissingTarget="); //$NON-NLS-1$
        builder.append(onMissingTarget);
        builder.append(", onIncompatibleType="); //$NON-NLS-1$
        builder.append(onIncompatibleType);
        builder.append(", formatVersion="); //$NON-NLS-1$
        builder.append(formatVersion);
        builder.append(", compressionKind="); //$NON-NLS-1$
        builder.append(compressionKind);
        builder.append(", stripeSize="); //$NON-NLS-1$
        builder.append(stripeSize);
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }
}
