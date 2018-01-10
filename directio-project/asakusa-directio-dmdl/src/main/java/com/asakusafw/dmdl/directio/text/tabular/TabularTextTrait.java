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
package com.asakusafw.dmdl.directio.text.tabular;

import static com.asakusafw.dmdl.directio.text.TextFormatConstants.*;

import java.util.Optional;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.directio.text.AttributeAnalyzer;
import com.asakusafw.dmdl.directio.text.EscapeSettings;
import com.asakusafw.dmdl.directio.text.TextFieldSettings;
import com.asakusafw.dmdl.directio.text.TextFormatSettings;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.Trait;
import com.asakusafw.runtime.io.text.tabular.TabularTextFormat;

/**
 * Attributes of tabular text models.
 * @since 0.9.1
 */
public class TabularTextTrait implements Trait<TabularTextTrait> {

    static final char DEFAULT_FIELD_SEPARATOR = TabularTextFormat.DEFAULT_FIELD_SEPARATOR;

    private final AstAttribute attribute;

    private final TextFormatSettings formatSettings;

    private final EscapeSettings escapeSettings;

    private final TextFieldSettings fieldSettings;

    /**
     * Creates a new instance.
     * @param attribute the original attribute
     * @param formatSettings the format settings
     * @param escapeSettings the escape settings
     * @param fieldSettings the field settings
     */
    public TabularTextTrait(
            AstAttribute attribute,
            TextFormatSettings formatSettings,
            EscapeSettings escapeSettings,
            TextFieldSettings fieldSettings) {
        this.attribute = attribute;
        this.formatSettings = formatSettings;
        this.escapeSettings = escapeSettings;
        this.fieldSettings = fieldSettings;
    }

    static Optional<TabularTextTrait> find(ModelDeclaration declaration) {
        return Optional.ofNullable(declaration.getTrait(TabularTextTrait.class));
    }

    static TabularTextTrait get(ModelDeclaration declaration) {
        return Optional.ofNullable(declaration.getTrait(TabularTextTrait.class))
                .orElseThrow(IllegalStateException::new);
    }

    static void register(DmdlSemantics environment, ModelDeclaration declaration, TabularTextTrait trait) {
        if (find(declaration).isPresent()) {
            environment.report(new Diagnostic(
                    Diagnostic.Level.ERROR, trait.attribute.name,
                    Messages.getString("TabularTextTrait.diagnosticDuplicateAttribute"), //$NON-NLS-1$
                    trait.attribute.name,
                    declaration.getName().identifier));
        } else {
            declaration.putTrait(TabularTextTrait.class, trait);
        }
    }

    @Override
    public AstAttribute getOriginalAst() {
        return attribute;
    }

    /**
     * Returns the format settings.
     * @return the format settings
     */
    public TextFormatSettings getFormatSettings() {
        return formatSettings;
    }

    /**
     * Returns the escape settings.
     * @return the escape settings
     */
    public EscapeSettings getEscapeSettings() {
        return escapeSettings;
    }

    /**
     * Returns the field settings.
     * @return the field settings
     */
    public TextFieldSettings getFieldSettings() {
        return fieldSettings;
    }

    boolean verify(DmdlSemantics environment, ModelDeclaration declaration) {
        boolean valid = true;
        valid &= formatSettings.verify(environment, attribute);
        valid &= escapeSettings.verify(environment, attribute);
        valid &= fieldSettings.verify(environment, attribute);
        if (valid == false) {
            return false;
        }
        AttributeAnalyzer analyzer = new AttributeAnalyzer(environment, attribute);
        if (escapeSettings.getCharacter().isPresent()) {
            char fieldSeparator = formatSettings.getFieldSeparator().orElse(DEFAULT_FIELD_SEPARATOR);
            char escapeCharacter = escapeSettings.getCharacter().getEntity();
            if (fieldSeparator == escapeCharacter) {
                analyzer.error(
                        escapeSettings.getCharacter().getDeclaration(),
                        Messages.getString("TabularTextTrait.diagnosticConflictCharacter"), //$NON-NLS-1$
                        ELEMENT_FIELD_SEPARATOR);
            }
        }
        return analyzer.hasError() == false;
    }
}
