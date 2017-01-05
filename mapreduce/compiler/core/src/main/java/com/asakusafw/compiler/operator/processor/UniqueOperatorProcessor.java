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
import com.asakusafw.compiler.operator.ExecutableAnalyzer.ShuffleKeySpec;
import com.asakusafw.compiler.operator.ImplementationBuilder;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.operator.Unique;

/**
 * Processes {@link Unique} operators.
 * @deprecated operator is not supported
 */
@Deprecated
@TargetOperator(Unique.class)
public class UniqueOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(context.environment, context.element);
        if (a.isAbstract() == false) {
            a.error(Messages.getString("UniqueOperatorProcessor.errorNotAbstract")); //$NON-NLS-1$
        }
        if (a.getReturnType().isVoid() == false) {
            a.error(Messages.getString("UniqueOperatorProcessor.errorNotVoid")); //$NON-NLS-1$
        }
        if (a.getParameterType(0).isModel() == false) {
            a.error(0, Messages.getString("UniqueOperatorProcessor.errorNotModel")); //$NON-NLS-1$
        }
        for (int i = 1, n = a.countParameters(); i < n; i++) {
            a.error(i, Messages.getString("UniqueOperatorProcessor.errorExtraParameter")); //$NON-NLS-1$
        }
        if (a.hasError()) {
            return null;
        }

        ShuffleKeySpec key = a.getParameterKeySpec(0);
        if (key == null) {
            a.error(Messages.getString("UniqueOperatorProcessor.errorMissingKeyAnnotation")); //$NON-NLS-1$
            return null;
        }
        a.validateShuffleKeys(key);
        if (a.hasError()) {
            return null;
        }

        Builder builder = new Builder(getTargetAnnotationType(), context);
        builder.addAttribute(FlowBoundary.SHUFFLE);
        builder.addAttribute(a.getObservationCount());
        builder.setDocumentation(a.getExecutableDocument());
        builder.addInput(
                a.getParameterDocument(0),
                a.getParameterName(0),
                a.getParameterType(0).getType(),
                0,
                key.getKey());
        builder.addOutput(
                Messages.getString("UniqueOperatorProcessor.javadocUnique"), //$NON-NLS-1$
                "unique", //$NON-NLS-1$
                a.getParameterType(0).getType(),
                a.getParameterName(0),
                null);
        builder.addOutput(
                Messages.getString("UniqueOperatorProcessor.javadocDuplicated"), //$NON-NLS-1$
                "duplicated", //$NON-NLS-1$
                a.getParameterType(0).getType(),
                a.getParameterName(0),
                null);
        return builder.toDescriptor();
    }

    @Override
    protected List<? extends TypeBodyDeclaration> override(Context context) {
        ImplementationBuilder builder = new ImplementationBuilder(context);
        ModelFactory f = context.environment.getFactory();
        builder.addStatement(new TypeBuilder(f, context.importer.toType(UnsupportedOperationException.class))
            .newObject(Models.toLiteral(f,
                    Messages.getString("UniqueOperatorProcessor.messageMethodBody"))) //$NON-NLS-1$
            .toThrowStatement());
        return builder.toImplementation();
    }
}
