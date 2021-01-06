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
package com.asakusafw.dmdl.directio.util;

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
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;

/**
 * Generates an implementation of {@code DirectFileInputDescription}.
 * @since 0.7.3
 */
public final class DirectFileInputDescriptionGenerator {

    static final String DESCRIPTION_CLASS =
            "com.asakusafw.vocabulary.directio.DirectFileInputDescription"; //$NON-NLS-1$

    static final String FORMAT_BASE_CLASS = "com.asakusafw.runtime.directio.DataFormat"; //$NON-NLS-1$

    private final EmitContext context;

    private final Description description;

    private final ModelFactory f;

    private DirectFileInputDescriptionGenerator(EmitContext context, Description description) {
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
        new DirectFileInputDescriptionGenerator(context, description).emit();
    }

    private void emit() throws IOException {
        Name base = Models.toName(f, DESCRIPTION_CLASS);
        ClassDeclaration decl = f.newClassDeclaration(
                new JavadocBuilder(f)
                    .inline("{0} for {1}.",
                            d -> d.text(description.getDescription()),
                            d -> d.linkType(context.resolve(description.getModelClassName())))
                    .toJavadoc(),
                getClassAttributes(),
                context.getTypeName(),
                Collections.emptyList(),
                context.resolve(base),
                Collections.emptyList(),
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
        List<TypeBodyDeclaration> results = new ArrayList<>();
        results.add(createGetModelTypeMethod(description.getModelClassName()));
        if (description.getBasePath() != null) {
            results.add(createGetBasePathMethod(description.getBasePath()));
        }
        if (description.getResourcePattern() != null) {
            results.add(createGetResourcePatternMethod(description.getResourcePattern()));
        }
        if (description.getFormatClassName() != null) {
            results.add(createGetFormatMethod(description.getFormatClassName()));
        }
        if (description.getOptional() != null) {
            results.add(createIsOptionalMethod(description.getOptional()));
        }
        if (description.getDataSize() != null) {
            results.add(createGetDataSizeMethod(description.getDataSize()));
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
        Name base = Models.toName(f, FORMAT_BASE_CLASS);
        Type type = f.newParameterizedType(
                context.resolve(Class.class),
                f.newWildcard(WildcardBoundKind.UPPER_BOUNDED, f.newParameterizedType(
                        context.resolve(base), f.newWildcard())));
        return createGetMethod("getFormat", type, f.newClassLiteral(context.resolve(value))); //$NON-NLS-1$
    }

    private MethodDeclaration createIsOptionalMethod(boolean value) {
        return createGetMethod("isOptional", context.resolve(boolean.class), Models.toLiteral(f, value)); //$NON-NLS-1$
    }

    private MethodDeclaration createGetDataSizeMethod(DataSize value) {
        Type type = context.resolve(DataSize.class);
        return createGetMethod("getDataSize", type, new TypeBuilder(f, type) //$NON-NLS-1$
                .field(value.name())
                .toExpression());
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
                Collections.emptyList(),
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

        private Name formatClassName;

        private Boolean optional;

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

        /**
         * Returns whether the target input is optional or not.
         * @return {@code true} : it is optional, {@code false} : it is not optional,
         *     or {@code null} it is not set
         */
        public Boolean getOptional() {
            return optional;
        }

        /**
         * Sets whether the target input is optional or not.
         * @param value the value to set
         */
        public void setOptional(Boolean value) {
            this.optional = value;
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
