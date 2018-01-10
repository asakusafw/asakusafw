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
package com.asakusafw.dmdl.directio.hive.parquet;

import java.math.BigInteger;
import java.util.Map;

import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import com.asakusafw.directio.hive.parquet.ParquetValueDriver;
import com.asakusafw.directio.hive.parquet.ParquetValueDrivers;
import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelMapping.FieldMappingStrategy;
import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.directio.hive.common.HiveDataModelTrait;
import com.asakusafw.dmdl.directio.hive.common.HiveFieldTrait;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

import parquet.column.ParquetProperties;
import parquet.hadoop.metadata.CompressionCodecName;

/**
 * Processes <code>&#64;directio.hive.parquet</code> attributes.
<h2>'&#64;directio.hive.parquet' attribute</h2>
The attributed declaration can have:
<ul>
<li> with {@code table_name=[string-literal]} as explicit table name (default: data model name) </li>
<li> with {@code format_version=[string-literal]} as parquet format version (default: system default) </li>
<li> with {@code compression=[string-literal]} as compression kind name (default: {@code "snappy"}) </li>
<li> with {@code block_size=integer} as block size (default: system default) </li>
<li> with {@code block_size=integer} as block size (default: system default) </li>
<li> with {@code data_page_size=integer} as data page size(default: system default) </li>
<li> with {@code dictionary_page_size=integer} as dictionary page size (default: system default) </li>
<li> with {@code enable_dictionary=boolean} as dictionary on/off (default: system default) </li>
<li> with {@code enable_validation=boolean} as validation on/off (default: system default) </li>
<li> with {@code field_mappping=[string-literal]}  (default: {@code "position"}) </li>
<li> with {@code missing_source=[string-literal]}  (default: {@code "logging"}) </li>
<li> with {@code missing_target=[string-literal]}  (default: {@code "logging"}) </li>
<li> with {@code incompatible_type=[string-literal]}  (default: {@code "fail"}) </li>
</ul>
 * @since 0.7.0
 */
