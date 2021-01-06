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
package com.asakusafw.dmdl.directio.text.csv;

import static com.asakusafw.dmdl.directio.text.TextFormatConstants.*;

import java.util.Optional;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.directio.text.QuoteSettings;
import com.asakusafw.dmdl.directio.text.TextFieldSettings;
import com.asakusafw.dmdl.directio.text.TextFormatSettings;
import com.asakusafw.dmdl.directio.util.AttributeAnalyzer;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.Trait;
import com.asakusafw.runtime.io.text.csv.CsvTextFormat;

/**
 * Attributes of CSV models.
 * @since 0.9.1
 */
public class CsvTextTrait implements Trait<CsvTextTrait> {

    static final char DEFAULT_FIELD_SEPARATOR = CsvTextFormat.DEFAULT_FIELD_SEPARATOR;

    private final AstAttribute attribute;

    private final TextFormatSettings formatSettings;

    private final QuoteSettings quoteSettings;

    private final TextFieldSettings fieldSettings;

    /**
     * Creates a new instance.
     * @param attribute the original attribute
     * @param formatSettings the format settings
     * @param quoteSettings the quote settings
     * @param fieldSettings the field settings
     */
    public CsvTextTrait(
            AstAttribute attribute,
            TextFormatSettings formatSettings,
            QuoteSettings quoteSettings,
            TextFieldSettings fieldSettings) {
        this.attribute = attribute;
        this.formatSettings = formatSettings;
        this.quoteSettings = quoteSettings;
        this.fieldSettings = fieldSettings;
    }

    static Optional<CsvTextTrait> find(ModelDeclaration declaration) {
        return Optional.ofNullable(declaration.getTrait(CsvTextTrait.class));
    }

    static CsvTextTrait get(ModelDeclaration declaration) {
        return Optional.ofNullable(declaration.getTrait(CsvTextTrait.class))
                .orElseThrow(IllegalStateException::new);
    }

    static void register(DmdlSemantics environment, ModelDeclaration declaration, CsvTextTrait trait) {
        if (find(declaration).isPresent()) {
            environment.report(new Diagnostic(
                    Diagnostic.Level.ERROR, trait.attribute.name,
                    Messages.getString("CsvTextTrait.diagnosticDuplicateAttribute"), //$NON-NLS-1$
                    trait.attribute.name,
                    declaration.getName().identifier));
        } else {
            declaration.putTrait(CsvTextTrait.class, trait);
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
     * Returns the quote settings.
     * @return the quote settings
     */
    public QuoteSettings getQuoteSettings() {
        return quoteSettings;
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
        valid &= quoteSettings.verify(environment, attribute);
        valid &= fieldSettings.verify(environment, attribute);
        if (valid == false) {
            return false;
        }
        AttributeAnalyzer analyzer = new AttributeAnalyzer(environment, attribute);
        if (quoteSettings.getCharacter().isPresent()) {
            char fieldSeparator = formatSettings.getFieldSeparator().orElse(DEFAULT_FIELD_SEPARATOR);
            char quoteCharacter = quoteSettings.getCharacter().getEntity();
            if (fieldSeparator == quoteCharacter) {
                analyzer.error(
                        quoteSettings.getCharacter().getDeclaration(),
                        Messages.getString("CsvTextTrait.diagnosticConflictCharacter"), //$NON-NLS-1$
                        ELEMENT_FIELD_SEPARATOR);
            }
        }
        return analyzer.hasError() == false;
    }
}
