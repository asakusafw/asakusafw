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
package com.asakusafw.dmdl.semantics;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstDescription;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;

/**
 * The world of DMDL semantics.
 */
public class DmdlSemantics {

    private final Map<String, ModelDeclaration> declaredModels = Maps.create();

    private final List<Diagnostic> diagnostics = Lists.create();

    private boolean sawError = false;

    /**
     * Declares a new property into this model.
     * @param modelOriginalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param modelName the name of this model
     * @param modelDescription the description of this model, or {@code null} if unknown
     * @param modelAttributes the attribtues of this model
     * @return the declared property
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ModelDeclaration declareModel(
            AstModelDefinition<?> modelOriginalAst,
            AstSimpleName modelName,
            AstDescription modelDescription,
            List<? extends AstAttribute> modelAttributes) {
        if (modelName == null) {
            throw new IllegalArgumentException("modelName must not be null"); //$NON-NLS-1$
        }
        if (modelAttributes == null) {
            throw new IllegalArgumentException("modelAttributes must not be null"); //$NON-NLS-1$
        }
        if (declaredModels.containsKey(modelName.identifier)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Model \"{0}\" is already declared in this session", //$NON-NLS-1$
                    modelName));
        }
        ModelDeclaration declared = new ModelDeclaration(
                this,
                modelOriginalAst,
                modelName,
                modelDescription,
                modelAttributes);
        declaredModels.put(modelName.identifier, declared);
        return declared;
    }

    /**
     * Creates and returns a new model symbol in this world.
     * @param modelName the name of target model
     * @return the created symbol
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ModelSymbol createModelSymbol(AstSimpleName modelName) {
        if (modelName == null) {
            throw new IllegalArgumentException("modelName must not be null"); //$NON-NLS-1$
        }
        return new ModelSymbol(this, modelName);
    }

    /**
     * Returns a declared model in this world.
     * @param modelName the name of the model
     * @return a declared model with the name, or {@code null} if not declared
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ModelDeclaration findModelDeclaration(String modelName) {
        if (modelName == null) {
            throw new IllegalArgumentException("modelName must not be null"); //$NON-NLS-1$
        }
        return declaredModels.get(modelName);
    }

    /**
     * Returns all models declared in this world.
     * @return all models
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Collection<ModelDeclaration> getDeclaredModels() {
        return Collections.unmodifiableCollection(declaredModels.values());
    }

    /**
     * Addes diagnostics about semantics analysis.
     * @param diagnosticList diagnostics
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void reportAll(Iterable<? extends Diagnostic> diagnosticList) {
        if (diagnosticList == null) {
            throw new IllegalArgumentException("diagnosticList must not be null"); //$NON-NLS-1$
        }
        for (Diagnostic diagnostic : diagnosticList) {
            report(diagnostic);
        }
    }

    /**
     * Addes a diagnostic about semantics analysis.
     * @param diagnostic the diagnostic
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void report(Diagnostic diagnostic) {
        if (diagnostic == null) {
            throw new IllegalArgumentException("diagnostic must not be null"); //$NON-NLS-1$
        }
        sawError |= (diagnostic.level == Level.ERROR);
        diagnostics.add(diagnostic);
    }

    /**
     * Returns iff some error diagnostics have been reported.
     * @return {@code true} iff semantic analysis has error
     */
    public boolean hasError() {
        return sawError;
    }

    /**
     * Returns the all diagnostics about semantics analysis.
     * @return all diagnostics
     */
    public List<Diagnostic> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }
}
