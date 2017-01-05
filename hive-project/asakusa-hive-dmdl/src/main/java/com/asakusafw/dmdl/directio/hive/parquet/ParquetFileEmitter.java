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
package com.asakusafw.dmdl.directio.hive.parquet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.asakusafw.directio.hive.parquet.AbstractParquetFileFormat;
import com.asakusafw.directio.hive.parquet.ParquetFormatConfiguration;
import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelMapping.FieldMappingStrategy;
import com.asakusafw.dmdl.directio.hive.common.HiveDataModelEmitter;
import com.asakusafw.dmdl.directio.util.DirectFileInputDescriptionGenerator;
import com.asakusafw.dmdl.directio.util.DirectFileOutputDescriptionGenerator;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.QualifiedName;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

import parquet.column.ParquetProperties.WriterVersion;
import parquet.hadoop.metadata.CompressionCodecName;

/**
 * Emits a {@link DataFormat} class for parquet files.
 * @since 0.7.0
 */
public class ParquetFileEmitter extends JavaDataModelDriver {

    /**
     * Category name for Hive Parquet file.
     */
    public static final String CATEGORY = "hive.parquet"; //$NON-NLS-1$

    /**
     * Simple class name pattern for data format.
     */
    public static final String PATTERN_FORMAT = "{0}ParquetFileFormat"; //$NON-NLS-1$

    /**
     * Simple class name pattern for skeleton of input description.
     */
    public static final String PATTERN_INPUT_DESCRIPTION = "Abstract{0}ParquetFileInputDescription"; //$NON-NLS-1$

    /**
     * Simple class name pattern for skeleton of output description.
     */
    public static final String PATTERN_OUTPUT_DESCRIPTION = "Abstract{0}ParquetFileOutputDescription"; //$NON-NLS-1$

