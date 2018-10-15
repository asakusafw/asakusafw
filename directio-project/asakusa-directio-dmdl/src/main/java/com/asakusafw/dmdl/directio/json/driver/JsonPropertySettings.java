/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import java.time.ZoneId;
import java.util.Map;
import java.util.function.Function;

import com.asakusafw.dmdl.directio.text.AttributeAnalyzer;
import com.asakusafw.dmdl.directio.util.ClassName;
import com.asakusafw.dmdl.directio.util.DatePattern;
import com.asakusafw.dmdl.directio.util.Value;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.runtime.io.json.ErrorAction;
import com.asakusafw.runtime.io.json.value.ValueOptionPropertyAdapter;

/**
 * Settings of JSON property.
 * @since 0.10.3
 */
public class JsonPropertySettings {

    private Value<ClassName> adapterClass = Value.undefined();

    private Value<DatePattern> dateFormat = Value.undefined();

    private Value<DatePattern> dateTimeFormat = Value.undefined();

    private Value<ZoneId> timeZone = Value.undefined();

    private Value<ErrorAction> missingInputAction = Value.undefined();

    private Value<ErrorAction> malformedInputAction = Value.undefined();

    private Value<ValueOptionPropertyAdapter.NullStyle> nullStyle = Value.undefined();

    /**
     * Returns the adapter class.
     * @return the adapter class
     */
    public Value<ClassName> getAdapterClass() {
        return adapterClass;
    }


    /**
     * Returns the date format.
     * @return the date format
     */
    public Value<DatePattern> getDateFormat() {
        return dateFormat;
    }


    /**
     * Returns the date-time format.
     * @return the date-time format
     */
    public Value<DatePattern> getDateTimeFormat() {
        return dateTimeFormat;
    }


    /**
     * Returns the time zone.
     * @return the time zone
     */
    public Value<ZoneId> getTimeZone() {
        return timeZone;
    }


    /**
     * Returns the missing input action.
     * @return the action
     */
    public Value<ErrorAction> getMissingInputAction() {
        return missingInputAction;
    }


    /**
     * Returns the malformed input action.
     * @return the action
     */
    public Value<ErrorAction> getMalformedInputAction() {
        return malformedInputAction;
    }


    /**
     * Returns the null style.
     * @return the null style
     */
    public Value<ValueOptionPropertyAdapter.NullStyle> getNullStyle() {
        return nullStyle;
    }

    /**
     * Merges this object and the given default settings into a new object.
     * @param defaults the default settings
     * @return the created settings
     */
    public JsonPropertySettings mergeDefaults(JsonPropertySettings defaults) {
        JsonPropertySettings settings = new JsonPropertySettings();
        settings.adapterClass = merge(defaults, JsonPropertySettings::getAdapterClass);
        settings.dateFormat = merge(defaults, JsonPropertySettings::getDateFormat);
        settings.dateTimeFormat = merge(defaults, JsonPropertySettings::getDateTimeFormat);
        settings.timeZone = merge(defaults, JsonPropertySettings::getTimeZone);
        settings.missingInputAction = merge(defaults, JsonPropertySettings::getMissingInputAction);
        settings.malformedInputAction = merge(defaults, JsonPropertySettings::getMalformedInputAction);
        settings.nullStyle = merge(defaults, JsonPropertySettings::getNullStyle);
        return settings;
    }

    private <T> Value<T> merge(JsonPropertySettings defaults, Function<JsonPropertySettings, Value<T>> mapping) {
        return mapping.apply(this).orDefault(mapping.apply(defaults));
    }

    /**
     * Consumes attribute elements about property settings.
     * @param environment the current environment
     * @param attribute the attribute
     * @param elements the element map to be consumed
     * @return consumed settings
     */
    public static JsonPropertySettings consume(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        AttributeAnalyzer analyzer = new AttributeAnalyzer(environment, attribute);
        JsonPropertySettings settings = new JsonPropertySettings();
        consumeAdapterClass(settings, analyzer, elements.remove(ELEMENT_FIELD_ADAPTER));
        consumeDateFormat(settings, analyzer, elements.remove(ELEMENT_DATE_FORMAT));
        consumeDateTimeFormat(settings, analyzer, elements.remove(ELEMENT_DATETIME_FORMAT));
        consumeTimeZone(settings, analyzer, elements.remove(ELEMENT_TIME_ZONE));
        consumeMalformedInputAction(settings, analyzer, elements.remove(ELEMENT_MALFORMED_INPUT_ACTION));
        consumeMissingInputAction(settings, analyzer, elements.remove(ELEMENT_MISSING_PROPERTY_ACTION));
        consumeNullStyle(settings, analyzer, elements.remove(ELEMENT_NULL_STYLE));
        return settings;
    }


    private static void consumeAdapterClass(
            JsonPropertySettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.adapterClass = analyzer.toClassName(element);
        }
    }

    private static void consumeDateFormat(
            JsonPropertySettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.dateFormat = analyzer.toDatePattern(element);
        }
    }

    private static void consumeDateTimeFormat(
            JsonPropertySettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.dateTimeFormat = analyzer.toDatePattern(element);
        }
    }

    private static void consumeTimeZone(
            JsonPropertySettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.timeZone = analyzer.toZoneIdWithNull(element);
        }
    }

    private static void consumeMalformedInputAction(
            JsonPropertySettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.malformedInputAction = analyzer.toEnumConstant(element, ErrorAction.class);
        }
    }

    private static void consumeMissingInputAction(
            JsonPropertySettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.missingInputAction = analyzer.toEnumConstant(element, ErrorAction.class);
        }
    }

    private static void consumeNullStyle(
            JsonPropertySettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.nullStyle = analyzer.toEnumConstant(element, ValueOptionPropertyAdapter.NullStyle.class);
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
