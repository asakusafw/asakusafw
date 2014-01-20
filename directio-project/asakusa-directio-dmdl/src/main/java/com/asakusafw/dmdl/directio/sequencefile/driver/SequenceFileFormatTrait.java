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
package com.asakusafw.dmdl.directio.sequencefile.driver;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Attributes for SequenceFile supported data models.
 * @since 0.4.0
 */
public class SequenceFileFormatTrait implements Trait<SequenceFileFormatTrait> {

    private final AstNode originalAst;

    private final Configuration configuration;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param configuration the CSV configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SequenceFileFormatTrait(AstNode originalAst, Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.configuration = configuration;
    }

    /**
     * Returns SequenceFile format configuration.
     * @return the format configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * The SequenceFile format configuration.
     * @since 0.4.0
     */
    public static class Configuration {
        // no special members
    }
}
