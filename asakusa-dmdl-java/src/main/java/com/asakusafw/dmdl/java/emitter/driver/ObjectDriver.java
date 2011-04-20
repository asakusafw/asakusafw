/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.dmdl.java.emitter.driver;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.ashigeru.lang.java.model.syntax.Annotation;
import com.ashigeru.lang.java.model.syntax.FormalParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.InfixOperator;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.Models;
import com.ashigeru.lang.java.model.util.TypeBuilder;

/**
 * Overrides default {@link Object} methods.
 */
public class ObjectDriver implements JavaDataModelDriver {

    @Override
    public List<Type> getInterfaces(EmitContext context, ModelDeclaration model) {
        return Collections.emptyList();
    }

    @Override
    public List<MethodDeclaration> getMethods(EmitContext context, ModelDeclaration model) {
        if (model.getOriginalAst().kind == ModelDefinitionKind.PROJECTIVE) {
            return Collections.emptyList();
        }
        List<MethodDeclaration> results = new ArrayList<MethodDeclaration>();
        results.add(createToString(context, model));
        results.add(createHashCode(context, model));
        results.add(createEquals(context, model));
        return results;
    }

    private MethodDeclaration createToString(EmitContext context, ModelDeclaration model) {
        assert context != null;
        assert model != null;
        ModelFactory f = context.getModelFactory();
        List<Statement> statements = new ArrayList<Statement>();
        SimpleName buffer = context.createVariableName("result");
        statements.add(new TypeBuilder(f, context.resolve(StringBuilder.class))
            .newObject()
            .toLocalVariableDeclaration(context.resolve(StringBuilder.class), buffer));
        statements.add(new ExpressionBuilder(f, buffer)
            .method("append", Models.toLiteral(f, "{"))
            .toStatement());
        statements.add(new ExpressionBuilder(f, buffer)
            .method("append", Models.toLiteral(f, "class=" + model.getName().identifier))
            .toStatement());
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            statements.add(new ExpressionBuilder(f, buffer)
                .method("append", Models.toLiteral(f, MessageFormat.format(
                        ", {0}=",
                        context.getFieldName(property))))
                .toStatement());
            statements.add(new ExpressionBuilder(f, buffer)
                .method("append", new ExpressionBuilder(f, f.newThis())
                    .field(context.getFieldName(property))
                    .toExpression())
                .toStatement());
        }
        statements.add(new ExpressionBuilder(f, buffer)
            .method("append", Models.toLiteral(f, "}"))
            .toStatement());
        statements.add(new ExpressionBuilder(f, buffer)
            .method("toString")
            .toReturnStatement());
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                context.resolve(String.class),
                f.newSimpleName("toString"),
                Collections.<FormalParameterDeclaration>emptyList(),
                statements);
    }

    private MethodDeclaration createHashCode(EmitContext context, ModelDeclaration model) {
        assert context != null;
        assert model != null;
        ModelFactory f = context.getModelFactory();
        List<Statement> statements = new ArrayList<Statement>();
        SimpleName prime = context.createVariableName("prime");
        SimpleName result = context.createVariableName("result");
        statements.add(new ExpressionBuilder(f, Models.toLiteral(f, 31))
            .toLocalVariableDeclaration(Models.toType(f, int.class), prime));
        statements.add(new ExpressionBuilder(f, Models.toLiteral(f, 1))
            .toLocalVariableDeclaration(Models.toType(f, int.class), result));
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            SimpleName field = context.getFieldName(property);
            statements.add(new ExpressionBuilder(f, result)
                .assignFrom(new ExpressionBuilder(f, prime)
                    .apply(InfixOperator.TIMES, result)
                    .apply(InfixOperator.PLUS, new ExpressionBuilder(f, field)
                        .method("hashCode")
                        .toExpression())
                    .toExpression())
                .toStatement());
        }
        statements.add(f.newReturnStatement(result));

        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                Models.toType(f, int.class),
                f.newSimpleName("hashCode"),
                Collections.<FormalParameterDeclaration>emptyList(),
                statements);
    }

    private MethodDeclaration createEquals(EmitContext context, ModelDeclaration model) {
        assert context != null;
        assert model != null;
        ModelFactory f = context.getModelFactory();
        List<Statement> statements = new ArrayList<Statement>();
        SimpleName obj = context.createVariableName("obj");
        statements.add(f.newIfStatement(
                new ExpressionBuilder(f, f.newThis())
                    .apply(InfixOperator.EQUALS, obj)
                    .toExpression(),
                f.newBlock(f.newReturnStatement(Models.toLiteral(f, true)))));
        statements.add(f.newIfStatement(
                new ExpressionBuilder(f, obj)
                    .apply(InfixOperator.EQUALS, Models.toNullLiteral(f))
                    .toExpression(),
                f.newBlock(f.newReturnStatement(Models.toLiteral(f, false)))));
        statements.add(f.newIfStatement(
                new ExpressionBuilder(f, f.newThis())
                    .method("getClass")
                    .apply(InfixOperator.NOT_EQUALS, new ExpressionBuilder(f, obj)
                        .method("getClass")
                        .toExpression())
                    .toExpression(),
                f.newBlock(f.newReturnStatement(Models.toLiteral(f, false)))));
        SimpleName other = context.createVariableName("other");
        Type self = context.resolve(context.getQualifiedTypeName());
        statements.add(new ExpressionBuilder(f, obj)
            .castTo(self)
            .toLocalVariableDeclaration(self, other));
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            SimpleName field = context.getFieldName(property);
            statements.add(f.newIfStatement(
                    new ExpressionBuilder(f, f.newThis())
                        .field(field)
                        .method("equals", new ExpressionBuilder(f, other)
                            .field(field)
                            .toExpression())
                        .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
                        .toExpression(),
                    f.newBlock(f.newReturnStatement(Models.toLiteral(f, false)))));
        }
        statements.add(f.newReturnStatement(Models.toLiteral(f, true)));

        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                Models.toType(f, boolean.class),
                f.newSimpleName("equals"),
                Collections.singletonList(f.newFormalParameterDeclaration(
                        context.resolve(Object.class),
                        obj)),
                statements);
    }

    @Override
    public List<Annotation> getTypeAnnotations(EmitContext context, ModelDeclaration model) {
        return Collections.emptyList();
    }

    @Override
    public List<Annotation> getMemberAnnotations(EmitContext context, PropertyDeclaration property) {
        return Collections.emptyList();
    }
}
