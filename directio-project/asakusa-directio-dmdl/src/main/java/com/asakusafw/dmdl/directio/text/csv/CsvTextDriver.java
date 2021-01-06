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

import java.util.Map;
import java.util.function.Function;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.directio.text.QuoteSettings;
import com.asakusafw.dmdl.directio.text.TextFieldSettings;
import com.asakusafw.dmdl.directio.text.TextFieldTrait;
import com.asakusafw.dmdl.directio.text.TextFormatConstants;
import com.asakusafw.dmdl.directio.text.TextFormatSettings;
import com.asakusafw.dmdl.directio.util.Value;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.text.csv</code> attributes.
 * @since 0.9.1
 * @see TextFormatConstants
 */
public class CsvTextDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String NAME = PREFIX_NAMESPACE + "csv"; //$NON-NLS-1$

    @Override
    public String getTargetName() {
        return NAME;
    }

    @Override
    public void process(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        TextFormatSettings format = TextFormatSettings.consume(environment, attribute, elements);
        QuoteSettings quote = QuoteSettings.consume(environment, attribute, elements);
        TextFieldSettings field = TextFieldSettings.consume(environment, attribute, elements);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
        CsvTextTrait trait = new CsvTextTrait(attribute, format, quote, field);
        if (trait.verify(environment, declaration)) {
            CsvTextTrait.register(environment, declaration, trait);
        }
    }

    @Override
    public void verify(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        CsvTextTrait parent = CsvTextTrait.get(declaration);
        verifyModel(environment, declaration, parent);
        declaration.getDeclaredProperties().stream()
            .filter(p -> TextFieldTrait.find(p).isPresent())
            .filter(p -> TextFieldTrait.get(p).getKind() == TextFieldTrait.Kind.VALUE)
            .forEachOrdered(property -> verifyField(
                    environment,
                    property,
                    parent.getFieldSettings(),
                    TextFieldTrait.get(property).getSettings()));
    }

    private static void verifyModel(DmdlSemantics environment, ModelDeclaration declaration, CsvTextTrait trait) {
        if (declaration.getDeclaredProperties().stream()
                .noneMatch(p -> TextFieldTrait.getKind(p) == TextFieldTrait.Kind.VALUE)) {
            environment.report(new Diagnostic(
                    Diagnostic.Level.ERROR, trait.getOriginalAst().name,
                    Messages.getString("CsvTextDriver.diagnosticNoAvailableField"), //$NON-NLS-1$
                    declaration.getName(),
                    trait.getOriginalAst().name));
        }
    }

    private static void verifyField(
            DmdlSemantics environment, PropertyDeclaration property,
            TextFieldSettings parent, TextFieldSettings child) {
        if (AttributeUtil.hasFieldType(property, BasicTypeKind.BOOLEAN)) {
            String nullFormat = resolve(parent, child, TextFieldSettings::getNullFormat, DEFAULT_NULL_FORMAT);
            String trueFormat = resolve(parent, child, TextFieldSettings::getTrueFormat, DEFAULT_TRUE_FORMAT);
            String falseFormat = resolve(parent, child, TextFieldSettings::getFalseFormat, DEFAULT_FALSE_FORMAT);
            if (trueFormat.equals(nullFormat)) {
                errorConflict(environment, property, ELEMENT_TRUE_FORMAT, ELEMENT_NULL_FORMAT);
            }
            if (falseFormat.equals(nullFormat)) {
                errorConflict(environment, property, ELEMENT_FALSE_FORMAT, ELEMENT_NULL_FORMAT);
            }
            if (falseFormat.equals(trueFormat)) {
                errorConflict(environment, property, ELEMENT_FALSE_FORMAT, ELEMENT_TRUE_FORMAT);
            }
        }
    }

    private static void errorConflict(
            DmdlSemantics environment,
            PropertyDeclaration property,
            String a, String b) {
        environment.report(new Diagnostic(
                Diagnostic.Level.ERROR, property.getName(),
                Messages.getString("CsvTextDriver.diagnosticConflictFormat"), //$NON-NLS-1$
                property.getOwner().getName(),
                property.getName(),
                a, b));
    }

    private static <T> T resolve(
            TextFieldSettings parent, TextFieldSettings child,
            Function<TextFieldSettings, Value<T>> property, T defaultValue) {
        return property.apply(child).orDefault(property.apply(parent)).orElse(defaultValue);
    }

}