public class ParquetFileDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "directio.hive.parquet"; //$NON-NLS-1$

    /**
     * The element name of explicit table name.
     */
    public static final String ELEMENT_TABLE_NAME = "table_name"; //$NON-NLS-1$

    /**
     * The element name of parquet format version.
     */
    public static final String ELEMENT_FORMAT_VERSION = "format_version"; //$NON-NLS-1$

    /**
     * The element name of compression codec name.
     */
    public static final String ELEMENT_COMPRESSION = "compression"; //$NON-NLS-1$

    /**
     * The element name of block size.
     */
    public static final String ELEMENT_BLOCK_SIZE = "block_size"; //$NON-NLS-1$

    /**
     * The element name of data page size.
     */
    public static final String ELEMENT_DATA_PAGE_SIZE = "data_page_size"; //$NON-NLS-1$

    /**
     * The element name of dictionary page size.
     */
    public static final String ELEMENT_DICTIONARY_PAGE_SIZE = "dictionary_page_size"; //$NON-NLS-1$

    /**
     * The element name of dictionary enabled.
     */
    public static final String ELEMENT_ENABLE_DICTIONARY = "enable_dictionary"; //$NON-NLS-1$

    /**
     * The element name of validation enabled.
     */
    public static final String ELEMENT_ENABLE_VALIDATION = "enable_validation"; //$NON-NLS-1$

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

    static final long MIN_PAGE_SIZE = 64L * 1024;

    static final long MIN_BLOCK_SIZE = 1L * 1024 * 1024;

    static final long MAX_SIZE = 1L * 1024 * 1024 * 1024;

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        ParquetFileTrait trait = analyzeElements(environment, attribute, elements);
        declaration.putTrait(ParquetFileTrait.class, trait);

        HiveDataModelTrait baseTrait = HiveDataModelTrait.get(declaration);
        baseTrait.addDataFormatNamer((context, model) -> ParquetFileEmitter.getClassName(context, model));
        baseTrait.setOriginalAst(attribute, false);
    }

    @Override
    public void verify(DmdlSemantics environment, ModelDeclaration declaration, AstAttribute attribute) {
        for (PropertyDeclaration property : declaration.getDeclaredProperties()) {
            if (HiveFieldTrait.get(property).isColumnPresent() == false) {
                continue;
            }
            Class<?> valueClass = EmitContext.getFieldTypeAsClass(property);
            TypeInfo typeInfo = HiveFieldTrait.getTypeInfo(property);
            ParquetValueDriver driver = ParquetValueDrivers.find(typeInfo, valueClass);
            if (driver == null) {
                environment.report(new Diagnostic(Diagnostic.Level.ERROR,
                        property.getOriginalAst(),
                        Messages.getString("ParquetFileDriver.diagnosticUnsupportedPropertyType"), //$NON-NLS-1$
                        typeInfo.getQualifiedName(),
                        property.getName().identifier,
                        property.getType()));
            }
        }
    }

    private ParquetFileTrait analyzeElements(
            DmdlSemantics environment,
            AstAttribute attribute,
            Map<String, AstAttributeElement> elements) {
        ParquetFileTrait result = new ParquetFileTrait();
        result.setOriginalAst(attribute, true);

        consumeTableName(environment, attribute, elements, result);

        consumeFormatVersion(environment, attribute, elements, result);
        consumeCompression(environment, attribute, elements, result);
        consumeBlockSize(environment, attribute, elements, result);
        consumeDataPageSize(environment, attribute, elements, result);
        consumeDictionaryPageSize(environment, attribute, elements, result);

        consumeEnableDictionary(environment, attribute, elements, result);
        consumeEnableValidation(environment, attribute, elements, result);

        consumeFieldMapping(environment, attribute, elements, result);
        consumeMissingSource(environment, attribute, elements, result);
        consumeMissingTarget(environment, attribute, elements, result);
        consumeIncompatibleType(environment, attribute, elements, result);

        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));

        return result;
    }

    private void consumeTableName(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
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
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
        AstLiteral formatVersion = take(environment, attribute, elements, ELEMENT_FORMAT_VERSION, LiteralKind.STRING);
        if (formatVersion != null) {
            String symbol = formatVersion.toStringValue();
            try {
                ParquetProperties.WriterVersion value = ParquetProperties.WriterVersion.fromString(symbol);
                result.configuration().withWriterVersion(value);
            } catch (IllegalArgumentException e) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        formatVersion,
                        Messages.getString("ParquetFileDriver.diagnosticUnknownElement"), //$NON-NLS-1$
                        TARGET_NAME,
                        ELEMENT_FORMAT_VERSION,
                        Messages.getString("ParquetFileDriver.labelVersion"), //$NON-NLS-1$
                        symbol));
            }
        }
    }

    private void consumeCompression(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
        CompressionCodecName option = consumeOption(
                environment, attribute, elements,
                ELEMENT_COMPRESSION, Messages.getString("ParquetFileDriver.labelCompression"), //$NON-NLS-1$
                CompressionCodecName.values());
        if (option != null) {
            result.configuration().withCompressionCodecName(option);
        }
    }

    private void consumeFieldMapping(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
        FieldMappingStrategy option = consumeOption(
                environment, attribute, elements,
                ELEMENT_FIELD_MAPPING, Messages.getString("ParquetFileDriver.labelFieldMappingStrategy"), //$NON-NLS-1$
                FieldMappingStrategy.values());
        if (option != null) {
            result.configuration().withFieldMappingStrategy(option);
        }
    }

    private void consumeMissingSource(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
        ExceptionHandlingStrategy option = consumeOption(
                environment, attribute, elements,
                ELEMENT_MISSING_SOURCE,
                Messages.getString("ParquetFileDriver.labelExceptionMappingStrategy"), //$NON-NLS-1$
                ExceptionHandlingStrategy.values());
        if (option != null) {
            result.configuration().withOnMissingSource(option);
        }
    }

    private void consumeMissingTarget(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
        ExceptionHandlingStrategy option = consumeOption(
                environment, attribute, elements,
                ELEMENT_MISSING_TARGET,
                Messages.getString("ParquetFileDriver.labelExceptionMappingStrategy"), //$NON-NLS-1$
                ExceptionHandlingStrategy.values());
        if (option != null) {
            result.configuration().withOnMissingTarget(option);
        }
    }

    private void consumeIncompatibleType(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
        ExceptionHandlingStrategy option = consumeOption(
                environment, attribute, elements,
                ELEMENT_INCOMPATIBLE_TYPE,
                Messages.getString("ParquetFileDriver.labelExceptionMappingStrategy"), //$NON-NLS-1$
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
                        Messages.getString("ParquetFileDriver.diagnosticUnknownElement"), //$NON-NLS-1$
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

    private void consumeBlockSize(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
        Integer size = consumeSize(
                environment, attribute, elements,
                ELEMENT_BLOCK_SIZE, MIN_BLOCK_SIZE, MAX_SIZE);
        if (size != null) {
            result.configuration().withBlockSize(size);
        }
    }

    private void consumeDataPageSize(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
        Integer size = consumeSize(
                environment, attribute, elements,
                ELEMENT_DATA_PAGE_SIZE, MIN_PAGE_SIZE, MAX_SIZE);
        if (size != null) {
            result.configuration().withDataPageSize(size);
        }
    }

    private void consumeDictionaryPageSize(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
        Integer size = consumeSize(
                environment, attribute, elements,
                ELEMENT_DICTIONARY_PAGE_SIZE, MIN_PAGE_SIZE, MAX_SIZE);
        if (size != null) {
            result.configuration().withDictionaryPageSize(size);
        }
    }

    private Integer consumeSize(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements,
            String key, long min, long max) {
        AstLiteral size = take(environment, attribute, elements, key, LiteralKind.INTEGER);
        if (size != null) {
            String label = label(key);
            BigInteger value = size.toIntegerValue();
            if (AttributeUtil.checkRange(environment, size, label, value, min, max)) {
                return value.intValue();
            }
        }
        return null;
    }

    private void consumeEnableDictionary(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
        AstLiteral value = take(environment, attribute, elements, ELEMENT_ENABLE_DICTIONARY, LiteralKind.BOOLEAN);
        if (value != null) {
            result.configuration().withEnableDictionary(value.toBooleanValue());
        }
    }

    private void consumeEnableValidation(
            DmdlSemantics environment, AstAttribute attribute,
            Map<String, AstAttributeElement> elements, ParquetFileTrait result) {
        AstLiteral value = take(environment, attribute, elements, ELEMENT_ENABLE_VALIDATION, LiteralKind.BOOLEAN);
        if (value != null) {
            result.configuration().withEnableValidation(value.toBooleanValue());
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
        return String.format("@%s(%s)", TARGET_NAME, key); //$NON-NLS-1$
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
