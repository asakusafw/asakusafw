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
package com.asakusafw.dmdl.windgate.csv.driver;

import java.text.SimpleDateFormat;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.Trait;
import com.asakusafw.runtime.io.csv.CsvConfiguration;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;

/**
 * Attributes for CSV supported data models.
 * @since 0.2.4
 */
public class CsvSupportTrait implements Trait<CsvSupportTrait> {

    private final AstNode originalAst;

    private final Configuration configuration;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param configuration the CSV configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CsvSupportTrait(AstNode originalAst, Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.configuration = configuration;
    }

    /**
     * Returns CSV configuration.
     * @return the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * The CSV format configuration.
     * @since 0.2.4
     * @see CsvConfiguration
     */
    public static class Configuration {

        private String charsetName = "UTF-8"; //$NON-NLS-1$

        private boolean enableHeader = false;

        private String trueFormat = CsvConfiguration.DEFAULT_TRUE_FORMAT;

        private String falseFormat = CsvConfiguration.DEFAULT_FALSE_FORMAT;

        private String dateFormat = CsvConfiguration.DEFAULT_DATE_FORMAT;

        private String dateTimeFormat = CsvConfiguration.DEFAULT_DATE_TIME_FORMAT;

        /**
         * Returns the charset name.
         * @return the charset name (default: UTF-8)
         */
        public String getCharsetName() {
            return charsetName;
        }

        /**
         * Sets the charset name.
         * @param charsetName the charset name
         */
        public void setCharsetName(String charsetName) {
            this.charsetName = charsetName;
        }

        /**
         * Returns whether the header is required.
         * @return {@code true} if header is required, otherwise {@code false} (default: false)
         */
        public boolean isEnableHeader() {
            return enableHeader;
        }

        /**
         * Configures whether the header is required.
         * @param enableHeader {@code true} to require header, otherwise {@code false}
         */
        public void setEnableHeader(boolean enableHeader) {
            this.enableHeader = enableHeader;
        }

        /**
         * Returns the {@code "true"} representation.
         * @return the representation (default: {@link CsvConfiguration#DEFAULT_TRUE_FORMAT})
         */
        public String getTrueFormat() {
            return trueFormat;
        }

        /**
         * Configures the {@code "true"} representation.
         * @param format the representation
         */
        public void setTrueFormat(String format) {
            this.trueFormat = format;
        }

        /**
         * Returns the {@code "false"} representation.
         * @return the representation (default: {@link CsvConfiguration#DEFAULT_FALSE_FORMAT})
         */
        public String getFalseFormat() {
            return falseFormat;
        }

        /**
         * Configures the {@code "false"} representation.
         * @param format the representation
         */
        public void setFalseFormat(String format) {
            this.falseFormat = format;
        }

        /**
         * Returns the {@link Date} representation in {@link SimpleDateFormat}.
         * @return the representation (default: {@link CsvConfiguration#DEFAULT_DATE_FORMAT})
         */
        public String getDateFormat() {
            return dateFormat;
        }

        /**
         * Configures the {@link Date} representation.
         * @param format the representation in {@link SimpleDateFormat}
         */
        public void setDateFormat(String format) {
            this.dateFormat = format;
        }

        /**
         * Returns the {@link DateTime} representation in {@link SimpleDateFormat}.
         * @return the representation (default: {@link CsvConfiguration#DEFAULT_DATE_TIME_FORMAT})
         */
        public String getDateTimeFormat() {
            return dateTimeFormat;
        }

        /**
         * Configures the {@link DateTime} representation.
         * @param format the representation in {@link SimpleDateFormat}
         */
        public void setDateTimeFormat(String format) {
            this.dateTimeFormat = format;
        }
    }
}
