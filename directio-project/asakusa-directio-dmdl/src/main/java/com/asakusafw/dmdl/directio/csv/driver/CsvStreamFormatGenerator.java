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
package com.asakusafw.dmdl.directio.csv.driver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.hadoop.io.compress.CompressionCodec;

import com.asakusafw.dmdl.directio.csv.driver.CsvFieldTrait.Kind;
import com.asakusafw.dmdl.directio.util.CharsetUtil;
import com.asakusafw.dmdl.directio.util.CodecNames;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.runtime.io.csv.CsvConfiguration;
import com.asakusafw.runtime.io.csv.CsvEmitter;
import com.asakusafw.runtime.io.csv.CsvParser;
import com.asakusafw.runtime.io.csv.directio.AbstractCsvStreamFormat;
import com.asakusafw.runtime.io.util.InputSplitter;
import com.asakusafw.runtime.io.util.InputSplitters;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Generates an implementation of {@link AbstractCsvStreamFormat} class.
 * @since 0.10.3
 */
public class CsvStreamFormatGenerator {

    private final EmitContext context;

    private final ModelDeclaration model;

    private final CsvFormatTrait.Configuration conf;

    private final ModelFactory f;

    /**
     * creates a new instance.
     * @param context the current context
     * @param model the target model
     * @param configuration the format configuration
     */
    public CsvStreamFormatGenerator(
            EmitContext context,
            ModelDeclaration model,
            CsvFormatTrait.Configuration configuration) {
        assert context != null;
        assert model != null;
        assert configuration != null;
        this.context = context;
        this.model = model;
        this.conf = configuration;
        this.f = context.getModelFactory();
    }

    /**
     * Emits an implementation of {@link AbstractCsvStreamFormat} class as a Java compilation unit.
     * @throws IOException if I/O error was occurred while emitting the compilation unit
     */
    public void emit() throws IOException {
        ClassDeclaration decl = f.newClassDeclaration(
                new JavadocBuilder(f)
                    .inline("CSV format for {0}.",
                            d -> d.linkType(context.resolve(model.getSymbol())))
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                context.getTypeName(),
                f.newParameterizedType(
                        context.resolve(AbstractCsvStreamFormat.class),
                        context.resolve(model.getSymbol())),
                Collections.emptyList(),
                createMembers());
        context.emit(decl);
    }

    private List<TypeBodyDeclaration> createMembers() {
        List<TypeBodyDeclaration> results = new ArrayList<>();
        results.add(createGetSupportedType());
        results.add(createGetConfiguration());
        results.add(createParse());
        results.add(createEmit());
        createGetInputSplitter().ifPresent(results::add);
        createGetCompressionCodecClass().ifPresent(results::add);
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

    private MethodDeclaration createGetConfiguration() {
        SimpleName head = f.newSimpleName("head"); //$NON-NLS-1$
        List<Statement> statements = new ArrayList<>();
        List<Expression> arguments = new ArrayList<>();
        arguments.add(new TypeBuilder(f, context.resolve(Charset.class))
            .method("forName", Models.toLiteral(f, conf.getCharsetName())) //$NON-NLS-1$
            .toExpression());
        if (conf.isEnableHeader() || conf.isForceHeader()) {
            SimpleName headers = f.newSimpleName("headers"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(ArrayList.class))
                .parameterize()
                .newObject()
                .toLocalVariableDeclaration(
                        new TypeBuilder(f, context.resolve(List.class))
                            .parameterize(context.resolve(String.class))
                            .toType(),
                        headers));
            List<Statement> headerStatements = new ArrayList<>();
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                if (CsvFieldTrait.getKind(property) == Kind.VALUE) {
                    String fieldName = CsvFieldTrait.getFieldName(property);
                    headerStatements.add(new ExpressionBuilder(f, headers)
                        .method("add", Models.toLiteral(f, fieldName)) //$NON-NLS-1$
                        .toStatement());
                }
            }
            statements.add(f.newIfStatement(head, f.newBlock(headerStatements)));
            arguments.add(headers);
        } else {
            arguments.add(new TypeBuilder(f, context.resolve(CsvConfiguration.class))
                .field("DEFAULT_HEADER_CELLS") //$NON-NLS-1$
                .toExpression());
        }
        arguments.add(Models.toLiteral(f, conf.getTrueFormat()));
        arguments.add(Models.toLiteral(f, conf.getFalseFormat()));
        arguments.add(Models.toLiteral(f, conf.getDateFormat()));
        arguments.add(Models.toLiteral(f, conf.getDateTimeFormat()));

