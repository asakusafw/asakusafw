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
package com.asakusafw.dmdl.windgate.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;

/**
 * Generates {@code JdbcImporterDescription} and {@code JdbcExporterDescription}.
 * @since 0.7.3
 */
public final class JdbcProcessDescriptionGenerator {

    // for reduce library dependencies
    private static final String IMPORTER_TYPE_NAME =
            "com.asakusafw.vocabulary.windgate.JdbcImporterDescription"; //$NON-NLS-1$

    // for reduce library dependencies
    private static final String EXPORTER_TYPE_NAME =
            "com.asakusafw.vocabulary.windgate.JdbcExporterDescription"; //$NON-NLS-1$

    private final EmitContext context;

    private final Description description;

    private final ModelFactory f;

    private final boolean importer;

    private JdbcProcessDescriptionGenerator(
            EmitContext context,
            Description description,
            boolean importer) {
        assert context != null;
        assert description != null;
        this.context = context;
        this.f = context.getModelFactory();
        this.importer = importer;
        this.description = description;
    }

    /**
     * Generates the class in the context.
     * @param context the target emit context
     * @param description the meta-description of target class
     * @throws IOException if generation was failed by I/O error
     */
    public static void generateImporter(EmitContext context, Description description) throws IOException {
        JdbcProcessDescriptionGenerator generator = new JdbcProcessDescriptionGenerator(context, description, true);
        generator.emit();
    }

    /**
     * Generates the class in the context.
     * @param context the target emit context
     * @param description the meta-description of target class
     * @throws IOException if generation was failed by I/O error
     */
    public static void generateExporter(EmitContext context, Description description) throws IOException {
        JdbcProcessDescriptionGenerator generator = new JdbcProcessDescriptionGenerator(context, description, false);
        generator.emit();
    }

    private void emit() throws IOException {
        ClassDeclaration decl = f.newClassDeclaration(
                new JavadocBuilder(f)
                    .inline("{0} for {1}.",
                            d -> d.text(description.getDescription()),
                            d -> d.linkType(context.resolve(description.getModelClassName())))
                    .toJavadoc(),
                getClassAttributes(),
                context.getTypeName(),
                context.resolve(Models.toName(f, importer ? IMPORTER_TYPE_NAME : EXPORTER_TYPE_NAME)),
                Collections.emptyList(),
                createMembers());
        context.emit(decl);
    }

    private List<? extends Attribute> getClassAttributes() {
        AttributeBuilder builder = new AttributeBuilder(f);
        builder.Public();
        if (description.getProfileName() == null
                || description.getTableName() == null
                || description.getColumnNames() == null
                || description.getSupportClassName() == null) {
            builder.Abstract();
        }
        return builder.toAttributes();
    }

    private List<TypeBodyDeclaration> createMembers() {
        List<TypeBodyDeclaration> results = new ArrayList<>();
        results.add(createGetModelType());
        if (description.getProfileName() != null) {
            results.add(createGetProfileName());
        }
        if (description.getTableName() != null) {
            results.add(createGetTableName());
        }
        if (description.getColumnNames() != null) {
            results.add(createGetColumnNames());
        }
        if (description.getCondition() != null) {
            results.add(createGetCondition());
        }
        if (description.getSupportClassName() != null) {
            results.add(createGetJdbcSupport());
        }
        if (description.getDataSize() != null) {
            results.add(createGetDataSize());
        }
        return results;
    }

    private MethodDeclaration createGetModelType() {
        return createGetter(
                new TypeBuilder(f, context.resolve(Class.class))
                    .parameterize(f.newWildcard(
                            WildcardBoundKind.UPPER_BOUNDED,
                            context.resolve(description.getModelClassName())))
                    .toType(),
                "getModelType", //$NON-NLS-1$
                f.newClassLiteral(context.resolve(description.getModelClassName())));
    }

    private MethodDeclaration createGetProfileName() {
        return createGetter(
                context.resolve(String.class),
                "getProfileName", //$NON-NLS-1$
                Models.toLiteral(f, description.getProfileName()));
    }

