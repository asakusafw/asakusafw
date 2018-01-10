/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.semantics.Declaration;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;

/**
 * {@link AttributeDriver} for {@link ModelDeclaration}s.
 * @since 0.2.4
 * @version 0.7.0
 */
public abstract class ModelAttributeDriver implements AttributeDriver {

    @Override
    public final void process(Context context, Declaration declaration, AstAttribute attribute) {
        assert attribute.name.toString().equals(getTargetName());
        if ((declaration instanceof ModelDeclaration) == false) {
            context.getEnvironment().report(new Diagnostic(
                    Level.ERROR,
                    declaration.getOriginalAst(),
                    Messages.getString("ModelAttributeDriver.diagnosticInvalidAttribute"), //$NON-NLS-1$
                    getTargetName()));
            return;
        }
        process(context.getEnvironment(), (ModelDeclaration) declaration, attribute);
    }

    @Override
    public final void verify(Context context, Declaration declaration, AstAttribute attribute) {
        assert attribute.name.toString().equals(getTargetName());
        if ((declaration instanceof ModelDeclaration) == false) {
            return;
        }
        verify(context.getEnvironment(), (ModelDeclaration) declaration, attribute);
    }

    /**
     * Processes and modifies the attributed data model declaration.
     * @param environment the processing environment
     * @param attribute the attribute with the {@link #getTargetName() target name}
     * @param declaration the model declaration with the {@code attribute}
     * @see #getTargetName()
     */
    public abstract void process(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute);

    /**
     * Verifies the attributed declaration.
     * This will be invoked after all attributes are
     * {@link AttributeDriver#process(Context, Declaration, AstAttribute) processed}.
     * @param environment the processing environment
     * @param attribute the attribute with the {@link #getTargetName() target name}
     * @param declaration the model declaration with the {@code attribute}
     * @see #getTargetName()
     * @since 0.7.0
     */
    public void verify(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        return;
    }
}
