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
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.directio.util.ClassName;
import com.asakusafw.dmdl.directio.util.DatePattern;
import com.asakusafw.dmdl.directio.util.DecimalPattern;
import com.asakusafw.dmdl.directio.util.MapValue;
import com.asakusafw.dmdl.directio.util.Value;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstAttributeValue;
import com.asakusafw.dmdl.model.AstAttributeValueArray;
import com.asakusafw.dmdl.model.AstAttributeValueMap;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;

/**
 * Analyzes DMDL attributes.
 * @since 0.9.1
 */
public class AttributeAnalyzer {

    private final DmdlSemantics environment;

    private final AstAttribute attribute;

    private boolean sawError = false;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @param attribute the source attribute
     */
    public AttributeAnalyzer(DmdlSemantics environment, AstAttribute attribute) {
        this.environment = environment;
        this.attribute = attribute;
    }

    /**
     * Whether or not errors were occurred.
     * @return {@code true} if occurred
     */
    public boolean hasError() {
        return sawError;
    }

    /**
     * Analyze the given element as a string value.
     * @param element the target element
     * @return the analyzed value, or undefined if the element value is not valid
     */
    public Value<String> toString(AstAttributeElement element) {
        return parseString(element.value)
                .map(s -> Value.of(element, s))
                .orElseGet(() -> {
                    error(element, Messages.getString("AttributeAnalyzer.diagnosticNotString")); //$NON-NLS-1$
                    return Value.undefined();
                });
    }

    /**
     * Analyze the given element as a nullable string value.
     * @param element the target element
     * @return the analyzed value, or undefined if the element value is not valid
     */
    public Value<String> toStringWithNull(AstAttributeElement element) {
        if (isName(element.value, VALUE_NULL)) {
            return Value.of(element, null);
        }
        return parseStringLiteral(element.value)
                .map(s -> Value.of(element, s))
                .orElseGet(() -> {
                    error(element, Messages.getString("AttributeAnalyzer.diagnosticNotString")); //$NON-NLS-1$
                    return Value.undefined();
                });
    }

    /**
     * Analyze the given element as a boolean value.
     * @param element the target element
     * @return the analyzed value, or undefined if the element value is not valid
     */
    public Value<Boolean> toBoolean(AstAttributeElement element) {
        return parseString(element.value)
                .map(s -> s.toLowerCase(Locale.ENGLISH))
                .flatMap(s -> {
                    if (s.equals(VALUE_TRUE)) {
                        return Optional.of(true);
                    } else if (s.equals(VALUE_FALSE)) {
                        return Optional.of(false);
                    } else {
                        return Optional.empty();
                    }
                })
                .map(b -> Value.of(element, b))
                .orElseGet(() -> {
                    error(element, Messages.getString("AttributeAnalyzer.diagnosticNotBoolean")); //$NON-NLS-1$
                    return Value.undefined();
                });
    }

    /**
     * Analyze the given element as a character value.
     * @param element the target element
     * @return the analyzed value, or undefined if the element value is not valid
     */
    public Value<Character> toCharacter(AstAttributeElement element) {
        return parseStringLiteral(element.value)
            .filter(s -> s.length() == 1)
            .map(s -> Value.of(element, s.charAt(0)))
            .orElseGet(() -> {
                error(element, Messages.getString("AttributeAnalyzer.diagnosticNotCharacter")); //$NON-NLS-1$
                return Value.undefined();
            });
    }

