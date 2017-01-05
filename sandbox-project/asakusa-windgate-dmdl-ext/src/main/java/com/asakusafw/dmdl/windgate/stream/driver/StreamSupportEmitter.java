/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.dmdl.windgate.stream.driver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
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
import com.asakusafw.dmdl.windgate.util.FsProcessDescriptionGenerator;
import com.asakusafw.runtime.io.RecordEmitter;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.runtime.io.TsvEmitter;
import com.asakusafw.runtime.io.TsvParser;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.ExpressionStatement;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
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
 * @since 0.2.2
 * @version 0.7.3
 */
public class StreamSupportEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(StreamSupportEmitter.class);

    /**
     * Category name for JDBC support.
     */
    public static final String CATEGORY_STREAM = "stream"; //$NON-NLS-1$

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
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "{0}StreamSupport"); //$NON-NLS-1$
        LOG.debug("Generating stream support for {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString());
        Generator.emit(next, model);
        LOG.debug("Generated stream support for {}: {}", //$NON-NLS-1$
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
                "Abstract{0}StreamImporterDescription"); //$NON-NLS-1$
        FsProcessDescriptionGenerator.Description desc = new FsProcessDescriptionGenerator.Description(
                Messages.getString("StreamSupportEmitter.javadocImporter"), //$NON-NLS-1$
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
                "Abstract{0}StreamExporterDescription"); //$NON-NLS-1$
        FsProcessDescriptionGenerator.Description desc = new FsProcessDescriptionGenerator.Description(
                Messages.getString("StreamSupportEmitter.javadocExporter"), //$NON-NLS-1$
                context.getQualifiedTypeName());
        desc.setSupportClassName(supportName);
        FsProcessDescriptionGenerator.generateExporter(next, desc);
    }

    private boolean isTarget(ModelDeclaration model) throws IOException {
        assert model != null;
        StreamSupportTrait trait = model.getTrait(StreamSupportTrait.class);
        if (trait == null) {
            return false;
        }
        if (trait.getTypeName().equalsIgnoreCase("TSV")) { //$NON-NLS-1$
            return true;
        }

        throw new IOException(MessageFormat.format(
                Messages.getString("StreamSupportEmitter.errorUnsupportedFormat"), //$NON-NLS-1$
                "TSV", //$NON-NLS-1$
                model.getName().identifier,
                trait.getOriginalAst().getRegion()));
    }

    private void checkPropertyType(ModelDeclaration model) throws IOException {
        assert model != null;
        for (PropertyDeclaration prop : model.getDeclaredProperties()) {
            Type type = prop.getType();
            if ((type instanceof BasicType) == false) {
                throw new IOException(MessageFormat.format(
                        Messages.getString("StreamSupportEmitter.errorUnsupportedType"), //$NON-NLS-1$
                        type,
                        prop.getOwner().getName().identifier,
                        prop.getName().identifier));
            }
        }
    }

    private static final class Generator {

        private static final String NAME_READER = "StreamReader"; //$NON-NLS-1$

        private static final String NAME_WRITER = "StreamWriter"; //$NON-NLS-1$

        private final EmitContext context;

        private final ModelDeclaration model;

        private final ModelFactory f;

        private Generator(EmitContext context, ModelDeclaration model) {
            assert context != null;
            assert model != null;
            this.context = context;
            this.model = model;
            this.f = context.getModelFactory();
        }

        static void emit(EmitContext context, ModelDeclaration model) throws IOException {
            assert context != null;
            assert model != null;
            Generator emitter = new Generator(context, model);
            emitter.emit();
        }

        private void emit() throws IOException {
            ClassDeclaration decl = f.newClassDeclaration(
                    new JavadocBuilder(f)
                        .inline("Supports binary stream for {0}.", //$NON-NLS-1$
                                d -> d.linkType(context.resolve(model.getSymbol())))
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .toAttributes(),
                    context.getTypeName(),
                    Collections.emptyList(),
                    null,
                    Collections.singletonList(f.newParameterizedType(
                            context.resolve(DataModelStreamSupport.class),
                            context.resolve(model.getSymbol()))),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = new ArrayList<>();
            results.add(createGetSupportedType());
            results.add(createCreateReader());
            results.add(createCreateWriter());
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
                    f.newSimpleName("getSupportedType"), //$NON-NLS-1$
                    Collections.emptyList(),
                    Arrays.asList(new Statement[] {
                            new TypeBuilder(f, context.resolve(model.getSymbol()))
                                .dotClass()
                                .toReturnStatement()
                    }));
            return decl;
        }

        private MethodDeclaration createCreateReader() {
            SimpleName stream = f.newSimpleName("stream"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(createNullCheck(stream));

            SimpleName reader = f.newSimpleName("reader"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(InputStreamReader.class))
                .newObject(stream, Models.toLiteral(f, "UTF-8")) //$NON-NLS-1$
                .toLocalVariableDeclaration(context.resolve(Reader.class), reader));

            SimpleName parser = f.newSimpleName("parser"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(TsvParser.class))
                .newObject(reader)
                .toLocalVariableDeclaration(context.resolve(RecordParser.class), parser));

            statements.add(new TypeBuilder(f, f.newNamedType(f.newSimpleName(NAME_READER)))
                .newObject(parser)
                .toReturnStatement());
            SimpleName path = f.newSimpleName("path"); //$NON-NLS-1$
            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
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
            SimpleName stream = f.newSimpleName("stream"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(createNullCheck(stream));

            SimpleName writer = f.newSimpleName("writer"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(OutputStreamWriter.class))
                .newObject(stream, Models.toLiteral(f, "UTF-8")) //$NON-NLS-1$
                .toLocalVariableDeclaration(context.resolve(Writer.class), writer));

            SimpleName emitter = f.newSimpleName("emitter"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(TsvEmitter.class))
                .newObject(writer)
                .toLocalVariableDeclaration(context.resolve(RecordEmitter.class), emitter));

            statements.add(new TypeBuilder(f, f.newNamedType(f.newSimpleName(NAME_WRITER)))
                .newObject(emitter)
                .toReturnStatement());

            SimpleName path = f.newSimpleName("path"); //$NON-NLS-1$
            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
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
            members.add(createPrivateField(RecordParser.class, parser));
            members.add(f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).toAttributes(),
                    f.newSimpleName(NAME_READER),
                    Arrays.asList(
                            f.newFormalParameterDeclaration(context.resolve(RecordParser.class), parser)),
                    Arrays.asList(mapField(parser))));

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
                statements.add(new ExpressionBuilder(f, parser)
                    .method("fill", new ExpressionBuilder(f, object) //$NON-NLS-1$
                        .method(context.getOptionGetterName(property))
                        .toExpression())
                    .toStatement());
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
            members.add(createPrivateField(RecordEmitter.class, emitter));
            members.add(f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).toAttributes(),
                    f.newSimpleName(NAME_WRITER),
                    Arrays.asList(f.newFormalParameterDeclaration(
                                    context.resolve(RecordEmitter.class), emitter)),
                    Arrays.asList(mapField(emitter))));

            SimpleName object = f.newSimpleName("object"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                statements.add(new ExpressionBuilder(f, emitter)
                    .method("emit", new ExpressionBuilder(f, object) //$NON-NLS-1$
                        .method(context.getOptionGetterName(property))
                        .toExpression())
                    .toStatement());
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
                    f.newSimpleName("flush"), //$NON-NLS-1$
                    Collections.emptyList(),
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
