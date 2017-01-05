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

import java.util.List;

import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Mock operator processor.
 */
@TargetOperator(MockOperator.class)
public class MockOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        if (context.element.getParameters().size() != 2) {
            context.environment.getMessager().printMessage(Diagnostic.Kind.ERROR, "invalid parameters");
            return null;
        }
        if (context.element.getReturnType().getKind() == TypeKind.VOID) {
            context.environment.getMessager().printMessage(Diagnostic.Kind.ERROR, "invalid return type");
            return null;
        }

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.countParameters() != 2) {
            return null;
        }
        if (a.getReturnType().isVoid()) {
            return null;
        }

        Builder builder = new Builder(MockOperator.class, context);
        builder.addInput(
                a.getParameterDocument(0),
                "in",
                a.getParameterType(0).getType(),
                0);

        builder.addParameter(
                a.getParameterDocument(1),
                "param",
                a.getParameterType(1).getType(),
                1);

        builder.addOutput(
                a.getReturnDocument(),
                "out",
                a.getReturnType().getType(),
                null,
                null);

        return builder.toDescriptor();
    }

    @Override
    protected List<? extends TypeBodyDeclaration> override(Context context) {
        ModelFactory factory = context.environment.getFactory();
        ImplementationBuilder builder = new ImplementationBuilder(context);
        builder.addStatement(new ExpressionBuilder(factory, builder.getParameterName(0))
            .apply(InfixOperator.PLUS, builder.getParameterName(1))
            .apply(InfixOperator.PLUS, Models.toLiteral(factory, "!"))
            .toReturnStatement());
        return builder.toImplementation();
    }
}
