/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.util.Optional;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Attributes of JSON models.
 * @since 0.10.3
 */
public class JsonFormatTrait implements Trait<JsonFormatTrait> {

    private final AstAttribute attribute;

    private final JsonStreamSettings streamSettings;

    private final JsonFormatSettings formatSettings;

    private final JsonPropertySettings propertySettings;

    /**
     * Creates a new instance.
     * @param attribute the original attribute
     * @param streamSettings the I/O stream settings
     * @param formatSettings the format settings
     * @param propertySettings the default property settings
     */
    public JsonFormatTrait(
            AstAttribute attribute,
            JsonStreamSettings streamSettings,
            JsonFormatSettings formatSettings,
            JsonPropertySettings propertySettings) {
        this.attribute = attribute;
        this.streamSettings = streamSettings;
        this.formatSettings = formatSettings;
        this.propertySettings = propertySettings;
    }

    static Optional<JsonFormatTrait> find(ModelDeclaration declaration) {
        return Optional.ofNullable(declaration.getTrait(JsonFormatTrait.class));
    }

    static JsonFormatTrait get(ModelDeclaration declaration) {
        return Optional.ofNullable(declaration.getTrait(JsonFormatTrait.class))
                .orElseThrow(IllegalStateException::new);
    }

    static void register(DmdlSemantics environment, ModelDeclaration declaration, JsonFormatTrait trait) {
        if (find(declaration).isPresent()) {
            environment.report(new Diagnostic(
                    Diagnostic.Level.ERROR, trait.attribute.name,
                    Messages.getString("JsonFormatTrait.diagnosticDuplicateAttribute"), //$NON-NLS-1$
                    trait.attribute.name,
                    declaration.getName().identifier));
        } else {
            declaration.putTrait(JsonFormatTrait.class, trait);
        }
    }

    @Override
    public AstAttribute getOriginalAst() {
        return attribute;
    }

    /**
     * Returns the I/O stream settings.
     * @return the I/O stream settings
     */
    public JsonStreamSettings getStreamSettings() {
        return streamSettings;
    }

    /**
     * Returns the format settings.
     * @return the format settings
     */
    public JsonFormatSettings getFormatSettings() {
        return formatSettings;
    }

    /**
     * Returns the property settings.
     * @return the property settings
     */
    public JsonPropertySettings getPropertySettings() {
        return propertySettings;
    }

    boolean verify(DmdlSemantics environment, ModelDeclaration declaration) {
        boolean valid = true;
        valid &= streamSettings.verify(environment, attribute);
        valid &= formatSettings.verify(environment, attribute);
        valid &= propertySettings.verify(environment, attribute);
        return valid;
    }
}