    /**
     * Analyze the given element as a character set.
     * @param element the target element
     * @return the analyzed value, or undefined if the element value is not valid
     */
    public Value<Charset> toCharset(AstAttributeElement element) {
        return parseString(element.value)
                .flatMap(s -> {
                    try {
                        return Optional.of(Charset.forName(s));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                })
                .map(cs -> Value.of(element, cs))
                .orElseGet(() -> {
                    error(element, Messages.getString("AttributeAnalyzer.diagnosticNotCharsetName")); //$NON-NLS-1$
                    return Value.undefined();
                });
    }

    /**
     * Analyze the given element as a date format.
     * @param element the target element
     * @return the analyzed value, or undefined if the element value is not valid
     */
    public Value<DecimalPattern> toDecimalPatternWithNull(AstAttributeElement element) {
        if (isName(element.value, VALUE_NULL)) {
            return Value.of(element, null);
        }
        return parseStringLiteral(element.value)
                .filter(DecimalPattern::isValid)
                .map(s -> Value.of(element, new DecimalPattern(s)))
                .orElseGet(() -> {
                    error(element, Messages.getString("AttributeAnalyzer.diagnosticNotDecimalFormat")); //$NON-NLS-1$
                    return Value.undefined();
                });
    }

    /**
     * Analyze the given element as a date format.
     * @param element the target element
     * @return the analyzed value, or undefined if the element value is not valid
     */
    public Value<DatePattern> toDatePattern(AstAttributeElement element) {
        return parseStringLiteral(element.value)
                .filter(DatePattern::isValid)
                .map(s -> Value.of(element, new DatePattern(s)))
                .orElseGet(() -> {
                    error(element, Messages.getString("AttributeAnalyzer.diagnosticNotDateFormat")); //$NON-NLS-1$
                    return Value.undefined();
                });
    }

    /**
     * Analyze the given element as a class name.
     * @param element the target element
     * @return the analyzed value, or undefined if the element value is not valid
     */
    public Value<ClassName> toClassName(AstAttributeElement element) {
        return toClassName(element, Optional::of);
    }

    /**
     * Analyze the given element as an enum constant.
     * @param <T> the enum type
     * @param element the target element
     * @param type the enum type
     * @return the analyzed value, or undefined if the element value is not valid
     */
    public <T extends Enum<T>> Value<T> toEnumConstant(AstAttributeElement element, Class<T> type) {
        return parseString(element.value)
                .flatMap(s -> {
                    try {
                        return Optional.of(Enum.valueOf(type, s.toUpperCase(Locale.ENGLISH)));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                })
                .map(v -> Value.of(element, v))
                .orElseGet(() -> {
                    error(element, Messages.getString("AttributeAnalyzer.diagnosticNotEnumConstant"), //$NON-NLS-1$
                            Arrays.stream(type.getEnumConstants())
                            .map(c -> c.name().toLowerCase(Locale.ENGLISH))
                            .collect(Collectors.joining(", "))); //$NON-NLS-1$
                    return Value.undefined();
                });
    }

    /**
     * Analyze the given element as a class name.
     * @param element the target element
     * @param resolver the resolver
     * @return the analyzed value, or undefined if the element value is not valid
     */
    public Value<ClassName> toClassName(AstAttributeElement element, Function<String, Optional<String>> resolver) {
        if (isName(element.value, VALUE_NULL)) {
            return Value.of(element, null);
        }
        return parseString(element.value)
                .flatMap(resolver)
                .filter(ClassName::isValid)
                .map(s -> Value.of(element, new ClassName(s)))
                .orElseGet(() -> {
                    error(element, Messages.getString("AttributeAnalyzer.diagnosticNotClassName")); //$NON-NLS-1$
                    return Value.undefined();
                });
    }

    /**
     * Analyze the given element as a character map.
     * @param element the target element
     * @return the analyzed map, or undefined if the element value is not valid
     */
    public MapValue<Character, Character> toCharacterMapWithNullValue(AstAttributeElement element) {
        if (element.value instanceof AstAttributeValueArray
                && ((AstAttributeValueArray) element.value).elements.isEmpty()) {
            return new MapValue<>(null);
        }
        if (!(element.value instanceof AstAttributeValueMap)) {
            error(element, Messages.getString("AttributeAnalyzer.diagnosticNotMap")); //$NON-NLS-1$
            return new MapValue<>(null);
        }
        MapValue<Character, Character> results = new MapValue<>(element);
        for (AstAttributeValueMap.Entry entry : ((AstAttributeValueMap) element.value).entries) {
            Character key = parseStringLiteral(entry.key)
                .filter(s -> s.length() == 1)
                .map(s -> s.charAt(0))
                .orElseGet(() -> {
                    error(element, entry, Messages.getString("AttributeAnalyzer.diagnosticMapKeyNotCharacter")); //$NON-NLS-1$
                    return null;
                });
            Character value = parseStringLiteral(entry.value)
                .filter(s -> s.length() == 1)
                .map(s -> s.charAt(0))
                .orElseGet(() -> {
                    if (isName(entry.value, TextFormatConstants.VALUE_NULL) == false) {
                        error(element, entry, Messages.getString("AttributeAnalyzer.diagnosticMapValueNotCharacter")); //$NON-NLS-1$
                    }
                    return null;
                });
            results.add(entry, key, value);
        }
        return results;
    }

    /**
     * Reports an error.
     * @param element the target element
     * @param message the error message pattern, <code>{0}</code> is reserved for the element path
     * @param arguments the pattern arguments
     */
    public void error(AstAttributeElement element, String message, Object... arguments) {
        report(Diagnostic.Level.ERROR, element.name, message, toArgs(element, arguments));
    }

    /**
     * Reports an error.
     * @param element the target element
     * @param entry the map entry
     * @param message the error message pattern, <code>{0}</code> is reserved for the element path
     * @param arguments the pattern arguments
     */
    public void error(
            AstAttributeElement element, AstAttributeValueMap.Entry entry,
            String message, Object... arguments) {
        report(Diagnostic.Level.ERROR, entry.key, message, toArgs(element, entry, arguments));
    }

    /**
     * Reports a warning.
     * @param element the target element
     * @param message the error message pattern, <code>{0}</code> is reserved for the element path
     * @param arguments the pattern arguments
     */
    public void warn(AstAttributeElement element, String message, Object... arguments) {
        report(Diagnostic.Level.WARN, element.name, message, toArgs(element, arguments));
    }

    /**
     * Reports a warning.
     * @param element the target element
     * @param entry the map entry
     * @param message the error message pattern, <code>{0}</code> is reserved for the element path
     * @param arguments the pattern arguments
     */
    public void warn(
            AstAttributeElement element, AstAttributeValueMap.Entry entry,
            String message, Object... arguments) {
        report(Diagnostic.Level.WARN, entry.key, message, toArgs(element, entry, arguments));
    }

    private Object[] toArgs(AstAttributeElement element, Object... args) {
        Object[] arguments = new Object[args.length + 1];
        arguments[0] = String.format("@%s(%s)", //$NON-NLS-1$
                attribute.name.toString(), element.name.identifier);
        System.arraycopy(args, 0, arguments, 1, args.length);
        return arguments;
    }

    private Object[] toArgs(AstAttributeElement element, AstAttributeValueMap.Entry entry, Object... args) {
        Object[] arguments = new Object[args.length + 1];
        arguments[0] = String.format("@%s(%s[%s])", //$NON-NLS-1$
                attribute.name.toString(), element.name.identifier, entry.key.token);
        System.arraycopy(args, 0, arguments, 1, args.length);
        return arguments;
    }

    private void report(Diagnostic.Level level, AstNode node, String message, Object[] arguments) {
        sawError |= level == Diagnostic.Level.ERROR;
        environment.report(new Diagnostic(level, node, message, arguments));
    }

    private static Optional<String> parseString(AstAttributeValue value) {
        return first(value,
                AttributeAnalyzer::parseStringLiteral,
                AttributeAnalyzer::bareLiteral,
                AttributeAnalyzer::parseSimpleName);
    }

    private static Optional<String> parseStringLiteral(AstAttributeValue value) {
        if (value instanceof AstLiteral) {
            AstLiteral literal = (AstLiteral) value;
            if (literal.getKind() == LiteralKind.STRING) {
                return Optional.of(literal.toStringValue());
            }
        }
        return Optional.empty();
    }

    private static Optional<String> bareLiteral(AstAttributeValue value) {
        if (value instanceof AstLiteral) {
            return Optional.of(((AstLiteral) value).getToken());
        }
        return Optional.empty();
    }

    private static Optional<String> parseSimpleName(AstAttributeValue value) {
        if (value instanceof AstSimpleName) {
            return Optional.of(((AstSimpleName) value).identifier);
        }
        return Optional.empty();
    }

    private static boolean isName(AstAttributeValue value, String constant) {
        return parseSimpleName(value)
                .filter(Predicate.isEqual(constant))
                .isPresent();
    }

    @SafeVarargs
    private static <K, V> Optional<V> first(K input, Function<? super K, Optional<V>>... mappers) {
        for (Function<? super K, Optional<V>> mapper : mappers) {
            Optional<V> value = mapper.apply(input);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }
}
