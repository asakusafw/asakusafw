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
package com.asakusafw.compiler.operator.processor;

import javax.lang.model.type.TypeMirror;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.TypeConstraint;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.CoGroup;
import com.asakusafw.vocabulary.operator.GroupSort;


/**
 * {@link GroupSort グループ整列演算子}を処理する。
 */
@TargetOperator(GroupSort.class)
public class GroupSortOperatorProcessor extends AbstractOperatorProcessor {

    private static final int RESULT_START = 1;

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isAbstract()) {
            a.error("グループ整列演算子はabstractで宣言できません");
        }
        if (a.getReturnType().isVoid() == false) {
            a.error("グループ整列演算子は戻り値にvoidを指定する必要があります");
        }
        if (a.getParameterType(0).isList() == false) {
            a.error(0, "グループ整列演算子の最初の引数はリスト型(java.util.List)である必要があります");
        } else if (a.getParameterType(0).getTypeArgument().isModel() == false) {
            a.error(0, "グループ整列演算子の最初の引数はリストのモデルオブジェクト型である必要があります");
        }

        int startParameters = RESULT_START;
        for (int i = RESULT_START, n = a.countParameters(); i < n; i++) {
            TypeConstraint param = a.getParameterType(i);
            if (param.isResult() == false) {
                break;
            } else if (param.getTypeArgument().isModel() == false) {
                a.error(i, "グループ整列演算子の結果は結果のモデルオブジェクト型である必要があります");
            } else {
                startParameters++;
            }
        }
        if (startParameters == RESULT_START) { // 結果型がない
            a.error("グループ整列演算子の引数には一つ以上の結果(Result)型を指定する必要があります");
        }
        for (int i = startParameters, n = a.countParameters(); i < n; i++) {
            TypeConstraint param = a.getParameterType(i);
            if (param.isResult()) {
                a.error(i, "ユーザー引数の後には結果型を含められません");
            } else if (param.isBasic() == false) {
                a.error(i, "ユーザー引数は文字列またはプリミティブ型である必要があります");
            }
        }
        if (a.hasError()) {
            return null;
        }

        ShuffleKey key = a.getParameterKey(0);
        if (key == null) {
            a.error("グループ整列演算子の引数には@Key注釈によってグループ化項目を指定する必要があります");
            return null;
        }
        GroupSort annotation = context.element.getAnnotation(GroupSort.class);
        if (annotation == null) {
            a.error("注釈の解釈に失敗しました");
            return null;
        }

        // redirect to @CoGroup
        Builder builder = new Builder(CoGroup.class, context);
        builder.addAttribute(FlowBoundary.SHUFFLE);
        builder.addAttribute(a.getObservationCount());
        builder.addAttribute(annotation.inputBuffer());
        builder.setDocumentation(a.getExecutableDocument());
        builder.addInput(
                a.getParameterDocument(0),
                a.getParameterName(0),
                a.getParameterType(0).getTypeArgument().getType(),
                0,
                key);
        for (int i = 1; i < startParameters; i++) {
            TypeConstraint outputType = a.getParameterType(i).getTypeArgument();
            TypeMirror outputTypeMirror = outputType.getType();
            String found = builder.findInput(outputTypeMirror);
            if (found == null && outputType.isProjectiveModel()) {
                a.error("出力型{0}に対する入力が見つかりません", outputTypeMirror);
            }
            builder.addOutput(
                    a.getParameterDocument(i),
                    a.getParameterName(i),
                    outputTypeMirror,
                    found,
                    i);
        }
        for (int i = startParameters, n = a.countParameters(); i < n; i++) {
            builder.addParameter(
                    a.getParameterDocument(i),
                    a.getParameterName(i),
                    a.getParameterType(i).getType(),
                    i);
        }
        return builder.toDescriptor();
    }
}