    private MethodDeclaration createGetJdbcSupport() {
        return createGetter(
                new TypeBuilder(f, context.resolve(Class.class))
                    .parameterize(f.newWildcard(
                            WildcardBoundKind.UPPER_BOUNDED,
                            new TypeBuilder(f, context.resolve(DataModelJdbcSupport.class))
                                .parameterize(f.newWildcard())
                                .toType()))
                    .toType(),
                "getJdbcSupport", //$NON-NLS-1$
                f.newClassLiteral(context.resolve(description.getSupportClassName())));
    }

    private MethodDeclaration createGetTableName() {
        return createGetter(
                context.resolve(String.class),
                "getTableName", //$NON-NLS-1$
                Models.toLiteral(f, description.getTableName()));
    }

    private MethodDeclaration createGetColumnNames() {
        List<Expression> arguments = new ArrayList<>();
        for (String name : description.getColumnNames()) {
            arguments.add(Models.toLiteral(f, name));
        }
        return createGetter(
                new TypeBuilder(f, context.resolve(List.class))
                    .parameterize(context.resolve(String.class))
                    .toType(),
                "getColumnNames", //$NON-NLS-1$
                new TypeBuilder(f, context.resolve(Arrays.class))
                    .method("asList", arguments) //$NON-NLS-1$
                    .toExpression());
    }

    private MethodDeclaration createGetCondition() {
        return createGetter(
                context.resolve(String.class),
                "getCondition", //$NON-NLS-1$
                Models.toLiteral(f, description.getCondition()));
    }

    private MethodDeclaration createGetDataSize() {
        Type type = context.resolve(DataSize.class);
        return createGetter(
                type,
                "getDataSize", //$NON-NLS-1$
                new TypeBuilder(f, type)
                    .field(description.getDataSize().name())
                    .toExpression());
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
                Collections.emptyList(),
                Arrays.asList(new ExpressionBuilder(f, value).toReturnStatement()));
    }


    /**
     * Represents the meta description.
     * @since 0.7.0
     */
    public static final class Description {

        private final String description;

        private final Name modelClassName;

        private String tableName;

        private List<String> columnNames;

        private String condition;

        private String profileName;

        private Name supportClassName;

        private DataSize dataSize;

        /**
         * Creates a new instance.
         * @param description the textual description
         * @param modelClassName the target data model class name
         */
        public Description(String description, Name modelClassName) {
            this.description = description;
            this.modelClassName = modelClassName;
        }

        /**
         * Returns the textual description for the target class.
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns the data model class name.
         * @return the data model class name
         */
        public Name getModelClassName() {
            return modelClassName;
        }

        /**
         * Returns the profile name.
         * @return the profile name, or {@code null} if it is not set
         */
        public String getProfileName() {
            return profileName;
        }

        /**
         * Sets the profile name.
         * @param value the value to set
         */
        public void setProfileName(String value) {
            this.profileName = value;
        }

        /**
         * Returns table name.
         * @return the path, or {@code null} if it is not set
         */
        public String getTableName() {
            return tableName;
        }

        /**
         * Sets the table name.
         * @param value the value to set
         */
        public void setTableName(String value) {
            this.tableName = value;
        }

        /**
         * Returns the column names.
         * @return the column names, or {@code null} if it is not set
         */
        public List<String> getColumnNames() {
            return columnNames;
        }

        /**
         * Sets column names.
         * @param value the value to set
         */
        public void setColumnNames(List<String> value) {
            this.columnNames = value;
        }

        /**
         * Returns condition.
         * @return the condition, or {@code null} if it is not set
         */
        public String getCondition() {
            return condition;
        }

        /**
         * Sets the condition.
         * @param value the value to set
         */
        public void setCondition(String value) {
            this.condition = value;
        }

        /**
         * Returns the format class name.
         * @return the format class name, or {@code null} if it is not set
         */
        public Name getSupportClassName() {
            return supportClassName;
        }

        /**
         * Sets the format class name.
         * @param value the value to set
         */
        public void setSupportClassName(Name value) {
            this.supportClassName = value;
        }

        /**
         * Returns the data size.
         * @return the data size, or {@code null} if it is not set
         */
        public DataSize getDataSize() {
            return dataSize;
        }

        /**
         * Sets the data size.
         * @param value the value to set
         */
        public void setDataSize(DataSize value) {
            this.dataSize = value;
        }
    }
}
