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

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.TypeConstraint;
import com.asakusafw.compiler.operator.ImplementationBuilder;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.Summarize;

/**
 * Processes {@link Summarize} operators.
 */
@TargetOperator(Summarize.class)
public class SummarizeOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isGeneric()) {
            a.error(Messages.getString("SummarizeOperatorProcessor.errorGeneric")); //$NON-NLS-1$
        }
        if (a.isAbstract() == false) {
            a.error(Messages.getString("SummarizeOperatorProcessor.errorNotAbstract")); //$NON-NLS-1$
        }
        TypeConstraint summarized = a.getReturnType();
        if (summarized.isConcreteModel() == false) {
            a.error(Messages.getString("SummarizeOperatorProcessor.errorNotModelResult")); //$NON-NLS-1$
        }
        TypeConstraint summarizee = a.getParameterType(0);
        if (summarizee.isModel() == false) {
            a.error(0, Messages.getString("SummarizeOperatorProcessor.errorNotModelInput")); //$NON-NLS-1$
        }
        for (int i = 1, n = a.countParameters(); i < n; i++) {
            a.error(i, Messages.getString("SummarizeOperatorProcessor.errorExtraParameter")); //$NON-NLS-1$
        }
        if (a.hasError()) {
            return null;
        }
        if (summarized.isSummarizedModel(summarizee.getType()) == false) {
            a.error(Messages.getString("SummarizeOperatorProcessor.errorNotSummarizedModel")); //$NON-NLS-1$
            return null;
        }

        ShuffleKey key = summarized.getSummarizeKey();
        Summarize annotation = context.element.getAnnotation(Summarize.class);
        if (annotation == null) {
            a.error(Messages.getString("SummarizeOperatorProcessor.errorInvalidAnnotation")); //$NON-NLS-1$
            return null;
        }
        OperatorProcessorUtil.checkPortName(a, new String[] {
                annotation.summarizedPort(),
        });
        if (a.hasError()) {
            return null;
        }

        Builder builder = new Builder(getTargetAnnotationType(), context);
        builder.addAttribute(FlowBoundary.SHUFFLE);
        builder.addAttribute(a.getObservationCount());
        builder.addAttribute(annotation.partialAggregation());
        builder.setDocumentation(a.getExecutableDocument());
        builder.addInput(
                a.getParameterDocument(0),
                a.getParameterName(0),
                a.getParameterType(0).getType(),
                0,
                key);
        builder.addOutput(
                a.getReturnDocument(),
                annotation.summarizedPort(),
                a.getReturnType().getType(),
                null,
                null);
        return builder.toDescriptor();
    }

    @Override
    protected List<? extends TypeBodyDeclaration> override(Context context) {
        ImplementationBuilder builder = new ImplementationBuilder(context);
        ModelFactory f = context.environment.getFactory();
        builder.addStatement(new TypeBuilder(f, context.importer.toType(UnsupportedOperationException.class))
            .newObject(Models.toLiteral(f,
                    Messages.getString("SummarizeOperatorProcessor.messageMethodBody"))) //$NON-NLS-1$
            .toThrowStatement());
        return builder.toImplementation();
    }
}
