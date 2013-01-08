/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
     */
    public static class Configuration {

        private String charsetName = DEFAULT_CHARSET.name();

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
    }
}
