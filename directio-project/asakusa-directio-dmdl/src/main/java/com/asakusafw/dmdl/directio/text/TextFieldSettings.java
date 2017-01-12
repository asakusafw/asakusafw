/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import com.asakusafw.dmdl.directio.util.ClassName;
import com.asakusafw.dmdl.directio.util.DatePattern;
import com.asakusafw.dmdl.directio.util.Value;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.runtime.io.text.csv.QuoteStyle;
import com.asakusafw.runtime.io.text.driver.ErrorAction;
import com.asakusafw.runtime.io.text.value.DecimalOptionFieldAdapter;

/**
 * Settings of text fields.
 * @since 0.9.1
 */
public class TextFieldSettings {

    private Value<ClassName> adapterClass = Value.undefined();

    private Value<String> nullFormat = Value.undefined();

    private Value<String> trueFormat = Value.undefined();

    private Value<String> falseFormat = Value.undefined();

    private Value<DatePattern> dateFormat = Value.undefined();

    private Value<DatePattern> dateTimeFormat = Value.undefined();

    private Value<DecimalOptionFieldAdapter.OutputStyle> decimalFormat = Value.undefined();

    private Value<Boolean> trimInputWhitespaces = Value.undefined();

    private Value<Boolean> skipEmptyInput = Value.undefined();

    private Value<ErrorAction> malformedInputAction = Value.undefined();

    private Value<ErrorAction> unmappableOutputAction = Value.undefined();

    private Value<QuoteStyle> quoteStyle = Value.undefined();

    /**
     * Returns the field adapter class.
     * @return the adapter
     */
    public Value<ClassName> getAdapterClass() {
        return adapterClass;
    }

    /**
     * Returns the string representation of {@code null}.
     * @return the string representation of {@code null}
     */
    public Value<String> getNullFormat() {
        return nullFormat;
    }

    /**
     * Returns the string representation of {@code true}.
     * @return the string representation of {@code true}
     */
    public Value<String> getTrueFormat() {
        return trueFormat;
    }

    /**
     * Returns the string representation of {@code false}.
     * @return the string representation of {@code false}
     */
    public Value<String> getFalseFormat() {
        return falseFormat;
    }

    /**
     * Returns the string representation of {@code DATE}.
     * @return the string representation of {@code DATE}
     */
    public Value<DatePattern> getDateFormat() {
        return dateFormat;
    }

    /**
     * Returns the string representation of {@code DATETIME}.
     * @return the string representation of {@code DATETIME}
     */
    public Value<DatePattern> getDateTimeFormat() {
        return dateTimeFormat;
    }

    /**
     * Returns the decimal output style.
     * @return the decimal output style
     */
    public Value<DecimalOptionFieldAdapter.OutputStyle> getDecimalFormat() {
        return decimalFormat;
    }

    /**
     * Returns whether or not trims input field leading/trailing white-space characters.
     * @return {@code true} if trims input field leading/trailing white-space characters, otherwise {@code false}
     */
    public Value<Boolean> getTrimInputWhitespaces() {
        return trimInputWhitespaces;
    }

    /**
     * Returns whether or not skips empty input field.
     * @return {@code true} if skips empty input field, otherwise {@code false}
     */
    public Value<Boolean> getSkipEmptyInput() {
        return skipEmptyInput;
    }

    /**
     * Returns the error action of malformed inputs.
     * @return the error action of malformed inputs
     */
    public Value<ErrorAction> getMalformedInputAction() {
        return malformedInputAction;
    }

    /**
     * Returns the error action of unmappable outputs.
     * @return the error action of unmappable outputs
     */
    public Value<ErrorAction> getUnmappableOutputAction() {
        return unmappableOutputAction;
    }

    /**
     * Returns the quote style.
     * @return the quote style
     */
    public Value<QuoteStyle> getQuoteStyle() {
        return quoteStyle;
    }

