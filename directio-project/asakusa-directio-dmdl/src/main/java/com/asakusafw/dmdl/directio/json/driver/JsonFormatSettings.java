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
package com.asakusafw.dmdl.directio.json.driver;

import static com.asakusafw.dmdl.directio.json.driver.JsonFormatConstants.*;

import java.nio.charset.Charset;
import java.util.Map;

import com.asakusafw.dmdl.directio.json.driver.JsonFormatConstants.DecimalStyle;
import com.asakusafw.dmdl.directio.util.AttributeAnalyzer;
import com.asakusafw.dmdl.directio.util.Value;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.runtime.io.json.ErrorAction;
import com.asakusafw.runtime.io.json.LineSeparator;

/**
 * Settings of JSON format.
 * @since 0.10.3
 */
public class JsonFormatSettings {

    private Value<Charset> charsetName = Value.undefined();

    private Value<LineSeparator> lineSeparator = Value.undefined();

    private Value<DecimalStyle> decimalStyle = Value.undefined();

    private Value<Boolean> escapeNoAscii = Value.undefined();

    private Value<ErrorAction> unknownPropertyAction = Value.undefined();

    /**
     * Returns the charset name.
     * @return the charset name
     */
    public Value<Charset> getCharsetName() {
        return charsetName;
    }

    /**
     * Returns the line separator.
     * @return the line separator
     */
    public Value<LineSeparator> getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Returns the decimal style.
     * @return the decimal style
     */
    public Value<DecimalStyle> getDecimalStyle() {
        return decimalStyle;
    }

    /**
     * Returns the escape no ASCII flag.
     * @return the flag
     */
    public Value<Boolean> getEscapeNoAscii() {
        return escapeNoAscii;
    }

    /**
     * Returns the unknown property action.
     * @return the action
     */
    public Value<ErrorAction> getUnknownPropertyAction() {
        return unknownPropertyAction;
    }

    /**
     * Consumes attribute elements about format settings.
     * @param environment the current environment
     * @param attribute the attribute
     * @param elements the element map to be consumed
     * @return the consumed settings
     */
    public static JsonFormatSettings consume(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        AttributeAnalyzer analyzer = new AttributeAnalyzer(environment, attribute);
        JsonFormatSettings settings = new JsonFormatSettings();
        consumeCharset(settings, analyzer, elements.remove(ELEMENT_CHARSET_NAME));
        consumeLineSeparator(settings, analyzer, elements.remove(ELEMENT_LINE_SEPARATOR));
        consumeDecimalStyle(settings, analyzer, elements.remove(ELEMENT_DECIMAL_STYLE));
        consumeEscapeNoAscii(settings, analyzer, elements.remove(ELEMENT_ESCAPE_NO_ASCII));
        consumeUnknownPropertyAction(settings, analyzer, elements.remove(ELEMENT_UNKNOWN_PROPERTY_ACTION));
        return settings;
    }

    private static void consumeCharset(
            JsonFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.charsetName = analyzer.toCharset(element);
        }
    }

    private static void consumeLineSeparator(
            JsonFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.lineSeparator = analyzer.toEnumConstant(element, LineSeparator.class);
        }
    }

    private static void consumeDecimalStyle(
            JsonFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.decimalStyle = analyzer.toEnumConstant(element, DecimalStyle.class);
        }
    }

    private static void consumeEscapeNoAscii(
            JsonFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.escapeNoAscii = analyzer.toBoolean(element);
        }
    }

    private static void consumeUnknownPropertyAction(
            JsonFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.unknownPropertyAction = analyzer.toEnumConstant(element, ErrorAction.class);
        }
    }

    /**
     * Verifies this settings.
     * @param environment the current environment
     * @param attribute the original attribute
     * @return {@code true} if the settings seems valid, otherwise {@code false}
     */
    public boolean verify(DmdlSemantics environment, AstAttribute attribute) {
        return true;
    }
}
