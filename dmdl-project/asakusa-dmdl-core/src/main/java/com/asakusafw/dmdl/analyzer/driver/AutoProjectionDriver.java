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
package com.asakusafw.dmdl.analyzer.driver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.PropertyReferenceDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.trait.ProjectionsTrait;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;auto_projection</code> annotations.
<h2>'&#64;auto_projection' attribute</h2>
The attributed declaration must be:
<ul>
<li> a model attribute </li>
<li> with no attribute elements </li>
</ul>
 */
public class AutoProjectionDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "auto_projection"; //$NON-NLS-1$

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(
            DmdlSemantics environment,
            ModelDeclaration declaration,
            AstAttribute attribute) {
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, attribute.elements));
        List<ModelSymbol> autoProjectios = collectProjections(environment, declaration);

        ProjectionsTrait projections = declaration.getTrait(ProjectionsTrait.class);
        if (projections == null) {
            projections = new ProjectionsTrait(declaration.getOriginalAst().expression, autoProjectios);
        } else {
            List<ModelSymbol> composite = new ArrayList<>();
            composite.addAll(projections.getProjections());
            composite.addAll(autoProjectios);
            projections = new ProjectionsTrait(declaration.getOriginalAst().expression, composite);
        }
        declaration.putTrait(ProjectionsTrait.class, projections);
    }

    private static List<ModelSymbol> collectProjections(DmdlSemantics environment, ModelDeclaration model) {
        assert environment != null;
        assert model != null;

        Set<String> saw = new HashSet<>();
        saw.add(model.getName().identifier);
        ProjectionsTrait projections = model.getTrait(ProjectionsTrait.class);
        if (projections != null) {
            for (ModelSymbol symbol : projections.getProjections()) {
                saw.add(symbol.getName().identifier);
            }
        }

        Map<String, Type> properties = model.getDeclaredProperties().stream()
                .collect(Collectors.toMap(
                        p -> p.getName().identifier,
                        p -> p.getType()));
        Map<String, Type> references = model.getDeclaredPropertyReferences().stream()
                .collect(Collectors.toMap(
                        p -> p.getName().identifier,
                        p -> p.getType()));
        List<ModelSymbol> autoProjectios = new ArrayList<>();
        for (ModelDeclaration other : environment.getDeclaredModels()) {
            // projection must be a projective model
            if (other.getOriginalAst().kind != ModelDefinitionKind.PROJECTIVE) {
                continue;
            }
            // projection must not be itself
            if (saw.contains(other.getName().identifier)) {
                continue;
            }
            saw.add(other.getName().identifier);

            if (contains(properties, references, other)) {
                autoProjectios.add(other.getSymbol());
            }
        }
        return autoProjectios;
    }

    private static boolean contains(
            Map<String, Type> properties, Map<String, Type> references,
            ModelDeclaration projection) {
        assert properties != null;
        assert projection != null;

        List<PropertyDeclaration> projectionProperties = projection.getDeclaredProperties();
        // pigeon-hole principle
        if (properties.size() < projectionProperties.size()) {
            return false;
        }
        // for all p in projection.properties:
        //   assert p in this.properties
        //   assert p.type == this.p.type
        for (PropertyDeclaration projectionProperty : projectionProperties) {
            Type type = properties.get(projectionProperty.getName().identifier);
            if (type == null || type.isSame(projectionProperty.getType()) == false) {
                return false;
            }
        }

        List<PropertyReferenceDeclaration> projectionReferences = projection.getDeclaredPropertyReferences();
        // pigeon-hole principle
        if (references.size() < projectionReferences.size()) {
            return false;
        }
        // if projection has a reference with actual body, we never select it
        if (projectionReferences.stream().anyMatch(p -> p.getReference().isStub() == false)) {
            return false;
        }
        // for all r in projection.references
        //   assert r in this.references
        //   assert r.type = this.r.type
        for (PropertyReferenceDeclaration projectionReference : projectionReferences) {
            Type type = references.get(projectionReference.getName().identifier);
            if (type == null || type.isSame(projectionReference.getType()) == false) {
                return false;
            }
        }

        return true;
    }
}
