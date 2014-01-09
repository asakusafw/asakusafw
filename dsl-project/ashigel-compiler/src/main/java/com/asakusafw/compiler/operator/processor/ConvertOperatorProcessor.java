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
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.vocabulary.operator.Convert;


/**
 * {@link Convert 変換演算子}を処理する。
 */
@TargetOperator(Convert.class)
public class ConvertOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isAbstract()) {
            a.error("変換演算子はabstractで宣言できません");
        }
        if (a.getReturnType().isConcreteModel() == false) {
            a.error("変換演算子は戻り値にモデルオブジェクト型を指定する必要があります");
        }
        if (a.getParameterType(0).isModel() == false) {
            a.error(0, "変換演算子の最初の引数はモデルオブジェクト型である必要があります");
        }
        for (int i = 1, n = a.countParameters(); i < n; i++) {
            if (a.getParameterType(i).isBasic() == false) {
                a.error(i, "変換演算子の2つ目以降の引数は文字列またはプリミティブ型である必要があります");
            }
        }
        Convert annotation = context.element.getAnnotation(Convert.class);
        if (annotation == null) {
            a.error("注釈の解釈に失敗しました");
            return null;
        }
        OperatorProcessorUtil.checkPortName(a, new String[] {
                annotation.originalPort(),
                annotation.convertedPort(),
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
                "入力された内容",
                annotation.originalPort(),
                a.getParameterType(0).getType(),
                a.getParameterName(0),
                0);
        builder.addOutput(
                a.getReturnDocument(),
                annotation.convertedPort(),
                a.getReturnType().getType(),
                null,
                null);
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
