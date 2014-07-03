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
 * @since 0.2.0
 * @version 0.7.0
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

    /**
     * Verifies the attributed declaration.
     * This will be invoked after all attributes are
     * {@link #process(DmdlSemantics, Declaration, AstAttribute) processed}.
     * @param environment the processing environment
     * @param attribute the attribute with the {@link #getTargetName() target name}
     * @param declaration the declaration with the {@code attribute}
     * @see #getTargetName()
     * @since 0.7.0
     */
    public void verify(
            DmdlSemantics environment,
            Declaration declaration,
            AstAttribute attribute) {
        return;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
