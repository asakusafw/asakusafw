/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import com.asakusafw.compiler.operator.ExecutableAnalyzer.ShuffleKeySpec;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.TypeConstraint;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.compiler.operator.processor.MasterKindOperatorAnalyzer.ResolveException;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
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
            a.error(Messages.getString("MasterJoinUpdateOperatorProcessor.errorAbstract")); //$NON-NLS-1$
        }
        if (a.getReturnType().isVoid() == false) {
            a.error(Messages.getString("MasterJoinUpdateOperatorProcessor.errorNotVoidResult")); //$NON-NLS-1$
        }
        TypeConstraint master = a.getParameterType(0);
        if (master.isModel() == false) {
            a.error(0, Messages.getString("MasterJoinUpdateOperatorProcessor.errorNotModelMaster")); //$NON-NLS-1$
        }
        TypeConstraint transaction = a.getParameterType(1);
        if (transaction.isModel() == false) {
            a.error(1, Messages.getString("MasterJoinUpdateOperatorProcessor.errorNotModelTransaction")); //$NON-NLS-1$
        }
        for (int i = 2, n = a.countParameters(); i < n; i++) {
            if (a.getParameterType(i).isBasic() == false) {
                a.error(i, Messages.getString(
                        "MasterJoinUpdateOperatorProcessor.errorInvalidOptionParameter")); //$NON-NLS-1$
            }
        }
        if (a.hasError()) {
            return null;
        }

        ShuffleKeySpec masterKey = a.getParameterKeySpec(0);
        if (masterKey == null) {
            a.error(Messages.getString(
                    "MasterJoinUpdateOperatorProcessor.errorMissingKeyAnnotationMaster")); //$NON-NLS-1$
        }
        ShuffleKeySpec transactionKey = a.getParameterKeySpec(1);
        if (transactionKey == null) {
            a.error(Messages.getString(
                    "MasterJoinUpdateOperatorProcessor.errorMissingKeyAnnotationTransaction")); //$NON-NLS-1$
        }
        a.validateShuffleKeys(masterKey, transactionKey);
        ExecutableElement selector = null;
        try {
            selector = MasterKindOperatorAnalyzer.findSelector(context.environment, context);
        } catch (ResolveException e) {
            a.error(e.getMessage());
        }

        MasterJoinUpdate annotation = context.element.getAnnotation(MasterJoinUpdate.class);
        if (annotation == null) {
            a.error(Messages.getString("MasterJoinUpdateOperatorProcessor.errorInvalidAnnotation")); //$NON-NLS-1$
            return null;
        }
        OperatorProcessorUtil.checkPortName(a, new String[] {
                annotation.updatedPort(),
                annotation.missedPort(),
        });
        if (a.hasError()) {
            return null;
        }
        assert masterKey != null;
        assert transactionKey != null;

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
                masterKey.getKey());
        builder.addInput(
                a.getParameterDocument(1),
                a.getParameterName(1),
                a.getParameterType(1).getType(),
                1,
                transactionKey.getKey());
        builder.addOutput(
                Messages.getString("MasterJoinUpdateOperatorProcessor.javadocJoined"), //$NON-NLS-1$
                annotation.updatedPort(),
                a.getParameterType(1).getType(),
                a.getParameterName(1),
                null);
        builder.addOutput(
                Messages.getString("MasterJoinUpdateOperatorProcessor.javadocMissed"), //$NON-NLS-1$
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
