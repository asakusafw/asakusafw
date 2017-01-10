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

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

import com.asakusafw.dmdl.directio.util.ClassName;
import com.asakusafw.dmdl.directio.util.CodecNames;
import com.asakusafw.dmdl.directio.util.Value;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.runtime.io.text.LineSeparator;
import com.asakusafw.runtime.io.text.driver.ErrorAction;
import com.asakusafw.runtime.io.text.driver.HeaderType;

/**
 * Settings of formatted text file.
 * @since 0.9.1
 */
public class TextFormatSettings {

    private Value<Charset> charset = Value.undefined();

    private Value<HeaderType> headerType = Value.undefined();

    private Value<ClassName> compressionType = Value.undefined();

    private Value<LineSeparator> lineSeparator = Value.undefined();

    private Value<Character> fieldSeparator = Value.undefined();

    private Value<ErrorAction> lessInputAction = Value.undefined();

    private Value<ErrorAction> moreInputAction = Value.undefined();

    private Value<ClassName> inputTransformerClass = Value.undefined();

    private Value<ClassName> outputTransformerClass = Value.undefined();

    /**
     * Returns the charset name.
     * @return the charset name
     */
    public Value<Charset> getCharset() {
        return charset;
    }

    /**
     * Returns the header type.
     * @return the header type
     */
    public Value<HeaderType> getHeaderType() {
        return headerType;
    }

    /**
     * Returns the compression type.
     * @return the compression type
     */
    public Value<ClassName> getCompressionType() {
        return compressionType;
    }

    /**
     * Returns the line separator.
     * @return the line separator
     */
    public Value<LineSeparator> getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Returns the field separator.
     * @return the field separator
     */
    public Value<Character> getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * Returns the error action type for less input fields.
     * @return the error action type for less input fields
     */
    public Value<ErrorAction> getLessInputAction() {
        return lessInputAction;
    }

    /**
     * Returns the error action type for extra input fields.
     * @return the error action type for extra input fields
     */
    public Value<ErrorAction> getMoreInputAction() {
        return moreInputAction;
    }

    /**
     * Returns the input transformer class.
     * @return the input transformer class
     */
    public Value<ClassName> getInputTransformerClass() {
        return inputTransformerClass;
    }

    /**
     * Returns the output transformer class.
     * @return the output transformer class
     */
    public Value<ClassName> getOutputTransformerClass() {
        return outputTransformerClass;
    }

    /**
     * Consumes attribute elements about escape settings, and returns corresponding {@link EscapeSettings}.
     * @param environment the current environment
     * @param attribute the attribute
     * @param elements the element map to be consumed
     * @return corresponded {@link EscapeSettings}.
     */
    public static TextFormatSettings consume(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        AttributeAnalyzer analyzer = new AttributeAnalyzer(environment, attribute);
        TextFormatSettings settings = new TextFormatSettings();
        consumeCharset(settings, analyzer, elements.remove(ELEMENT_CHARSET_NAME));
        consumeHeaderType(settings, analyzer, elements.remove(ELEMENT_HEADER_TYPE));
        consumeCompressionType(settings, analyzer, elements.remove(ELEMENT_COMPRESSION_TYPE));
        consumeLineSeparator(settings, analyzer, elements.remove(ELEMENT_LINE_SEPARATOR));
        consumeFieldSeparator(settings, analyzer, elements.remove(ELEMENT_FIELD_SEPARATOR));
        consumeLessInputAction(settings, analyzer, elements.remove(ELEMENT_LESS_INPUT_ACTION));
        consumeMoreInputAction(settings, analyzer, elements.remove(ELEMENT_MORE_INPUT_ACTION));
        consumeInputTransformerClass(settings, analyzer, elements.remove(ELEMENT_INPUT_TRANSFORMER));
        consumeOutputTransformerClass(settings, analyzer, elements.remove(ELEMENT_OUTPUT_TRANSFORMER));
        return settings;
    }

    private static void consumeCharset(
            TextFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.charset = analyzer.toCharset(element);
        }
    }

    private static void consumeHeaderType(
            TextFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.headerType = analyzer.toEnumConstant(element, HeaderType.class);
        }
    }

    private static void consumeCompressionType(
            TextFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.compressionType = analyzer.toClassName(
                    element,
                    s -> Optional.of(CodecNames.resolveCodecName(s)));
        }
    }

    private static void consumeLineSeparator(
            TextFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.lineSeparator = analyzer.toEnumConstant(element, LineSeparator.class);
        }
    }

    private static void consumeFieldSeparator(
            TextFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.fieldSeparator = analyzer.toCharacter(element);
        }
    }

    private static void consumeLessInputAction(
            TextFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.lessInputAction = analyzer.toEnumConstant(element, ErrorAction.class);
        }
    }

    private static void consumeMoreInputAction(
            TextFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.moreInputAction = analyzer.toEnumConstant(element, ErrorAction.class);
        }
    }

    private static void consumeInputTransformerClass(
            TextFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.inputTransformerClass = analyzer.toClassName(element);
        }
    }

    private static void consumeOutputTransformerClass(
            TextFormatSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.outputTransformerClass = analyzer.toClassName(element);
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
        if (fieldSeparator.isPresent()) {
            char c = fieldSeparator.getEntity();
            if (c == '\r' || c == '\n') {
                analyzer.error(fieldSeparator.getDeclaration(), Messages.getString("TextFormatSettings.diagnosticConflictLineSeparator")); //$NON-NLS-1$
            }
        }
        return analyzer.hasError() == false;
    }
}
