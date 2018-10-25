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

import java.util.Map;
import java.util.Optional;

import com.asakusafw.dmdl.directio.json.driver.JsonFormatConstants.JsonFormatKind;
import com.asakusafw.dmdl.directio.util.AttributeAnalyzer;
import com.asakusafw.dmdl.directio.util.ClassName;
import com.asakusafw.dmdl.directio.util.CodecNames;
import com.asakusafw.dmdl.directio.util.Value;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.semantics.DmdlSemantics;

/**
 * Settings of JSON I/O stream.
 * @since 0.10.3
 */
public class JsonStreamSettings {

    private Value<JsonFormatKind> formatKind = Value.undefined();

    private Value<ClassName> compressionType = Value.undefined();

    /**
     * Returns the format kind.
     * @return the format kind
     */
    public Value<JsonFormatKind> getFormatKind() {
        return formatKind;
    }

    /**
     * Returns the compression type.
     * @return the compression type
     */
    public Value<ClassName> getCompressionType() {
        return compressionType;
    }

    /**
     * Consumes attribute elements about I/O stream settings.
     * @param environment the current environment
     * @param attribute the attribute
     * @param elements the element map to be consumed
     * @return the consumed settings
     */
    public static JsonStreamSettings consume(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        AttributeAnalyzer analyzer = new AttributeAnalyzer(environment, attribute);
        JsonStreamSettings settings = new JsonStreamSettings();
        consumeFormatKind(settings, analyzer, elements.remove(ELEMENT_FORMAT_KIND));
        consumeCompressionType(settings, analyzer, elements.remove(ELEMENT_COMPRESSION_TYPE));
        return settings;
    }

    private static void consumeFormatKind(
            JsonStreamSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.formatKind = analyzer.toEnumConstant(element, JsonFormatKind.class);
        }
    }

    private static void consumeCompressionType(
            JsonStreamSettings settings, AttributeAnalyzer analyzer, AstAttributeElement element) {
        if (element != null) {
            settings.compressionType = analyzer.toClassName(
                    element,
                    s -> Optional.of(CodecNames.resolveCodecName(s)));
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
