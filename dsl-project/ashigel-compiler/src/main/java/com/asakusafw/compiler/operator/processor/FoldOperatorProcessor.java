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
package com.asakusafw.compiler.operator.processor;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.ShuffleKeySpec;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.TypeConstraint;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.operator.Fold;


/**
 * {@link Fold 畳み込み演算子}を処理する。
 */
@TargetOperator(Fold.class)
public class FoldOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isAbstract()) {
            a.error("畳み込み演算子はabstractで宣言できません");
        }
        if (a.getReturnType().isVoid() == false) {
            a.error("畳み込み演算子は戻り値にvoidを指定する必要があります");
        }
        TypeConstraint left = a.getParameterType(0);
        if (left.isModel() == false) {
            a.error(0, "畳み込み演算子の1つ目の引数はモデルオブジェクト型である必要があります");
        }
        TypeConstraint right = a.getParameterType(1);
        if (right.isModel() == false) {
            a.error(1, "畳み込み演算子の2つ目の引数はモデルオブジェクト型である必要があります");
        }
        for (int i = 2, n = a.countParameters(); i < n; i++) {
            if (a.getParameterType(i).isBasic() == false) {
                a.error(i, "畳み込み演算子の3つ目以降の引数は文字列またはプリミティブ型である必要があります");
            }
        }
        if (a.hasError()) {
            return null;
        }

        if (context.environment.getTypeUtils().isSameType(left.getType(), right.getType()) == false) {
            a.error(1, "畳み込み演算子の1つ目の引数と2つ目の引数は同じ型である必要があります");
        }
        ShuffleKeySpec foldKey = a.getParameterKeySpec(0);
        if (foldKey == null) {
            a.error("畳み込み演算子の引数には@Key注釈によってグループ化項目を指定する必要があります");
        }
        a.validateShuffleKeys(foldKey);
        Fold annotation = context.element.getAnnotation(Fold.class);
        if (annotation == null) {
            a.error("注釈の解釈に失敗しました");
            return null;
        }
        OperatorProcessorUtil.checkPortName(a, new String[] {
                annotation.outputPort(),
        });
        if (a.hasError()) {
            return null;
        }
        assert foldKey != null;

        Builder builder = new Builder(getTargetAnnotationType(), context);
        builder.addAttribute(FlowBoundary.SHUFFLE);
        builder.addAttribute(a.getObservationCount());
        builder.addAttribute(annotation.partialAggregation());
        builder.setDocumentation(a.getExecutableDocument());
        builder.addInput(
                a.getParameterDocument(1),
                Fold.INPUT,
                a.getParameterType(1).getType(),
                1,
                foldKey.getKey());
        builder.addOutput(
                "畳み込みの結果",
                annotation.outputPort(),
                a.getParameterType(0).getType(),
                Fold.INPUT,
                0);
        for (int i = 2, n = a.countParameters(); i < n; i++) {
            builder.addParameter(
                    a.getParameterDocument(i),
                    a.getParameterName(i),
                    a.getParameterType(i).getType(),
                    i);
        }
        return builder.toDescriptor();
    }
}
