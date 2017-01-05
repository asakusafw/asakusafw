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

import java.text.MessageFormat;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.ShuffleKeySpec;
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
import com.asakusafw.vocabulary.operator.MasterCheck;

/**
 * Processes {@link MasterCheck} operators.
 */
@TargetOperator(MasterCheck.class)
public class MasterCheckOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isAbstract() == false) {
            a.error(Messages.getString("MasterCheckOperatorProcessor.errorNotAbstract")); //$NON-NLS-1$
        }
        if (a.getReturnType().isBoolean() == false) {
            a.error(Messages.getString("MasterCheckOperatorProcessor.errorNotBooleanResult")); //$NON-NLS-1$
        }
        TypeConstraint master = a.getParameterType(0);
        if (master.isModel() == false) {
            a.error(0, Messages.getString("MasterCheckOperatorProcessor.errorNotModelMaster")); //$NON-NLS-1$
        }
        TypeConstraint transaction = a.getParameterType(1);
        if (transaction.isModel() == false) {
            a.error(1, Messages.getString("MasterCheckOperatorProcessor.errorNotModelTransaction")); //$NON-NLS-1$
        }
        for (int i = 2, n = a.countParameters(); i < n; i++) {
            a.error(i, Messages.getString("MasterCheckOperatorProcessor.errorExtraParameter")); //$NON-NLS-1$
        }
        if (a.hasError()) {
            return null;
        }

        ShuffleKeySpec masterKey = a.getParameterKeySpec(0);
        if (masterKey == null) {
            a.error(Messages.getString(
                    "MasterCheckOperatorProcessor.errorMissingKeyAnnotationMaster")); //$NON-NLS-1$
        }
        ShuffleKeySpec transactionKey = a.getParameterKeySpec(1);
        if (transactionKey == null) {
            a.error(Messages.getString(
                    "MasterCheckOperatorProcessor.errorMissingKeyAnnotationTransaction")); //$NON-NLS-1$
        }
        a.validateShuffleKeys(masterKey, transactionKey);
        ExecutableElement selector = null;
        try {
            selector = MasterKindOperatorAnalyzer.findSelector(context);
        } catch (ResolveException e) {
            a.error(e.getMessage());
        }
        MasterCheck annotation = context.element.getAnnotation(MasterCheck.class);
        if (annotation == null) {
            a.error(Messages.getString("MasterCheckOperatorProcessor.errorInvalidAnnoation")); //$NON-NLS-1$
            return null;
        }
        OperatorProcessorUtil.checkPortName(a, new String[] {
                annotation.foundPort(),
                annotation.missedPort(),
        });
        if (a.hasError()) {
            return null;
        }
        assert masterKey != null;
        assert transactionKey != null;

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
                masterKey.getKey());
        builder.addInput(
                a.getParameterDocument(1),
                a.getParameterName(1),
                a.getParameterType(1).getType(),
                1,
                transactionKey.getKey());
        builder.addOutput(
                MessageFormat.format(
                        Messages.getString("MasterCheckOperatorProcessor.javadocFound"), //$NON-NLS-1$
                        a.getParameterName(0), a.getParameterName(1)),
                annotation.foundPort(),
                a.getParameterType(1).getType(),
                a.getParameterName(1),
                null);
        builder.addOutput(
                MessageFormat.format(
                        Messages.getString("MasterCheckOperatorProcessor.javadocMissed"), //$NON-NLS-1$
                        a.getParameterName(0), a.getParameterName(1)),
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
                    Messages.getString("MasterCheckOperatorProcessor.messageMethodBody"))) //$NON-NLS-1$
            .toThrowStatement());
        return builder.toImplementation();
    }
}
