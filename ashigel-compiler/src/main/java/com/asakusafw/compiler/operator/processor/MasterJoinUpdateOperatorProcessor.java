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
package com.asakusafw.compiler.operator.processor;

import javax.lang.model.element.ExecutableElement;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.TypeConstraint;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.compiler.operator.processor.MasterKindOperatorAnalyzer.ResolveException;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.MasterJoinUpdate;


/**
 * {@link MasterJoinUpdate マスタつき更新演算子}を処理する。
 */
@TargetOperator(MasterJoinUpdate.class)
public class MasterJoinUpdateOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isAbstract()) {
            a.error("マスタつき更新演算子はabstractで宣言できません");
        }
        if (a.getReturnType().isVoid() == false) {
            a.error("マスタつき更新演算子は戻り値にvoidを指定する必要があります");
        }
        TypeConstraint master = a.getParameterType(0);
        if (master.isModel() == false) {
            a.error(0, "マスタつき更新演算子の一つ目の引数はモデルオブジェクト型である必要があります");
        }
        TypeConstraint transaction = a.getParameterType(1);
        if (transaction.isModel() == false) {
            a.error(1, "マスタつき更新演算子の二つ目の引数はモデルオブジェクト型である必要があります");
        }
        for (int i = 2, n = a.countParameters(); i < n; i++) {
            if (a.getParameterType(i).isBasic() == false) {
                a.error(i, "マスタつき更新演算子の2つ目以降の引数は文字列またはプリミティブ型である必要があります");
            }
        }
        if (a.hasError()) {
            return null;
        }

        ShuffleKey masterKey = a.getParameterKey(0);
        if (masterKey == null) {
            a.error("マスタつき更新演算子の引数には@Key注釈によってグループ化項目を指定する必要があります");
        }
        ShuffleKey transactionKey = a.getParameterKey(1);
        if (transactionKey == null) {
            a.error("マスタつき更新演算子の引数には@Key注釈によってグループ化項目を指定する必要があります");
        }
        ExecutableElement selector = null;
        try {
            selector = MasterKindOperatorAnalyzer.findSelector(context.environment, context);
        } catch (ResolveException e) {
            a.error(e.getMessage());
        }

        MasterJoinUpdate annotation = context.element.getAnnotation(MasterJoinUpdate.class);
        if (annotation == null) {
            a.error("注釈の解釈に失敗しました");
            return null;
        }
        OperatorProcessorUtil.checkPortName(a, new String[] {
                annotation.updatedPort(),
                annotation.missedPort(),
        });
        if (a.hasError()) {
            return null;
        }

        Builder builder = new Builder(getTargetAnnotationType(), context);
        builder.addAttribute(a.getObservationCount());
        builder.addAttribute(FlowBoundary.SHUFFLE);
        if (selector != null) {
            builder.addOperatorHelper(selector);
        }
        builder.setDocumentation(a.getExecutableDocument());
        builder.addInput(
                a.getParameterDocument(0),
                a.getParameterName(0),
                a.getParameterType(0).getType(),
                0,
                masterKey);
        builder.addInput(
                a.getParameterDocument(1),
                a.getParameterName(1),
                a.getParameterType(1).getType(),
                1,
                transactionKey);
        builder.addOutput(
                "引き当ておよび更新が成功したデータ",
                annotation.updatedPort(),
                a.getParameterType(1).getType(),
                a.getParameterName(1),
                null);
        builder.addOutput(
                "引き当てに失敗したデータ",
                annotation.missedPort(),
                a.getParameterType(1).getType(),
                a.getParameterName(1),
                null);
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
