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
package com.asakusafw.dmdl.analyzer.driver;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.trait.ProjectionsTrait;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Sets;

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
            List<ModelSymbol> composite = Lists.create();
            composite.addAll(projections.getProjections());
            composite.addAll(autoProjectios);
            projections = new ProjectionsTrait(declaration.getOriginalAst().expression, composite);
        }
        declaration.putTrait(ProjectionsTrait.class, projections);
    }

    private List<ModelSymbol> collectProjections(DmdlSemantics environment, ModelDeclaration model) {
        assert environment != null;
        assert model != null;
        Map<String, Type> properties = Maps.create();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            properties.put(property.getName().identifier, property.getType());
        }

        Set<String> saw = Sets.create();
        saw.add(model.getName().identifier);
        ProjectionsTrait projections = model.getTrait(ProjectionsTrait.class);
        if (projections != null) {
            for (ModelSymbol symbol : projections.getProjections()) {
                saw.add(symbol.getName().identifier);
            }
        }

        List<ModelSymbol> autoProjectios = Lists.create();
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

            if (contains(properties, other)) {
                autoProjectios.add(other.getSymbol());
            }
        }
        return autoProjectios;
    }

    private boolean contains(Map<String, Type> properties, ModelDeclaration other) {
        assert properties != null;
        assert other != null;

        List<PropertyDeclaration> projectionProperties = other.getDeclaredProperties();
        // pigeonhole principle
        if (properties.size() < projectionProperties.size()) {
            return false;
        }

        // for all p in other.properties:
        //   assert p in this.properties
        //   assert this.p.type == other.p.type
        for (PropertyDeclaration projectionProperty : projectionProperties) {
            Type type = properties.get(projectionProperty.getName().identifier);
            if (type == null || type.isSame(projectionProperty.getType()) == false) {
                return false;
            }
        }
        return true;
    }
}
