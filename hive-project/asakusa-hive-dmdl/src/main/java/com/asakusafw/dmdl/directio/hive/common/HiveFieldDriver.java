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
package com.asakusafw.dmdl.directio.hive.common;

import java.util.Map;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.PropertyAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.hive.field</code> attributes.
<h2>'&#64;directio.hive.field' attribute</h2>
The attributed declaration must be:
<ul>
<li> with name=[string-literal] (default: property name)</li>
</ul>
 * @since 0.7.0
 */
public class HiveFieldDriver extends PropertyAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "directio.hive.field"; //$NON-NLS-1$

    /**
     * The element name.
     */
    public static final String ELEMENT_NAME = "name"; //$NON-NLS-1$

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        HiveFieldTrait trait = HiveFieldTrait.get(declaration);
        trait.setOriginalAst(attribute, true);
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        analyzeElements(environment, attribute, elements, trait);
    }

    private void analyzeElements(
            DmdlSemantics environment,
            AstAttribute attribute,
            Map<String, AstAttributeElement> elements,
            HiveFieldTrait trait) {
        AstLiteral name =
                AttributeUtil.takeLiteral(environment, attribute, elements, ELEMENT_NAME, LiteralKind.STRING, true);
        if (name != null) {
            String value = name.toStringValue();
            if (AttributeUtil.checkPresent(environment, name, label(ELEMENT_NAME), value)) {
                trait.setColumnName(value);
            }
        }
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
    }

    private static String label(String key) {
        return String.format("@%s(%s)", TARGET_NAME, key); //$NON-NLS-1$
    }
}