        SimpleName config = f.newSimpleName("config"); //$NON-NLS-1$
        statements.add(new TypeBuilder(f, context.resolve(CsvConfiguration.class))
                    .newObject(arguments)
                    .toLocalVariableDeclaration(context.resolve(CsvConfiguration.class), config));
        if (conf.isForceHeader()) {
            statements.add(f.newIfStatement(head, f.newBlock(new ExpressionBuilder(f, config)
                    .method("setForceConsumeHeader", Models.toLiteral(f, conf.isForceHeader())) //$NON-NLS-1$
                    .toStatement())));
        }
        statements.add(new ExpressionBuilder(f, config)
            .method("setLineBreakInValue", Models.toLiteral(f, conf.isAllowLinefeed())) //$NON-NLS-1$
            .toStatement());
        createSetForceQuoteColumnsStatement(config).ifPresent(statements::add);
        statements.add(new ExpressionBuilder(f, config).toReturnStatement());
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Protected()
                    .toAttributes(),
                context.resolve(CsvConfiguration.class),
                f.newSimpleName("getConfiguration"), //$NON-NLS-1$
                Arrays.asList(
                        f.newFormalParameterDeclaration(context.resolve(boolean.class), head)),
                statements);
    }

    private Optional<Statement> createSetForceQuoteColumnsStatement(SimpleName config) {
        List<Integer> force = new ArrayList<>();
        int index = 0;
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            switch (CsvFieldTrait.getKind(property)) {
            case VALUE:
            case FILE_NAME:
            case LINE_NUMBER:
            case RECORD_NUMBER:
                if (CsvFieldTrait.getQuoteStrategy(property) == CsvFieldTrait.QuoteStrategy.ALWAYS) {
                    force.add(index);
                }
                index++;
                break;
            default:
                // ignored
                break;
            }
        }
        if (force.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ExpressionBuilder(f, config)
                .method("setForceQuoteColumns", force.stream()
                        .map(columnIndex -> Models.toLiteral(f, columnIndex))
                        .collect(Collectors.toList()))
                .toStatement());
    }

    private MethodDeclaration createParse() {
        SimpleName input = f.newSimpleName("input");
        SimpleName object = f.newSimpleName("model");
        SimpleName path = f.newSimpleName("path");
        List<Statement> statements = new ArrayList<>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            switch (CsvFieldTrait.getKind(property, Kind.VALUE)) {
            case VALUE:
                statements.add(new ExpressionBuilder(f, input)
                    .method("fill", new ExpressionBuilder(f, object) //$NON-NLS-1$
                        .method(context.getOptionGetterName(property))
                        .toExpression())
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
                            castIfInt(property.getType(), new ExpressionBuilder(f, input)
                                    .method("getCurrentLineNumber") //$NON-NLS-1$
                                    .toExpression()))
                    .toStatement());
                break;
            case RECORD_NUMBER:
                statements.add(new ExpressionBuilder(f, object)
                    .method(context.getValueSetterName(property),
                            castIfInt(property.getType(), new ExpressionBuilder(f, input)
                                    .method("getCurrentRecordNumber") //$NON-NLS-1$
                                    .toExpression()))
                    .toStatement());
                break;
            default:
                // ignored
                break;
            }
        }
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Protected()
                    .toAttributes(),
                Collections.emptyList(),
                context.resolve(void.class),
                f.newSimpleName("parse"),
                Arrays.asList(
                        f.newFormalParameterDeclaration(context.resolve(CsvParser.class), input),
                        f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), object),
                        f.newFormalParameterDeclaration(context.resolve(StringOption.class), path)),
                0,
                Arrays.asList(context.resolve(IOException.class)),
                f.newBlock(statements));
    }

    private TypeBodyDeclaration createEmit() {
        SimpleName output = f.newSimpleName("output");
        SimpleName object = f.newSimpleName("model");
        SimpleName path = f.newSimpleName("path");
        List<Statement> statements = model.getDeclaredProperties().stream()
            .filter(it -> CsvFieldTrait.getKind(it) == Kind.VALUE)
            .map(it -> new ExpressionBuilder(f, output)
                    .method("emit", new ExpressionBuilder(f, object) //$NON-NLS-1$
                        .method(context.getOptionGetterName(it))
                        .toExpression())
                    .toStatement())
            .collect(Collectors.toList());
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Protected()
                    .toAttributes(),
                Collections.emptyList(),
                context.resolve(void.class),
                f.newSimpleName("emit"),
                Arrays.asList(
                        f.newFormalParameterDeclaration(context.resolve(CsvEmitter.class), output),
                        f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), object),
                        f.newFormalParameterDeclaration(context.resolve(StringOption.class), path)),
                0,
                Arrays.asList(context.resolve(IOException.class)),
                f.newBlock(statements));
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
        if (conf.isAllowLinefeed()) {
            return false;
        }
        if (conf.getCodecName() != null) {
            return false;
        }
        if (conf.getCharsetName() != null) {
            Charset cs = Charset.forName(conf.getCharsetName());
            if (!CharsetUtil.isAsciiCompatible(cs)) {
                return false;
            }
        }
        if (model.getDeclaredProperties().stream()
                .map(CsvFieldTrait::getKind)
                .anyMatch(it -> it == CsvFieldTrait.Kind.LINE_NUMBER
                        || it == CsvFieldTrait.Kind.RECORD_NUMBER)) {
            return false;
        }
        return true;
    }

    private Optional<MethodDeclaration> createGetCompressionCodecClass() {
        String codecClass = CodecNames.resolveCodecName(conf.getCodecName());
        if (codecClass != null) {
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
                    Arrays.asList(new TypeBuilder(f, context.resolve(Models.toName(f, codecClass)))
                            .dotClass()
                            .toReturnStatement())));
        } else {
            return Optional.empty();
        }
    }

    private Expression castIfInt(Type type, Expression expression) {
        assert type instanceof BasicType;
        BasicTypeKind kind = ((BasicType) type).getKind();
        assert kind == BasicTypeKind.INT || kind == BasicTypeKind.LONG;
        if (kind == BasicTypeKind.LONG) {
            return expression;
        } else {
            return new ExpressionBuilder(f, expression).castTo(context.resolve(int.class)).toExpression();
        }
    }
}