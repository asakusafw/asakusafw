/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import com.asakusafw.operator.Constants;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.builtin.DslBuilder.ElementRef;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Document;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.Reference;

/**
 * {@link OperatorDriver} for {@code MasterCheck} annotation.
 */
public class MasterCheckOperatorDriver implements OperatorDriver {

    private static final String JOINED_PORT = "foundPort"; //$NON-NLS-1$

    private static final String MISSED_PORT = "missedPort"; //$NON-NLS-1$

    @Override
    public ClassDescription getAnnotationTypeName() {
        return Constants.getBuiltinOperatorClass("MasterCheck"); //$NON-NLS-1$
    }

    @Override
    public OperatorDescription analyze(Context context) {
        DslBuilder dsl = new DslBuilder(context);
        if (dsl.method().modifiers().contains(Modifier.ABSTRACT) == false) {
            dsl.method().error(Messages.getString("MasterCheckOperatorDriver.errorNotAbstract")); //$NON-NLS-1$
        }
        if (dsl.result().type().isBoolean() == false) {
            dsl.method().error(Messages.getString("MasterCheckOperatorDriver.errorReturnNotBooleanType")); //$NON-NLS-1$
        }
        MasterKindOperatorHelper.consumeMaster(dsl);
        MasterKindOperatorHelper.consumeTx(dsl);
        ExecutableElement selector = MasterKindOperatorHelper.extractMasterSelection(dsl);
        for (ElementRef p : dsl.parametersFrom(2)) {
            if (p.type().isExtra()) {
                dsl.consumeExtraParameter(p);
                if (selector == null) {
                    p.warn(Messages.getString("MasterCheckOperatorDriver.warnExtraParameterWithoutSelection")); //$NON-NLS-1$
                }
            } else {
                p.error(Messages.getString("MasterCheckOperatorDriver.errorExtraParameterInvalidType")); //$NON-NLS-1$
            }
        }
        if (dsl.getInputs().isEmpty() == false) {
            Node txInput = dsl.getInputs().get(dsl.getInputs().size() - 1);
            dsl.addOutput(
                    Document.text(Messages.getString("MasterCheckOperatorDriver.javadocJoinOutput")), //$NON-NLS-1$
                    dsl.annotation().string(JOINED_PORT),
                    txInput.getType(),
                    txInput.getReference());
            dsl.addOutput(
                    Document.text(Messages.getString("MasterCheckOperatorDriver.javadocMissOutput")), //$NON-NLS-1$
                    dsl.annotation().string(MISSED_PORT),
                    txInput.getType(),
                    Reference.special(String.valueOf(false)));
        }
        dsl.setSupport(selector);
        dsl.requireShuffle();
        return dsl.toDescription();
    }
}