    /**
     * Returns the generated class name for this.
     * @param context the current context
     * @param model the target data model
     * @return the factory class name
     */
    public static QualifiedName getClassName(EmitContext context, ModelDeclaration model) {
        return createContext(context, model).getQualifiedTypeName();
    }

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (model.getTrait(ParquetFileTrait.class) == null) {
            return;
        }
        Name formatClassName = Generator.generate(createContext(context, model), model);
        generateInputDescription(context, formatClassName, model);
        generateOutputDescription(context, formatClassName, model);
    }

    private static EmitContext createContext(EmitContext context, ModelDeclaration model) {
        return new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY,
                PATTERN_FORMAT);
    }

    private void generateInputDescription(
            EmitContext context, Name formatClassName, ModelDeclaration model) throws IOException {
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY,
                PATTERN_INPUT_DESCRIPTION);
        DirectFileInputDescriptionGenerator.Description desc = new DirectFileInputDescriptionGenerator.Description(
                "Parquet file input", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileInputDescriptionGenerator.generate(next, desc);
    }

    private void generateOutputDescription(
            EmitContext context, Name formatClassName, ModelDeclaration model) throws IOException {
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY,
                PATTERN_OUTPUT_DESCRIPTION);
        DirectFileOutputDescriptionGenerator.Description desc = new DirectFileOutputDescriptionGenerator.Description(
                "Parquet file output", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileOutputDescriptionGenerator.generate(next, desc);
    }

    private static final class Generator {

        private final EmitContext context;

        private final ModelDeclaration model;

        private final ModelFactory f;

        private Generator(EmitContext context, ModelDeclaration model) {
            this.context = context;
            this.model = model;
            this.f = context.getModelFactory();
        }

        static QualifiedName generate(EmitContext context, ModelDeclaration model) throws IOException {
            new Generator(context, model).emit();
            return context.getQualifiedTypeName();
        }

        private void emit() throws IOException {
            ClassDeclaration decl = f.newClassDeclaration(
                    new JavadocBuilder(f)
                        .inline("Hive Parquet data format for {0}.",
                                d -> d.linkType(context.resolve(model.getSymbol())))
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .toAttributes(),
                    context.getTypeName(),
                    Collections.emptyList(),
                    new TypeBuilder(f, context.resolve(AbstractParquetFileFormat.class))
                        .parameterize(context.resolve(model.getSymbol()))
                        .toType(),
                    Collections.emptyList(),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = new ArrayList<>();
            results.add(createGetTableNameMethod());
            results.add(createGetDataModelDescriptorMethod());
            results.add(createGetFormatConfigurationMethod());
            return results;
        }

        private MethodDeclaration createGetTableNameMethod() {
            Expression value = Models.toLiteral(f, ParquetFileTrait.getTableName(model));
            return createMethod("getTableName", context.resolve(String.class), value); //$NON-NLS-1$
        }

        private MethodDeclaration createGetDataModelDescriptorMethod() {
            Name factory = HiveDataModelEmitter.getClassName(context, model);
            Expression value = new TypeBuilder(f, context.resolve(factory))
                .method(HiveDataModelEmitter.NAME_GETTER_METHOD)
                .toExpression();
            return createMethod(
                    "getDataModelDescriptor", //$NON-NLS-1$
                    context.resolve(DataModelDescriptor.class),
                    value);
        }

        private MethodDeclaration createGetFormatConfigurationMethod() {
            SimpleName result = f.newSimpleName("result"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(new TypeBuilder(f, context.resolve(ParquetFormatConfiguration.class))
                    .newObject()
                    .toLocalVariableDeclaration(context.resolve(ParquetFormatConfiguration.class), result));

            ParquetFormatConfiguration conf = model.getTrait(ParquetFileTrait.class).configuration();
            if (conf.getFieldMappingStrategy() != null) {
                statements.add(new ExpressionBuilder(f, result)
                        .method("withFieldMappingStrategy", //$NON-NLS-1$
                                new TypeBuilder(f, context.resolve(FieldMappingStrategy.class))
                            .field(conf.getFieldMappingStrategy().name())
                            .toExpression())
                        .toStatement());
            }
            if (conf.getOnMissingSource() != null) {
                statements.add(new ExpressionBuilder(f, result)
                        .method("withOnMissingSource", //$NON-NLS-1$
                                new TypeBuilder(f, context.resolve(ExceptionHandlingStrategy.class))
                            .field(conf.getOnMissingSource().name())
                            .toExpression())
                        .toStatement());
            }
            if (conf.getOnMissingTarget() != null) {
                statements.add(new ExpressionBuilder(f, result)
                        .method("withOnMissingTarget", //$NON-NLS-1$
                                new TypeBuilder(f, context.resolve(ExceptionHandlingStrategy.class))
                            .field(conf.getOnMissingTarget().name())
                            .toExpression())
                        .toStatement());
            }
            if (conf.getOnIncompatibleType() != null) {
                statements.add(new ExpressionBuilder(f, result)
                        .method("withOnIncompatibleType", //$NON-NLS-1$
                                new TypeBuilder(f, context.resolve(ExceptionHandlingStrategy.class))
                            .field(conf.getOnIncompatibleType().name())
                            .toExpression())
                        .toStatement());
            }
            if (conf.getCompressionCodecName() != null) {
                statements.add(new ExpressionBuilder(f, result)
                    .method("withCompressionCodecName", //$NON-NLS-1$
                            new TypeBuilder(f, context.resolve(CompressionCodecName.class))
                                .field(conf.getCompressionCodecName().name())
                                .toExpression())
                    .toStatement());
            }
            if (conf.getBlockSize() != null) {
                statements.add(new ExpressionBuilder(f, result)
                    .method("withBlockSize", Models.toLiteral(f, conf.getBlockSize())) //$NON-NLS-1$
                    .toStatement());
            }
            if (conf.getDataPageSize() != null) {
                statements.add(new ExpressionBuilder(f, result)
                    .method("withDataPageSize", Models.toLiteral(f, conf.getDataPageSize())) //$NON-NLS-1$
                    .toStatement());
            }
            if (conf.getDictionaryPageSize() != null) {
                statements.add(new ExpressionBuilder(f, result)
                    .method("withDictionaryPageSize", Models.toLiteral(f, conf.getDictionaryPageSize())) //$NON-NLS-1$
                    .toStatement());
            }
            if (conf.getEnableDictionary() != null) {
                statements.add(new ExpressionBuilder(f, result)
                    .method("withEnableDictionary", Models.toLiteral(f, conf.getEnableDictionary())) //$NON-NLS-1$
                    .toStatement());
            }
            if (conf.getEnableValidation() != null) {
                statements.add(new ExpressionBuilder(f, result)
                    .method("withEnableValidation", Models.toLiteral(f, conf.getEnableValidation())) //$NON-NLS-1$
                    .toStatement());
            }
            if (conf.getWriterVersion() != null) {
                statements.add(new ExpressionBuilder(f, result)
                    .method("withWriterVersion", //$NON-NLS-1$
                            new TypeBuilder(f, context.resolve(WriterVersion.class))
                                .field(conf.getWriterVersion().name())
                                .toExpression())
                    .toStatement());
            }
            statements.add(new ExpressionBuilder(f, result).toReturnStatement());
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(ParquetFormatConfiguration.class),
                    f.newSimpleName("getFormatConfiguration"), //$NON-NLS-1$
                    Collections.emptyList(),
                    statements);
        }

        private MethodDeclaration createMethod(String methodName, Type returnType, Expression value) {
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    returnType,
                    f.newSimpleName(methodName),
                    Collections.emptyList(),
                    Arrays.asList(new ExpressionBuilder(f, value).toReturnStatement()));
        }
    }
}
