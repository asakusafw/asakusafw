/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import com.asakusafw.dmdl.directio.util.DirectFileInputDescriptionGenerator;
import com.asakusafw.dmdl.directio.util.DirectFileOutputDescriptionGenerator;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormat;
import com.asakusafw.runtime.directio.hadoop.SequenceFileFormat;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
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
    public static final String CATEGORY_STREAM = "sequencefile"; //$NON-NLS-1$

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return;
        }
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
                "{0}SequenceFileFormat"); //$NON-NLS-1$
        LOG.debug("Generating SequenceFile format for {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString());
        FormatGenerator.emit(next, model, model.getTrait(SequenceFileFormatTrait.class).getConfiguration());
        LOG.debug("Generated SequenceFile format for {}: {}", //$NON-NLS-1$
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
                "Abstract{0}SequenceFileInputDescription"); //$NON-NLS-1$
        DirectFileInputDescriptionGenerator.Description desc = new DirectFileInputDescriptionGenerator.Description(
                "SequenceFile input", context.getQualifiedTypeName()); //$NON-NLS-1$
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
                "Abstract{0}SequenceFileOutputDescription"); //$NON-NLS-1$
        DirectFileOutputDescriptionGenerator.Description desc = new DirectFileOutputDescriptionGenerator.Description(
                "SequenceFile output", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileOutputDescriptionGenerator.generate(next, desc);
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
                        .text("SequenceFile format for ") //$NON-NLS-1$
                        .linkType(context.resolve(model.getSymbol()))
                        .text(".") //$NON-NLS-1$
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
                    f.newSimpleName("getSupportedType"), //$NON-NLS-1$
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
                    f.newSimpleName("createKeyObject"), //$NON-NLS-1$
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Arrays.asList(new TypeBuilder(f, context.resolve(NullWritable.class))
                        .method("get") //$NON-NLS-1$
                        .toReturnStatement()));
        }

        private MethodDeclaration createCreateValueObject() {
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(model.getSymbol()),
                    f.newSimpleName("createValueObject"), //$NON-NLS-1$
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Arrays.asList(new TypeBuilder(f, context.resolve(model.getSymbol()))
                        .newObject()
                        .toReturnStatement()));
        }

        private MethodDeclaration createCopyToModel() {
            SimpleName key = f.newSimpleName("key"); //$NON-NLS-1$
            SimpleName value = f.newSimpleName("value"); //$NON-NLS-1$
            SimpleName internal = f.newSimpleName("model"); //$NON-NLS-1$
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(void.class),
                    f.newSimpleName("copyToModel"), //$NON-NLS-1$
                    Arrays.asList(new FormalParameterDeclaration[] {
                            f.newFormalParameterDeclaration(context.resolve(NullWritable.class), key),
                            f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), value),
                            f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), internal),
                    }),
                    Arrays.asList(new ExpressionBuilder(f, internal)
                        .method("copyFrom", value) //$NON-NLS-1$
                        .toStatement()));
        }

        private MethodDeclaration createCopyFromModel() {
            SimpleName key = f.newSimpleName("key"); //$NON-NLS-1$
            SimpleName value = f.newSimpleName("value"); //$NON-NLS-1$
            SimpleName internal = f.newSimpleName("model"); //$NON-NLS-1$
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(void.class),
                    f.newSimpleName("copyFromModel"), //$NON-NLS-1$
                    Arrays.asList(new FormalParameterDeclaration[] {
                            f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), internal),
                            f.newFormalParameterDeclaration(context.resolve(NullWritable.class), key),
                            f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), value),
                    }),
                    Arrays.asList(new ExpressionBuilder(f, value)
                        .method("copyFrom", internal) //$NON-NLS-1$
                        .toStatement()));
        }
    }
}
