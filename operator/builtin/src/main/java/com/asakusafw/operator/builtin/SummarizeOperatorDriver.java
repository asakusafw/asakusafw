/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.operator.builtin;

import javax.lang.model.element.Modifier;

import com.asakusafw.operator.Constants;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.builtin.DslBuilder.AnnotationRef;
import com.asakusafw.operator.builtin.DslBuilder.ElementRef;
import com.asakusafw.operator.builtin.DslBuilder.KeyRef;
import com.asakusafw.operator.builtin.DslBuilder.TypeRef;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.OperatorDescription;

/**
 * {@link OperatorDriver} for {@code Summarize} annotation.
 */
public class SummarizeOperatorDriver implements OperatorDriver {

    private static final String OUTPUT_PORT = "summarizedPort"; //$NON-NLS-1$

    private static final String PARTIAL_AGGREGATION = "partialAggregation"; //$NON-NLS-1$

    @Override
    public ClassDescription getAnnotationTypeName() {
        return Constants.getBuiltinOperatorClass("Summarize"); //$NON-NLS-1$
    }

    @Override
    public OperatorDescription analyze(Context context) {
        DslBuilder dsl = new DslBuilder(context);
        if (dsl.method().modifiers().contains(Modifier.ABSTRACT) == false) {
            dsl.method().error(Messages.getString("SummarizeOperatorDriver.errorNotAbstract")); //$NON-NLS-1$
        }
        if (dsl.result().type().isDataModel() == false) {
            dsl.method().error(Messages.getString("SummarizeOperatorDriver.errorReturnNotDataModelType")); //$NON-NLS-1$
        }
        // TODO: allow generic
        if (dsl.isGeneric()) {
            dsl.method().error(Messages.getString("SummarizeOperatorDriver.errorGeneric")); //$NON-NLS-1$
        }
        ElementRef p0 = dsl.parameter(0);
        if (p0.type().isDataModel() == false) {
            p0.error(Messages.getString("SummarizeOperatorDriver.errorInputNotDataModelType")); //$NON-NLS-1$
        }
        if (dsl.sawError()) {
            return null;
        }

        AnnotationRef summarized = dsl.result().type().annotation(Constants.TYPE_SUMMARIZED);
        if (summarized == null) {
            dsl.result().error(Messages.getString("SummarizeOperatorDriver.errorInputNotSummarizedModelType")); //$NON-NLS-1$
            return null;
        }
        AnnotationRef term = summarized.annotation("term"); //$NON-NLS-1$
        if (term == null) {
            dsl.result().error(Messages.getString("SummarizeOperatorDriver.errorSummarizedModelMissingTerms")); //$NON-NLS-1$
            return null;
        }

        TypeRef source = term.type("source"); //$NON-NLS-1$
        if (source.isEqualTo(p0.type())) {
            AnnotationRef shuffle = term.annotation("shuffle"); //$NON-NLS-1$
            if (shuffle == null) {
                dsl.result().error(
                        Messages.getString("SummarizeOperatorDriver.errorSummarizzedModelMissingTermsShuffle")); //$NON-NLS-1$
                return null;
            }
            KeyRef key = p0.resolveKey(p0.type(), shuffle.get());
            dsl.addInput(p0.document(), p0.name(), p0.type().mirror(), key, p0.reference());
            dsl.addOutput(
                    dsl.result().document(),
                    dsl.annotation().string(OUTPUT_PORT),
                    dsl.result().type().mirror(),
                    dsl.result().reference());
        }

        for (ElementRef p : dsl.parametersFrom(1)) {
            TypeRef type = p.type();
            if (type.isDataModel()) {
                p.error(Messages.getString("SummarizeOperatorDriver.errorInputTooMany")); //$NON-NLS-1$
            } else if (type.isBasic()) { // unsupported
                p.error(Messages.getString("SummarizeOperatorDriver.errorParameterBasicType")); //$NON-NLS-1$
            } else {
                p.error(Messages.getString("SummarizeOperatorDriver.errorParameterUnsupportedType")); //$NON-NLS-1$
            }
        }
        dsl.requireShuffle();
        dsl.addAttribute(dsl.annotation().constant(PARTIAL_AGGREGATION));
        return dsl.toDescription();
    }
}
