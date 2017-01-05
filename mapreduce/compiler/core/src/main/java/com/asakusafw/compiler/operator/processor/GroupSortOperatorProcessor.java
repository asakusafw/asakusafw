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

import javax.lang.model.type.TypeMirror;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.ShuffleKeySpec;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.TypeConstraint;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.operator.CoGroup;
import com.asakusafw.vocabulary.operator.GroupSort;


/**
 * Processes {@link GroupSort} operators.
 */
@TargetOperator(GroupSort.class)
public class GroupSortOperatorProcessor extends AbstractOperatorProcessor {

    private static final int RESULT_START = 1;

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isAbstract()) {
            a.error(Messages.getString("GroupSortOperatorProcessor.errorAbstract")); //$NON-NLS-1$
        }
        if (a.getReturnType().isVoid() == false) {
            a.error(Messages.getString("GroupSortOperatorProcessor.errorNotVoidResult")); //$NON-NLS-1$
        }
        if (a.getParameterType(0).isIterable() == false) {
            a.error(0, Messages.getString("GroupSortOperatorProcessor.errorNotListInput")); //$NON-NLS-1$
        } else if (a.getParameterType(0).getTypeArgument().isModel() == false) {
            a.error(0, Messages.getString("GroupSortOperatorProcessor.errorNotModelInput")); //$NON-NLS-1$
        }

        int startParameters = RESULT_START;
        for (int i = RESULT_START, n = a.countParameters(); i < n; i++) {
            TypeConstraint param = a.getParameterType(i);
            if (param.isResult() == false) {
                break;
            } else if (param.getTypeArgument().isModel() == false) {
                a.error(i, Messages.getString("GroupSortOperatorProcessor.errorNotModelOutput")); //$NON-NLS-1$
            } else {
                startParameters++;
            }
        }
        if (startParameters == RESULT_START) { // missing Result<_>
            a.error(Messages.getString("GroupSortOperatorProcessor.errorMissingOutput")); //$NON-NLS-1$
        }
        for (int i = startParameters, n = a.countParameters(); i < n; i++) {
            TypeConstraint param = a.getParameterType(i);
            if (param.isResult()) {
                a.error(i, Messages.getString("GroupSortOperatorProcessor.errorInvalidResult")); //$NON-NLS-1$
            } else if (param.isBasic() == false) {
                a.error(i, Messages.getString("GroupSortOperatorProcessor.errorInvalidOptionParameter")); //$NON-NLS-1$
            }
        }
        if (a.hasError()) {
            return null;
        }

        ShuffleKeySpec key = a.getParameterKeySpec(0);
        if (key == null) {
            a.error(Messages.getString("GroupSortOperatorProcessor.errorMissingKeyAnnotation")); //$NON-NLS-1$
            return null;
        }
        a.validateShuffleKeys(key);
        GroupSort annotation = context.element.getAnnotation(GroupSort.class);
        if (annotation == null) {
            a.error(Messages.getString("GroupSortOperatorProcessor.errorInvalidAnnotation")); //$NON-NLS-1$
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
                key.getKey());
        for (int i = 1; i < startParameters; i++) {
            TypeConstraint outputType = a.getParameterType(i).getTypeArgument();
            TypeMirror outputTypeMirror = outputType.getType();
            String found = builder.findInput(outputTypeMirror);
            if (found == null && outputType.isProjectiveModel()) {
                a.error(Messages.getString("GroupSortOperatorProcessor.errorUnboundOutput"), //$NON-NLS-1$
                        outputTypeMirror);
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
