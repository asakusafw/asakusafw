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
package com.asakusafw.dmdl.java.emitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.java.Configuration;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Abstract super class which emits a record/joined/summarized model
 * as a Java model class.
 */
public class ConcreteModelEmitter {

    private final ModelDeclaration model;

    private final EmitContext context;

    private final JavaDataModelDriver driver;

    private final ModelFactory f;

    /**
     * Creates and returns a new instance.
     * @param semantics the semantic model root
     * @param config the current configuration
     * @param model the model to emit
     * @param driver the emitter driver
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ConcreteModelEmitter(
            DmdlSemantics semantics,
            Configuration config,
            ModelDeclaration model,
            JavaDataModelDriver driver) {
        if (semantics == null) {
            throw new IllegalArgumentException("semantics must not be null"); //$NON-NLS-1$
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null"); //$NON-NLS-1$
        }
        if (driver == null) {
            throw new IllegalArgumentException("driver must not be null"); //$NON-NLS-1$
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        this.model = model;
        this.context = new EmitContext(
                semantics,
                config,
                model,
                NameConstants.CATEGORY_DATA_MODEL,
                NameConstants.PATTERN_DATA_MODEL);
        this.driver = driver;
        this.f = config.getFactory();
    }

    /**
     * Emits the projective model.
     * @throws IOException if failed to emit a source program
     */
    public void emit() throws IOException {
        driver.generateResources(context, model);
        context.emit(f.newClassDeclaration(
                new JavadocBuilder(f)
                    .text("{0}を表すデータモデルクラス。", context.getDescription(model))
                    .toJavadoc(),
                createModifiers(),
                context.getTypeName(),
                null,
                createSuperInterfaces(),
                createMembers()));
    }

    private List<Attribute> createModifiers() throws IOException {
        List<Attribute> results = new ArrayList<Attribute>();
        results.addAll(driver.getTypeAnnotations(context, model));
        results.addAll(new AttributeBuilder(f)
            .Public()
            .toAttributes());
        return results;
    }

    private List<Type> createSuperInterfaces() throws IOException {
        List<Type> results = new ArrayList<Type>();
        results.add(f.newParameterizedType(
                context.resolve(DataModel.class),
                context.resolve(context.getQualifiedTypeName())));
        results.addAll(driver.getInterfaces(context, model));
        return results;
    }

    private List<TypeBodyDeclaration> createMembers() throws IOException {
        List<TypeBodyDeclaration> results = new ArrayList<TypeBodyDeclaration>();
        results.addAll(createPropertyFields());
        results.addAll(driver.getFields(context, model));
        results.addAll(createDataModelMethods());
        results.addAll(createPropertyAccessors());
        results.addAll(driver.getMethods(context, model));
        return results;
    }

