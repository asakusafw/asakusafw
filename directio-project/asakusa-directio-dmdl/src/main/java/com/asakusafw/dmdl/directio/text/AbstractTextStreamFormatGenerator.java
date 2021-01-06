/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.hadoop.io.compress.CompressionCodec;

import com.asakusafw.dmdl.directio.util.CharsetUtil;
import com.asakusafw.dmdl.directio.util.ClassName;
import com.asakusafw.dmdl.directio.util.Value;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.util.JavaName;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.dmdl.util.AttributeUtil;
import com.asakusafw.runtime.io.text.TextFormat;
import com.asakusafw.runtime.io.text.TextInput;
import com.asakusafw.runtime.io.text.directio.AbstractTextStreamFormat;
import com.asakusafw.runtime.io.text.driver.FieldDefinition;
import com.asakusafw.runtime.io.text.driver.RecordDefinition;
import com.asakusafw.runtime.io.text.value.BooleanOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.ByteOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.DateOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.DateTimeOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.DecimalOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.DoubleOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.FloatOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.IntOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.LongOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.ShortOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.StringOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.ValueOptionFieldAdapter;
import com.asakusafw.runtime.io.util.InputSplitter;
import com.asakusafw.runtime.io.util.InputSplitters;
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
 * Generates {@link AbstractTextStreamFormat}.
 * @since 0.9.1
 */
public abstract class AbstractTextStreamFormatGenerator {

    private static final Map<BasicTypeKind, Class<? extends ValueOptionFieldAdapter<?>>> ADAPTER_TYPES;
    static {
        Map<BasicTypeKind, Class<? extends ValueOptionFieldAdapter<?>>> map = new EnumMap<>(BasicTypeKind.class);
        map.put(BasicTypeKind.BYTE, ByteOptionFieldAdapter.class);
        map.put(BasicTypeKind.SHORT, ShortOptionFieldAdapter.class);
        map.put(BasicTypeKind.INT, IntOptionFieldAdapter.class);
        map.put(BasicTypeKind.LONG, LongOptionFieldAdapter.class);
        map.put(BasicTypeKind.FLOAT, FloatOptionFieldAdapter.class);
        map.put(BasicTypeKind.DOUBLE, DoubleOptionFieldAdapter.class);
        map.put(BasicTypeKind.DECIMAL, DecimalOptionFieldAdapter.class);
        map.put(BasicTypeKind.TEXT, StringOptionFieldAdapter.class);
        map.put(BasicTypeKind.BOOLEAN, BooleanOptionFieldAdapter.class);
        map.put(BasicTypeKind.DATE, DateOptionFieldAdapter.class);
        map.put(BasicTypeKind.DATETIME, DateTimeOptionFieldAdapter.class);
        ADAPTER_TYPES = map;
    }

    /**
     * The current context.
     */
    protected final EmitContext context;

    /**
     * The target model.
     */
    protected final ModelDeclaration model;

    private final ModelFactory f;

    private final TextFormatSettings formatSettings;

    private final TextFieldSettings fieldDefaultSettings;

    /**
     * Creates a new instance.
     * @param context the current context
     * @param model the target model
     * @param formatSettings the text format settings
     * @param fieldDefaultSettings the field default settings
     */
    public AbstractTextStreamFormatGenerator(
            EmitContext context, ModelDeclaration model,
            TextFormatSettings formatSettings, TextFieldSettings fieldDefaultSettings) {
        this.context = context;
        this.model = model;
        this.formatSettings = formatSettings;
        this.fieldDefaultSettings = fieldDefaultSettings;
        this.f = context.getModelFactory();
    }

    /**
     * Emits an implementation of {@link AbstractTextStreamFormat} class as a Java compilation unit.
     * @param description the format description
     * @throws IOException if I/O error was occurred while emitting the compilation unit
     */
    protected void emit(String description) throws IOException {
        ClassDeclaration decl = f.newClassDeclaration(
                new JavadocBuilder(f)
                    .inline(Messages.getString("AbstractTextStreamFormatGenerator.javadocClassOverview"), //$NON-NLS-1$
                            d -> d.text(description),
                            d -> d.linkType(context.resolve(model.getSymbol())))
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                context.getTypeName(),
                f.newParameterizedType(
                        context.resolve(AbstractTextStreamFormat.class),
                        context.resolve(model.getSymbol())),
                Collections.emptyList(),
                createMembers());
        context.emit(decl);
    }

    private List<? extends TypeBodyDeclaration> createMembers() {
        List<TypeBodyDeclaration> results = new ArrayList<>();
        results.add(createGetSupportedType());
        results.add(createCreateTextFormat());
        results.addAll(createCreateRecordDefinition());
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

    private MethodDeclaration createCreateTextFormat() {
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                context.resolve(TextFormat.class),
                f.newSimpleName("createTextFormat"), //$NON-NLS-1$
                Collections.emptyList(),
                createGetTextFormatInternal());
    }

    /**
     * Returns a body of {@link AbstractTextStreamFormat#getTextFormat()}.
     * @return the body statements
     */
    protected abstract List<Statement> createGetTextFormatInternal();

