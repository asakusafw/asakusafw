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
package com.asakusafw.dmdl.directio.hive.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.directio.DirectFileOutputDescription;

/**
 * Generates an implementation of {@link DirectFileOutputDescription}.
 * @since 0.7.0
 */
public final class DirectFileOutputDescriptionGenerator {

    private final EmitContext context;

    private final Description description;

    private final ModelFactory f;

    private DirectFileOutputDescriptionGenerator(EmitContext context, Description description) {
        this.context = context;
        this.description = description;
        this.f = context.getModelFactory();
    }

    /**
     * Generates the class in the context.
     * @param context the target emit context
     * @param description the meta-description of target class
     * @throws IOException if generation was failed by I/O error
     */
    public static void generate(EmitContext context, Description description) throws IOException {
        new DirectFileOutputDescriptionGenerator(context, description).emit();
    }

    private void emit() throws IOException {
        ClassDeclaration decl = f.newClassDeclaration(
                new JavadocBuilder(f)
                    .text("{0} for ", description.getDescription()) //$NON-NLS-1$
                    .linkType(context.resolve(description.getModelClassName()))
                    .text(".") //$NON-NLS-1$
                    .toJavadoc(),
                getClassAttributes(),
                context.getTypeName(),
                Collections.<TypeParameterDeclaration>emptyList(),
                context.resolve(DirectFileOutputDescription.class),
                Collections.<Type>emptyList(),
                createMembers());
        context.emit(decl);
    }

    private List<? extends Attribute> getClassAttributes() {
        AttributeBuilder builder = new AttributeBuilder(f);
        builder.Public();
        if (description.getBasePath() == null
                || description.getResourcePattern() == null
                || description.getFormatClassName() == null) {
            builder.Abstract();
        }
        return builder.toAttributes();
    }

    private List<TypeBodyDeclaration> createMembers() {
        List<TypeBodyDeclaration> results = Lists.create();
        results.add(createGetModelTypeMethod(description.getModelClassName()));
        if (description.getBasePath() != null) {
            results.add(createGetBasePathMethod(description.getBasePath()));
        }
        if (description.getResourcePattern() != null) {
            results.add(createGetResourcePatternMethod(description.getResourcePattern()));
        }
        if (description.getOrder().isEmpty() == false) {
            results.add(createGetOrderMethod(description.getOrder()));
        }
        if (description.getDeletePatterns().isEmpty() == false) {
            results.add(createGetDeletePatternsMethod(description.getDeletePatterns()));
        }
        if (description.getFormatClassName() != null) {
            results.add(createGetFormatMethod(description.getFormatClassName()));
        }
        return results;
    }

    private MethodDeclaration createGetModelTypeMethod(Name value) {
        return createGetMethod(
                "getModelType", //$NON-NLS-1$
                f.newParameterizedType(context.resolve(Class.class), f.newWildcard()),
                f.newClassLiteral(context.resolve(value)));
    }

    private MethodDeclaration createGetBasePathMethod(String value) {
        return createGetMethod(
                "getBasePath", //$NON-NLS-1$
                context.resolve(String.class),
                Models.toLiteral(f, value));
    }

    private MethodDeclaration createGetResourcePatternMethod(String value) {
        return createGetMethod(
                "getResourcePattern", //$NON-NLS-1$
                context.resolve(String.class),
                Models.toLiteral(f, value));
    }

    private MethodDeclaration createGetFormatMethod(Name value) {
        Type type = f.newParameterizedType(
                context.resolve(Class.class),
                f.newWildcard(WildcardBoundKind.UPPER_BOUNDED, f.newParameterizedType(
                        context.resolve(DataFormat.class), f.newWildcard())));
        return createGetMethod("getFormat", type, f.newClassLiteral(context.resolve(value))); //$NON-NLS-1$
    }

    private MethodDeclaration createGetOrderMethod(List<String> values) {
        return createGetListOfStringMethod("getOrder", values); //$NON-NLS-1$
    }

    private MethodDeclaration createGetDeletePatternsMethod(List<String> values) {
        return createGetListOfStringMethod("getDeletePatterns", values); //$NON-NLS-1$
    }

    private MethodDeclaration createGetListOfStringMethod(String name, List<String> values) {
        List<Expression> arguments = Lists.create();
        for (String element : values) {
            arguments.add(Models.toLiteral(f, element));
        }
        Type type = f.newParameterizedType(context.resolve(List.class), context.resolve(String.class));
        Expression value = new TypeBuilder(f, context.resolve(Arrays.class))
            .method("asList", arguments) //$NON-NLS-1$
            .toExpression();
        return createGetMethod(name, type, value);
    }

    private MethodDeclaration createGetMethod(String name, Type type, Expression value) {
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                type,
                f.newSimpleName(name),
                Collections.<FormalParameterDeclaration>emptyList(),
                Arrays.asList(f.newReturnStatement(value)));
    }

    /**
     * Represents the meta description.
     * @since 0.7.0
     */
    public static final class Description {

        private final String description;

        private final Name modelClassName;

        private String basePath;

        private String resourcePattern;

        private final List<String> order = Lists.create();

        private final List<String> deletePatterns = Lists.create();

        private Name formatClassName;

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
         * Returns base path.
         * @return the base path, or {@code null} if it is not set
         */
        public String getBasePath() {
            return basePath;
        }

        /**
         * Sets the base path.
         * @param value the value to set
         */
        public void setBasePath(String value) {
            this.basePath = value;
        }

        /**
         * Returns the resource pattern.
         * @return the resource pattern, or {@code null} if it is not set
         */
        public String getResourcePattern() {
            return resourcePattern;
        }

        /**
         * Sets the resource pattern.
         * @param value the value to set
         */
        public void setResourcePattern(String value) {
            this.resourcePattern = value;
        }

        /**
         * Returns the view of order expression list.
         * Clients can modify the returned list.
         * @return the order expression list
         */
        public List<String> getOrder() {
            return this.order;
        }

        /**
         * Returns the view of delete patterns.
         * Clients can modify the returned list.
         * @return the delete patterns
         */
        public List<String> getDeletePatterns() {
            return deletePatterns;
        }

        /**
         * Returns the format class name.
         * @return the format class name, or {@code null} if it is not set
         */
        public Name getFormatClassName() {
            return formatClassName;
        }

        /**
         * Sets the format class name.
         * @param value the value to set
         */
        public void setFormatClassName(Name value) {
            this.formatClassName = value;
        }
    }
}
