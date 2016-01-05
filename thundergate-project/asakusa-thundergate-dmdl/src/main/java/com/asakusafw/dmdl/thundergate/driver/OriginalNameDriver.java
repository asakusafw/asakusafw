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

import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.Declaration;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.spi.AttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;thundergate.name</code> annotations.
<h2>'&#64;thundergate.name' attribute</h2>
The attributed declaration must be:
<ul>
<li> with value=[string-literal] </li>
</ul>
 */
public class OriginalNameDriver extends AttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "thundergate.name";

    /**
     * The element name.
     */
    public static final String ELEMENT_NAME = "value";

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(DmdlSemantics environment, Declaration declaration, AstAttribute attribute) {
        assert attribute.name.toString().equals(TARGET_NAME);
        String value = getString(environment, attribute);
        if (value != null) {
            declaration.putTrait(
                    OriginalNameTrait.class,
                    new OriginalNameTrait(attribute, value));
        }
    }

    private String getString(DmdlSemantics environment, AstAttribute attribute) {
        assert environment != null;
        assert attribute != null;
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        AstAttributeElement target = elements.remove(ELEMENT_NAME);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
        if (target == null) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    attribute.name,
                    "@{0} must declare an element \"{1}=...\"",
                    TARGET_NAME,
                    ELEMENT_NAME));
            return null;
        } else if ((target.value instanceof AstLiteral) == false) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    target,
                    "@{0}.{1} must be a string literal",
                    TARGET_NAME,
                    ELEMENT_NAME));
            return null;
        } else {
            AstLiteral literal = (AstLiteral) target.value;
            if (literal.kind != LiteralKind.STRING) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        target,
                        "@{0}.{1} must be a string literal",
                        TARGET_NAME,
                        ELEMENT_NAME));
                return null;
            }
            return literal.toStringValue();
        }
    }
}
