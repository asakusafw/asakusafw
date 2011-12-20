/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.dmdl.windgate.csv.driver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.dmdl.windgate.csv.driver.CsvFieldTrait.Kind;
import com.asakusafw.dmdl.windgate.csv.driver.CsvSupportTrait.Configuration;
import com.asakusafw.runtime.io.csv.CsvConfiguration;
import com.asakusafw.runtime.io.csv.CsvEmitter;
import com.asakusafw.runtime.io.csv.CsvParser;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport.DataModelReader;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport.DataModelWriter;
import com.ashigeru.lang.java.model.syntax.ClassDeclaration;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.ExpressionStatement;
import com.ashigeru.lang.java.model.syntax.FieldDeclaration;
import com.ashigeru.lang.java.model.syntax.FormalParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.InfixOperator;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.Name;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.TypeBodyDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.WildcardBoundKind;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.JavadocBuilder;
import com.ashigeru.lang.java.model.util.Models;
import com.ashigeru.lang.java.model.util.TypeBuilder;

/**
 * Emits {@link DataModelStreamSupport} implementations.
 * @since 0.2.4
 */
public class CsvSupportEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(CsvSupportEmitter.class);

    /**
     * Category name for CSV support.
     */
    public static final String CATEGORY_STREAM = "csv";

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return;
        }
        checkPropertyType(model);
        Name supportName = generateSupport(context, model);
        generateImporter(context, model, supportName);
        generateExporter(context, model, supportName);
    }

    private Name generateSupport(EmitContext context, ModelDeclaration model) throws IOException {
        assert context != null;
        assert model != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "{0}CsvSupport");
        LOG.debug("Generating CSV support for {}",
                context.getQualifiedTypeName().toNameString());
        SupportGenerator.emit(next, model, model.getTrait(CsvSupportTrait.class).getConfiguration());
        LOG.debug("Generated CSV support for {}: {}",
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
                "Abstract{0}CsvImporterDescription");
        LOG.debug("Generating CSV importer description for {}",
                context.getQualifiedTypeName().toNameString());
        DescriptionGenerator.emitImporter(next, model, supportName);
        LOG.debug("Generated CSV importer description for {}: {}",
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
                "Abstract{0}CsvExporterDescription");
        LOG.debug("Generating CSV exporter description for {}",
                context.getQualifiedTypeName().toNameString());
        DescriptionGenerator.emitExporter(next, model, supportName);
        LOG.debug("Generated CSV exporter description for {}: {}",
                context.getQualifiedTypeName().toNameString(),
                next.getQualifiedTypeName().toNameString());
        return next.getQualifiedTypeName();
    }

    private boolean isTarget(ModelDeclaration model) {
        assert model != null;
        CsvSupportTrait trait = model.getTrait(CsvSupportTrait.class);
        return trait != null;
    }

    private void checkPropertyType(ModelDeclaration model) throws IOException {
        assert model != null;
        for (PropertyDeclaration prop : model.getDeclaredProperties()) {
            if (isValueField(prop)) {
                Type type = prop.getType();
                if ((type instanceof BasicType) == false) {
                    throw new IOException(MessageFormat.format(
                            "Type \"{0}\" can not map to CSV field: {1}.{2} ",
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

    private static final class SupportGenerator {

        private static final String NAME_READER = "StreamReader";

        private static final String NAME_WRITER = "StreamWriter";

        private static final String METHOD_CHARSET = "getCharset";

        private static final String METHOD_CONFIG = "getConfiguration";

        private static final String FIELD_PATH_NAME = "pathText";

        private final EmitContext context;

        private final ModelDeclaration model;

        private final Configuration conf;

        private final ModelFactory f;

        private SupportGenerator(EmitContext context, ModelDeclaration model, Configuration configuration) {
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
            SupportGenerator emitter = new SupportGenerator(context, model, conf);
            emitter.emit();
        }

        private void emit() throws IOException {
            ClassDeclaration decl = f.newClassDeclaration(
                    new JavadocBuilder(f)
                        .text("Supports CSV for ")
                        .linkType(context.resolve(model.getSymbol()))
                        .text(".")
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .Final()
                        .toAttributes(),
                    context.getTypeName(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    null,
                    Collections.singletonList(f.newParameterizedType(
                            context.resolve(DataModelStreamSupport.class),
                            context.resolve(model.getSymbol()))),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = new ArrayList<TypeBodyDeclaration>();
            results.add(createGetCharset());
            results.add(createGetConfiguration());
            results.add(createGetSupportedType());
            results.add(createCreateReader());
            results.add(createCreateWriter());
            results.add(createReaderClass());
            results.add(createWriterClass());
            return results;
        }

        private MethodDeclaration createGetCharset() {
            return f.newMethodDeclaration(
                    new JavadocBuilder(f)
                        .text("Returns this CSV format configuration.")
                        .returns()
                            .text("CSV format configuration")
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Protected()
                        .toAttributes(),
                    context.resolve(Charset.class),
                    f.newSimpleName(METHOD_CHARSET),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Arrays.asList(new TypeBuilder(f, context.resolve(Charset.class))
                        .method("forName", Models.toLiteral(f, conf.getCharsetName()))
                        .toReturnStatement()));
        }

        private MethodDeclaration createGetConfiguration() {
            List<Statement> statements = new ArrayList<Statement>();
            List<Expression> arguments = new ArrayList<Expression>();
            if (conf.isEnableHeader()) {
                SimpleName headers = f.newSimpleName("headers");
                statements.add(new TypeBuilder(f, context.resolve(ArrayList.class))
                    .parameterize(context.resolve(String.class))
                    .newObject()
                    .toLocalVariableDeclaration(
                            new TypeBuilder(f, context.resolve(List.class))
                                .parameterize(context.resolve(String.class))
                                .toType(),
                            headers));
                for (PropertyDeclaration property : model.getDeclaredProperties()) {
                    if (isValueField(property)) {
                        String fieldName = CsvFieldTrait.getFieldName(property);
                        statements.add(new ExpressionBuilder(f, headers)
                            .method("add", Models.toLiteral(f, fieldName))
                            .toStatement());
                    }
                }
                arguments.add(headers);
            } else {
                arguments.add(new TypeBuilder(f, context.resolve(CsvConfiguration.class))
                    .field("DEFAULT_HEADER_CELLS")
                    .toExpression());
            }
            arguments.add(Models.toLiteral(f, conf.getTrueFormat()));
            arguments.add(Models.toLiteral(f, conf.getFalseFormat()));
            arguments.add(Models.toLiteral(f, conf.getDateFormat()));
            arguments.add(Models.toLiteral(f, conf.getDateTimeFormat()));
            statements.add(new TypeBuilder(f, context.resolve(CsvConfiguration.class))
                        .newObject(arguments)
                        .toReturnStatement());
            return f.newMethodDeclaration(
                    new JavadocBuilder(f)
                        .text("Returns this CSV format configuration.")
                        .returns()
                            .text("CSV format configuration")
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Protected()
                        .toAttributes(),
                    context.resolve(CsvConfiguration.class),
                    f.newSimpleName(METHOD_CONFIG),
                    Collections.<FormalParameterDeclaration>emptyList(),
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
                    f.newSimpleName("getSupportedType"),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Arrays.asList(new Statement[] {
                            new TypeBuilder(f, context.resolve(model.getSymbol()))
                                .dotClass()
                                .toReturnStatement()
                    }));
            return decl;
        }

        private MethodDeclaration createCreateReader() {
            SimpleName path = f.newSimpleName("path");
            SimpleName stream = f.newSimpleName("stream");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(createNullCheck(path));
            statements.add(createNullCheck(stream));

            SimpleName reader = f.newSimpleName("reader");
            statements.add(new TypeBuilder(f, context.resolve(InputStreamReader.class))
                .newObject(stream, new ExpressionBuilder(f, f.newThis())
                    .method(METHOD_CHARSET)
                    .toExpression())
                .toLocalVariableDeclaration(context.resolve(Reader.class), reader));

            SimpleName parser = f.newSimpleName("parser");
            statements.add(new TypeBuilder(f, context.resolve(CsvParser.class))
                .newObject(reader, path, new ExpressionBuilder(f, f.newThis())
                    .method(METHOD_CONFIG)
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
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(f.newParameterizedType(
                            context.resolve(DataModelReader.class),
                            context.resolve(model.getSymbol()))),
                    f.newSimpleName("createReader"),
                    Arrays.asList(
                            f.newFormalParameterDeclaration(context.resolve(String.class), path),
                            f.newFormalParameterDeclaration(context.resolve(InputStream.class), stream)),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(statements));
            return decl;
        }

        private MethodDeclaration createCreateWriter() {
            SimpleName path = f.newSimpleName("path");
            SimpleName stream = f.newSimpleName("stream");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(createNullCheck(path));
            statements.add(createNullCheck(stream));

            SimpleName writer = f.newSimpleName("writer");
            statements.add(new TypeBuilder(f, context.resolve(OutputStreamWriter.class))
                .newObject(stream, new ExpressionBuilder(f, f.newThis())
                    .method(METHOD_CHARSET)
                    .toExpression())
                .toLocalVariableDeclaration(context.resolve(Writer.class), writer));

            SimpleName emitter = f.newSimpleName("emitter");
            statements.add(new TypeBuilder(f, context.resolve(CsvEmitter.class))
                .newObject(writer, path, new ExpressionBuilder(f, f.newThis())
                    .method(METHOD_CONFIG)
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
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(f.newParameterizedType(
                            context.resolve(DataModelWriter.class),
                            context.resolve(model.getSymbol()))),
                    f.newSimpleName("createWriter"),
                    Arrays.asList(
                            f.newFormalParameterDeclaration(context.resolve(String.class), path),
                            f.newFormalParameterDeclaration(context.resolve(OutputStream.class), stream)),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(statements));
            return decl;
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
            List<TypeBodyDeclaration> members = new ArrayList<TypeBodyDeclaration>();
            members.add(createPrivateField(CsvParser.class, parser));
            List<ExpressionStatement> constructorStatements = new ArrayList<ExpressionStatement>();
            constructorStatements.add(mapField(parser));
            if (hasFileName()) {
                members.add(createPrivateField(StringOption.class, f.newSimpleName(FIELD_PATH_NAME)));
                constructorStatements.add(new ExpressionBuilder(f, f.newSimpleName(FIELD_PATH_NAME))
                    .assignFrom(new TypeBuilder(f, context.resolve(StringOption.class))
                        .newObject(new ExpressionBuilder(f, parser)
                            .method("getPath")
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

            SimpleName object = f.newSimpleName("object");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(f.newIfStatement(
                    new ExpressionBuilder(f, parser)
                        .method("next")
                        .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
                        .toExpression(),
                    f.newBlock(new ExpressionBuilder(f, Models.toLiteral(f, false))
                        .toReturnStatement())));
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                switch (CsvFieldTrait.getKind(property, Kind.VALUE)) {
                case VALUE:
                    statements.add(new ExpressionBuilder(f, parser)
                        .method("fill", new ExpressionBuilder(f, object)
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
                            .method("getCurrentLineNumber")
                            .toExpression())
                        .toStatement());
                    break;
                case RECORD_NUMBER:
                    statements.add(new ExpressionBuilder(f, object)
                        .method(context.getValueSetterName(property), new ExpressionBuilder(f, parser)
                            .method("getCurrentRecordNumber")
                            .toExpression())
                        .toStatement());
                    break;
                default:
                    // ignored
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
                            context.resolve(DataModelReader.class),
                            context.resolve(model.getSymbol()))),
                    members);
        }

        private ClassDeclaration createWriterClass() {
            SimpleName emitter = f.newSimpleName("emitter");
            List<TypeBodyDeclaration> members = new ArrayList<TypeBodyDeclaration>();
            members.add(createPrivateField(CsvEmitter.class, emitter));
            members.add(f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).toAttributes(),
                    f.newSimpleName(NAME_WRITER),
                    Arrays.asList(f.newFormalParameterDeclaration(context.resolve(CsvEmitter.class), emitter)),
                    Arrays.asList(mapField(emitter))));

            SimpleName object = f.newSimpleName("object");
            List<Statement> statements = new ArrayList<Statement>();
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
                    f.newSimpleName("flush"),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(new ExpressionBuilder(f, emitter)
                        .method("flush")
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
                            context.resolve(DataModelWriter.class),
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

    private static final class DescriptionGenerator {

        // for reduce library dependencies
        private static final String IMPORTER_TYPE_NAME = "com.asakusafw.vocabulary.windgate.FsImporterDescription";

        // for reduce library dependencies
        private static final String EXPORTER_TYPE_NAME = "com.asakusafw.vocabulary.windgate.FsExporterDescription";

        private final EmitContext context;

        private final ModelDeclaration model;

        private final com.ashigeru.lang.java.model.syntax.Type supportClass;

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
                        .text(" {0} description using WindGate CSV",
                                importer ? "importer" : "exporter")
                        .text(".")
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .Abstract()
                        .toAttributes(),
                    context.getTypeName(),
                    context.resolve(Models.toName(f, importer ? IMPORTER_TYPE_NAME : EXPORTER_TYPE_NAME)),
                    Collections.<com.ashigeru.lang.java.model.syntax.Type>emptyList(),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = new ArrayList<TypeBodyDeclaration>();
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
                    "getStreamSupport",
                    f.newClassLiteral(supportClass));
        }

        private MethodDeclaration createGetter(
                com.ashigeru.lang.java.model.syntax.Type type,
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
