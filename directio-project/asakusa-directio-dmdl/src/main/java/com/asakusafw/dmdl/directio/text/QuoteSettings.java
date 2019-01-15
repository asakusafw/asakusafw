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
package com.asakusafw.dmdl.directio.text;

import static com.asakusafw.dmdl.directio.text.TextFormatConstants.*;

import java.util.Map;

import com.asakusafw.dmdl.directio.util.AttributeAnalyzer;
import com.asakusafw.dmdl.directio.util.Value;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.runtime.io.text.csv.QuoteStyle;

/**
 * Settings of quoting fields.
 * @since 0.9.1
 */
public class QuoteSettings {

    private Value<Character> character = Value.undefined();

    private Value<Boolean> allowLineFeedInField = Value.undefined();

    private Value<QuoteStyle> defaultStyle = Value.undefined();

    private Value<QuoteStyle> headerStyle = Value.undefined();

    /**
     * Returns quote character.
     * @return quote character
     */
    public Value<Character> getCharacter() {
        return character;
    }

    /**
     * Returns whether or not LF can appear as field values.
     * @return {@code trur} if it is enabled, otherwise {@code false}
     */
    public Value<Boolean> getAllowLineFeedInField() {
        return allowLineFeedInField;
    }

    /**
     * Returns the default quote style.
     * @return the default quote style
     */
    public Value<QuoteStyle> getDefaultStyle() {
        return defaultStyle;
    }

    /**
     * Returns the quote style for header fields.
     * @return the quote style for header fields
     */
    public Value<QuoteStyle> getHeaderStyle() {
        return headerStyle;
    }

    /**
     * Consumes attribute elements about escape settings, and returns corresponding {@link QuoteSettings}.
     * @param environment the current environment
     * @param attribute the attribute
     * @param elements the element map to be consumed
     * @return corresponded {@link QuoteSettings}.
     */
    public static QuoteSettings consume(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        AttributeAnalyzer analyzer = new AttributeAnalyzer(environment, attribute);
        QuoteSettings settings = new QuoteSettings();
        consumeCharacter(settings, analyzer, elements.remove(ELEMENT_QUOTE_CHARACTER));
        consumeDefaultStyle(settings, analyzer, elements.remove(ELEMENT_DEFAULT_QUOTE_STYLE));
        consumeHeaderStyle(settings, analyzer, elements.remove(ELEMENT_HEADER_QUOTE_STYLE));
        consumeAllowLineFeedInField(settings, analyzer, elements.remove(ELEMENT_ALLOW_LINE_FEED_IN_FIELD));
        return settings;
    }

    private static void consumeCharacter(
            QuoteSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.character = analyzer.toCharacter(element);
        }
    }

    private static void consumeDefaultStyle(
            QuoteSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.defaultStyle = analyzer.toEnumConstant(element, QuoteStyle.class);
        }
    }

    private static void consumeHeaderStyle(
            QuoteSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.headerStyle = analyzer.toEnumConstant(element, QuoteStyle.class);
        }
    }

    private static void consumeAllowLineFeedInField(
            QuoteSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.allowLineFeedInField = analyzer.toBoolean(element);
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
        if (isLineSeparator(character.getEntity())) {
            analyzer.error(character.getDeclaration(),
                    Messages.getString("QuoteSettings.diagnosticConflictLineSeparator")); //$NON-NLS-1$
        }
        return analyzer.hasError() == false;
    }

    private static boolean isLineSeparator(Character c) {
        return c != null && (c == '\r' || c == '\n');
    }
}
