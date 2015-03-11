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
package com.asakusafw.dmdl.directio.csv.driver;

import java.text.SimpleDateFormat;
import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.directio.csv.driver.CsvFormatTrait.Configuration;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;
import com.asakusafw.runtime.io.csv.CsvConfiguration;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;

/**
 * Processes <code>&#64;directio.csv</code> attributes.
<h2>'&#64;directio.csv' attribute</h2>
The attributed declaration can have:
<ul>
<li> with {@code charset=[string-literal]} as charset name (default: UTF-8) </li>
<li> with {@code has_header=TRUE|FALSE} as whether header is required (default: FALSE) </li>
<li> with {@code allow_linefeed=TRUE|FALSE} as whether each field can contain linefeed (default: FALSE) </li>
<li>
    with {@code true=[string-literal]} as {@code "true"} representation
    (default: {@link CsvConfiguration#DEFAULT_TRUE_FORMAT})
</li>
<li>
    with {@code false=[string-literal]} as {@code "false"} representation
    (default: {@link CsvConfiguration#DEFAULT_FALSE_FORMAT})
</li>
<li>
    with {@code date=[string-literal]} as {@link Date} format
    (default: {@link CsvConfiguration#DEFAULT_DATE_FORMAT})
</li>
<li>
    with {@code datetime=[string-literal]} as {@link DateTime} format
    (default: {@link CsvConfiguration#DEFAULT_DATE_TIME_FORMAT})
</li>
<li> with {@code compression=[string-literal]} as compression name (default: plain) </li>
</ul>
 * @since 0.2.5
 * @version 0.5.2
 */
public class CsvFormatDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "directio.csv"; //$NON-NLS-1$

    /**
     * The element name of charset name.
     */
    public static final String ELEMENT_CHARSET_NAME = "charset"; //$NON-NLS-1$

    /**
     * The element name of whether header is required.
     */
    public static final String ELEMENT_HAS_HEADER_NAME = "has_header"; //$NON-NLS-1$

    /**
     * The element name of whether value can contain linefeed.
     */
    public static final String ELEMENT_ALLOW_LINEFEED = "allow_linefeed"; //$NON-NLS-1$

    /**
     * The element name of codec name.
     * @since 0.5.2
     */
    public static final String ELEMENT_CODEC_NAME = "compression"; //$NON-NLS-1$

    /**
     * The element name of {@code true} representation.
     */
    public static final String ELEMENT_TRUE_NAME = "true"; //$NON-NLS-1$

    /**
     * The element name of {@code false} representation.
     */
    public static final String ELEMENT_FALSE_NAME = "false"; //$NON-NLS-1$

    /**
     * The element name of {@link Date} representation.
     */
    public static final String ELEMENT_DATE_NAME = "date"; //$NON-NLS-1$

    /**
     * The element name of {@link DateTime} representation.
     */
    public static final String ELEMENT_DATE_TIME_NAME = "datetime"; //$NON-NLS-1$

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        Configuration conf = analyzeConfig(environment, attribute, elements);
        if (conf != null) {
            declaration.putTrait(CsvFormatTrait.class, new CsvFormatTrait(attribute, conf));
        }
    }

    private Configuration analyzeConfig(
            DmdlSemantics environment,
            AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        AstLiteral charset = take(environment, elements, ELEMENT_CHARSET_NAME, LiteralKind.STRING);
        AstLiteral header = take(environment, elements, ELEMENT_HAS_HEADER_NAME, LiteralKind.BOOLEAN);
        AstLiteral allowlf = take(environment, elements, ELEMENT_ALLOW_LINEFEED, LiteralKind.BOOLEAN);
        AstLiteral trueRep = take(environment, elements, ELEMENT_TRUE_NAME, LiteralKind.STRING);
        AstLiteral falseRep = take(environment, elements, ELEMENT_FALSE_NAME, LiteralKind.STRING);
        AstLiteral dateFormat = take(environment, elements, ELEMENT_DATE_NAME, LiteralKind.STRING);
        AstLiteral dateTimeFormat = take(environment, elements, ELEMENT_DATE_TIME_NAME, LiteralKind.STRING);
        AstLiteral codec = take(environment, elements, ELEMENT_CODEC_NAME, LiteralKind.STRING);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));

        Configuration result = new Configuration();
        if (charset != null && checkNotEmpty(environment, ELEMENT_CHARSET_NAME, charset)) {
            result.setCharsetName(charset.toStringValue());
        }
        if (header != null) {
            result.setEnableHeader(header.toBooleanValue());
        }
        if (allowlf != null) {
            result.setAllowLinefeed(allowlf.toBooleanValue());
        }
        if (trueRep != null && checkNotEmpty(environment, ELEMENT_TRUE_NAME, trueRep)) {
            result.setTrueFormat(trueRep.toStringValue());
        }
        if (falseRep != null && checkNotEmpty(environment, ELEMENT_FALSE_NAME, falseRep)) {
            result.setFalseFormat(falseRep.toStringValue());
        }
        if (dateFormat != null && checkDateFormat(environment, ELEMENT_DATE_NAME, dateFormat)) {
            result.setDateFormat(dateFormat.toStringValue());
        }
        if (dateTimeFormat != null && checkDateFormat(environment, ELEMENT_DATE_TIME_NAME, dateTimeFormat)) {
            result.setDateTimeFormat(dateTimeFormat.toStringValue());
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
                    Messages.getString("CsvFormatDriver.diagnosticEmptyElement"), //$NON-NLS-1$
                    TARGET_NAME,
                    name));
            return false;
        }
        return true;
    }

    private boolean checkDateFormat(DmdlSemantics environment, String name, AstLiteral stringLiteral) {
        assert environment != null;
        assert name != null;
        assert stringLiteral != null;
        assert stringLiteral.kind == LiteralKind.STRING;
        if (checkNotEmpty(environment, name, stringLiteral) == false) {
            return false;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(stringLiteral.toStringValue());
            format.format(new java.util.Date());
        } catch (IllegalArgumentException e) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    stringLiteral,
                    Messages.getString("CsvFormatDriver.diagnosticInvalidDateFormat"), //$NON-NLS-1$
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
                    Messages.getString("CsvFormatDriver.diagnosticInvalidLiteralKind"), //$NON-NLS-1$
                    TARGET_NAME,
                    elementName,
                    kind));
            return null;
        } else {
            AstLiteral literal = (AstLiteral) element.value;
            if (literal.kind != kind) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        element,
                        Messages.getString("CsvFormatDriver.diagnosticInvalidLiteralKind"), //$NON-NLS-1$
                        TARGET_NAME,
                        elementName,
                        kind));
                return null;
            }
            return literal;
        }
    }
}
