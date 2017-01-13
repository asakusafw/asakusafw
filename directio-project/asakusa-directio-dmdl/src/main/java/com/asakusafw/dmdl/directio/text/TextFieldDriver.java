/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.text;

import static com.asakusafw.dmdl.directio.text.TextFormatConstants.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.directio.util.Value;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.PropertyAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.text.field</code> attributes.
 * @since 0.9.1
 * @see TextFormatConstants
 */
public class TextFieldDriver extends PropertyAttributeDriver {

    @Override
    public String getTargetName() {
        return ATTRIBUTE_FIELD;
    }

    @Override
    public void process(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        String name = AttributeUtil.takeString(environment, attribute, elements, ELEMENT_FIELD_NAME, false);
        TextFieldSettings settings = TextFieldSettings.consume(environment, attribute, elements);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
        TextFieldTrait trait = new TextFieldTrait(attribute, name, settings);
        if (trait.verify(environment, declaration)) {
            TextFieldTrait.register(environment, declaration, attribute, trait);
        }
    }

    @Override
    public void verify(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        TextFieldSettings settings = TextFieldTrait.getSettings(declaration);
        requireType(environment, settings.getTrueFormat(), declaration, BasicTypeKind.BOOLEAN);
        requireType(environment, settings.getFalseFormat(), declaration, BasicTypeKind.BOOLEAN);
        requireType(environment, settings.getNumberFormat(), declaration,
                BasicTypeKind.BYTE, BasicTypeKind.SHORT, BasicTypeKind.INT, BasicTypeKind.LONG,
                BasicTypeKind.FLOAT, BasicTypeKind.DOUBLE,
                BasicTypeKind.DECIMAL);
        requireType(environment, settings.getDecimalOutputStyle(), declaration, BasicTypeKind.DECIMAL);
        requireType(environment, settings.getDateFormat(), declaration, BasicTypeKind.DATE);
        requireType(environment, settings.getDateTimeFormat(), declaration, BasicTypeKind.DATETIME);
    }

    private void requireType(
            DmdlSemantics environment,
            Value<?> value, PropertyDeclaration property, BasicTypeKind... kinds) {
        if (value.isPresent()) {
            AstAttributeElement element = value.getDeclaration();
            assert element != null;
            if (Arrays.stream(kinds)
                    .anyMatch(kind -> AttributeUtil.hasFieldType(property, kind))) {
                environment.report(new Diagnostic(
                        Diagnostic.Level.WARN, element.name,
                        Messages.getString("TextFieldDriver.diagnosticInvalidType"), //$NON-NLS-1$
                        getTargetName(),
                        element.name.identifier,
                        property.getOwner().getName().identifier,
                        property.getName().identifier,
                        kinds.length == 1 ? kinds[0] : Arrays.stream(kinds)
                                .map(Object::toString)
                                .collect(Collectors.joining(",", "{", "}")))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }
}
