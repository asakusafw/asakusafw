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

import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.vocabulary.flow.graph.Connectivity;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.operator.Logging;

/**
 * {@link Logging ロギング演算子}を処理する。
 */
@TargetOperator(Logging.class)
public class LoggingOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isAbstract()) {
            a.error("ロギング演算子はabstractで宣言できません");
        }
        if (a.getReturnType().isString() == false) {
            a.error("ロギング演算子は戻り値にString型を指定する必要があります");
        }
        if (a.getParameterType(0).isModel() == false) {
            a.error(0, "ロギング演算子の最初の引数はモデルオブジェクト型である必要があります");
        }
        for (int i = 1, n = a.countParameters(); i < n; i++) {
            if (a.getParameterType(i).isBasic() == false) {
                a.error(i, "ロギング演算子の2つ目以降の引数は文字列またはプリミティブ型である必要があります");
            }
        }
        Logging annotation = context.element.getAnnotation(Logging.class);
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

        List<DocElement> elements = Lists.create();
        elements.addAll(a.getExecutableDocument());
        elements.add(context.environment.getFactory().newDocText(
                "<p>なお、この演算子の出力は結線しなくても自動的に停止演算子に結線される。</p>"));

        Builder builder = new Builder(getTargetAnnotationType(), context);
        builder.addAttribute(a.getObservationCount(ObservationCount.AT_LEAST_ONCE));
        builder.addAttribute(Connectivity.OPTIONAL);
        builder.addAttribute(annotation.value());
        builder.setDocumentation(elements);
        builder.addInput(
                a.getParameterDocument(0),
                a.getParameterName(0),
                a.getParameterType(0).getType(),
                0);
        builder.addOutput(
                "入力された内容",
                annotation.outputPort(),
                a.getParameterType(0).getType(),
                a.getParameterName(0),
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
