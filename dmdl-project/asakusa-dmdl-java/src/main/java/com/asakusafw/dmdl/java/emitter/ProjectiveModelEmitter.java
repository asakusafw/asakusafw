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
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;

/**
 * Abstract super class which emits a projective model
 * as a Java model class.
 */
public class ProjectiveModelEmitter {

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
    public ProjectiveModelEmitter(
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
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        if (driver == null) {
            throw new IllegalArgumentException("driver must not be null"); //$NON-NLS-1$
        }
        this.model = model;
        this.driver = driver;
        this.context = new EmitContext(
                semantics,
                config,
                model,
                NameConstants.CATEGORY_DATA_MODEL,
                NameConstants.PATTERN_DATA_MODEL);
        this.f = config.getFactory();
    }

    /**
     * Emits the projective model.
     * @throws IOException if failed to emit a source program
     */
    public void emit() throws IOException {
        driver.generateResources(context, model);
        context.emit(f.newInterfaceDeclaration(
                new JavadocBuilder(f)
                    .text(Messages.getString("ProjectiveModelEmitter.javadocClass"), //$NON-NLS-1$
                            context.getDescription(model))
                    .toJavadoc(),
                createModifiers(),
                context.getTypeName(),
                driver.getInterfaces(context, model),
                createMembers()));
    }

    private List<Attribute> createModifiers() throws IOException {
        List<Attribute> results = new ArrayList<>();
        results.addAll(driver.getTypeAnnotations(context, model));
        results.addAll(new AttributeBuilder(f)
            .Public()
            .toAttributes());
        return results;
    }

    private List<TypeBodyDeclaration> createMembers() throws IOException {
        List<TypeBodyDeclaration> results = new ArrayList<>();
        results.addAll(driver.getFields(context, model));
        results.addAll(createPropertyAccessors());
        results.addAll(driver.getMethods(context, model));
        return results;
    }

    private List<MethodDeclaration> createPropertyAccessors() throws IOException {
        List<MethodDeclaration> results = new ArrayList<>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            results.add(createGetter(property));
            results.add(createSetter(property));
            results.add(createOptionGetter(property));
            results.add(createOptionSetter(property));
        }
        return results;
    }

    private MethodDeclaration createGetter(PropertyDeclaration property) {
        assert property != null;
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text(Messages.getString("ProjectiveModelEmitter.javadocGetter"), //$NON-NLS-1$
                            context.getDescription(property))
                    .returns()
                        .text(context.getDescription(property))
                    .exception(context.resolve(NullPointerException.class))
                        .text(Messages.getString("ProjectiveModelEmitter.javadocGetterNullPointerException"), //$NON-NLS-1$
                                context.getDescription(property))
                    .toJavadoc(),
                new AttributeBuilder(f).toAttributes(),
                Collections.emptyList(),
                context.getValueType(property),
                context.getValueGetterName(property),
                Collections.emptyList(),
                0,
                Collections.emptyList(),
                null);
    }

    private MethodDeclaration createSetter(PropertyDeclaration property) {
        assert property != null;
        SimpleName paramName = context.createVariableName("value"); //$NON-NLS-1$
        Type valueType = context.getValueType(property);
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text(Messages.getString("ProjectiveModelEmitter.javadocSetter"), //$NON-NLS-1$
                            context.getDescription(property))
                    .param(paramName)
                        .text(Messages.getString("ProjectiveModelEmitter.javadocSetterParameter"), //$NON-NLS-1$
                                context.getDescription(property))
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .toAttributes(),
                Collections.emptyList(),
                context.resolve(void.class),
                context.getValueSetterName(property),
                Arrays.asList(new FormalParameterDeclaration[] {
                        f.newFormalParameterDeclaration(valueType, paramName)
                }),
                0,
                Collections.emptyList(),
                null);
    }

    private MethodDeclaration createOptionGetter(PropertyDeclaration property) throws IOException {
        assert property != null;
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text(Messages.getString("ProjectiveModelEmitter.javadocOptionGetter"), //$NON-NLS-1$
                            context.getDescription(property))
                    .returns()
                        .text(context.getDescription(property))
                    .toJavadoc(),
                driver.getMemberAnnotations(context, property),
                Collections.emptyList(),
                context.getFieldType(property),
                context.getOptionGetterName(property),
                Collections.emptyList(),
                0,
                Collections.emptyList(),
                null);
    }

    private MethodDeclaration createOptionSetter(PropertyDeclaration property) {
        assert property != null;
        SimpleName paramName = context.createVariableName("option"); //$NON-NLS-1$

        Type optionType = context.getFieldType(property);
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text(Messages.getString("ProjectiveModelEmitter.javadocOptionSetter"), //$NON-NLS-1$
                            context.getDescription(property))
                    .param(paramName)
                        .text(Messages.getString("ProjectiveModelEmitter.javadocOptionSetterParameter"), //$NON-NLS-1$
                                context.getDescription(property))
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .toAttributes(),
                Collections.emptyList(),
                context.resolve(void.class),
                context.getOptionSetterName(property),
                Arrays.asList(new FormalParameterDeclaration[] {
                        f.newFormalParameterDeclaration(optionType, paramName)
                }),
                0,
                Collections.emptyList(),
                null);
    }
}
