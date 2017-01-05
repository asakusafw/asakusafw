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

import java.util.List;

import javax.lang.model.element.ExecutableElement;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.TypeConstraint;
import com.asakusafw.compiler.operator.ImplementationBuilder;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.compiler.operator.processor.MasterKindOperatorAnalyzer.ResolveException;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.MasterJoin;

/**
 * Processes {@link MasterJoin} operators.
 */
@TargetOperator(MasterJoin.class)
public class MasterJoinOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isGeneric()) {
            a.error(Messages.getString("MasterJoinOperatorProcessor.errorGeneric")); //$NON-NLS-1$
        }
        if (a.isAbstract() == false) {
            a.error(Messages.getString("MasterJoinOperatorProcessor.errorNotAbstract")); //$NON-NLS-1$
        }
        TypeConstraint joined = a.getReturnType();
        if (joined.isConcreteModel() == false) {
            a.error(Messages.getString("MasterJoinOperatorProcessor.errorNotModelResult")); //$NON-NLS-1$
        }
        TypeConstraint master = a.getParameterType(0);
        if (master.isModel() == false) {
            a.error(0, Messages.getString("MasterJoinOperatorProcessor.errorNotModelMaster")); //$NON-NLS-1$
        }
        TypeConstraint transaction = a.getParameterType(1);
        if (transaction.isModel() == false) {
            a.error(1, Messages.getString("MasterJoinOperatorProcessor.errorNotModelTransaction")); //$NON-NLS-1$
        }
        for (int i = 2, n = a.countParameters(); i < n; i++) {
            a.error(i, Messages.getString("MasterJoinOperatorProcessor.errorExtraParameter")); //$NON-NLS-1$
        }
        ExecutableElement selector = null;
        try {
            selector = MasterKindOperatorAnalyzer.findSelector(context);
        } catch (ResolveException e) {
            a.error(e.getMessage());
        }
        if (joined.isJoinedModel(master.getType(), transaction.getType()) == false) {
            a.error(Messages.getString("MasterJoinOperatorProcessor.errorNotJoinedModel")); //$NON-NLS-1$
            return null;
        }

        ShuffleKey masterKey = joined.getJoinKey(master.getType());
        ShuffleKey transactionKey = joined.getJoinKey(transaction.getType());

        MasterJoin annotation = context.element.getAnnotation(MasterJoin.class);
        if (annotation == null) {
            a.error(Messages.getString("MasterJoinOperatorProcessor.errorInvalidAnnotation")); //$NON-NLS-1$
            return null;
        }
        OperatorProcessorUtil.checkPortName(a, new String[] {
                annotation.joinedPort(),
                annotation.missedPort(),
        });
        if (a.hasError()) {
            return null;
        }

        Builder builder = new Builder(getTargetAnnotationType(), context);
        builder.addAttribute(FlowBoundary.SHUFFLE);
        builder.addAttribute(a.getObservationCount());
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
                a.getReturnDocument(),
                annotation.joinedPort(),
                a.getReturnType().getType(),
                null,
                null);
        builder.addOutput(
                Messages.getString("MasterJoinOperatorProcessor.javadocMissing"), //$NON-NLS-1$
                annotation.missedPort(),
                a.getParameterType(1).getType(),
                a.getParameterName(1),
                null);
        return builder.toDescriptor();
    }

    @Override
    protected List<? extends TypeBodyDeclaration> override(Context context) {
        ImplementationBuilder builder = new ImplementationBuilder(context);
        ModelFactory f = context.environment.getFactory();
        builder.addStatement(new TypeBuilder(f, context.importer.toType(UnsupportedOperationException.class))
            .newObject(Models.toLiteral(f,
                    Messages.getString("MasterJoinOperatorProcessor.messageMethodBody"))) //$NON-NLS-1$
            .toThrowStatement());
        return builder.toImplementation();
    }
}
