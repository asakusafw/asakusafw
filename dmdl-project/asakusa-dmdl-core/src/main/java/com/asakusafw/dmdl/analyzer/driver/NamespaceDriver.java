/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.dmdl.analyzer.driver;

import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstName;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.trait.NamespaceTrait;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;namespace</code> annotations.
<h2>'&#64;namespace' attribute</h2>
The attributed declaration must be:
<ul>
<li> a model attribute </li>
</ul>
 */
public class NamespaceDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "namespace";

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
        AstName name = getName(environment, attribute);
        if (name != null) {
            declaration.putTrait(
                    NamespaceTrait.class,
                    new NamespaceTrait(attribute, name));
        }
    }

    private AstName getName(DmdlSemantics environment, AstAttribute attribute) {
        assert environment != null;
        assert attribute != null;
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        AstAttributeElement nameElement = elements.remove(ELEMENT_NAME);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
        if (nameElement == null) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    attribute.name,
                    "@{0} must declare an element \"{1}=...\"",
                    TARGET_NAME,
                    ELEMENT_NAME));
            return null;
        } else if ((nameElement.value instanceof AstName) == false) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    nameElement,
                    "@{0}.{1} must be a name",
                    TARGET_NAME,
                    ELEMENT_NAME));
            return null;
        } else {
            return (AstName) nameElement.value;
        }
    }
}
