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
package com.asakusafw.dmdl.windgate.stream.driver;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Trait for holding format name.
 */
public class StreamSupportTrait implements Trait<StreamSupportTrait> {

    private final AstNode originalAst;

    private final String formatName;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param formatName the supported for mat name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public StreamSupportTrait(AstNode originalAst, String formatName) {
        if (originalAst == null) {
            throw new IllegalArgumentException("originalAst must not be null"); //$NON-NLS-1$
        }
        if (formatName == null) {
            throw new IllegalArgumentException("formatName must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.formatName = formatName;
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns the format name.
     * @return the format name
     */
    public String getTypeName() {
        return formatName;
    }
}