    private List<MethodDeclaration> createCreateRecordDefinition() {
        SimpleName builder = f.newSimpleName("builder"); //$NON-NLS-1$
        List<Statement> statements = new ArrayList<>();
        statements.add(new TypeBuilder(f, context.resolve(RecordDefinition.class))
                .method("builder", f.newClassLiteral(context.resolve(model.getSymbol()))) //$NON-NLS-1$
                .toLocalVariableDeclaration(
                        f.newParameterizedType(
                                context.resolve(RecordDefinition.Builder.class),
                                context.resolve(model.getSymbol())),
                        builder));
        List<MethodDeclaration> fields = buildRecordDefinition(statements, builder);
        statements.add(new ExpressionBuilder(f, builder)
                .method("build") //$NON-NLS-1$
                .toReturnStatement());
        List<MethodDeclaration> results = new ArrayList<>();
        results.add(f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Protected()
                    .toAttributes(),
                f.newParameterizedType(
                        context.resolve(RecordDefinition.class),
                        context.resolve(model.getSymbol())),
                f.newSimpleName("createRecordDefinition"), //$NON-NLS-1$
                Collections.emptyList(),
                statements));
        results.addAll(fields);
        return results;
    }

    private List<MethodDeclaration> buildRecordDefinition(List<Statement> statements, SimpleName builder) {
        formatSettings.getHeaderType().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withHeaderType", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        formatSettings.getLessInputAction().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withOnLessInput", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        formatSettings.getMoreInputAction().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withOnMoreInput", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        fieldDefaultSettings.getTrimInputWhitespaces().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withTrimInput", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        fieldDefaultSettings.getSkipEmptyInput().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withSkipEmptyInput", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        fieldDefaultSettings.getMalformedInputAction().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withOnMalformedInput", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        fieldDefaultSettings.getUnmappableOutputAction().ifPresent(v -> statements.add(
                new ExpressionBuilder(f, builder)
                    .method("withOnUnmappableOutput", resolve(v)) //$NON-NLS-1$
                    .toStatement()));
        List<MethodDeclaration> fields = new ArrayList<>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            if (TextFieldTrait.getKind(property) != TextFieldTrait.Kind.VALUE) {
                continue;
            }
            MethodDeclaration method = createGetFieldDefinition(property);
            fields.add(method);
            statements.add(new ExpressionBuilder(f, builder)
                    .method("withField", //$NON-NLS-1$
                            new TypeBuilder(f, context.resolve(model.getSymbol()))
                                .methodReference(context.getOptionGetterName(property))
                                .toExpression(),
                            new ExpressionBuilder(f, f.newThis())
                                .method(method.getName())
                                .toExpression())
                    .toStatement());
        }
        return fields;
    }

    private MethodDeclaration createGetFieldDefinition(PropertyDeclaration property) {
        SimpleName builder = f.newSimpleName("builder"); //$NON-NLS-1$
        List<Statement> statements = new ArrayList<>();
        statements.add(new TypeBuilder(f, context.resolve(FieldDefinition.class))
                .method("builder", //$NON-NLS-1$
                        resolve(TextFieldTrait.getName(property)),
                        buildFieldAdapter(property))
                .toLocalVariableDeclaration(
                        f.newParameterizedType(
                                context.resolve(FieldDefinition.Builder.class),
                                context.getFieldType(property)),
                        builder));
        TextFieldSettings settings = TextFieldTrait.getSettings(property);
        settings.getTrimInputWhitespaces().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withTrimInput", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        settings.getSkipEmptyInput().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withSkipEmptyInput", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        settings.getMalformedInputAction().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withOnMalformedInput", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        settings.getUnmappableOutputAction().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withOnUnmappableOutput", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        settings.getQuoteStyle().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                .method("withOutputOption", resolve(v)) //$NON-NLS-1$
                .toStatement()));
        statements.add(new ExpressionBuilder(f, builder)
                .method("build") //$NON-NLS-1$
                .toReturnStatement());
        JavaName name = JavaName.of(property.getName());
        name.addFirst("get"); //$NON-NLS-1$
        name.addLast("field"); //$NON-NLS-1$
        name.addLast("definition"); //$NON-NLS-1$
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .inline(Messages.getString("AbstractTextStreamFormatGenerator.javafocGetFieldDefinitionOverview"), //$NON-NLS-1$
                            d -> d.linkMethod(
                                    context.resolve(model.getSymbol()),
                                    context.getOptionGetterName(property)))
                    .returns()
                        .text(Messages.getString("AbstractTextStreamFormatGenerator.javadocGetFieldDefinitionReturn")) //$NON-NLS-1$
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Protected()
                    .toAttributes(),
                f.newParameterizedType(
                        context.resolve(FieldDefinition.class),
                        context.getFieldType(property)),
                f.newSimpleName(name.toMemberName()),
                Collections.emptyList(),
                statements);
    }

    private Expression buildFieldAdapter(PropertyDeclaration property) {
        TextFieldSettings settings = TextFieldTrait.getSettings(property);
        Value<ClassName> adapterClass = setting(settings, TextFieldSettings::getAdapterClass);
        if (adapterClass.isPresent()) {
            return new TypeBuilder(f, resolve(adapterClass.getEntity()))
                    .constructorReference()
                    .toExpression();
        }

        BasicTypeKind kind = ((BasicType) property.getType()).getKind();
        Class<? extends ValueOptionFieldAdapter<?>> basicAdapterClass = ADAPTER_TYPES.get(kind);
        assert basicAdapterClass != null;

        ExpressionBuilder builder = new TypeBuilder(f, context.resolve(basicAdapterClass)).method("builder"); //$NON-NLS-1$
        setting(settings, TextFieldSettings::getNullFormat).ifPresent(v -> builder
                .method("withNullFormat", resolve(v))); //$NON-NLS-1$
        switch (kind) {
        case BOOLEAN:
            setting(settings, TextFieldSettings::getTrueFormat).ifPresent(v -> builder
                    .method("withTrueFormat", resolve(v))); //$NON-NLS-1$
            setting(settings, TextFieldSettings::getFalseFormat).ifPresent(v -> builder
                    .method("withFalseFormat", resolve(v))); //$NON-NLS-1$
            break;
        case DATE:
            setting(settings, TextFieldSettings::getDateFormat).ifPresent(v -> builder
                    .method("withDateFormat", resolve(v.toString()))); //$NON-NLS-1$
            break;
        case DATETIME:
            setting(settings, TextFieldSettings::getDateTimeFormat).ifPresent(v -> builder
                    .method("withDateTimeFormat", resolve(v.toString()))); //$NON-NLS-1$
            setting(settings, TextFieldSettings::getTimeZone).ifPresent(v -> builder
                    .method("withTimeZone", resolve(v.getId()))); //$NON-NLS-1$
            break;
        case DECIMAL:
            setting(settings, TextFieldSettings::getNumberFormat).ifPresent(v -> builder
                    .method("withNumberFormat", resolve(v.toString()))); //$NON-NLS-1$
            setting(settings, TextFieldSettings::getDecimalOutputStyle).ifPresent(v -> builder
                    .method("withOutputStyle", resolve(v))); //$NON-NLS-1$
            break;
        case BYTE:
        case INT:
        case SHORT:
        case LONG:
        case FLOAT:
        case DOUBLE:
            setting(settings, TextFieldSettings::getNumberFormat).ifPresent(v -> builder
                    .method("withNumberFormat", resolve(v.toString()))); //$NON-NLS-1$
            break;
        case TEXT:
            // no special members
            break;
        default:
            throw new AssertionError(kind);
        }
        return builder.method("lazy").toExpression(); //$NON-NLS-1$
    }

    private <T> Value<T> setting(TextFieldSettings settings, Function<TextFieldSettings, Value<T>> getter) {
        return getter.apply(settings).orDefault(getter.apply(fieldDefaultSettings));
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
        if (formatSettings.getCharset().isPresent()) {
            if (!CharsetUtil.isAsciiCompatible(formatSettings.getCharset().getEntity())) {
                return false;
            }
        }
        if (formatSettings.getCompressionType().isPresent()) {
            return false;
        }
        if (model.getDeclaredProperties().stream()
                .map(TextFieldTrait::getKind)
                .anyMatch(Predicate.isEqual(TextFieldTrait.Kind.LINE_NUMBER)
                        .or(Predicate.isEqual(TextFieldTrait.Kind.RECORD_NUMBER)))) {
            return false;
        }
        return isSplittableInternal();
    }

    /**
     * Returns whether or not the input is splittable.
     * @return {@code true} if it is splittable, otherwise {@code false}
     */
    protected abstract boolean isSplittableInternal();

    private Optional<MethodDeclaration> createGetCompressionCodecClass() {
        if (formatSettings.getCompressionType().isPresent()) {
            ClassName codec = formatSettings.getCompressionType().getEntity();
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
            switch (TextFieldTrait.getKind(property)) {
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
                throw new AssertionError(TextFieldTrait.getKind(property));
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
                                            context.resolve(TextInput.class),
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

    /**
     * Resolves a value.
     * @param value the value
     * @return the resolved expression
     */
    protected Expression resolve(boolean value) {
        return Models.toLiteral(f, value);
    }

    /**
     * Resolves a value.
     * @param value the value
     * @return the resolved expression
     */
    protected Expression resolve(char value) {
        return Models.toLiteral(f, value);
    }

    /**
     * Resolves a value.
     * @param value the value
     * @return the resolved expression
     */
    protected Expression resolve(String value) {
        return Models.toLiteral(f, value);
    }

    /**
     * Resolves a value.
     * @param value the value
     * @return the resolved expression
     */
    protected Expression resolve(Enum<?> value) {
        return new TypeBuilder(f, context.resolve(value.getDeclaringClass()))
                .field(value.name())
                .toExpression();
    }

    /**
     * Resolves a value.
     * @param type the value
     * @return the resolved expression
     */
    protected Type resolve(ClassName type) {
        return context.resolve(Models.toName(f, type.toString()));
    }
}
