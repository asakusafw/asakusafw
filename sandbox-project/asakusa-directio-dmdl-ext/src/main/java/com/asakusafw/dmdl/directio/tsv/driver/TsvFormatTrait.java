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
package com.asakusafw.dmdl.directio.tsv.driver;

import java.nio.charset.Charset;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Attributes for special TSV supported data models.
 * @since 0.5.0
 */
public class TsvFormatTrait implements Trait<TsvFormatTrait> {

    static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private final AstNode originalAst;

    private final Configuration configuration;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param configuration the TSV configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TsvFormatTrait(AstNode originalAst, Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.configuration = configuration;
    }

    /**
     * Returns TSV configuration.
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
     * The TSV format configuration.
     * @since 0.5.0
     * @version 0.5.2
     */
    public static class Configuration {

        private String charsetName = DEFAULT_CHARSET.name();

        private String codecName = null;

        private boolean enableHeader = false;

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
         * Returns the codec name.
         * @return the codec name, or {@code null} if no codecs are specified
         * @since 0.5.2
         */
        public String getCodecName() {
            return codecName;
        }

        /**
         * Sets the codec name.
         * @param codecName the codec name, or {@code null} to reset it
         * @since 0.5.2
         */
        public void setCodecName(String codecName) {
            this.codecName = codecName;
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
    }
}
