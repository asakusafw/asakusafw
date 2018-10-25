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
package com.asakusafw.dmdl.directio.line.driver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.directio.line.driver.LineFormatTrait.Configuration;
import com.asakusafw.dmdl.directio.util.CodecNames;
import com.asakusafw.dmdl.directio.util.DirectFileInputDescriptionGenerator;
import com.asakusafw.dmdl.directio.util.DirectFileOutputDescriptionGenerator;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.hadoop.ConfigurableBinaryStreamFormat;
import com.asakusafw.runtime.directio.util.DelimiterRangeInputStream;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.line.LineConfiguration;
import com.asakusafw.runtime.io.line.LineInput;
import com.asakusafw.runtime.io.line.LineOutput;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ExpressionStatement;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
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
 * Emits {@link BinaryStreamFormat} implementation for line based text.
 * @since 0.7.5
 */
public class LineFormatEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(LineFormatEmitter.class);

    /**
     * Category name for line format.
     */
    public static final String CATEGORY_STREAM = "line"; //$NON-NLS-1$

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return;
        }
        validate(model);
        Name supportName = generateFormat(context, model);
        generateInputDescription(context, supportName, model);
        generateOutputDescription(context, supportName, model);
    }

    private void validate(ModelDeclaration model) throws IOException {
        boolean sawBody = false;
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            LineFieldTrait.Kind kind = LineFieldTrait.getKind(property);
            switch (kind) {
            case BODY:
                if (sawBody) {
                    throw new IOException(MessageFormat.format(
                            Messages.getString("LineFormatEmitter.errorDuplicateBody"), //$NON-NLS-1$
                            model.getName().identifier));
                }
                sawBody = true;
                checkType(property, BasicTypeKind.TEXT);
                break;
            case FILE_NAME:
                checkType(property, BasicTypeKind.TEXT);
                break;
            case LINE_NUMBER:
                checkType(property, BasicTypeKind.INT, BasicTypeKind.LONG);
                break;
            default:
                break;
            }
        }
        if (sawBody == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("LineFormatEmitter.errorMissingBody"), //$NON-NLS-1$
                    model.getName().identifier));
        }
    }

    private void checkType(PropertyDeclaration property, BasicTypeKind... kinds) throws IOException {
        Set<BasicTypeKind> set = EnumSet.noneOf(BasicTypeKind.class);
        Collections.addAll(set, kinds);
        Type type = property.getType();
        if ((type instanceof BasicType) == false || set.contains(((BasicType) type).getKind()) == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("LineFormatEmitter.errorInconsistentType"), //$NON-NLS-1$
                    kinds.length == 1 ? kinds[0] : set,
                    property.getOwner().getName().identifier,
                    property.getName().identifier));
        }
    }

    static PropertyDeclaration findProperty(ModelDeclaration model, LineFieldTrait.Kind kind) {
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            if (LineFieldTrait.getKind(property) == kind) {
                return property;
            }
        }
        return null;
    }

    private Name generateFormat(EmitContext context, ModelDeclaration model) throws IOException {
        assert context != null;
        assert model != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "{0}LineFormat"); //$NON-NLS-1$
        LOG.debug("Generating line format for {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString());
        FormatGenerator.emit(next, model, model.getTrait(LineFormatTrait.class).getConfiguration());
        LOG.debug("Generated line format for {}: {}", //$NON-NLS-1$
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
                "Abstract{0}LineInputDescription"); //$NON-NLS-1$
        DirectFileInputDescriptionGenerator.Description desc = new DirectFileInputDescriptionGenerator.Description(
                "Line file input", context.getQualifiedTypeName()); //$NON-NLS-1$
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
                "Abstract{0}LineOutputDescription"); //$NON-NLS-1$
        DirectFileOutputDescriptionGenerator.Description desc = new DirectFileOutputDescriptionGenerator.Description(
                "Line file output", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileOutputDescriptionGenerator.generate(next, desc);
    }

    private boolean isTarget(ModelDeclaration model) {
        assert model != null;
        LineFormatTrait trait = model.getTrait(LineFormatTrait.class);
        return trait != null;
    }

    private static final class FormatGenerator {

        private static final String NAME_READER = "RecordReader"; //$NON-NLS-1$

        private static final String NAME_WRITER = "RecordWriter"; //$NON-NLS-1$

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
                        .inline("Line format for {0}.",
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

            ExpressionBuilder builder = new TypeBuilder(f, context.resolve(LineConfiguration.class))
                .newObject()
                .method("withCharset", new TypeBuilder(f, context.resolve(Charset.class)) //$NON-NLS-1$
                    .method("forName", Models.toLiteral(f, conf.getCharsetName())) //$NON-NLS-1$
                    .toExpression());
            return f.newMethodDeclaration(
                    new JavadocBuilder(f)
                        .text("Returns this line format configuration.") //$NON-NLS-1$
                        .param(head)
                            .text("whether or not configure for head of the file") //$NON-NLS-1$
                        .returns()
                            .text("line format configuration") //$NON-NLS-1$
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Protected()
                        .toAttributes(),
                    context.resolve(LineConfiguration.class),
                    f.newSimpleName(METHOD_CONFIG),
                    Arrays.asList(
                            f.newFormalParameterDeclaration(context.resolve(boolean.class), head)),
                    Arrays.asList(builder.toReturnStatement()));
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
                : Models.toLiteral(f, -1L);
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
            if (conf.getCodecName() != null) {
                return false;
            }
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                switch (LineFieldTrait.getKind(property)) {
                case BODY:
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
                                    "{0} does not support fragmentation.", //$NON-NLS-1$
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
                                f.newConditionalExpression(
                                        f.newInfixExpression(
                                                fragmentSize,
                                                InfixOperator.GREATER_EQUALS,
                                                Models.toLiteral(f, 0L)),
                                        fragmentSize,
                                        new TypeBuilder(f, context.resolve(Long.class))
                                            .field("MAX_VALUE") //$NON-NLS-1$
                                            .toExpression()),
                                isNotHead)
                        .toExpression())
                    .toStatement());
            } else {
                statements.add(new ExpressionBuilder(f, fragmentInput)
                    .assignFrom(blessInputStream(stream))
                    .toStatement());
            }

            SimpleName parser = f.newSimpleName("parser"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(LineInput.class))
                    .method("newInstance", fragmentInput, path, new ExpressionBuilder(f, f.newThis()) //$NON-NLS-1$
                        .method(METHOD_CONFIG, new ExpressionBuilder(f, offset)
                            .apply(InfixOperator.EQUALS, Models.toLiteral(f, 0L))
                            .toExpression())
                        .toExpression())
                    .toLocalVariableDeclaration(context.resolve(LineInput.class), parser));

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
            Expression output = blessOutputStream(stream);
            statements.add(new TypeBuilder(f, context.resolve(LineOutput.class))
                .method("newInstance", output, path, new ExpressionBuilder(f, f.newThis()) //$NON-NLS-1$
                    .method(METHOD_CONFIG, Models.toLiteral(f, true))
                    .toExpression())
                .toLocalVariableDeclaration(context.resolve(LineOutput.class), emitter));

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
            List<ExpressionStatement> constructorStatements = new ArrayList<>();
            List<FormalParameterDeclaration> constructorParameters = new ArrayList<>();
            members.add(createPrivateField(LineInput.class, parser));
            constructorParameters.add(f.newFormalParameterDeclaration(context.resolve(LineInput.class), parser));
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
                    constructorParameters,
                    constructorStatements));

            SimpleName object = f.newSimpleName("object"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            PropertyDeclaration body = findProperty(model, LineFieldTrait.Kind.BODY);
            statements.add(f.newIfStatement(
                    new ExpressionBuilder(f, parser)
                        .method("readTo", new ExpressionBuilder(f, object) //$NON-NLS-1$
                                .method(context.getOptionGetterName(body))
                                .toExpression())
                        .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
                        .toExpression(),
                    f.newBlock(new ExpressionBuilder(f, Models.toLiteral(f, false))
                        .toReturnStatement())));

            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                switch (LineFieldTrait.getKind(property)) {
                case FILE_NAME:
                    statements.add(new ExpressionBuilder(f, object)
                        .method(context.getOptionSetterName(property), f.newSimpleName(FIELD_PATH_NAME))
                        .toStatement());
                    break;

                case LINE_NUMBER:
                    statements.add(new ExpressionBuilder(f, object)
                        .method(context.getValueSetterName(property),
                                castIfInt(property.getType(), new ExpressionBuilder(f, parser)
                                    .method("getLineNumber") //$NON-NLS-1$
                                    .toExpression()))
                        .toStatement());
                    break;

                default:
                    // ignored
                    break;
                }
            }
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

        private ClassDeclaration createWriterClass() {
            SimpleName emitter = f.newSimpleName("emitter"); //$NON-NLS-1$
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.add(createPrivateField(LineOutput.class, emitter));
            members.add(f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).toAttributes(),
                    f.newSimpleName(NAME_WRITER),
                    Arrays.asList(f.newFormalParameterDeclaration(context.resolve(LineOutput.class), emitter)),
                    Arrays.asList(mapField(emitter))));

            SimpleName object = f.newSimpleName("object"); //$NON-NLS-1$
            PropertyDeclaration body = findProperty(model, LineFieldTrait.Kind.BODY);
            Statement statement = new ExpressionBuilder(f, emitter)
                        .method("write", new ExpressionBuilder(f, object) //$NON-NLS-1$
                            .method(context.getOptionGetterName(body))
                            .toExpression())
                        .toStatement();

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
                    f.newBlock(statement)));
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
            return findProperty(model, LineFieldTrait.Kind.FILE_NAME) != null;
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
