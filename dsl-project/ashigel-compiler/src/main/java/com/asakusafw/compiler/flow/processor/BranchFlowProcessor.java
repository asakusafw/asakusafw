/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.processor;

import java.lang.reflect.Method;
import java.util.List;

import com.asakusafw.compiler.common.EnumUtil;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.flow.LineEndProcessor;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Tuple2;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Branch;
import com.asakusafw.vocabulary.operator.Update;

/**
 * {@link Update 分岐演算子}を処理する。
 */
@TargetOperator(Branch.class)
public class BranchFlowProcessor extends LineEndProcessor {

    @Override
    public void emitLineEnd(Context context) {
        ModelFactory f = context.getModelFactory();
        Expression input = context.getInput();
        Expression impl = context.createImplementation();
        OperatorDescription desc = context.getOperatorDescription();

        Method method = desc.getDeclaration().toMethod();
        assert method != null : desc.getDeclaration();
        Class<?> enumType = method.getReturnType();

        List<Expression> arguments = Lists.create();
        arguments.add(input);
        for (OperatorDescription.Parameter param : desc.getParameters()) {
            arguments.add(Models.toLiteral(f, param.getValue()));
        }
        Expression result = context.createLocalVariable(
                enumType,
                new ExpressionBuilder(f, impl)
                    .method(desc.getDeclaration().getName(), arguments)
                    .toExpression());
        List<Tuple2<Enum<?>, FlowElementPortDescription>> constants =
            EnumUtil.extractConstants(enumType, desc.getOutputPorts());

        List<Statement> cases = Lists.create();
        for (Tuple2<Enum<?>, FlowElementPortDescription> tuple : constants) {
            Enum<?> constant = tuple.first;
            FlowElementPortDescription port = tuple.second;
            ResultMirror next = context.getOutput(port);
            cases.add(f.newSwitchCaseLabel(f.newSimpleName(constant.name())));
            cases.add(next.createAdd(input));
            cases.add(f.newBreakStatement());
        }
        cases.add(f.newSwitchDefaultLabel());
        cases.add(new TypeBuilder(f, context.convert(AssertionError.class))
            .newObject(result)
            .toThrowStatement());
        context.add(f.newSwitchStatement(result, cases));
    }
}
