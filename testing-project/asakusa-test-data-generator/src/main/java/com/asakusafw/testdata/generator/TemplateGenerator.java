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
package com.asakusafw.testdata.generator;

import java.io.IOException;

import com.asakusafw.dmdl.semantics.ModelDeclaration;

/**
 * Abstract interface of test template generators.
 * @since 0.2.0
 */
public interface TemplateGenerator {

    /**
     * Generates a test template for the specified model.
     * @param model the target model
     * @throws IOException if failed to generate a workbook
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    void generate(ModelDeclaration model) throws IOException;

    /**
     * Returns the title of this generator.
     * @return the title
     */
    String getTitle();
}
