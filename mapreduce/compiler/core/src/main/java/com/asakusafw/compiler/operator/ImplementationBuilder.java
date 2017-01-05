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
package com.asakusafw.compiler.operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldAccessExpression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * A builder for building operator implementation classes.
 */
public class ImplementationBuilder {

    private final ExecutableElement element;

    private final ModelFactory factory;

    private final ImportBuilder importer;

    private final NameGenerator names;

    private final Jsr269 converter;

    private final List<Statement> statements;

    private final List<FieldDeclaration> fields;

    /**
     * Creates a new instance.
     * @param context the current context
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ImplementationBuilder(OperatorProcessor.Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        this.element = context.element;
        this.factory = context.environment.getFactory();
        this.importer = context.importer;
        this.names = context.names;
        this.converter = new Jsr269(factory);
        this.statements = new ArrayList<>();
        this.fields = new ArrayList<>();
    }

    /**
     * Returns the parameter name.
     * @param index the parameter number (0-origin)
     * @return the parameter name
     */
    public SimpleName getParameterName(int index) {
        VariableElement parameter = element.getParameters().get(index);
        return factory.newSimpleName(parameter.getSimpleName().toString());
    }

    /**
     * Returns the parameter type.
     * @param index the parameter number (0-origin)
     * @return the parameter type
     */
    public Type getParameterType(int index) {
        VariableElement parameter = element.getParameters().get(index);
        return importer.resolve(converter.convert(parameter.asType()));
    }

    /**
     * Adds a statement into the implementation method.
     * @param statement the target statement
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void addStatement(Statement statement) {
        Precondition.checkMustNotBeNull(statement, "statement"); //$NON-NLS-1$
        this.statements.add(statement);
    }

    /**
     * Adds a statement of data model copy into the implementation method.
     * @param from an expression which represents the source data model object
     * @param to an expression which represents the destination data model object
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public void addCopyStatement(Expression from, Expression to) {
        Precondition.checkMustNotBeNull(from, "from"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(to, "to"); //$NON-NLS-1$
        this.statements.add(new ExpressionBuilder(factory, to)
            .method("copyFrom", from) //$NON-NLS-1$
            .toStatement());
    }

    /**
     * Adds a field that holds a data model object.
     * @param type the field type
     * @param name the field name
     * @return the expression an expression which represents accessing the generated field
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public FieldAccessExpression addModelObjectField(TypeMirror type, String name) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        return addModelObjectField(converter.convert(type), name);
    }

    /**
     * Adds a field that holds a data model object.
     * @param type the field type
     * @param name the field name
     * @return the expression an expression which represents accessing the generated field
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public FieldAccessExpression addModelObjectField(Type type, String name) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        SimpleName fieldName = names.create(name);
        Type fieldType = importer.resolve(type);
        fields.add(factory.newFieldDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .toAttributes(),
                fieldType,
                fieldName,
                new TypeBuilder(factory, fieldType)
                    .newObject()
                    .toExpression()));
        return factory.newFieldAccessExpression(factory.newThis(), fieldName);
    }

    /**
     * Returns the member declaration of operator implementations.
     * @return the member declaration of operator implementations
     */
    public List<TypeBodyDeclaration> toImplementation() {
        List<TypeBodyDeclaration> results = new ArrayList<>();
        results.addAll(fields);
        results.add(toMethodDeclaration());
        return results;
    }

    private MethodDeclaration toMethodDeclaration() {
        return factory.newMethodDeclaration(
                null,
                new AttributeBuilder(factory)
                    .annotation(importer.toType(Override.class))
                    .Public()
                    .toAttributes(),
                toTypeParameters(),
                importer.resolve(converter.convert(element.getReturnType())),
                factory.newSimpleName(element.getSimpleName().toString()),
                toParameters(),
                0,
                Collections.emptyList(),
                factory.newBlock(statements));
    }

    private List<FormalParameterDeclaration> toParameters() {
        List<? extends VariableElement> parameters = element.getParameters();
        List<FormalParameterDeclaration> results = new ArrayList<>();
        for (int i = 0, n = parameters.size(); i < n; i++) {
            VariableElement var = parameters.get(i);
            results.add(factory.newFormalParameterDeclaration(
                    Collections.emptyList(),
                    importer.resolve(converter.convert(var.asType())),
                    (i == n - 1) && element.isVarArgs(),
                    factory.newSimpleName(var.getSimpleName().toString()),
                    0));
        }
        return results;
    }

    private List<TypeParameterDeclaration> toTypeParameters() {
        List<? extends TypeParameterElement> typeParameters = element.getTypeParameters();
        if (typeParameters.isEmpty()) {
            return Collections.emptyList();
        }
        List<TypeParameterDeclaration> results = new ArrayList<>();
        for (TypeParameterElement typeParameter : typeParameters) {
            SimpleName name = factory.newSimpleName(typeParameter.getSimpleName().toString());
            List<Type> typeBounds = new ArrayList<>();
            for (TypeMirror typeBound : typeParameter.getBounds()) {
                typeBounds.add(importer.resolve(converter.convert(typeBound)));
            }
            results.add(factory.newTypeParameterDeclaration(name, typeBounds));
        }
        return results;
    }
}
