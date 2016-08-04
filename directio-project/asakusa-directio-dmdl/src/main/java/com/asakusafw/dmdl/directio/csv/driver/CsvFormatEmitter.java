/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.directio.csv.driver.CsvFieldTrait.Kind;
import com.asakusafw.dmdl.directio.csv.driver.CsvFormatTrait.Configuration;
import com.asakusafw.dmdl.directio.util.CodecNames;
import com.asakusafw.dmdl.directio.util.DirectFileInputDescriptionGenerator;
import com.asakusafw.dmdl.directio.util.DirectFileOutputDescriptionGenerator;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.hadoop.ConfigurableBinaryStreamFormat;
import com.asakusafw.runtime.directio.util.DelimiterRangeInputStream;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.csv.CsvConfiguration;
import com.asakusafw.runtime.io.csv.CsvEmitter;
import com.asakusafw.runtime.io.csv.CsvParser;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ExpressionStatement;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Emits {@link BinaryStreamFormat} implementations.
 * @since 0.2.5
 */
public class CsvFormatEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(CsvFormatEmitter.class);

    /**
     * Category name for CSV format.
     */
    public static final String CATEGORY_STREAM = "csv"; //$NON-NLS-1$

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return;
        }
        checkPropertyType(model);
        Name supportName = generateFormat(context, model);
        generateInputDescription(context, supportName, model);
        generateOutputDescription(context, supportName, model);
    }

    private Name generateFormat(EmitContext context, ModelDeclaration model) throws IOException {
        assert context != null;
        assert model != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "{0}CsvFormat"); //$NON-NLS-1$
        LOG.debug("Generating CSV format for {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString());
        FormatGenerator.emit(next, model, model.getTrait(CsvFormatTrait.class).getConfiguration());
        LOG.debug("Generated CSV format for {}: {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString(),
                next.getQualifiedTypeName().toNameString());
        return next.getQualifiedTypeName();
    }

    private void generateInputDescription(
            EmitContext context, Name formatClassName, ModelDeclaration model) throws IOException {
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "Abstract{0}CsvInputDescription"); //$NON-NLS-1$
        DirectFileInputDescriptionGenerator.Description desc = new DirectFileInputDescriptionGenerator.Description(
                "CSV file input", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileInputDescriptionGenerator.generate(next, desc);
    }

    private void generateOutputDescription(
            EmitContext context, Name formatClassName, ModelDeclaration model) throws IOException {
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "Abstract{0}CsvOutputDescription"); //$NON-NLS-1$
        DirectFileOutputDescriptionGenerator.Description desc = new DirectFileOutputDescriptionGenerator.Description(
                "CSV file output", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileOutputDescriptionGenerator.generate(next, desc);
    }

    private boolean isTarget(ModelDeclaration model) {
        assert model != null;
        CsvFormatTrait trait = model.getTrait(CsvFormatTrait.class);
        return trait != null;
    }

    private void checkPropertyType(ModelDeclaration model) throws IOException {
        assert model != null;
        for (PropertyDeclaration prop : model.getDeclaredProperties()) {
            if (isValueField(prop)) {
                Type type = prop.getType();
                if ((type instanceof BasicType) == false) {
                    throw new IOException(MessageFormat.format(
                            Messages.getString("CsvFormatEmitter.errorUnsupportedType"), //$NON-NLS-1$
                            type,
                            prop.getOwner().getName().identifier,
                            prop.getName().identifier));
                }
            }
        }
    }

    static boolean isValueField(PropertyDeclaration property) {
        assert property != null;
        return CsvFieldTrait.getKind(property, Kind.VALUE) == Kind.VALUE;
    }

    private static final class FormatGenerator {

        private static final String NAME_READER = "Reader"; //$NON-NLS-1$

        private static final String NAME_WRITER = "Writer"; //$NON-NLS-1$

        private static final String METHOD_CONFIG = "getConfiguration"; //$NON-NLS-1$

        private static final String FIELD_PATH_NAME = "pathText"; //$NON-NLS-1$

        private final EmitContext context;

        private final ModelDeclaration model;

        private final Configuration conf;

        private final ModelFactory f;

        private FormatGenerator(EmitContext context, ModelDeclaration model, Configuration configuration) {
            assert context != null;
            assert model != null;
            assert configuration != null;
            this.context = context;
            this.model = model;
            this.conf = configuration;
            this.f = context.getModelFactory();
        }

        static void emit(EmitContext context, ModelDeclaration model, Configuration conf) throws IOException {
            assert context != null;
            assert model != null;
            assert conf != null;
            FormatGenerator emitter = new FormatGenerator(context, model, conf);
            emitter.emit();
        }

        private void emit() throws IOException {
            Class<?> superClass = isHadoopConfRequired() ? ConfigurableBinaryStreamFormat.class
                    : BinaryStreamFormat.class;
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
                            context.resolve(superClass),
                            context.resolve(model.getSymbol())),
                    Collections.emptyList(),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = new ArrayList<>();
            results.add(createGetConfiguration());
            results.add(createGetSupportedType());
            results.add(createGetPreferredFragmentSize());
            results.add(createGetMinimumFragmentSize());
            results.add(createCreateReader());
            results.add(createCreateWriter());
            results.add(createReaderClass());
            results.add(createWriterClass());
            return results;
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
                    if (isValueField(property)) {
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
            statements.add(new ExpressionBuilder(f, config).toReturnStatement());
            return f.newMethodDeclaration(
                    new JavadocBuilder(f)
                        .text("Returns this CSV format configuration.") //$NON-NLS-1$
                        .param(head)
                            .text("whether configure for head of file or not") //$NON-NLS-1$
                        .returns()
                            .text("CSV format configuration") //$NON-NLS-1$
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Protected()
                        .toAttributes(),
                    context.resolve(CsvConfiguration.class),
                    f.newSimpleName(METHOD_CONFIG),
                    Arrays.asList(
                            f.newFormalParameterDeclaration(context.resolve(boolean.class), head)),
                    statements);
        }

        private MethodDeclaration createGetSupportedType() {
            MethodDeclaration decl = f.newMethodDeclaration(
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
                    Arrays.asList(new Statement[] {
                            new TypeBuilder(f, context.resolve(model.getSymbol()))
                                .dotClass()
                                .toReturnStatement()
                    }));
            return decl;
        }

        private MethodDeclaration createGetPreferredFragmentSize() {
            Expression value = Models.toLiteral(f, -1L);
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(long.class),
                    f.newSimpleName("getPreferredFragmentSize"), //$NON-NLS-1$
                    Collections.emptyList(),
                    Collections.singletonList(new ExpressionBuilder(f, value).toReturnStatement()));
        }

        private MethodDeclaration createGetMinimumFragmentSize() {
            boolean fastMode = isFastMode();
            Expression value = fastMode
                ? new TypeBuilder(f, context.resolve(Long.class)).field("MAX_VALUE").toExpression() //$NON-NLS-1$
                : Models.toLiteral(f, -1);
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(long.class),
                    f.newSimpleName("getMinimumFragmentSize"), //$NON-NLS-1$
                    Collections.emptyList(),
                    Collections.singletonList(new ExpressionBuilder(f, value).toReturnStatement()));
        }

        private boolean isFastMode() {
            if (conf.isAllowLinefeed()) {
                return false;
            }
            if (conf.getCodecName() != null) {
                return false;
            }
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                switch (CsvFieldTrait.getKind(property, Kind.VALUE)) {
                case VALUE:
                case FILE_NAME:
                case IGNORE:
                    break;
                default:
                    return false;
                }
            }
            return true;
        }

        private boolean isHadoopConfRequired() {
            return conf.getCodecName() != null;
        }

        private MethodDeclaration createCreateReader() {
            SimpleName dataType = f.newSimpleName("dataType"); //$NON-NLS-1$
            SimpleName path = f.newSimpleName("path"); //$NON-NLS-1$
            SimpleName stream = f.newSimpleName("stream"); //$NON-NLS-1$
            SimpleName offset = f.newSimpleName("offset"); //$NON-NLS-1$
            SimpleName fragmentSize = f.newSimpleName("fragmentSize"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(createNullCheck(dataType));
            statements.add(createNullCheck(path));
            statements.add(createNullCheck(stream));
            Expression isNotHead = new ExpressionBuilder(f, offset)
                .apply(InfixOperator.GREATER, Models.toLiteral(f, 0L))
                .toExpression();
            if (isFastMode() == false) {
                statements.add(f.newIfStatement(
                        isNotHead,
                        f.newBlock(new TypeBuilder(f, context.resolve(IllegalArgumentException.class))
                            .newObject(Models.toLiteral(f, MessageFormat.format(
                                    Messages.getString("CsvFormatEmitter.messageNotSupportFragmentation"), //$NON-NLS-1$
                                    context.getQualifiedTypeName().toNameString())))
                            .toThrowStatement())));
            }

            SimpleName fragmentInput = f.newSimpleName("fragmentInput"); //$NON-NLS-1$
            statements.add(f.newLocalVariableDeclaration(
                    context.resolve(InputStream.class),
                    fragmentInput,
                    null));
            if (isFastMode()) {
                statements.add(new ExpressionBuilder(f, fragmentInput)
                    .assignFrom(new TypeBuilder(f, context.resolve(DelimiterRangeInputStream.class))
                        .newObject(
                                blessInputStream(stream),
                                Models.toLiteral(f, '\n'),
                                fragmentSize,
                                isNotHead)
                        .toExpression())
                    .toStatement());
            } else {
                statements.add(new ExpressionBuilder(f, fragmentInput)
                    .assignFrom(blessInputStream(stream))
                    .toStatement());
            }

            SimpleName parser = f.newSimpleName("parser"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(CsvParser.class))
                .newObject(fragmentInput, path, new ExpressionBuilder(f, f.newThis())
                    .method(METHOD_CONFIG, new ExpressionBuilder(f, offset)
                        .apply(InfixOperator.EQUALS, Models.toLiteral(f, 0L))
                        .toExpression())
                    .toExpression())
                .toLocalVariableDeclaration(context.resolve(CsvParser.class), parser));

            statements.add(new TypeBuilder(f, f.newNamedType(f.newSimpleName(NAME_READER)))
                .newObject(parser)
                .toReturnStatement());
            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
                    f.newParameterizedType(
                            context.resolve(ModelInput.class),
                            context.resolve(model.getSymbol())),
                    f.newSimpleName("createInput"), //$NON-NLS-1$
                    Arrays.asList(
                            f.newFormalParameterDeclaration(
                                    f.newParameterizedType(
                                            context.resolve(Class.class),
                                            f.newWildcard(
                                                    WildcardBoundKind.UPPER_BOUNDED,
                                                    context.resolve(model.getSymbol()))),
                                    dataType),
                            f.newFormalParameterDeclaration(context.resolve(String.class), path),
                            f.newFormalParameterDeclaration(context.resolve(InputStream.class), stream),
                            f.newFormalParameterDeclaration(context.resolve(long.class), offset),
                            f.newFormalParameterDeclaration(context.resolve(long.class), fragmentSize)),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(statements));
            return decl;
        }

        private MethodDeclaration createCreateWriter() {
            SimpleName dataType = f.newSimpleName("dataType"); //$NON-NLS-1$
            SimpleName path = f.newSimpleName("path"); //$NON-NLS-1$
            SimpleName stream = f.newSimpleName("stream"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(createNullCheck(path));
            statements.add(createNullCheck(stream));

            SimpleName emitter = f.newSimpleName("emitter"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(CsvEmitter.class))
                .newObject(blessOutputStream(stream), path, new ExpressionBuilder(f, f.newThis())
                    .method(METHOD_CONFIG, Models.toLiteral(f, true))
                    .toExpression())
                .toLocalVariableDeclaration(context.resolve(CsvEmitter.class), emitter));

            statements.add(new TypeBuilder(f, f.newNamedType(f.newSimpleName(NAME_WRITER)))
                .newObject(emitter)
                .toReturnStatement());

            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
                    context.resolve(f.newParameterizedType(
                            context.resolve(ModelOutput.class),
                            context.resolve(model.getSymbol()))),
                    f.newSimpleName("createOutput"), //$NON-NLS-1$
                    Arrays.asList(
                            f.newFormalParameterDeclaration(
                                    f.newParameterizedType(
                                            context.resolve(Class.class),
                                            f.newWildcard(
                                                    WildcardBoundKind.UPPER_BOUNDED,
                                                    context.resolve(model.getSymbol()))),
                                    dataType),
                            f.newFormalParameterDeclaration(context.resolve(String.class), path),
                            f.newFormalParameterDeclaration(context.resolve(OutputStream.class), stream)),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(statements));
            return decl;
        }

        private Expression blessInputStream(SimpleName stream) {
            Expression codec = createCompressionCodec();
            if (codec == null) {
                return stream;
            }
            return new ExpressionBuilder(f, codec)
                .method("createInputStream", stream) //$NON-NLS-1$
                .toExpression();
        }

        private Expression blessOutputStream(SimpleName stream) {
            Expression codec = createCompressionCodec();
            if (codec == null) {
                return stream;
            }
            return new ExpressionBuilder(f, codec)
                .method("createOutputStream", stream) //$NON-NLS-1$
                .toExpression();
        }

        private Expression createCompressionCodec() {
            String codecName = CodecNames.resolveCodecName(conf.getCodecName());
            if (codecName == null) {
                return null;
            }
            assert isHadoopConfRequired();
            return new TypeBuilder(f,
                    context.resolve(Models.toName(f, "org.apache.hadoop.util.ReflectionUtils"))) //$NON-NLS-1$
                .method("newInstance", //$NON-NLS-1$
                        new TypeBuilder(f, context.resolve(Models.toName(f, codecName)))
                            .dotClass()
                            .toExpression(),
                        new ExpressionBuilder(f, f.newThis())
                            .method("getConf") //$NON-NLS-1$
                            .toExpression())
                .toExpression();
        }

        private Statement createNullCheck(SimpleName parameter) {
            assert parameter != null;
            return f.newIfStatement(
                    new ExpressionBuilder(f, parameter)
                        .apply(InfixOperator.EQUALS, Models.toNullLiteral(f))
                        .toExpression(),
                    f.newBlock(new TypeBuilder(f, context.resolve(IllegalArgumentException.class))
                        .newObject(Models.toLiteral(f, MessageFormat.format(
                                "{0} must not be null", //$NON-NLS-1$
                                parameter.getToken())))
                        .toThrowStatement()));
        }

        private ClassDeclaration createReaderClass() {
            SimpleName parser = f.newSimpleName("parser"); //$NON-NLS-1$
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.add(createPrivateField(CsvParser.class, parser));
            List<ExpressionStatement> constructorStatements = new ArrayList<>();
            constructorStatements.add(mapField(parser));
            if (hasFileName()) {
                members.add(createPrivateField(StringOption.class, f.newSimpleName(FIELD_PATH_NAME)));
                constructorStatements.add(new ExpressionBuilder(f, f.newThis())
                    .field(FIELD_PATH_NAME)
                    .assignFrom(new TypeBuilder(f, context.resolve(StringOption.class))
                        .newObject(new ExpressionBuilder(f, parser)
                            .method("getPath") //$NON-NLS-1$
                            .toExpression())
                        .toExpression())
                    .toStatement());
            }
            members.add(f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).toAttributes(),
                    f.newSimpleName(NAME_READER),
                    Arrays.asList(
                            f.newFormalParameterDeclaration(context.resolve(CsvParser.class), parser)),
                    constructorStatements));

            SimpleName object = f.newSimpleName("object"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(f.newIfStatement(
                    new ExpressionBuilder(f, parser)
                        .method("next") //$NON-NLS-1$
                        .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
                        .toExpression(),
                    f.newBlock(new ExpressionBuilder(f, Models.toLiteral(f, false))
                        .toReturnStatement())));
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                switch (CsvFieldTrait.getKind(property, Kind.VALUE)) {
                case VALUE:
                    statements.add(new ExpressionBuilder(f, parser)
                        .method("fill", new ExpressionBuilder(f, object) //$NON-NLS-1$
                            .method(context.getOptionGetterName(property))
                            .toExpression())
                        .toStatement());
                    break;
                case FILE_NAME:
                    statements.add(new ExpressionBuilder(f, object)
                        .method(context.getOptionSetterName(property), f.newSimpleName(FIELD_PATH_NAME))
                        .toStatement());
                    break;
                case LINE_NUMBER:
                    statements.add(new ExpressionBuilder(f, object)
                        .method(context.getValueSetterName(property), new ExpressionBuilder(f, parser)
                            .method("getCurrentLineNumber") //$NON-NLS-1$
                            .toExpression())
                        .toStatement());
                    break;
                case RECORD_NUMBER:
                    statements.add(new ExpressionBuilder(f, object)
                        .method(context.getValueSetterName(property), new ExpressionBuilder(f, parser)
                            .method("getCurrentRecordNumber") //$NON-NLS-1$
                            .toExpression())
                        .toStatement());
                    break;
                default:
                    // ignored
                    break;
                }
            }
            statements.add(new ExpressionBuilder(f, parser)
                .method("endRecord") //$NON-NLS-1$
                .toStatement());
            statements.add(new ExpressionBuilder(f, Models.toLiteral(f, true))
                .toReturnStatement());
            members.add(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
                    context.resolve(boolean.class),
                    f.newSimpleName("readTo"), //$NON-NLS-1$
                    Arrays.asList(f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), object)),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(statements)));
            members.add(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName("close"), //$NON-NLS-1$
                    Collections.emptyList(),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(new ExpressionBuilder(f, parser)
                        .method("close") //$NON-NLS-1$
                        .toStatement())));

            return f.newClassDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Static()
                        .Final()
                        .toAttributes(),
                    f.newSimpleName(NAME_READER),
                    null,
                    Arrays.asList(f.newParameterizedType(
                            context.resolve(ModelInput.class),
                            context.resolve(model.getSymbol()))),
                    members);
        }

        private ClassDeclaration createWriterClass() {
            SimpleName emitter = f.newSimpleName("emitter"); //$NON-NLS-1$
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.add(createPrivateField(CsvEmitter.class, emitter));
            members.add(f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).toAttributes(),
                    f.newSimpleName(NAME_WRITER),
                    Arrays.asList(f.newFormalParameterDeclaration(context.resolve(CsvEmitter.class), emitter)),
                    Arrays.asList(mapField(emitter))));

            SimpleName object = f.newSimpleName("object"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                if (isValueField(property)) {
                    statements.add(new ExpressionBuilder(f, emitter)
                        .method("emit", new ExpressionBuilder(f, object) //$NON-NLS-1$
                            .method(context.getOptionGetterName(property))
                            .toExpression())
                        .toStatement());
                }
            }
            statements.add(new ExpressionBuilder(f, emitter)
                .method("endRecord") //$NON-NLS-1$
                .toStatement());

            members.add(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName("write"), //$NON-NLS-1$
                    Arrays.asList(f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), object)),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(statements)));
            members.add(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName("close"), //$NON-NLS-1$
                    Collections.emptyList(),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(new ExpressionBuilder(f, emitter)
                        .method("close") //$NON-NLS-1$
                        .toStatement())));

            return f.newClassDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Static()
                        .Final()
                        .toAttributes(),
                    f.newSimpleName(NAME_WRITER),
                    null,
                    Arrays.asList(f.newParameterizedType(
                            context.resolve(ModelOutput.class),
                            context.resolve(model.getSymbol()))),
                    members);
        }

        private boolean hasFileName() {
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                if (CsvFieldTrait.getKind(property, Kind.VALUE) == Kind.FILE_NAME) {
                    return true;
                }
            }
            return false;
        }

        private ExpressionStatement mapField(SimpleName name) {
            return new ExpressionBuilder(f, f.newThis())
                .field(name)
                .assignFrom(name)
                .toStatement();
        }

        private FieldDeclaration createPrivateField(Class<?> type, SimpleName name) {
            return f.newFieldDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Final()
                        .toAttributes(),
                    context.resolve(type),
                    name,
                    null);
        }
    }
}
