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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.asakusafw.dmdl.directio.util.MapValue;
import com.asakusafw.dmdl.directio.util.Value;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.runtime.io.text.TextUtil;

/**
 * Settings of character escape.
 * @since 0.9.1
 */
public class EscapeSettings {

    private Value<Character> character = Value.undefined();

    private MapValue<Character, Character> sequences = new MapValue<>(null);

    private Value<Boolean> escapeLineSeparator = Value.undefined();

    /**
     * Returns the escape character.
     * @return the escape character
     */
    public Value<Character> getCharacter() {
        return character;
    }

    /**
     * Returns the escape sequence map.
     * @return the escape sequence map
     */
    public MapValue<Character, Character> getSequences() {
        return sequences;
    }

    /**
     * Returns whether or not line separators can be escaped.
     * @return {@code true} if enabled, otherwise {@code false}
     */
    public Value<Boolean> getEscapeLineSeparator() {
        return escapeLineSeparator;
    }

    /**
     * Consumes attribute elements about escape settings, and returns corresponding {@link EscapeSettings}.
     * @param environment the current environment
     * @param attribute the attribute
     * @param elements the element map to be consumed
     * @return corresponded {@link EscapeSettings}.
     */
    public static EscapeSettings consume(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        AttributeAnalyzer analyzer = new AttributeAnalyzer(environment, attribute);
        EscapeSettings settings = new EscapeSettings();
        consumeCharacter(settings, analyzer, elements.remove(ELEMENT_ESCAPE_CHARACTER));
        consumeSequences(settings, analyzer, elements.remove(ELEMENT_ESCAPE_SEQUENCE_MAP));
        consumeEscapeLineSeparator(settings, analyzer, elements.remove(ELEMENT_ESCAPE_LINE_SEPARATOR));
        return settings;
    }

    private static void consumeCharacter(
            EscapeSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.character = analyzer.toCharacter(element);
        }
    }

    private static void consumeSequences(
            EscapeSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.sequences = analyzer.toCharacterMapWithNullValue(element);
        }
    }

    private static void consumeEscapeLineSeparator(
            EscapeSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.escapeLineSeparator = analyzer.toBoolean(element);
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
        if (character.isPresent() == false) {
            if (sequences.getDeclaration() != null) {
                errorMissing(analyzer, sequences.getDeclaration(), ELEMENT_ESCAPE_CHARACTER);
            }
            if (escapeLineSeparator.isPresent()) {
                errorMissing(analyzer, escapeLineSeparator.getDeclaration(), ELEMENT_ESCAPE_CHARACTER);
            }
        } else {
            if (isLineSeparator(character.getEntity())) {
                analyzer.error(character.getDeclaration(),
                        Messages.getString("EscapeSettings.diagnosticConflictLineSeparator")); //$NON-NLS-1$
            }
            if (sequences.getDeclaration() != null) {
                Set<Character> sawKeys = new HashSet<>();
                Set<Character> sawValues = new HashSet<>();
                for (MapValue.Entry<Character, Character> entry : sequences.getEntries()) {
                    Character value = entry.getValue();
                    Character key = entry.getKey();
                    if (isLineSeparator(key)) {
                        analyzer.error(sequences.getDeclaration(), entry.getDeclaration(),
                                "{0} must not be line separator characters"); //$NON-NLS-1$
                    }
                    if (sawKeys.contains(key)) {
                        analyzer.warn(sequences.getDeclaration(), entry.getDeclaration(),
                                Messages.getString("EscapeSettings.diagnosticEscapeSequenceDuplicateKey"), //$NON-NLS-1$
                                TextUtil.quote(String.valueOf(key)));
                    }
                    if (sawValues.contains(value)) {
                        analyzer.warn(sequences.getDeclaration(), entry.getDeclaration(),
                                Messages.getString("EscapeSettings.diagnosticEscapeSequenceDuplicateValue"), //$NON-NLS-1$
                                value == null ? VALUE_NULL : TextUtil.quote(String.valueOf(value)));
                    }
                    sawKeys.add(key);
                    sawValues.add(value);
                }
            }
        }
        return analyzer.hasError() == false;
    }

    private static boolean isLineSeparator(Character c) {
        return c != null && (c == '\r' || c == '\n');
    }

    private static void errorMissing(AttributeAnalyzer analyzer, AstAttributeElement declaration, String attribute) {
        analyzer.error(declaration, Messages.getString("EscapeSettings.diagnosticMissingRelatedAttribute"), attribute); //$NON-NLS-1$
    }
}
