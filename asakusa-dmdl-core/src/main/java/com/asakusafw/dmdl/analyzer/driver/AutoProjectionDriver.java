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
package com.asakusafw.dmdl.analyzer.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.semantics.Declaration;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.trait.ProjectionsTrait;
import com.asakusafw.dmdl.spi.AttributeDriver;
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
public class AutoProjectionDriver implements AttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "auto_projection";

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(
            DmdlSemantics environment,
            Declaration declaration,
            AstAttribute attribute) {
        assert attribute.name.toString().equals(TARGET_NAME);
        if ((declaration instanceof ModelDeclaration) == false) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    declaration.getOriginalAst(),
                    "@{0} is not suitable for properties",
                    TARGET_NAME));
            return;
        }
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, attribute.elements));
        ModelDeclaration model = (ModelDeclaration) declaration;
        List<ModelSymbol> autoProjectios = collectProjections(environment, model);

        ProjectionsTrait projections = model.getTrait(ProjectionsTrait.class);
        if (projections == null) {
            projections = new ProjectionsTrait(model.getOriginalAst().expression, autoProjectios);
        } else {
            List<ModelSymbol> composite = new ArrayList<ModelSymbol>();
            composite.addAll(projections.getProjections());
            composite.addAll(autoProjectios);
            projections = new ProjectionsTrait(model.getOriginalAst().expression, composite);
        }
        model.putTrait(ProjectionsTrait.class, projections);
    }

    private List<ModelSymbol> collectProjections(DmdlSemantics environment, ModelDeclaration model) {
        assert environment != null;
        assert model != null;
        Map<String, Type> properties = new HashMap<String, Type>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            properties.put(property.getName().identifier, property.getType());
        }

        Set<String> saw = new HashSet<String>();
        saw.add(model.getName().identifier);
        ProjectionsTrait projections = model.getTrait(ProjectionsTrait.class);
        if (projections != null) {
            for (ModelSymbol symbol : projections.getProjections()) {
                saw.add(symbol.getName().identifier);
            }
        }

        List<ModelSymbol> autoProjectios = new ArrayList<ModelSymbol>();
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