    private List<FieldDeclaration> createPropertyFields() {
        List<FieldDeclaration> results = new ArrayList<FieldDeclaration>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            Type type = context.getFieldType(property);
            SimpleName name = context.getFieldName(property);
            results.add(f.newFieldDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Final()
                        .toAttributes(),
                    type,
                    name,
                    context.getFieldInitializer(property)));
        }
        return results;
    }

    private List<MethodDeclaration> createDataModelMethods() {
        List<MethodDeclaration> results = new ArrayList<MethodDeclaration>();
        results.add(createResetMethod());
        results.add(createCopyMethod());
        return results;
    }

    private MethodDeclaration createResetMethod() {
        List<Statement> statements = new ArrayList<Statement>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            statements.add(new ExpressionBuilder(f, f.newThis())
                .field(context.getFieldName(property))
                .method("setNull")
                .toStatement());
        }
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .annotation(context.resolve(SuppressWarnings.class),
                            Models.toLiteral(f, "deprecation"))
                    .Public()
                    .toAttributes(),
                context.resolve(void.class),
                f.newSimpleName("reset"),
                Collections.<FormalParameterDeclaration>emptyList(),
                statements);
    }

    private MethodDeclaration createCopyMethod() {
        SimpleName other = context.createVariableName("other");
        List<Statement> statements = new ArrayList<Statement>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            statements.add(new ExpressionBuilder(f, f.newThis())
                .field(context.getFieldName(property))
                .method("copyFrom", new ExpressionBuilder(f, other)
                    .field(context.getFieldName(property))
                    .toExpression())
                .toStatement());
        }
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .annotation(context.resolve(SuppressWarnings.class),
                            Models.toLiteral(f, "deprecation"))
                    .Public()
                    .toAttributes(),
                context.resolve(void.class),
                f.newSimpleName("copyFrom"),
                Collections.singletonList(f.newFormalParameterDeclaration(
                        context.resolve(context.getQualifiedTypeName()),
                        other)),
                statements);
    }

    private List<MethodDeclaration> createPropertyAccessors() throws IOException {
        List<MethodDeclaration> results = new ArrayList<MethodDeclaration>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            results.add(createValueGetter(property));
            results.add(createValueSetter(property));
            results.add(createOptionGetter(property));
            results.add(createOptionSetter(property));
        }
        return results;
    }

    private MethodDeclaration createValueGetter(PropertyDeclaration property) {
        assert property != null;
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.addAll(new AttributeBuilder(f)
            .Public()
            .toAttributes());

        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("{0}を返す。",
                            context.getDescription(property))
                    .returns()
                        .text("{0}",
                                context.getDescription(property))
                    .exception(context.resolve(NullPointerException.class))
                        .text("{0}の値が<code>null</code>である場合",
                                context.getDescription(property))
                    .toJavadoc(),
                attributes,
                context.getValueType(property),
                context.getValueGetterName(property),
                Collections.<FormalParameterDeclaration>emptyList(),
                Collections.singletonList(new ExpressionBuilder(f, f.newThis())
                    .field(context.getFieldName(property))
                    .method("get")
                    .toReturnStatement()));
    }

    private MethodDeclaration createValueSetter(PropertyDeclaration property) {
        assert property != null;
        SimpleName paramName = context.createVariableName("value");
        Type valueType = context.getValueType(property);
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("{0}を設定する。",
                            context.getDescription(property))
                    .param(paramName)
                        .text("設定する値",
                                context.getDescription(property))
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .annotation(
                            context.resolve(SuppressWarnings.class),
                            Models.toLiteral(f, "deprecation"))
                    .Public()
                    .toAttributes(),
                context.resolve(void.class),
                context.getValueSetterName(property),
                Arrays.asList(new FormalParameterDeclaration[] {
                        f.newFormalParameterDeclaration(valueType, paramName)
                }),
                Collections.singletonList(new ExpressionBuilder(f, f.newThis())
                    .field(context.getFieldName(property))
                    .method("modify", paramName)
                    .toStatement()));
    }

    private MethodDeclaration createOptionGetter(PropertyDeclaration property) throws IOException {
        assert property != null;
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.addAll(driver.getMemberAnnotations(context, property));
        attributes.addAll(new AttributeBuilder(f)
            .Public()
            .toAttributes());

        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("<code>null</code>を許す{0}を返す。",
                            context.getDescription(property))
                    .returns()
                        .text("{0}",
                                context.getDescription(property))
                    .toJavadoc(),
                attributes,
                context.getFieldType(property),
                context.getOptionGetterName(property),
                Collections.<FormalParameterDeclaration>emptyList(),
                Collections.singletonList(new ExpressionBuilder(f, f.newThis())
                    .field(context.getFieldName(property))
                    .toReturnStatement()));
    }

    private MethodDeclaration createOptionSetter(PropertyDeclaration property) {
        assert property != null;
        SimpleName paramName = context.createVariableName("option");
        Type optionType = context.getFieldType(property);
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("{0}を設定する。",
                            context.getDescription(property))
                    .param(paramName)
                        .text("設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる",
                                context.getDescription(property))
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .annotation(
                            context.resolve(SuppressWarnings.class),
                            Models.toLiteral(f, "deprecation"))
                    .Public()
                    .toAttributes(),
                context.resolve(void.class),
                context.getOptionSetterName(property),
                Arrays.asList(new FormalParameterDeclaration[] {
                        f.newFormalParameterDeclaration(optionType, paramName)
                }),
                Collections.singletonList(new ExpressionBuilder(f, f.newThis())
                    .field(context.getFieldName(property))
                    .method("copyFrom", paramName)
                    .toStatement()));
    }
}
