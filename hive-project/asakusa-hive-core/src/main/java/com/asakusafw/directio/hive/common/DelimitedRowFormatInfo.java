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

/**
 * Represents {@code ROW FORMAT} in Hive.
 * @since 0.7.0
 */
public class DelimitedRowFormatInfo implements RowFormatInfo {

    private String fieldsTerminatedBy;

    private String escapedBy;

    private String collectionItemsTerminatedBy;

    private String mapKeysTerminatedBy;

    private String linesTerminatedBy;

    private String nullDefinedAs;

    @Override
    public Kind getKind() {
        return Kind.DELIMITED;
    }

    /**
     * Returns {@code FIELDS TERMINATED BY}.
     * @return {@code FIELDS TERMINATED BY}, or {@code null} if it is not defined
     */
    public String getFieldsTerminatedBy() {
        return fieldsTerminatedBy;
    }

    /**
     * Sets {@code FIELDS TERMINATED BY}.
     * @param value the value
     */
    public void setFieldsTerminatedBy(String value) {
        this.fieldsTerminatedBy = value;
    }

    /**
     * Returns {@code ESCAPED BY}.
     * @return {@code ESCAPED BY}, or {@code null} if it is not defined
     */
    public String getEscapedBy() {
        return escapedBy;
    }

    /**
     * Sets {@code ESCAPED BY}.
     * @param value the value
     */
    public void setEscapedBy(String value) {
        this.escapedBy = value;
    }

    /**
     * Returns {@code COLLECTION ITEMS TERMINATED BY}.
     * @return {@code COLLECTION ITEMS TERMINATED BY}, or {@code null} if it is not defined
     */
    public String getCollectionItemsTerminatedBy() {
        return collectionItemsTerminatedBy;
    }

    /**
     * Sets {@code ESCAPED BY}.
     * @param value the value
     */
    public void setCollectionItemsTerminatedBy(String value) {
        this.collectionItemsTerminatedBy = value;
    }

    /**
     * Returns {@code MAP KEYS TERMINATED BY}.
     * @return {@code MAP KEYS TERMINATED BY}, or {@code null} if it is not defined
     */
    public String getMapKeysTerminatedBy() {
        return mapKeysTerminatedBy;
    }

    /**
     * Sets {@code ESCAPED BY}.
     * @param value the value
     */
    public void setMapKeysTerminatedBy(String value) {
        this.mapKeysTerminatedBy = value;
    }

    /**
     * Returns {@code LINES TERMINATED BY}.
     * @return {@code LINES TERMINATED BY}, or {@code null} if it is not defined
     */
    public String getLinesTerminatedBy() {
        return linesTerminatedBy;
    }

    /**
     * Sets {@code ESCAPED BY}.
     * @param value the value
     */
    public void setLinesTerminatedBy(String value) {
        this.linesTerminatedBy = value;
    }

    /**
     * Returns {@code NULL DEFINED AS}.
     * @return {@code NULL DEFINED AS}, or {@code null} if it is not defined
     */
    public String getNullDefinedAs() {
        return nullDefinedAs;
    }

    /**
     * Sets {@code NULL DEFINED AS}.
     * @param value the value
     */
    public void setNullDefinedAs(String value) {
        this.nullDefinedAs = value;
    }
}
