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
package com.asakusafw.compiler.operator.processor;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.vocabulary.operator.Convert;

/**
 * Processes {@link Convert} operators.
 */
@TargetOperator(Convert.class)
public class ConvertOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isAbstract()) {
            a.error(Messages.getString("ConvertOperatorProcessor.errorAbstract")); //$NON-NLS-1$
        }
        if (a.getReturnType().isConcreteModel() == false) {
            a.error(Messages.getString("ConvertOperatorProcessor.errorNotModelResult")); //$NON-NLS-1$
        }
        if (a.getParameterType(0).isModel() == false) {
            a.error(0, Messages.getString("ConvertOperatorProcessor.errorNotModel")); //$NON-NLS-1$
        }
        for (int i = 1, n = a.countParameters(); i < n; i++) {
            if (a.getParameterType(i).isBasic() == false) {
                a.error(i, Messages.getString("ConvertOperatorProcessor.errorInvalidOptionParameter")); //$NON-NLS-1$
            }
        }
        Convert annotation = context.element.getAnnotation(Convert.class);
        if (annotation == null) {
            a.error(Messages.getString("ConvertOperatorProcessor.errorInvalidAnnotation")); //$NON-NLS-1$
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
                Messages.getString("ConvertOperatorProcessor.javadocInput"), //$NON-NLS-1$
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
