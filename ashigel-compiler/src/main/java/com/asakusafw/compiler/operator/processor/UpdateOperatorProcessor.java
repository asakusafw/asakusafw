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

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.vocabulary.operator.Update;


/**
 * {@link Update 更新演算子}を処理する。
 */
@TargetOperator(Update.class)
public class UpdateOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(getEnvironment(), context.element);
        if (a.isAbstract()) {
            a.error("更新演算子はabstractで宣言できません");
        }
        if (a.getReturnType().isVoid() == false) {
            a.error("更新演算子は戻り値にvoidを指定する必要があります");
        }
        if (a.getParameterType(0).isModel() == false) {
            a.error(0, "更新演算子の最初の引数はモデルオブジェクト型である必要があります");
        }
        for (int i = 1, n = a.countParameters(); i < n; i++) {
            if (a.getParameterType(i).isBasic() == false) {
                a.error(i, "更新演算子の2つ目以降の引数は文字列またはプリミティブ型である必要があります");
            }
        }
        Update annotation = context.element.getAnnotation(Update.class);
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

        Builder builder = new Builder(getTargetAnnotationType(), context);
        builder.addAttribute(a.getObservationCount());
        builder.setDocumentation(a.getExecutableDocument());
        builder.addInput(
                a.getParameterDocument(0),
                a.getParameterName(0),
                a.getParameterType(0).getType(),
                0);
        builder.addOutput(
                "結果",
                annotation.outputPort(),
                a.getParameterType(0).getType(),
                0);
        for (int i = 1, n = a.countParameters(); i < n; i++) {
            builder.addParameter(
                    a.getParameterDocument(i),
                    a.getParameterName(i),
                    a.getParameterType(i).getType(),
                    i);
        }
        return builder.toDescriptor();
    }
}
