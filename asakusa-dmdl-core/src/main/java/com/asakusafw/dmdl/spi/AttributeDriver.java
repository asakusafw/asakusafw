/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.dmdl.spi;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.semantics.Declaration;
import com.asakusafw.dmdl.semantics.DmdlSemantics;

/**
 * Analyzes and processes attribtues in form of {@link AstAttribute AST}.
 * <p>
 * To enhance DMDL compiler, clients can implement this
 * and put the class name in
 * {@code META-INF/services/com.asakusafw.dmdl.spi.AttributeDriver}.
 * </p>
 */
public abstract class AttributeDriver {

    /**
     * Returns the qualified name of the target attribute.
     * @return the target attribute name
     */
    public abstract String getTargetName();

    /**
     * Processes and modifies the attributed declaration.
     * @param environment the processing environment
     * @param attribute the attribtue with the {@link #getTargetName() target name}
     * @param declaration the declaration with the {@code attribute}
     * @see #getTargetName()
     */
    public abstract void process(
            DmdlSemantics environment,
            Declaration declaration,
            AstAttribute attribute);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
