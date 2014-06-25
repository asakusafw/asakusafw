/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.hive.orc;

import java.math.BigInteger;
import java.util.Map;

import org.apache.hadoop.hive.ql.io.orc.CompressionKind;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;

import com.asakusafw.directio.hive.serde.DataModelDriver.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelDriver.FieldMappingStrategy;
import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.directio.hive.common.HiveDataModelTrait;
import com.asakusafw.dmdl.directio.hive.common.Namer;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Processes <code>&#64;directio.hive.orc</code> attributes.
<h2>'&#64;directio.hive.orc' attribute</h2>
The attributed declaration can have:
<ul>
<li> with {@code table_name=[string-literal]} as explicit table name (default: data model name) </li>
<li> with {@code format_version=[string-literal]} as ORCFile format version (default: system default) </li>
<li> with {@code compression=[string-literal]} as compression kind name (default: {@code "snappy"}) </li>
<li> with {@code stripe_size=integer} as stripe size (default: system default) </li>
<li> with {@code field_mappping=[string-literal]}  (default: {@code "position"}) </li>
<li> with {@code missing_source=[string-literal]}  (default: {@code "logging"}) </li>
<li> with {@code missing_target=[string-literal]}  (default: {@code "logging"}) </li>
<li> with {@code incompatible_type=[string-literal]}  (default: {@code "fail"}) </li>
</ul>
 * @since 0.7.0
 */
