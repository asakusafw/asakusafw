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
package com.asakusafw.dmdl.java.analyzer;

import com.asakusafw.dmdl.analyzer.DmdlAnalyzerEnhancer;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.semantics.DmdlSemantics;

/**
 * Enhances DMDL analyzer for Java data model generation.
 * @since 0.7.0
 */
public class JavaDataModelAnalyzerEnhancer extends DmdlAnalyzerEnhancer {

    @Override
    public void validateSyntax(DmdlSemantics root, AstModelDefinition<?> definition) {
        definition.accept(null, new ExtraSyntaxValidator(root));
    }
}
