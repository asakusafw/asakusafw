/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.sequencefile.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.directio.sequencefile.driver.SequenceFileFormatTrait.Configuration;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormat;
import com.asakusafw.runtime.directio.hadoop.SequenceFileFormat;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
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
 * Emits {@link HadoopFileFormat} implementations.
 * @since 0.4.0
 */
public class SequenceFileFormatEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(SequenceFileFormatEmitter.class);

    /**
     * Category name for this format.
     */
    public static final String CATEGORY_STREAM = "sequencefile";

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return;
        }
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
                "{0}SequenceFileFormat");
        LOG.debug("Generating SequenceFile format for {}",
                context.getQualifiedTypeName().toNameString());
        FormatGenerator.emit(next, model, model.getTrait(SequenceFileFormatTrait.class).getConfiguration());
        LOG.debug("Generated SequenceFile format for {}: {}",
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
                "Abstract{0}SequenceFileInputDescription");
        LOG.debug("Generating SequenceFile input description for {}",
                context.getQualifiedTypeName().toNameString());
        DescriptionGenerator.emitImporter(next, model, supportName);
        LOG.debug("Generated SequenceFile input description for {}: {}",
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
                "Abstract{0}SequenceFileOutputDescription");
        LOG.debug("Generating SequenceFile output description for {}",
                context.getQualifiedTypeName().toNameString());
        DescriptionGenerator.emitExporter(next, model, supportName);
        LOG.debug("Generated SequenceFile output description for {}: {}",
                context.getQualifiedTypeName().toNameString(),
                next.getQualifiedTypeName().toNameString());
        return next.getQualifiedTypeName();
    }

    private boolean isTarget(ModelDeclaration model) {
        assert model != null;
        SequenceFileFormatTrait trait = model.getTrait(SequenceFileFormatTrait.class);
        return trait != null;
    }

    private static final class FormatGenerator {

        private final EmitContext context;

        private final ModelDeclaration model;

        private final ModelFactory f;

        private FormatGenerator(EmitContext context, ModelDeclaration model, Configuration configuration) {
            assert context != null;
            assert model != null;
            assert configuration != null;
            this.context = context;
            this.model = model;
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
                        .text("SequenceFile format for ")
                        .linkType(context.resolve(model.getSymbol()))
                        .text(".")
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .toAttributes(),
                    context.getTypeName(),
                    f.newParameterizedType(
                            context.resolve(SequenceFileFormat.class),
                            context.resolve(NullWritable.class),
                            context.resolve(model.getSymbol()),
                            context.resolve(model.getSymbol())),
                    Collections.<com.asakusafw.utils.java.model.syntax.Type>emptyList(),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = new ArrayList<TypeBodyDeclaration>();
            results.add(createGetSupportedType());
            results.add(createCreateKeyObject());
            results.add(createCreateValueObject());
            results.add(createCopyToModel());
            results.add(createCopyFromModel());
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

        private MethodDeclaration createCreateKeyObject() {
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(NullWritable.class),
                    f.newSimpleName("createKeyObject"),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Arrays.asList(f.newBlock(new TypeBuilder(f, context.resolve(NullWritable.class))
                        .method("get")
                        .toReturnStatement())));
        }

        private MethodDeclaration createCreateValueObject() {
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(model.getSymbol()),
                    f.newSimpleName("createValueObject"),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Arrays.asList(f.newBlock(new TypeBuilder(f, context.resolve(model.getSymbol()))
                        .newObject()
                        .toReturnStatement())));
        }

        private MethodDeclaration createCopyToModel() {
            SimpleName key = f.newSimpleName("key");
            SimpleName value = f.newSimpleName("value");
            SimpleName internal = f.newSimpleName("model");
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(void.class),
                    f.newSimpleName("copyToModel"),
                    Arrays.asList(new FormalParameterDeclaration[] {
                            f.newFormalParameterDeclaration(context.resolve(NullWritable.class), key),
                            f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), value),
                            f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), internal),
                    }),
                    Arrays.asList(f.newBlock(new ExpressionBuilder(f, internal)
                        .method("copyFrom", value)
                        .toStatement())));
        }

        private MethodDeclaration createCopyFromModel() {
            SimpleName key = f.newSimpleName("key");
            SimpleName value = f.newSimpleName("value");
            SimpleName internal = f.newSimpleName("model");
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(void.class),
                    f.newSimpleName("copyFromModel"),
                    Arrays.asList(new FormalParameterDeclaration[] {
                            f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), internal),
                            f.newFormalParameterDeclaration(context.resolve(NullWritable.class), key),
                            f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), value),
                    }),
                    Arrays.asList(f.newBlock(new ExpressionBuilder(f, value)
                        .method("copyFrom", internal)
                        .toStatement())));
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
                        .text(" {0} description using Direct I/O SequenceFile",
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
