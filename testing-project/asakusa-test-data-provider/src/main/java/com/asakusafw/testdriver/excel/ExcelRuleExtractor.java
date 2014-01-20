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
package com.asakusafw.testdriver.excel;

import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.asakusafw.testdriver.rule.DataModelCondition;

/**
 * Extracts rules from Excel sheet.
 * @since 0.2.0
 */
public interface ExcelRuleExtractor {

    /**
     * Returns {@code true} iff this extractor supports the sheet.
     * @param sheet target sheet
     * @return {@code true} iff this extractor supports the sheet, or {@code false} otherwise
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    boolean supports(Sheet sheet);

    /**
     * Extract predicates about presence/absense of each data model objects.
     * @param sheet target sheet
     * @return the extracted value
     * @throws FormatException if the sheet format is invalid
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    Set<DataModelCondition> extractDataModelCondition(Sheet sheet) throws FormatException;

    /**
     * Extract row index of describing conditions about each property.
     * @param sheet target sheet
     * @return the start row index (0-origin)
     * @throws FormatException if the sheet format is invalid
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    int extractPropertyRowStartIndex(Sheet sheet) throws FormatException;

    /**
     * Extract property name from the row.
     * @param row source row
     * @return the extracted value, or {@code null} if target cell is blank
     * @throws FormatException if the sheet format is invalid
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    String extractName(Row row) throws FormatException;

    /**
     * Extract value predicate from the row.
     * @param row source row
     * @return the extracted value
     * @throws FormatException if the sheet format is invalid
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    ValueConditionKind extractValueCondition(Row row) throws FormatException;

    /**
     * Extract nullity predicate from the row.
     * @param row source row
     * @return the extracted value
     * @throws FormatException if the sheet format is invalid
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    NullityConditionKind extractNullityCondition(Row row) throws FormatException;

    /**
     * Invalid format of excel condition sheet.
     * @since 0.2.0
     */
    public class FormatException extends Exception {

        private static final long serialVersionUID = -1107137393611473010L;

        /**
         * Creates a new instance.
         * @param message message (nullable)
         */
        public FormatException(String message) {
            super(message);
        }

        /**
         * Creates a new instance.
         * @param message message (nullable)
         * @param cause original exception (nullable)
         */
        public FormatException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
