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
package com.asakusafw.dmdl.thundergate.driver;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstAttributeValue;
import com.asakusafw.dmdl.model.AstAttributeValueArray;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;
import com.asakusafw.utils.collections.Lists;

/**
 * Processes <code>&#64;thundergate.primary_key</code> attributes.
<h2>'&#64;thundergate.primary_key' attribute</h2>
The attributed declaration must be:
<ul>
<li> a model attribute </li>
<li> value=[array of property symbols] </li>
</ul>
 */
public class PrimaryKeyDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "thundergate.primary_key";

    /**
     * The element name.
     */
    public static final String ELEMENT_NAME = "value";

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(
            DmdlSemantics environment,
            ModelDeclaration declaration,
            AstAttribute attribute) {
        List<PropertySymbol> properties = getProperties(environment, declaration, attribute);
        declaration.putTrait(
                PrimaryKeyTrait.class,
                new PrimaryKeyTrait(attribute, properties));
    }

    private List<PropertySymbol> getProperties(
            DmdlSemantics environment,
            ModelDeclaration declaration,
            AstAttribute attribute) {
        assert environment != null;
        assert declaration != null;
        assert attribute != null;
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        AstAttributeElement nameElement = elements.remove(ELEMENT_NAME);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
        if (nameElement == null) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    attribute.name,
                    "@{0} must declare the element \"{1}=...\"",
                    TARGET_NAME,
                    ELEMENT_NAME));
            return Collections.emptyList();
        } else if ((nameElement.value instanceof AstAttributeValueArray) == false) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    nameElement,
                    "@{0}.{1} must be an array of name",
                    TARGET_NAME,
                    ELEMENT_NAME));
            return Collections.emptyList();
        }
        AstAttributeValueArray array = (AstAttributeValueArray) nameElement.value;
        List<PropertySymbol> properties = Lists.create();
        for (AstAttributeValue value : array.elements) {
            if ((value instanceof AstSimpleName) == false) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        value,
                        "{0}.{1} must be a simple property name of array",
                        TARGET_NAME,
                        ELEMENT_NAME));
                continue;
            }
            PropertySymbol property = declaration.createPropertySymbol((AstSimpleName) value);
            if (property.findDeclaration() == null) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        value,
                        "{0} is not declared in {1}",
                        value,
                        declaration.getName()));
                continue;
            }
            properties.add(property);
        }
        return properties;
    }
}