public class OrcFileDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "directio.hive.orc"; //$NON-NLS-1$

    /**
     * The element name of explicit table name.
     */
    public static final String ELEMENT_TABLE_NAME = "table_name"; //$NON-NLS-1$

    /**
     * The element name of ORCFile format version.
     */
    public static final String ELEMENT_FORMAT_VERSION = "format_version"; //$NON-NLS-1$

    /**
     * The element name of compression kind name.
     */
    public static final String ELEMENT_COMPRESSION_KIND = "compression"; //$NON-NLS-1$

    /**
     * The element name of stripe size.
     */
    public static final String ELEMENT_STRIPE_SIZE = "stripe_size"; //$NON-NLS-1$

    /**
     * The element name of field mapping strategy.
     */
    public static final String ELEMENT_FIELD_MAPPING = "field_mapping"; //$NON-NLS-1$

    /**
     * The element name of missing source handling strategy.
     */
    public static final String ELEMENT_MISSING_SOURCE = "on_missing_source"; //$NON-NLS-1$

    /**
     * The element name of missing target handling strategy.
     */
    public static final String ELEMENT_MISSING_TARGET = "on_missing_target"; //$NON-NLS-1$

    /**
     * The element name of missing incompatible type handling strategy.
     */
    public static final String ELEMENT_INCOMPATIBLE_TYPE = "on_incompatible_type"; //$NON-NLS-1$

    static final long MINIMUM_STRIPE_SIZE = 1L * 1024 * 1024;

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        OrcFileTrait trait = analyzeElements(environment, attribute, elements);
        declaration.putTrait(OrcFileTrait.class, trait);

        HiveDataModelTrait baseTrait = HiveDataModelTrait.get(declaration);
        baseTrait.addDataFormatNamer(new Namer() {
            @Override
            public Name computeName(EmitContext context, ModelDeclaration model) {
                return OrcFileEmitter.getClassName(context, model);
            }
        });
        baseTrait.setOriginalAst(attribute, false);
    }

    private OrcFileTrait analyzeElements(
            DmdlSemantics environment,
            AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        OrcFileTrait result = new OrcFileTrait();
        result.setOriginalAst(attribute, true);

        consumeTableName(environment, attribute, elements, result);

        consumeFormatVersion(environment, attribute, elements, result);
        consumeCompressionKind(environment, attribute, elements, result);
        consumeStripeSize(environment, attribute, elements, result);

        consumeFieldMapping(environment, attribute, elements, result);
        consumeMissingSource(environment, attribute, elements, result);
        consumeMissingTarget(environment, attribute, elements, result);
        consumeIncompatibleType(environment, attribute, elements, result);

        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));

        return result;
    }

    private void consumeTableName(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, OrcFileTrait result) {
        AstLiteral tableName = take(environment, attribute, elements, ELEMENT_TABLE_NAME, LiteralKind.STRING);
        if (tableName != null) {
            String value = tableName.toStringValue();
            if (AttributeUtil.checkPresent(environment, tableName, label(ELEMENT_TABLE_NAME), value)) {
                result.setTableName(value);
            }
        }
    }

    private void consumeFormatVersion(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, OrcFileTrait result) {
        AstLiteral formatVersion = take(environment, attribute, elements, ELEMENT_FORMAT_VERSION, LiteralKind.STRING);
        if (formatVersion != null) {
            String symbol = formatVersion.toStringValue();
            try {
                OrcFile.Version value = OrcFile.Version.byName(symbol);
                result.configuration().withFormatVersion(value);
            } catch (IllegalArgumentException e) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        formatVersion,
                        "@{0}({1}) must be a valid ORCFile version number: {2}",
                        TARGET_NAME,
                        ELEMENT_FORMAT_VERSION,
                        symbol));
            }
        }
    }

    private void consumeCompressionKind(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, OrcFileTrait result) {
        CompressionKind option = consumeOption(
                environment, attribute, elements,
                ELEMENT_COMPRESSION_KIND, "compression name",
                CompressionKind.values());
        if (option != null) {
            result.configuration().withCompressionKind(option);
        }
    }

    private void consumeFieldMapping(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, OrcFileTrait result) {
        FieldMappingStrategy option = consumeOption(
                environment, attribute, elements,
                ELEMENT_FIELD_MAPPING, "field mapping strategy name",
                FieldMappingStrategy.values());
        if (option != null) {
            result.configuration().withFieldMappingStrategy(option);
        }
    }

    private void consumeMissingSource(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, OrcFileTrait result) {
        ExceptionHandlingStrategy option = consumeOption(
                environment, attribute, elements,
                ELEMENT_MISSING_SOURCE, "exception handling strategy name",
                ExceptionHandlingStrategy.values());
        if (option != null) {
            result.configuration().withOnMissingSource(option);
        }
    }

    private void consumeMissingTarget(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, OrcFileTrait result) {
        ExceptionHandlingStrategy option = consumeOption(
                environment, attribute, elements,
                ELEMENT_MISSING_TARGET, "exception handling strategy name",
                ExceptionHandlingStrategy.values());
        if (option != null) {
            result.configuration().withOnMissingTarget(option);
        }
    }

    private void consumeIncompatibleType(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, OrcFileTrait result) {
        ExceptionHandlingStrategy option = consumeOption(
                environment, attribute, elements,
                ELEMENT_INCOMPATIBLE_TYPE, "exception handling strategy name",
                ExceptionHandlingStrategy.values());
        if (option != null) {
            result.configuration().withOnIncompatibleType(option);
        }
    }

    private <T extends Enum<?>> T consumeOption(
            DmdlSemantics environment, AstAttribute attribute, Map<String, AstAttributeElement> elements,
            String key, String description, T[] options) {
        AstLiteral literal = take(environment, attribute, elements, key, LiteralKind.STRING);
        if (literal != null) {
            String symbol = literal.toStringValue();
            T value = find(options, symbol);
            if (value == null) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        literal,
                        "@{0}({1}) must be a valid {2}: {3}",
                        TARGET_NAME,
                        key,
                        description,
                        symbol));
            } else {
                return value;
            }
        }
        return null;
    }

    private void consumeStripeSize(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, OrcFileTrait result) {
        AstLiteral stripeSize = take(environment, attribute, elements, ELEMENT_STRIPE_SIZE, LiteralKind.INTEGER);
        if (stripeSize != null) {
            String label = label(ELEMENT_STRIPE_SIZE);
            BigInteger value = stripeSize.toIntegerValue();
            if (AttributeUtil.checkRange(environment, stripeSize, label, value, MINIMUM_STRIPE_SIZE, Long.MAX_VALUE)) {
                result.configuration().withStripeSize(value.longValue());
            }
        }
    }

    private AstLiteral take(
            DmdlSemantics environment,
            AstAttribute attribute,
            Map<String, AstAttributeElement> elements,
            String elementName,
            LiteralKind kind) {
        return AttributeUtil.takeLiteral(environment, attribute, elements, elementName, kind, false);
    }

    private static String label(String key) {
        return String.format("@%s(%s)", TARGET_NAME, key);
    }

    private <T extends Enum<?>> T find(T[] values, String symbol) {
        for (T value : values) {
            if (value.name().equalsIgnoreCase(symbol)) {
                return value;
            }
        }
        return null;
    }
}
