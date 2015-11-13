/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.line.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.directio.line.driver.LineFormatTrait.Configuration;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.line</code> attributes.
<h2>'&#64;directio.line' attribute</h2>
The attributed declaration can have:
<ul>
<li> with {@code charset=[string-literal]} as charset name (default: UTF-8) </li>
<li> with {@code compression=[string-literal]} as compression name (default: plain) </li>
</ul>
 * @since 0.7.5
 */
public class LineFormatDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "directio.line"; //$NON-NLS-1$

    /**
     * The element name of charset name.
     */
    public static final String ELEMENT_CHARSET_NAME = "charset"; //$NON-NLS-1$

    /**
     * The element name of codec name.
     */
    public static final String ELEMENT_CODEC_NAME = "compression"; //$NON-NLS-1$

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        Configuration conf = analyzeConfig(environment, attribute, elements);
        if (conf != null) {
            declaration.putTrait(LineFormatTrait.class, new LineFormatTrait(attribute, conf));
        }
    }

    private Configuration analyzeConfig(
            DmdlSemantics environment,
            AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        AstLiteral charset = take(environment, elements, ELEMENT_CHARSET_NAME, LiteralKind.STRING);
        AstLiteral codec = take(environment, elements, ELEMENT_CODEC_NAME, LiteralKind.STRING);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));

        Configuration result = new Configuration();
        if (charset != null && checkNotEmpty(environment, ELEMENT_CHARSET_NAME, charset)) {
            result.setCharsetName(charset.toStringValue());
        }
        if (codec != null && checkNotEmpty(environment, ELEMENT_CODEC_NAME, codec)) {
            result.setCodecName(codec.toStringValue());
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
                    Messages.getString("LineFormatDriver.errorEmptyString"), //$NON-NLS-1$
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
                    Messages.getString("LineFormatDriver.errorNotString"), //$NON-NLS-1$
                    TARGET_NAME,
                    elementName));
            return null;
        } else {
            AstLiteral literal = (AstLiteral) element.value;
            if (literal.kind != kind) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        element,
                        Messages.getString("LineFormatDriver.errorNotString"), //$NON-NLS-1$
                        TARGET_NAME,
                        elementName));
                return null;
            }
            return literal;
        }
    }

    @Override
    public void verify(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        processImplicitBody(environment, declaration, attribute);
    }

    private void processImplicitBody(DmdlSemantics environment, ModelDeclaration model, AstAttribute attribute) {
        List<PropertyDeclaration> bodies = new ArrayList<PropertyDeclaration>();
        List<PropertyDeclaration> implicits = new ArrayList<PropertyDeclaration>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            switch (LineFieldTrait.getKind(property)) {
            case BODY:
                bodies.add(property);
                break;
            case IGNORE:
                Type type = property.getType();
                if (type instanceof BasicType) {
                    if (((BasicType) type).getKind() == BasicTypeKind.TEXT) {
                        implicits.add(property);
                    }
                }
                break;
            default:
                break;
            }
        }
        if (bodies.size() == 1) {
            // has explicit body property
            return;
        } else if (bodies.size() > 1) {
            // has multiple body properties
            environment.report(new Diagnostic(
                    Level.ERROR,
                    attribute.getRegion(),
                    Messages.getString("LineFormatDriver.errorDuplicateBody"), //$NON-NLS-1$
                    LineBodyDriver.TARGET_NAME,
                    model.getName().identifier));
        } else {
            // missing body properties
            if (implicits.size() == 1) {
                // has trivial body property
                PropertyDeclaration implicit = implicits.get(0);
                implicit.putTrait(LineFieldTrait.class, new LineFieldTrait(null, LineFieldTrait.Kind.BODY));
            } else {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        attribute.getRegion(),
                        Messages.getString("LineFormatDriver.errorMissingBody"), //$NON-NLS-1$
                        LineBodyDriver.TARGET_NAME,
                        model.getName().identifier));
            }
        }
    }
}
