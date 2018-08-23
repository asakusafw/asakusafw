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
package com.asakusafw.dmdl.java.emitter.driver;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.trait.ProjectionsTrait;
import com.asakusafw.dmdl.semantics.trait.ReferencesTrait;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * Make models inherit their projective models.
 * @since 0.1.0
 * @version 0.10.2
 */
public class ProjectionDriver extends JavaDataModelDriver {

    static final String PREFIX_KEY = "com.asakusafw.dmdl.java.emitter.driver.ProjectionDriver"; //$NON-NLS-1$

    static final String KEY_INDIRECT_INHERIT = PREFIX_KEY + ".indirect"; //$NON-NLS-1$

    static final boolean DEFAULT_INDIRECT_INHERIT = true;

    static final boolean INDIRECT_INHERIT = Optional.ofNullable(System.getProperty(KEY_INDIRECT_INHERIT))
            .map(Boolean::parseBoolean)
            .orElse(DEFAULT_INDIRECT_INHERIT);

    @Override
    public List<Type> getInterfaces(EmitContext context, ModelDeclaration model) {
        return collect(context, model).stream()
                .map(context::resolve)
                .collect(Collectors.toList());
    }

    private List<ModelSymbol> collect(EmitContext context, ModelDeclaration model) {
        if (model.getTrait(ReferencesTrait.class) == null) {
            return Collections.emptyList();
        }
        List<ModelSymbol> projections = collectProjections(context, model)
                .distinct()
                .collect(Collectors.toList());
        return proneDuplications(context, projections);
    }

    private Stream<ModelSymbol> collectProjections(EmitContext context, ModelDeclaration model) {
        Set<ModelSymbol> projections = model.findTrait(ProjectionsTrait.class)
                .map(ProjectionsTrait::getProjections)
                .<Set<ModelSymbol>>map(HashSet::new)
                .orElse(Collections.emptySet());
        List<ModelSymbol> descendants = model.findTrait(ReferencesTrait.class)
                .map(ReferencesTrait::getReferences)
                .orElse(Collections.emptyList());
        return descendants.stream()
                .flatMap(it -> {
                    if (projections.contains(it)) {
                        // projective models inherits others by using Java mechanism
                        return Stream.of(it);
                    } else if (INDIRECT_INHERIT) {
                        // inherit descendant projective models instead of non-projective
                        // because we cannot inherit non-projective models in Java mechanism
                        ModelDeclaration decl = it.findDeclaration();
                        if (decl == null) {
                            return Stream.empty();
                        }
                        return collectProjections(context, decl);
                    } else {
                        return Stream.empty();
                    }
                });
    }

    private List<ModelSymbol> proneDuplications(EmitContext context, List<ModelSymbol> projections) {
        if (projections.size() < 2) {
            return projections;
        }
        assert projections.size() == projections.stream().distinct().count();
        ModelSymbol[] a = projections.toArray(new ModelSymbol[projections.size()]);
        for (int i = 0; i < a.length; i++) {
            if (a[i] == null) {
                continue;
            }
            Set<ModelSymbol> desc = collectDescendants(context, a[i]).collect(Collectors.toSet());
            if (desc.isEmpty()) {
                continue;
            }
            for (int j = 0; j < a.length; j++) {
                if (i == j || a[j] == null) {
                    continue;
                }
                if (desc.contains(a[j])) {
                    a[j] = null;
                }
            }
        }
        return Arrays.stream(a).filter(it -> it != null).collect(Collectors.toList());
    }

    private Stream<ModelSymbol> collectDescendants(EmitContext context, ModelSymbol symbol) {
        ModelDeclaration decl = symbol.findDeclaration();
        if (decl == null) {
            return Stream.empty();
        }
        return decl.findTrait(ProjectionsTrait.class)
                .map(ProjectionsTrait::getProjections)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(it -> Stream.concat(Stream.of(it), collectDescendants(context, it)));
    }
}
