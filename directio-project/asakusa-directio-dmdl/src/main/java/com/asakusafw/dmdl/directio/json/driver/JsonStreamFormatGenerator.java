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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.hadoop.io.compress.CompressionCodec;

import com.asakusafw.dmdl.directio.json.driver.JsonFormatConstants.DecimalStyle;
import com.asakusafw.dmdl.directio.util.ClassName;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.util.JavaName;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.dmdl.util.AttributeUtil;
import com.asakusafw.runtime.io.json.JsonFormat;
import com.asakusafw.runtime.io.json.JsonInput;
import com.asakusafw.runtime.io.json.PropertyDefinition;
import com.asakusafw.runtime.io.json.directio.AbstractJsonStreamFormat;
import com.asakusafw.runtime.io.json.directio.InputSplitter;
import com.asakusafw.runtime.io.json.directio.InputSplitters;
import com.asakusafw.runtime.io.json.value.BooleanOptionPropertyAdapter;
import com.asakusafw.runtime.io.json.value.ByteOptionPropertyAdapter;
import com.asakusafw.runtime.io.json.value.DateOptionPropertyAdapter;
import com.asakusafw.runtime.io.json.value.DateTimeOptionPropertyAdapter;
import com.asakusafw.runtime.io.json.value.DecimalOptionPropertyAdapter;
import com.asakusafw.runtime.io.json.value.DoubleOptionPropertyAdapter;
import com.asakusafw.runtime.io.json.value.FloatOptionPropertyAdapter;
import com.asakusafw.runtime.io.json.value.IntOptionPropertyAdapter;
import com.asakusafw.runtime.io.json.value.LongOptionPropertyAdapter;
import com.asakusafw.runtime.io.json.value.ShortOptionPropertyAdapter;
import com.asakusafw.runtime.io.json.value.StringOptionPropertyAdapter;
import com.asakusafw.runtime.io.json.value.ValueOptionPropertyAdapter;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Generates an implementation of {@link AbstractJsonStreamFormat}.
 * @since 0.9.1
 */
public class JsonStreamFormatGenerator {

    private static final Map<BasicTypeKind, Class<? extends ValueOptionPropertyAdapter<?>>> ADAPTER_TYPES;
    static {
        Map<BasicTypeKind, Class<? extends ValueOptionPropertyAdapter<?>>> map = new EnumMap<>(BasicTypeKind.class);
        map.put(BasicTypeKind.BYTE, ByteOptionPropertyAdapter.class);
        map.put(BasicTypeKind.SHORT, ShortOptionPropertyAdapter.class);
        map.put(BasicTypeKind.INT, IntOptionPropertyAdapter.class);
        map.put(BasicTypeKind.LONG, LongOptionPropertyAdapter.class);
        map.put(BasicTypeKind.FLOAT, FloatOptionPropertyAdapter.class);
        map.put(BasicTypeKind.DOUBLE, DoubleOptionPropertyAdapter.class);
        map.put(BasicTypeKind.DECIMAL, DecimalOptionPropertyAdapter.class);
        map.put(BasicTypeKind.TEXT, StringOptionPropertyAdapter.class);
        map.put(BasicTypeKind.BOOLEAN, BooleanOptionPropertyAdapter.class);
        map.put(BasicTypeKind.DATE, DateOptionPropertyAdapter.class);
        map.put(BasicTypeKind.DATETIME, DateTimeOptionPropertyAdapter.class);
        ADAPTER_TYPES = map;
    }

    private static final Pattern PATTERN_ASCII_NOT_COMPAT = Pattern.compile("\\bUTF-(16|32)(BE|LE)?\\b"); //$NON-NLS-1$

