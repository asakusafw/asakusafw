/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import java.util.Map;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.PropertyAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.text.file_name</code> attributes.
 * @since 0.9.1
 * @see TextFormatConstants
 */
public class TextFileNameDriver extends PropertyAttributeDriver {

    @Override
    public String getTargetName() {
        return ATTRIBUTE_FILE_NAME_FIELD;
    }

    @Override
    public void process(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
        TextFieldTrait.register(
                environment, declaration, attribute,
                new TextFieldTrait(attribute, TextFieldTrait.Kind.FILE_NAME));
    }

    @Override
    public void verify(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        AttributeUtil.checkFieldType(environment, declaration, attribute, BasicTypeKind.TEXT);
    }
}
