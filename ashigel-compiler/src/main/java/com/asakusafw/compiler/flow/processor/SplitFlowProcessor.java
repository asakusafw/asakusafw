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
package com.asakusafw.compiler.flow.processor;


import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.flow.LineEndProcessor;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Split;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;

/**
 * {@link Split 分割演算子}を処理する。
 */
@TargetOperator(Split.class)
public class SplitFlowProcessor extends LineEndProcessor {

    @Override
    public void emitLineEnd(Context context) {
        ModelFactory f = context.getModelFactory();
        Expression input = context.getInput();
        Expression impl = context.createImplementation();
        OperatorDescription desc = context.getOperatorDescription();

        ResultMirror left = context.getOutput(context.getOutputPort(Split.ID_OUTPUT_LEFT));
        ResultMirror right = context.getOutput(context.getOutputPort(Split.ID_OUTPUT_RIGHT));

        context.add(new ExpressionBuilder(f, impl)
            .method(desc.getDeclaration().getName(), input, left.get(), right.get())
            .toStatement());
    }
}