    private static final Set<Charset> KNOWN_ASCII_NOT_COMPAT = Charset.availableCharsets().values().stream()
            .filter(s -> PATTERN_ASCII_NOT_COMPAT.matcher(s.name()).find())
            .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));

    /**
     * The current context.
     */
    protected final EmitContext context;

    /**
     * The target model.
     */
    protected final ModelDeclaration model;

    private final ModelFactory f;

    private final JsonStreamSettings streamSettings;

    private final JsonFormatSettings formatSettings;

    private final JsonPropertySettings fieldDefaultSettings;

    /**
     * Creates a new instance.
     * @param context the current context
     * @param model the target model
     * @param streamSettings the JSON I/O stream settings
     * @param formatSettings the JSON format settings
     * @param fieldDefaultSettings the field default settings
     */
    public JsonStreamFormatGenerator(
            EmitContext context, ModelDeclaration model,
            JsonStreamSettings streamSettings,
            JsonFormatSettings formatSettings,
            JsonPropertySettings fieldDefaultSettings) {
        this.context = context;
        this.model = model;
        this.streamSettings = streamSettings;
        this.formatSettings = formatSettings;
        this.fieldDefaultSettings = fieldDefaultSettings;
        this.f = context.getModelFactory();
    }

    /**
     * Emits an implementation of {@link AbstractJsonStreamFormat} class as a Java compilation unit.
     * @throws IOException if I/O error was occurred while emitting the compilation unit
     */
    protected void emit() throws IOException {
        ClassDeclaration decl = f.newClassDeclaration(
                new JavadocBuilder(f)
                    .inline(Messages.getString("JsonStreamFormatGenerator.javadocClassOverview"), //$NON-NLS-1$
                            d -> d.linkType(context.resolve(model.getSymbol())))
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                context.getTypeName(),
                f.newParameterizedType(
                        context.resolve(AbstractJsonStreamFormat.class),
                        context.resolve(model.getSymbol())),
                Collections.emptyList(),
                createMembers());
        context.emit(decl);
    }

    private List<? extends TypeBodyDeclaration> createMembers() {
        List<TypeBodyDeclaration> results = new ArrayList<>();
        results.add(createGetSupportedType());
        results.addAll(createConfigureJsonFormat());
        createGetInputSplitter().ifPresent(results::add);
        createGetCompressionCodecClass().ifPresent(results::add);
        createAfterInput().ifPresent(results::add);
        createBeforeOutput().ifPresent(results::add);
        return results;
    }

    private MethodDeclaration createGetSupportedType() {
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                f.newParameterizedType(
                        context.resolve(Class.class),
                        context.resolve(model.getSymbol())),
                f.newSimpleName("getSupportedType"), //$NON-NLS-1$
                Collections.emptyList(),
                Arrays.asList(new TypeBuilder(f, context.resolve(model.getSymbol()))
                    .dotClass()
                    .toReturnStatement()));
    }

    private List<MethodDeclaration> createConfigureJsonFormat() {
        SimpleName builder = f.newSimpleName("builder"); //$NON-NLS-1$
        List<Statement> statements = new ArrayList<>();

        List<MethodDeclaration> properties = new ArrayList<>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            if (JsonPropertyTrait.getKind(property) != JsonPropertyTrait.Kind.VALUE) {
                continue;
            }
            MethodDeclaration method = createGetPropertyDefinition(property);
            properties.add(method);
            statements.add(new ExpressionBuilder(f, builder)
                    .method("withProperty", //$NON-NLS-1$
                            new TypeBuilder(f, context.resolve(model.getSymbol()))
                                .methodReference(context.getOptionGetterName(property))
                                .toExpression(),
                            f.newMethodInvocationExpression(null, method.getName()))
                    .toStatement());
        }

        formatSettings.getCharsetName().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withCharset", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        formatSettings.getLineSeparator().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withLineSeparator", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        formatSettings.getDecimalStyle().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withUsePlainDecimal", resolve(v == DecimalStyle.PLAIN)) //$NON-NLS-1$
                .toStatement()));
        formatSettings.getEscapeNoAscii().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withEscapeNoAsciiCharacter", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        formatSettings.getUnknownPropertyAction().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withOnUnknownInput", resolve(v)) //$NON-NLS-1$
                .toStatement()));

        List<MethodDeclaration> results = new ArrayList<>();
        results.add(f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Protected()
                    .toAttributes(),
                context.resolve(void.class),
                f.newSimpleName("configureJsonFormat"), //$NON-NLS-1$
                Collections.singletonList(f.newFormalParameterDeclaration(
                        f.newParameterizedType(
                                context.resolve(JsonFormat.Builder.class),
                                context.resolve(model.getSymbol())),
                        builder)),
                statements));
        results.addAll(properties);
        return results;
    }

    private MethodDeclaration createGetPropertyDefinition(PropertyDeclaration property) {
        SimpleName builder = f.newSimpleName("builder"); //$NON-NLS-1$
        List<Statement> statements = new ArrayList<>();
        statements.add(new TypeBuilder(f, context.resolve(PropertyDefinition.class))
                .method("builder", //$NON-NLS-1$
                        resolve(JsonPropertyTrait.getName(property)),
                        buildFieldAdapter(property))
                .toLocalVariableDeclaration(
                        f.newParameterizedType(
                                context.resolve(PropertyDefinition.Builder.class),
                                context.getFieldType(property)),
                        builder));
        JsonPropertySettings settings = getPropertySettings(property);
        settings.getMalformedInputAction().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withOnMalformedInput", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        settings.getMissingInputAction().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withOnMissingInput", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        statements.add(new ExpressionBuilder(f, builder)
                .method("build") //$NON-NLS-1$
                .toReturnStatement());
        JavaName name = JavaName.of(property.getName());
        name.addFirst("get"); //$NON-NLS-1$
        name.addLast("property"); //$NON-NLS-1$
        name.addLast("definition"); //$NON-NLS-1$
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .inline(Messages.getString("JsonStreamFormatGenerator.javadocGetPropertyDefinitionOverview"), //$NON-NLS-1$
                            d -> d.linkType(context.resolve(PropertyDefinition.class)),
                            d -> d.linkMethod(
                                    context.resolve(model.getSymbol()),
                                    context.getOptionGetterName(property)))
                    .returns()
                        .text(Messages.getString("JsonStreamFormatGenerator.javadocGetPropertyDefinitionReturn")) //$NON-NLS-1$
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Protected()
                    .toAttributes(),
                f.newParameterizedType(
                        context.resolve(PropertyDefinition.class),
                        context.getFieldType(property)),
                f.newSimpleName(name.toMemberName()),
                Collections.emptyList(),
                statements);
    }

    private JsonPropertySettings getPropertySettings(PropertyDeclaration property) {
        return JsonPropertyTrait.getSettings(property).mergeDefaults(fieldDefaultSettings);
    }

    private Expression buildFieldAdapter(PropertyDeclaration property) {
        JsonPropertySettings settings = getPropertySettings(property);
        if (settings.getAdapterClass().isPresent()) {
            return new TypeBuilder(f, resolve(settings.getAdapterClass().getEntity()))
                    .constructorReference()
                    .toExpression();
        }

        BasicTypeKind kind = ((BasicType) property.getType()).getKind();
        Class<? extends ValueOptionPropertyAdapter<?>> basicAdapterClass = ADAPTER_TYPES.get(kind);
        assert basicAdapterClass != null;

        ExpressionBuilder builder = new TypeBuilder(f, context.resolve(basicAdapterClass)).method("builder"); //$NON-NLS-1$
        settings.getNullStyle().ifPresent(v -> builder
                .method("withNullStyle", resolve(v))); //$NON-NLS-1$
        switch (kind) {
        case DATE:
            settings.getDateFormat().ifPresent(v -> builder
                    .method("withDateFormat", resolve(v.toString()))); //$NON-NLS-1$
            break;

        case DATETIME:
            settings.getDateTimeFormat().ifPresent(v -> builder
                    .method("withDateTimeFormat", resolve(v.toString()))); //$NON-NLS-1$
            settings.getTimeZone().ifPresent(v -> builder
                    .method("withTimeZone", resolve(v.getId()))); //$NON-NLS-1$
            break;

        case BOOLEAN:
        case BYTE:
        case INT:
        case SHORT:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case DECIMAL:
        case TEXT:
            // no special members
            break;
        default:
            throw new AssertionError(kind);
        }
        return builder.method("lazy").toExpression(); //$NON-NLS-1$
    }

    private Optional<MethodDeclaration> createGetInputSplitter() {
        if (isSplittable()) {
            return Optional.of(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Protected()
                        .toAttributes(),
                    context.resolve(InputSplitter.class),
                    f.newSimpleName("getInputSplitter"), //$NON-NLS-1$
                    Collections.emptyList(),
                    Arrays.asList(new TypeBuilder(f, context.resolve(InputSplitters.class))
                            .method("byLineFeed") //$NON-NLS-1$
                            .toReturnStatement())));
        } else {
            return Optional.empty();
        }
    }

    private boolean isSplittable() {
        if (isSplittable(streamSettings.getFormatKind().orElse(JsonFormatConstants.DEFAULT_FORMAT_KIND)) == false) {
            return false;
        }
        if (streamSettings.getCompressionType().isPresent()) {
            return false;
        }
        if (formatSettings.getCharsetName().isPresent()) {
            if (KNOWN_ASCII_NOT_COMPAT.contains(formatSettings.getCharsetName().getEntity())) {
                return false;
            }
        }
        if (model.getDeclaredProperties().stream()
                .map(JsonPropertyTrait::getKind)
                .anyMatch(it -> it == JsonPropertyTrait.Kind.LINE_NUMBER
                             || it == JsonPropertyTrait.Kind.RECORD_NUMBER)) {
            return false;
        }
        return true;
    }

    private static boolean isSplittable(JsonFormatConstants.JsonFormatKind kind) {
        return kind == JsonFormatConstants.JsonFormatKind.JSONL;
    }

    private Optional<MethodDeclaration> createGetCompressionCodecClass() {
        if (streamSettings.getCompressionType().isPresent()) {
            ClassName codec = streamSettings.getCompressionType().getEntity();
            return Optional.of(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Protected()
                        .toAttributes(),
                    new TypeBuilder(f, context.resolve(Class.class))
                        .parameterize(f.newWildcardExtends(context.resolve(CompressionCodec.class)))
                        .toType(),
                    f.newSimpleName("getCompressionCodecClass"), //$NON-NLS-1$
                    Collections.emptyList(),
                    Arrays.asList(new TypeBuilder(f, resolve(codec))
                            .dotClass()
                            .toReturnStatement())));
        } else {
            return Optional.empty();
        }
    }

    private Optional<MethodDeclaration> createAfterInput() {
        SimpleName object = f.newSimpleName("object"); //$NON-NLS-1$
        SimpleName path = f.newSimpleName("path"); //$NON-NLS-1$
        SimpleName input = f.newSimpleName("input"); //$NON-NLS-1$
        List<Statement> statements = new ArrayList<>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            switch (JsonPropertyTrait.getKind(property)) {
            case VALUE:
                break; // does nothing
            case IGNORE:
                statements.add(new ExpressionBuilder(f, object)
                        .method(context.getOptionSetterName(property), Models.toNullLiteral(f))
                        .toStatement());
                break;
            case FILE_NAME:
                statements.add(new ExpressionBuilder(f, object)
                        .method(context.getOptionSetterName(property), path)
                        .toStatement());
                break;
            case LINE_NUMBER:
                statements.add(new ExpressionBuilder(f, object)
                        .method(context.getValueSetterName(property),
                                adjustLong(property, new ExpressionBuilder(f, input)
                                        .method("getLineNumber") //$NON-NLS-1$
                                        .apply(InfixOperator.PLUS, Models.toLiteral(f, 1L))))
                        .toStatement());
                break;
            case RECORD_NUMBER:
                statements.add(new ExpressionBuilder(f, object)
                        .method(context.getValueSetterName(property),
                                adjustLong(property, new ExpressionBuilder(f, input)
                                        .method("getRecordIndex") //$NON-NLS-1$
                                        .apply(InfixOperator.PLUS, Models.toLiteral(f, 1L))))
                        .toStatement());
                break;
            default:
                throw new AssertionError(JsonPropertyTrait.getKind(property));
            }
        }
        if (statements.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Protected()
                        .toAttributes(),
                    context.resolve(void.class),
                    f.newSimpleName("afterInput"), //$NON-NLS-1$
                    Arrays.asList(
                            f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), object),
                            f.newFormalParameterDeclaration(context.resolve(StringOption.class), path),
                            f.newFormalParameterDeclaration(
                                    f.newParameterizedType(
                                            context.resolve(JsonInput.class),
                                            context.resolve(model.getSymbol())),
                                    input)),
                    statements));
        }
    }

    private Expression adjustLong(PropertyDeclaration property, ExpressionBuilder builder) {
        if (AttributeUtil.hasFieldType(property, BasicTypeKind.LONG)) {
            return builder.toExpression();
        } else if (AttributeUtil.hasFieldType(property, BasicTypeKind.INT)) {
            return builder.castTo(context.resolve(int.class)).toExpression();
        } else {
            throw new AssertionError(property);
        }
    }

    private Optional<MethodDeclaration> createBeforeOutput() {
        return Optional.empty();
    }

    private Expression resolve(boolean value) {
        return Models.toLiteral(f, value);
    }

    private Expression resolve(String value) {
        return Models.toLiteral(f, value);
    }

    private Expression resolve(Enum<?> value) {
        return new TypeBuilder(f, context.resolve(value.getDeclaringClass()))
                .field(value.name())
                .toExpression();
    }

    private Type resolve(ClassName type) {
        return context.resolve(Models.toName(f, type.toString()));
    }

    private Expression resolve(Charset value) {
        return new TypeBuilder(f, context.resolve(Charset.class))
                .method("forName", Models.toLiteral(f, value.name())) //$NON-NLS-1$
                .toExpression();
    }
}
