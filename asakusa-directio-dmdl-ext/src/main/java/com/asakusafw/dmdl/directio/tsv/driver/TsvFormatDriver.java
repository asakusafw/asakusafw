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
package com.asakusafw.dmdl.directio.tsv.driver;

import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.directio.tsv.driver.TsvFormatTrait.Configuration;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.tsv</code> attributes.
<h2>'&#64;directio.tsv' attribute</h2>
The attributed declaration can have:
<ul>
<li> with {@code charset=[string-literal]} as charset name (default: UTF-8) </li>
</ul>
 * @since 0.5.0
 */
public class TsvFormatDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "directio.tsv";

    /**
     * The element name of charset name.
     */
    public static final String ELEMENT_CHARSET_NAME = "charset";

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        Configuration conf = analyzeConfig(environment, attribute, elements);
        if (conf != null) {
            declaration.putTrait(TsvFormatTrait.class, new TsvFormatTrait(attribute, conf));
        }
    }

    private Configuration analyzeConfig(
            DmdlSemantics environment,
            AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        AstLiteral charset = take(environment, elements, ELEMENT_CHARSET_NAME, LiteralKind.STRING);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));

        Configuration result = new Configuration();
        if (charset != null && checkNotEmpty(environment, ELEMENT_CHARSET_NAME, charset)) {
            result.setCharsetName(charset.toStringValue());
        }
        return result;
    }

    private boolean checkNotEmpty(DmdlSemantics environment, String name, AstLiteral stringLiteral) {
        assert environment != null;
        assert name != null;
        assert stringLiteral != null;
        assert stringLiteral.kind == LiteralKind.STRING;
        if (stringLiteral.toStringValue().isEmpty()) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    stringLiteral,
                    "@{0}({1}) must not be empty",
                    TARGET_NAME,
                    name));
            return false;
        }
        return true;
    }

    private AstLiteral take(
            DmdlSemantics environment,
            Map<String, AstAttributeElement> elements,
            String elementName,
            LiteralKind kind) {
        assert environment != null;
        assert elements != null;
        assert elementName != null;
        assert kind != null;
        AstAttributeElement element = elements.remove(elementName);
        if (element == null) {
            return null;
        } else if ((element.value instanceof AstLiteral) == false) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    element,
                    "@{0}({1}) must be a string literal",
                    TARGET_NAME,
                    elementName));
            return null;
        } else {
            AstLiteral literal = (AstLiteral) element.value;
            if (literal.kind != kind) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        element,
                        "@{0}({1}) must be a string literal",
                        TARGET_NAME,
                        elementName));
                return null;
            }
            return literal;
        }
    }
}
