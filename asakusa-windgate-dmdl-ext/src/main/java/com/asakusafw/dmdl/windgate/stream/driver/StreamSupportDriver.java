/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.dmdl.windgate.stream.driver;

import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.Declaration;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.spi.AttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;windgate.stream_format</code> attributes.
<h2>'&#64;windgate.stream_support' attribute</h2>
The attributed declaration must be:
<ul>
<li> with type={@code "<format-name>"} </li>
</ul>
 * @since 0.2.2
 */
public class StreamSupportDriver extends AttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "windgate.stream_format";

    /**
     * The element name.
     */
    public static final String ELEMENT_NAME = "type";

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(
            DmdlSemantics environment,
            Declaration declaration,
            AstAttribute attribute) {
        assert attribute.name.toString().equals(TARGET_NAME);
        if ((declaration instanceof ModelDeclaration) == false) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    declaration.getOriginalAst(),
                    "@{0} is for only for models",
                    TARGET_NAME));
            return;
        }
        String value = getString(environment, attribute);
        if (value != null) {
            declaration.putTrait(
                    StreamSupportTrait.class,
                    new StreamSupportTrait(attribute, value));
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

    @Override
    public String toString() {
        return StreamSupportDriver.class.getSimpleName();
    }
}
