/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.thundergate.driver;

import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.Declaration;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.bulkloader.OriginalName;

/**
 * Emits {@link OriginalName} annotations.
 */
public class OriginalNameEmitter extends JavaDataModelDriver {

    @Override
    public List<Annotation> getTypeAnnotations(EmitContext context, ModelDeclaration model) {
        ModelFactory f = context.getModelFactory();
        Expression value = Models.toLiteral(f, getOriginalName(model));
        return new AttributeBuilder(f)
            .annotation(context.resolve(OriginalName.class), "value", value)
            .toAnnotations();
    }

    @Override
    public List<Annotation> getMemberAnnotations(EmitContext context, PropertyDeclaration property) {
        OriginalNameTrait trait = property.getTrait(OriginalNameTrait.class);
        if (trait == null) {
            return Collections.emptyList();
        }

        ModelFactory f = context.getModelFactory();
        Expression value = Models.toLiteral(f, trait.getName());
        return new AttributeBuilder(f)
            .annotation(context.resolve(OriginalName.class), "value", value)
            .toAnnotations();
    }

    /**
     * Returns the original name for the specified declaration.
     * If the declaration has {@link OriginalNameTrait}, then returns the explicit name,
     * otherwise returns the default name which will be inferred.
     * @param declaration the declaration
     * @return the original name or default name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static String getOriginalName(Declaration declaration) {
        if (declaration == null) {
            throw new IllegalArgumentException("declaration must not be null"); //$NON-NLS-1$
        }
        OriginalNameTrait trait = declaration.getTrait(OriginalNameTrait.class);
        if (trait == null) {
            return declaration.getName().getSimpleName().identifier.toUpperCase();
        } else {
            return trait.getName();
        }
    }
}
