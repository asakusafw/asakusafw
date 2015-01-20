/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.dmdl.analyzer;

import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.semantics.DmdlSemantics;

/**
 * Enhances {@link DmdlAnalyzer}.
 * @since 0.7.1
 */
public abstract class DmdlAnalyzerEnhancer {

    /**
     * No enhancements.
     */
    public static final DmdlAnalyzerEnhancer NULL = new DmdlAnalyzerEnhancer() {
        // no special members
    };

    /**
     * Validates the syntax of definitions.
     * @param root the semantics root; this may have no definitions
     * @param definition the syntax model
     */
    public void validateSyntax(DmdlSemantics root, AstModelDefinition<?> definition) {
        return;
    }
}
