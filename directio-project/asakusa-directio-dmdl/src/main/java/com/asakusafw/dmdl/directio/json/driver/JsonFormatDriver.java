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
package com.asakusafw.dmdl.directio.json.driver;

import static com.asakusafw.dmdl.directio.json.driver.JsonFormatConstants.*;

import java.util.HashMap;
import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.json</code> attributes.
 * @since 0.10.3
 * @see JsonFormatConstants
 */
public class JsonFormatDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String NAME = BASE_NAMESPACE;

    @Override
    public String getTargetName() {
        return NAME;
    }

    @Override
    public void process(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        JsonStreamSettings stream = JsonStreamSettings.consume(environment, attribute, elements);
        JsonFormatSettings format = JsonFormatSettings.consume(environment, attribute, elements);
        JsonPropertySettings field = JsonPropertySettings.consume(environment, attribute, elements);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
        JsonFormatTrait trait = new JsonFormatTrait(attribute, stream, format, field);
        if (trait.verify(environment, declaration)) {
            JsonFormatTrait.register(environment, declaration, trait);
        }
    }

    @Override
    public void verify(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        JsonFormatTrait parent = JsonFormatTrait.get(declaration);
        verifyModel(environment, declaration, parent);
    }

    private static void verifyModel(DmdlSemantics environment, ModelDeclaration declaration, JsonFormatTrait trait) {
        if (declaration.getDeclaredProperties().stream()
                .noneMatch(p -> JsonPropertyTrait.getKind(p) == JsonPropertyTrait.Kind.VALUE)) {
            environment.report(new Diagnostic(
                    Diagnostic.Level.ERROR, trait.getOriginalAst().name,
                    Messages.getString("JsonFormatDriver.diagnosticNoAvailableProperty"), //$NON-NLS-1$
                    declaration.getName(),
                    trait.getOriginalAst().name));
        }
        Map<String, PropertyDeclaration> properties = new HashMap<>();
        for (PropertyDeclaration property : declaration.getDeclaredProperties()) {
            JsonPropertyTrait info = JsonPropertyTrait.find(property)
                    .filter(it -> it.getKind() == JsonPropertyTrait.Kind.VALUE)
                    .orElse(null);
            if (info == null) {
                continue;
            }
            String jsonName = JsonPropertyTrait.getName(property);
            PropertyDeclaration conflict = properties.putIfAbsent(jsonName, property);
            if (conflict != null) {
                environment.report(new Diagnostic(
                        Diagnostic.Level.ERROR, trait.getOriginalAst().name,
                        Messages.getString("JsonFormatDriver.diagnosticDuplicatePropertyName"), //$NON-NLS-1$
                        jsonName,
                        property.getOwner().getName(),
                        property.getName(),
                        conflict.getOwner().getName(),
                        conflict.getName()));
            }
        }
    }
}