    /**
     * Consumes attribute elements about escape settings, and returns corresponding {@link EscapeSettings}.
     * @param environment the current environment
     * @param attribute the attribute
     * @param elements the element map to be consumed
     * @return corresponded {@link EscapeSettings}.
     */
    public static TextFieldSettings consume(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        AttributeAnalyzer analyzer = new AttributeAnalyzer(environment, attribute);
        TextFieldSettings settings = new TextFieldSettings();
        consumeAdapterClass(settings, analyzer, elements.remove(ELEMENT_FIELD_ADAPTER));
        consumeNullFormat(settings, analyzer, elements.remove(ELEMENT_NULL_FORMAT));
        consumeTrueFormat(settings, analyzer, elements.remove(ELEMENT_TRUE_FORMAT));
        consumeFalseFormat(settings, analyzer, elements.remove(ELEMENT_FALSE_FORMAT));
        consumeDateFormat(settings, analyzer, elements.remove(ELEMENT_DATE_FORMAT));
        consumeDateTimeFormat(settings, analyzer, elements.remove(ELEMENT_DATETIME_FORMAT));
        consumeDecimalFormat(settings, analyzer, elements.remove(ELEMENT_DECIMAL_FORMAT));
        consumeTrimInputWhitespaces(settings, analyzer, elements.remove(ELEMENT_TRIM_INPUT_WHITESPACES));
        consumeSkipEmptyInput(settings, analyzer, elements.remove(ELEMENT_SKIP_EMPTY_INPUT));
        consumeMalformedInputAction(settings, analyzer, elements.remove(ELEMENT_MALFORMED_INPUT_ACTION));
        consumeUnmappableOutputAction(settings, analyzer, elements.remove(ELEMENT_UNMAPPABLE_OUTPUT_ACTION));
        consumeQuoteStyle(settings, analyzer, elements.remove(ELEMENT_QUOTE_STYLE));
        return settings;
    }

    private static void consumeAdapterClass(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.adapterClass = analyzer.toClassName(element);
        }
    }

    private static void consumeNullFormat(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.nullFormat = analyzer.toStringWithNull(element);
        }
    }

    private static void consumeTrueFormat(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.trueFormat = analyzer.toString(element);
        }
    }

    private static void consumeFalseFormat(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.falseFormat = analyzer.toString(element);
        }
    }

    private static void consumeDateFormat(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.dateFormat = analyzer.toDatePattern(element);
        }
    }

    private static void consumeDateTimeFormat(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.dateTimeFormat = analyzer.toDatePattern(element);
        }
    }

    private static void consumeDecimalFormat(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.decimalFormat = analyzer.toEnumConstant(element, DecimalOptionFieldAdapter.OutputStyle.class);
        }
    }

    private static void consumeTrimInputWhitespaces(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.trimInputWhitespaces = analyzer.toBoolean(element);
        }
    }

    private static void consumeSkipEmptyInput(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.skipEmptyInput = analyzer.toBoolean(element);
        }
    }

    private static void consumeMalformedInputAction(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.malformedInputAction = analyzer.toEnumConstant(element, ErrorAction.class);
        }
    }

    private static void consumeUnmappableOutputAction(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.unmappableOutputAction = analyzer.toEnumConstant(element, ErrorAction.class);
        }
    }

    private static void consumeQuoteStyle(
            TextFieldSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.quoteStyle = analyzer.toEnumConstant(element, QuoteStyle.class);
        }
    }

    /**
     * Verifies this settings.
     * @param environment the current environment
     * @param attribute the original attribute
     * @return {@code true} if the settings seems valid, otherwise {@code false}
     */
    public boolean verify(DmdlSemantics environment, AstAttribute attribute) {
        AttributeAnalyzer analyzer = new AttributeAnalyzer(environment, attribute);
        checkNotConflict(analyzer, trueFormat, nullFormat);
        checkNotConflict(analyzer, falseFormat, nullFormat);
        checkNotConflict(analyzer, falseFormat, trueFormat);
        return analyzer.hasError() == false;
    }

    private static void checkNotConflict(AttributeAnalyzer analyzer, Value<?> a, Value<?> b) {
        if (a.isPresent() && b.isPresent() && a.equals(b)) {
            analyzer.error(a.getDeclaration(), Messages.getString("TextFieldSettings.diagnosticConflictFormat"), b.getDeclaration().name); //$NON-NLS-1$
        }
    }
}
