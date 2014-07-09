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
package com.asakusafw.dmdl.directio.tsv.driver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.directio.tsv.driver.TsvFieldTrait.Kind;
import com.asakusafw.dmdl.directio.tsv.driver.TsvFormatTrait.Configuration;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.util.DelimiterRangeInputStream;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.TsvEmitter;
import com.asakusafw.runtime.io.TsvParser;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.utils.collections.Lists;
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
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Emits {@link BinaryStreamFormat} implementations.
 * @since 0.2.5
 * @version 0.5.3
 */
public class TsvFormatEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(TsvFormatEmitter.class);

    /**
     * Category name for TSV format.
     */
    public static final String CATEGORY_STREAM = "tsv";

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return;
        }
        checkPropertyType(model);
        Name supportName = generateFormat(context, model);
        generateImporter(context, model, supportName);
        generateExporter(context, model, supportName);
    }

    private Name generateFormat(EmitContext context, ModelDeclaration model) throws IOException {
        assert context != null;
        assert model != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "{0}TsvFormat");
        LOG.debug("Generating TSV format for {}",
                context.getQualifiedTypeName().toNameString());
        FormatGenerator.emit(next, model, model.getTrait(TsvFormatTrait.class).getConfiguration());
        LOG.debug("Generated TSV format for {}: {}",
                context.getQualifiedTypeName().toNameString(),
                next.getQualifiedTypeName().toNameString());
        return next.getQualifiedTypeName();
    }

    private Name generateImporter(EmitContext context, ModelDeclaration model, Name supportName) throws IOException {
        assert context != null;
        assert model != null;
        assert supportName != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "Abstract{0}TsvInputDescription");
        LOG.debug("Generating TSV input description for {}",
                context.getQualifiedTypeName().toNameString());
        DescriptionGenerator.emitImporter(next, model, supportName);
        LOG.debug("Generated TSV input description for {}: {}",
                context.getQualifiedTypeName().toNameString(),
                next.getQualifiedTypeName().toNameString());
        return next.getQualifiedTypeName();
    }

    private Name generateExporter(EmitContext context, ModelDeclaration model, Name supportName) throws IOException {
        assert context != null;
        assert model != null;
        assert supportName != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "Abstract{0}TsvOutputDescription");
        LOG.debug("Generating TSV output description for {}",
                context.getQualifiedTypeName().toNameString());
        DescriptionGenerator.emitExporter(next, model, supportName);
        LOG.debug("Generated TSV output description for {}: {}",
                context.getQualifiedTypeName().toNameString(),
                next.getQualifiedTypeName().toNameString());
        return next.getQualifiedTypeName();
    }

    private boolean isTarget(ModelDeclaration model) {
        assert model != null;
        TsvFormatTrait trait = model.getTrait(TsvFormatTrait.class);
        return trait != null;
    }

    private void checkPropertyType(ModelDeclaration model) throws IOException {
        assert model != null;
        for (PropertyDeclaration prop : model.getDeclaredProperties()) {
            if (isValueField(prop)) {
                Type type = prop.getType();
                if ((type instanceof BasicType) == false) {
                    throw new IOException(MessageFormat.format(
                            "Type \"{0}\" can not map to TSV field: {1}.{2} ",
                            type,
                            prop.getOwner().getName().identifier,
                            prop.getName().identifier));
                }
            }
        }
    }

    static boolean isValueField(PropertyDeclaration property) {
        assert property != null;
        return TsvFieldTrait.getKind(property, Kind.VALUE) == Kind.VALUE;
    }

    private static final class FormatGenerator {

        private static final String NAME_ADD_HEADER = "addHeader";

        private static final String NAME_SKIP_HEADER = "skipHeader";

        private static final String NAME_READER = "RecordReader";

        private static final String NAME_WRITER = "RecordWriter";

        private static final Map<String, String> CODEC_SHORT_NAMES;
        static {
            Map<String, String> map = new HashMap<String, String>();
            map.put("gzip", "org.apache.hadoop.io.compress.GzipCodec");
            map.put("deflate", "org.apache.hadoop.io.compress.DeflateCodec");
            map.put("bzip2", "org.apache.hadoop.io.compress.BZip2Codec");
            map.put("snappy", "org.apache.hadoop.io.compress.SnappyCodec");
            CODEC_SHORT_NAMES = map;
        }

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
            ClassDeclaration decl = f.newClassDeclaration(
                    new JavadocBuilder(f)
                        .text("TSV format for ")
                        .linkType(context.resolve(model.getSymbol()))
                        .text(".")
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .Final()
                        .toAttributes(),
                    context.getTypeName(),
                    f.newParameterizedType(
                            context.resolve(BinaryStreamFormat.class),
                            context.resolve(model.getSymbol())),
                    Collections.<com.asakusafw.utils.java.model.syntax.Type>emptyList(),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = Lists.create();
            results.add(createGetSupportedType());
            results.add(createGetPreferredFragmentSize());
            results.add(createGetMinimumFragmentSize());
            results.add(createCreateReader());
            results.add(createCreateWriter());
            if (conf.isEnableHeader()) {
                results.add(createAddHeader());
                results.add(createSkipHeader());
            }
            results.add(createReaderClass());
            results.add(createWriterClass());
            return results;
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
                    f.newSimpleName("getSupportedType"),
                    Collections.<FormalParameterDeclaration>emptyList(),
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
                    f.newSimpleName("getPreferredFragmentSize"),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Collections.singletonList(new ExpressionBuilder(f, value).toReturnStatement()));
        }

        private MethodDeclaration createGetMinimumFragmentSize() {
            boolean fastMode = isFastMode();
            Expression value = fastMode
                ? new TypeBuilder(f, context.resolve(Long.class)).field("MAX_VALUE").toExpression()
                : Models.toLiteral(f, -1L);
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(long.class),
                    f.newSimpleName("getMinimumFragmentSize"),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Collections.singletonList(new ExpressionBuilder(f, value).toReturnStatement()));
        }

        private boolean isFastMode() {
            return conf.getCodecName() == null;
        }

        private MethodDeclaration createCreateReader() {
            SimpleName dataType = f.newSimpleName("dataType");
            SimpleName path = f.newSimpleName("path");
            SimpleName stream = f.newSimpleName("stream");
            SimpleName offset = f.newSimpleName("offset");
            SimpleName fragmentSize = f.newSimpleName("fragmentSize");
            List<Statement> statements = Lists.create();
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
                                    "{0} does not support fragmentation.",
                                    context.getQualifiedTypeName().toNameString())))
                            .toThrowStatement())));
            }

            SimpleName fragmentInput = f.newSimpleName("fragmentInput");
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

            if (conf.isEnableHeader()) {
                statements.add(f.newIfStatement(
                        new ExpressionBuilder(f, offset)
                            .apply(InfixOperator.EQUALS, Models.toLiteral(f, 0L))
                            .toExpression(),
                        f.newBlock(new ExpressionBuilder(f, f.newThis())
                                .method(NAME_SKIP_HEADER, fragmentInput)
                                .toStatement())));
            }

            SimpleName parser = f.newSimpleName("parser");
            statements.add(new TypeBuilder(f, context.resolve(TsvParser.class))
                .newObject(new TypeBuilder(f, context.resolve(InputStreamReader.class))
                        .newObject(fragmentInput, Models.toLiteral(f, conf.getCharsetName()))
                        .toExpression())
                .toLocalVariableDeclaration(context.resolve(TsvParser.class), parser));

            List<Expression> arguments = Lists.create();
            arguments.add(parser);
            if (hasFileName()) {
                arguments.add(new TypeBuilder(f, context.resolve(StringOption.class))
                        .newObject(path)
                        .toExpression());
            }
            statements.add(new TypeBuilder(f, f.newNamedType(f.newSimpleName(NAME_READER)))
                .newObject(arguments)
                .toReturnStatement());
            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    f.newParameterizedType(
                            context.resolve(ModelInput.class),
                            context.resolve(model.getSymbol())),
                    f.newSimpleName("createInput"),
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
            SimpleName dataType = f.newSimpleName("dataType");
            SimpleName path = f.newSimpleName("path");
            SimpleName stream = f.newSimpleName("stream");
            List<Statement> statements = Lists.create();
            statements.add(createNullCheck(path));
            statements.add(createNullCheck(stream));

            SimpleName emitter = f.newSimpleName("emitter");
            statements.add(new TypeBuilder(f, context.resolve(TsvEmitter.class))
                .newObject(new TypeBuilder(f, context.resolve(OutputStreamWriter.class))
                        .newObject(blessOutputStream(stream), Models.toLiteral(f, conf.getCharsetName()))
                        .toExpression())
                .toLocalVariableDeclaration(context.resolve(TsvEmitter.class), emitter));

            if (conf.isEnableHeader()) {
                statements.add(new ExpressionBuilder(f, f.newThis())
                                .method(NAME_ADD_HEADER, emitter)
                                .toStatement());
            }

            statements.add(new TypeBuilder(f, f.newNamedType(f.newSimpleName(NAME_WRITER)))
                .newObject(emitter)
                .toReturnStatement());

            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(f.newParameterizedType(
                            context.resolve(ModelOutput.class),
                            context.resolve(model.getSymbol()))),
                    f.newSimpleName("createOutput"),
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

        private MethodDeclaration createAddHeader() {
            SimpleName emitter = f.newSimpleName("emitter");
            SimpleName buf = f.newSimpleName("buf");
            List<Statement> statements = Lists.create();
            statements.add(new TypeBuilder(f, context.resolve(StringOption.class))
                    .newObject()
                    .toLocalVariableDeclaration(context.resolve(StringOption.class), buf));

            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                switch (TsvFieldTrait.getKind(property, Kind.VALUE)) {
                case VALUE:
                    statements.add(new ExpressionBuilder(f, buf)
                            .method("modify", Models.toLiteral(f, TsvFieldTrait.getFieldName(property)))
                            .toStatement());
                    statements.add(new ExpressionBuilder(f, emitter)
                        .method("emit", buf)
                        .toStatement());
                    break;
                default:
                    // ignored
                    break;
                }
            }
            statements.add(new ExpressionBuilder(f, emitter)
                .method("endRecord")
                .toStatement());
            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Deprecated.class))
                        .Private()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName(NAME_ADD_HEADER),
                    Arrays.asList(f.newFormalParameterDeclaration(context.resolve(TsvEmitter.class), emitter)),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(statements));
            return decl;
        }

        private MethodDeclaration createSkipHeader() {
            SimpleName input = f.newSimpleName("stream");
            List<Statement> body = Lists.create();

            SimpleName c = f.newSimpleName("c");
            body.add(new ExpressionBuilder(f, input)
                    .method("read")
                    .toLocalVariableDeclaration(context.resolve(int.class), c));
            body.add(f.newIfStatement(
                    new ExpressionBuilder(f, c)
                        .apply(InfixOperator.LESS, Models.toLiteral(f, 0))
                        .toExpression(),
                    f.newBlock(f.newBreakStatement())));
            body.add(f.newIfStatement(
                    new ExpressionBuilder(f, c)
                        .apply(InfixOperator.EQUALS, Models.toLiteral(f, '\n'))
                        .toExpression(),
                    f.newBlock(f.newBreakStatement())));
            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName(NAME_SKIP_HEADER),
                    Arrays.asList(f.newFormalParameterDeclaration(context.resolve(InputStream.class), input)),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(f.newWhileStatement(Models.toLiteral(f, true), f.newBlock(body))));
            return decl;
        }

        private Expression blessInputStream(SimpleName stream) {
            Expression codec = createCompressionCodec();
            if (codec == null) {
                return stream;
            }
            return new ExpressionBuilder(f, codec)
                .method("createInputStream", stream)
                .toExpression();
        }

        private Expression blessOutputStream(SimpleName stream) {
            Expression codec = createCompressionCodec();
            if (codec == null) {
                return stream;
            }
            return new ExpressionBuilder(f, codec)
                .method("createOutputStream", stream)
                .toExpression();
        }

        private Expression createCompressionCodec() {
            String codecName = conf.getCodecName();
            if (codecName == null) {
                return null;
            }
            if (CODEC_SHORT_NAMES.containsKey(codecName)) {
                codecName = CODEC_SHORT_NAMES.get(codecName);
            }
            assert codecName != null;
            return new TypeBuilder(f, context.resolve(Models.toName(f, "org.apache.hadoop.util.ReflectionUtils")))
                .method("newInstance",
                        new TypeBuilder(f, context.resolve(Models.toName(f, codecName)))
                            .dotClass()
                            .toExpression(),
                        new TypeBuilder(f, context.resolve(Models.toName(f, "org.apache.hadoop.conf.Configuration")))
                            .newObject(Models.toLiteral(f, false))
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
                                "{0} must not be null",
                                parameter.getToken())))
                        .toThrowStatement()));
        }

        private ClassDeclaration createReaderClass() {
            SimpleName parser = f.newSimpleName("parser");
            SimpleName path = f.newSimpleName("path");

            List<TypeBodyDeclaration> members = Lists.create();
            List<ExpressionStatement> constructorStatements = Lists.create();
            List<FormalParameterDeclaration> constructorParameters = Lists.create();
            members.add(createPrivateField(TsvParser.class, parser));
            constructorParameters.add(f.newFormalParameterDeclaration(context.resolve(TsvParser.class), parser));
            constructorStatements.add(mapField(parser));
            if (hasFileName()) {
                members.add(createPrivateField(StringOption.class, path));
                constructorParameters.add(f.newFormalParameterDeclaration(context.resolve(StringOption.class), path));
                constructorStatements.add(mapField(path));
            }
            members.add(f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).toAttributes(),
                    f.newSimpleName(NAME_READER),
                    constructorParameters,
                    constructorStatements));

            SimpleName object = f.newSimpleName("object");
            List<Statement> statements = Lists.create();
            statements.add(f.newIfStatement(
                    new ExpressionBuilder(f, parser)
                        .method("next")
                        .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
                        .toExpression(),
                    f.newBlock(new ExpressionBuilder(f, Models.toLiteral(f, false))
                        .toReturnStatement())));
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                switch (TsvFieldTrait.getKind(property, Kind.VALUE)) {
                case VALUE:
                    statements.add(new ExpressionBuilder(f, parser)
                        .method("fill", new ExpressionBuilder(f, object)
                            .method(context.getOptionGetterName(property))
                            .toExpression())
                        .toStatement());
                    break;
                case FILE_NAME:
                    statements.add(new ExpressionBuilder(f, object)
                        .method(context.getOptionSetterName(property), path)
                        .toStatement());
                    break;
                default:
                    // ignored
                    break;
                }
            }
            statements.add(new ExpressionBuilder(f, parser)
                .method("endRecord")
                .toStatement());
            statements.add(new ExpressionBuilder(f, Models.toLiteral(f, true))
                .toReturnStatement());
            members.add(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(boolean.class),
                    f.newSimpleName("readTo"),
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
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName("close"),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(new ExpressionBuilder(f, parser)
                        .method("close")
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
            SimpleName emitter = f.newSimpleName("emitter");
            List<TypeBodyDeclaration> members = Lists.create();
            members.add(createPrivateField(TsvEmitter.class, emitter));
            members.add(f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).toAttributes(),
                    f.newSimpleName(NAME_WRITER),
                    Arrays.asList(f.newFormalParameterDeclaration(context.resolve(TsvEmitter.class), emitter)),
                    Arrays.asList(mapField(emitter))));

            SimpleName object = f.newSimpleName("object");
            List<Statement> statements = Lists.create();
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                if (isValueField(property)) {
                    statements.add(new ExpressionBuilder(f, emitter)
                        .method("emit", new ExpressionBuilder(f, object)
                            .method(context.getOptionGetterName(property))
                            .toExpression())
                        .toStatement());
                }
            }
            statements.add(new ExpressionBuilder(f, emitter)
                .method("endRecord")
                .toStatement());

            members.add(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName("write"),
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
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName("close"),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(new ExpressionBuilder(f, emitter)
                        .method("close")
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
                if (TsvFieldTrait.getKind(property, Kind.VALUE) == Kind.FILE_NAME) {
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

    private static final class DescriptionGenerator {

        // for reduce library dependencies
        private static final String IMPORTER_TYPE_NAME =
            "com.asakusafw.vocabulary.directio.DirectFileInputDescription";

        // for reduce library dependencies
        private static final String EXPORTER_TYPE_NAME =
            "com.asakusafw.vocabulary.directio.DirectFileOutputDescription";

        private final EmitContext context;

        private final ModelDeclaration model;

        private final com.asakusafw.utils.java.model.syntax.Type supportClass;

        private final ModelFactory f;

        private final boolean importer;

        private DescriptionGenerator(
                EmitContext context,
                ModelDeclaration model,
                Name supportClassName,
                boolean importer) {
            assert context != null;
            assert model != null;
            assert supportClassName != null;
            this.context = context;
            this.model = model;
            this.f = context.getModelFactory();
            this.importer = importer;
            this.supportClass = context.resolve(supportClassName);
        }

        static void emitImporter(
                EmitContext context,
                ModelDeclaration model,
                Name supportClassName) throws IOException {
            assert context != null;
            assert model != null;
            assert supportClassName != null;
            DescriptionGenerator emitter = new DescriptionGenerator(context, model, supportClassName, true);
            emitter.emit();
        }

        static void emitExporter(
                EmitContext context,
                ModelDeclaration model,
                Name supportClassName) throws IOException {
            assert context != null;
            assert model != null;
            assert supportClassName != null;
            DescriptionGenerator emitter = new DescriptionGenerator(context, model, supportClassName, false);
            emitter.emit();
        }

        private void emit() throws IOException {
            ClassDeclaration decl = f.newClassDeclaration(
                    new JavadocBuilder(f)
                        .text("An abstract implementation of ")
                        .linkType(context.resolve(model.getSymbol()))
                        .text(" {0} description using Direct I/O TSV",
                                importer ? "importer" : "exporter")
                        .text(".")
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .Abstract()
                        .toAttributes(),
                    context.getTypeName(),
                    context.resolve(Models.toName(f, importer ? IMPORTER_TYPE_NAME : EXPORTER_TYPE_NAME)),
                    Collections.<com.asakusafw.utils.java.model.syntax.Type>emptyList(),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = Lists.create();
            results.add(createGetModelType());
            results.add(createGetStreamSupport());
            return results;
        }

        private MethodDeclaration createGetModelType() {
            return createGetter(
                    new TypeBuilder(f, context.resolve(Class.class))
                        .parameterize(f.newWildcard(
                                WildcardBoundKind.UPPER_BOUNDED,
                                context.resolve(model.getSymbol())))
                        .toType(),
                    "getModelType",
                    f.newClassLiteral(context.resolve(model.getSymbol())));
        }

        private MethodDeclaration createGetStreamSupport() {
            return createGetter(
                    new TypeBuilder(f, context.resolve(Class.class))
                        .parameterize(supportClass)
                        .toType(),
                    "getFormat",
                    f.newClassLiteral(supportClass));
        }

        private MethodDeclaration createGetter(
                com.asakusafw.utils.java.model.syntax.Type type,
                String name,
                Expression value) {
            assert type != null;
            assert name != null;
            assert value != null;
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    type,
                    f.newSimpleName(name),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Arrays.asList(new ExpressionBuilder(f, value).toReturnStatement()));
        }
    }
}
