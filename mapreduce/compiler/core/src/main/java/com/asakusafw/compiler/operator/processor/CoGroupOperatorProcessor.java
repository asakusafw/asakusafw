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

import java.util.ArrayList;
import java.util.List;

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

/**
 * Processes {@link CoGroup} operators.
 */
@TargetOperator(CoGroup.class)
public class CoGroupOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isAbstract()) {
            a.error(Messages.getString("CoGroupOperatorProcessor.errorAbstract")); //$NON-NLS-1$
        }
        if (a.getReturnType().isVoid() == false) {
            a.error(Messages.getString("CoGroupOperatorProcessor.errorNotVoidResult")); //$NON-NLS-1$
        }
        int startResults = 0;
        for (int i = 0, n = a.countParameters(); i < n; i++) {
            TypeConstraint type = a.getParameterType(i);
            if (type.isResult()) {
                break;
            }
            if (type.isIterable() == false) {
                a.error(i, Messages.getString("CoGroupOperatorProcessor.errorNotListInput")); //$NON-NLS-1$
            } else if (type.getTypeArgument().isModel() == false) {
                a.error(i, Messages.getString("CoGroupOperatorProcessor.errorNotModelInput")); //$NON-NLS-1$
            }
            startResults++;
        }
        if (startResults == 0) { // missing List<_>
            a.error(Messages.getString("CoGroupOperatorProcessor.errorMissingInput")); //$NON-NLS-1$
        }

        int startParameters = startResults;
        for (int i = startResults, n = a.countParameters(); i < n; i++) {
            TypeConstraint param = a.getParameterType(i);
            if (param.isResult() == false) {
                break;
            } else if (param.getTypeArgument().isModel() == false) {
                a.error(i, Messages.getString("CoGroupOperatorProcessor.errorNotModelOutput")); //$NON-NLS-1$
            } else {
                startParameters++;
            }
        }
        if (startParameters == startResults) { // missing Result<_>
            a.error(Messages.getString("CoGroupOperatorProcessor.errorMissingOutput")); //$NON-NLS-1$
        }
        for (int i = startParameters, n = a.countParameters(); i < n; i++) {
            TypeConstraint param = a.getParameterType(i);
            if (param.isResult()) {
                a.error(i, Messages.getString("CoGroupOperatorProcessor.errorInvalidResult")); //$NON-NLS-1$
            } else if (param.isBasic() == false) {
                a.error(i, Messages.getString("CoGroupOperatorProcessor.errorInvalidOptionParameter")); //$NON-NLS-1$
            }
        }
        if (a.hasError()) {
            return null;
        }

        List<ShuffleKeySpec> keys = new ArrayList<>();
        for (int i = 0; i < startResults; i++) {
            ShuffleKeySpec key = a.getParameterKeySpec(i);
            if (key == null) {
                a.error(i, Messages.getString("CoGroupOperatorProcessor.errorMissingKeyAnnotation")); //$NON-NLS-1$
            } else {
                keys.add(key);
            }
        }
        a.validateShuffleKeys(keys);
        if (a.hasError()) {
            return null;
        }
        CoGroup annotation = context.element.getAnnotation(CoGroup.class);
        if (annotation == null) {
            a.error(Messages.getString("CoGroupOperatorProcessor.errorInvalidAnnotation")); //$NON-NLS-1$
            return null;
        }

        Builder builder = new Builder(getTargetAnnotationType(), context);
        builder.addAttribute(FlowBoundary.SHUFFLE);
        builder.addAttribute(a.getObservationCount());
        builder.addAttribute(annotation.inputBuffer());
        builder.setDocumentation(a.getExecutableDocument());
        for (int i = 0; i < startResults; i++) {
            builder.addInput(
                    a.getParameterDocument(i),
                    a.getParameterName(i),
                    a.getParameterType(i).getTypeArgument().getType(),
                    i,
                    keys.get(i).getKey());
        }
        for (int i = startResults; i < startParameters; i++) {
            TypeConstraint outputType = a.getParameterType(i).getTypeArgument();
            TypeMirror outputTypeMirror = outputType.getType();
            String found = builder.findInput(outputTypeMirror);
            if (found == null && outputType.isProjectiveModel()) {
                a.error(Messages.getString("CoGroupOperatorProcessor.errorUnboundOutput"), //$NON-NLS-1$
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
