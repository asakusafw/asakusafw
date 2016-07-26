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
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Overrides default {@link Object} methods.
 */
public class ObjectDriver extends JavaDataModelDriver {

    @Override
    public List<MethodDeclaration> getMethods(EmitContext context, ModelDeclaration model) {
        if (model.getOriginalAst().kind == ModelDefinitionKind.PROJECTIVE) {
            return Collections.emptyList();
        }
        List<MethodDeclaration> results = new ArrayList<>();
        results.add(createToString(context, model));
        results.add(createHashCode(context, model));
        results.add(createEquals(context, model));
        return results;
    }

    private MethodDeclaration createToString(EmitContext context, ModelDeclaration model) {
        assert context != null;
        assert model != null;
        ModelFactory f = context.getModelFactory();
        List<Statement> statements = new ArrayList<>();
        SimpleName buffer = context.createVariableName("result"); //$NON-NLS-1$
        statements.add(new TypeBuilder(f, context.resolve(StringBuilder.class))
            .newObject()
            .toLocalVariableDeclaration(context.resolve(StringBuilder.class), buffer));
        statements.add(new ExpressionBuilder(f, buffer)
            .method("append", Models.toLiteral(f, "{")) //$NON-NLS-1$ //$NON-NLS-2$
            .toStatement());
        statements.add(new ExpressionBuilder(f, buffer)
            .method("append", Models.toLiteral(f, "class=" + model.getName().identifier)) //$NON-NLS-1$ //$NON-NLS-2$
            .toStatement());
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            statements.add(new ExpressionBuilder(f, buffer)
                .method("append", //$NON-NLS-1$
                        Models.toLiteral(f,
                                MessageFormat.format(", {0}=", context.getFieldName(property)))) //$NON-NLS-1$
                .toStatement());
            statements.add(new ExpressionBuilder(f, buffer)
                .method("append", new ExpressionBuilder(f, f.newThis()) //$NON-NLS-1$
                    .field(context.getFieldName(property))
                    .toExpression())
                .toStatement());
        }
        statements.add(new ExpressionBuilder(f, buffer)
            .method("append", Models.toLiteral(f, "}")) //$NON-NLS-1$ //$NON-NLS-2$
            .toStatement());
        statements.add(new ExpressionBuilder(f, buffer)
            .method("toString") //$NON-NLS-1$
            .toReturnStatement());
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                context.resolve(String.class),
                f.newSimpleName("toString"), //$NON-NLS-1$
                Collections.emptyList(),
                statements);
    }

    private MethodDeclaration createHashCode(EmitContext context, ModelDeclaration model) {
        assert context != null;
        assert model != null;
        ModelFactory f = context.getModelFactory();
        List<Statement> statements = new ArrayList<>();
        SimpleName prime = context.createVariableName("prime"); //$NON-NLS-1$
        SimpleName result = context.createVariableName("result"); //$NON-NLS-1$
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
                        .method("hashCode") //$NON-NLS-1$
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
                f.newSimpleName("hashCode"), //$NON-NLS-1$
                Collections.emptyList(),
                statements);
    }

    private MethodDeclaration createEquals(EmitContext context, ModelDeclaration model) {
        assert context != null;
        assert model != null;
        ModelFactory f = context.getModelFactory();
        List<Statement> statements = new ArrayList<>();
        SimpleName obj = context.createVariableName("obj"); //$NON-NLS-1$
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
                    .method("getClass") //$NON-NLS-1$
                    .apply(InfixOperator.NOT_EQUALS, new ExpressionBuilder(f, obj)
                        .method("getClass") //$NON-NLS-1$
                        .toExpression())
                    .toExpression(),
                f.newBlock(f.newReturnStatement(Models.toLiteral(f, false)))));
        SimpleName other = context.createVariableName("other"); //$NON-NLS-1$
        Type self = context.resolve(context.getQualifiedTypeName());
        statements.add(new ExpressionBuilder(f, obj)
            .castTo(self)
            .toLocalVariableDeclaration(self, other));
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            SimpleName field = context.getFieldName(property);
            statements.add(f.newIfStatement(
                    new ExpressionBuilder(f, f.newThis())
                        .field(field)
                        .method("equals", new ExpressionBuilder(f, other) //$NON-NLS-1$
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
                f.newSimpleName("equals"), //$NON-NLS-1$
                Collections.singletonList(f.newFormalParameterDeclaration(
                        context.resolve(Object.class),
                        obj)),
                statements);
    }
}
