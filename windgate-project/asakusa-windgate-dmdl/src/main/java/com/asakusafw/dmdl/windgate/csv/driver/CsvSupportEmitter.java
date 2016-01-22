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
package com.asakusafw.dmdl.windgate.csv.driver;

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

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.dmdl.windgate.csv.driver.CsvFieldTrait.Kind;
import com.asakusafw.dmdl.windgate.csv.driver.CsvSupportTrait.Configuration;
import com.asakusafw.dmdl.windgate.util.FsProcessDescriptionGenerator;
import com.asakusafw.runtime.io.csv.CsvConfiguration;
import com.asakusafw.runtime.io.csv.CsvEmitter;
import com.asakusafw.runtime.io.csv.CsvParser;
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
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport.DataModelReader;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport.DataModelWriter;

/**
 * Emits {@link DataModelStreamSupport} implementations.
 * @since 0.2.4
 */
public class CsvSupportEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(CsvSupportEmitter.class);

    /**
     * Category name for CSV support.
     */
    public static final String CATEGORY_STREAM = "csv"; //$NON-NLS-1$

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
                "{0}CsvSupport"); //$NON-NLS-1$
        LOG.debug("Generating CSV support for {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString());
        SupportGenerator.emit(next, model, model.getTrait(CsvSupportTrait.class).getConfiguration());
        LOG.debug("Generated CSV support for {}: {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString(),
                next.getQualifiedTypeName().toNameString());
        return next.getQualifiedTypeName();
    }

    private void generateImporter(EmitContext context, ModelDeclaration model, Name supportName) throws IOException {
        assert context != null;
        assert model != null;
        assert supportName != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "Abstract{0}CsvImporterDescription"); //$NON-NLS-1$
        FsProcessDescriptionGenerator.Description desc = new FsProcessDescriptionGenerator.Description(
                Messages.getString("CsvSupportEmitter.javadocImporterClass"), //$NON-NLS-1$
                context.getQualifiedTypeName());
        desc.setSupportClassName(supportName);
        FsProcessDescriptionGenerator.generateImporter(next, desc);
    }

    private void generateExporter(EmitContext context, ModelDeclaration model, Name supportName) throws IOException {
        assert context != null;
        assert model != null;
        assert supportName != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "Abstract{0}CsvExporterDescription"); //$NON-NLS-1$
        FsProcessDescriptionGenerator.Description desc = new FsProcessDescriptionGenerator.Description(
                Messages.getString("CsvSupportEmitter.javadocExporterClass"), //$NON-NLS-1$
                context.getQualifiedTypeName());
        desc.setSupportClassName(supportName);
        FsProcessDescriptionGenerator.generateExporter(next, desc);
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
                            Messages.getString("CsvSupportEmitter.errorUnsupportedType"), //$NON-NLS-1$
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

        private static final String NAME_READER = "StreamReader"; //$NON-NLS-1$

        private static final String NAME_WRITER = "StreamWriter"; //$NON-NLS-1$

        private static final String METHOD_CONFIG = "getConfiguration"; //$NON-NLS-1$

        private static final String FIELD_PATH_NAME = "pathText"; //$NON-NLS-1$

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
                        .text(Messages.getString("CsvSupportEmitter.javadocClass"), //$NON-NLS-1$
                                context.getTypeName(model.getSymbol()))
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
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
            List<TypeBodyDeclaration> results = new ArrayList<>();
            results.add(createGetConfiguration());
            results.add(createGetSupportedType());
            results.add(createCreateReader());
            results.add(createCreateWriter());
            results.add(createReaderClass());
            results.add(createWriterClass());
            return results;
        }

        private MethodDeclaration createGetConfiguration() {
            List<Statement> statements = new ArrayList<>();
            List<Expression> arguments = new ArrayList<>();
            arguments.add(new TypeBuilder(f, context.resolve(Charset.class))
                .method("forName", Models.toLiteral(f, conf.getCharsetName())) //$NON-NLS-1$
                .toExpression());
            if (conf.isEnableHeader() || conf.isForceHeader()) {
                SimpleName headers = f.newSimpleName("headers"); //$NON-NLS-1$
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
                            .method("add", Models.toLiteral(f, fieldName)) //$NON-NLS-1$
                            .toStatement());
                    }
                }
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
                statements.add(new ExpressionBuilder(f, config)
                    .method("setForceConsumeHeader", Models.toLiteral(f, conf.isForceHeader())) //$NON-NLS-1$
                    .toStatement());
            }
            statements.add(new ExpressionBuilder(f, config).toReturnStatement());
            return f.newMethodDeclaration(
                    new JavadocBuilder(f)
                        .text(Messages.getString("CsvSupportEmitter.javadocGetConfiguration")) //$NON-NLS-1$
                        .returns()
                            .text(Messages.getString("CsvSupportEmitter.javadocGetConfigurationResult")) //$NON-NLS-1$
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
                    f.newSimpleName("getSupportedType"), //$NON-NLS-1$
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Arrays.asList(new Statement[] {
                            new TypeBuilder(f, context.resolve(model.getSymbol()))
                                .dotClass()
                                .toReturnStatement()
                    }));
            return decl;
        }

        private MethodDeclaration createCreateReader() {
            SimpleName path = f.newSimpleName("path"); //$NON-NLS-1$
            SimpleName stream = f.newSimpleName("stream"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(createNullCheck(path));
            statements.add(createNullCheck(stream));

            SimpleName parser = f.newSimpleName("parser"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(CsvParser.class))
                .newObject(stream, path, new ExpressionBuilder(f, f.newThis())
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
                    f.newSimpleName("createReader"), //$NON-NLS-1$
                    Arrays.asList(
                            f.newFormalParameterDeclaration(context.resolve(String.class), path),
                            f.newFormalParameterDeclaration(context.resolve(InputStream.class), stream)),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(statements));
            return decl;
        }

        private MethodDeclaration createCreateWriter() {
            SimpleName path = f.newSimpleName("path"); //$NON-NLS-1$
            SimpleName stream = f.newSimpleName("stream"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(createNullCheck(path));
            statements.add(createNullCheck(stream));

            SimpleName emitter = f.newSimpleName("emitter"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(CsvEmitter.class))
                .newObject(stream, path, new ExpressionBuilder(f, f.newThis())
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
                    f.newSimpleName("createWriter"), //$NON-NLS-1$
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
                constructorStatements.add(new ExpressionBuilder(f, f.newSimpleName(FIELD_PATH_NAME))
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
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(boolean.class),
                    f.newSimpleName("readTo"), //$NON-NLS-1$
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
                    Collections.<TypeParameterDeclaration>emptyList(),
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
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName("flush"), //$NON-NLS-1$
                    Collections.<FormalParameterDeclaration>emptyList(),
                    0,
                    Arrays.asList(context.resolve(IOException.class)),
                    f.newBlock(new ExpressionBuilder(f, emitter)
                        .method("flush") //$NON-NLS-1$
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
}
