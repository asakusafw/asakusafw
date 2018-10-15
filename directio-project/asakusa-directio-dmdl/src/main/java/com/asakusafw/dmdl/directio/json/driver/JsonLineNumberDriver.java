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

import java.util.Map;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.PropertyAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.json.line_number</code> attributes.
 * @since 0.10.3
 * @see JsonFormatConstants
 */
public class JsonLineNumberDriver extends PropertyAttributeDriver {

    @Override
    public String getTargetName() {
        return ATTRIBUTE_LINE_NUMBER_FIELD;
    }

    @Override
    public void process(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
        JsonPropertyTrait.register(
                environment, declaration, attribute,
                new JsonPropertyTrait(attribute, JsonPropertyTrait.Kind.LINE_NUMBER));
    }

    @Override
    public void verify(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        AttributeUtil.checkFieldType(environment, declaration, attribute, BasicTypeKind.INT, BasicTypeKind.LONG);
    }
}
