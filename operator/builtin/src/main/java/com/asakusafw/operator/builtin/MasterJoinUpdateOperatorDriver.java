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
 * {@link OperatorDriver} for {@code MasterJoinUpdate} annotation.
 */
public class MasterJoinUpdateOperatorDriver implements OperatorDriver {

    private static final String JOINED_PORT = "updatedPort"; //$NON-NLS-1$

    private static final String MISSED_PORT = "missedPort"; //$NON-NLS-1$

    @Override
    public ClassDescription getAnnotationTypeName() {
        return Constants.getBuiltinOperatorClass("MasterJoinUpdate"); //$NON-NLS-1$
    }

    @Override
    public OperatorDescription analyze(Context context) {
        DslBuilder dsl = new DslBuilder(context);
        if (dsl.method().modifiers().contains(Modifier.ABSTRACT)) {
            dsl.method().error(Messages.getString("MasterJoinUpdateOperatorDriver.errorAbstract")); //$NON-NLS-1$
        }
        if (dsl.result().type().isVoid() == false) {
            dsl.method().error(Messages.getString("MasterJoinUpdateOperatorDriver.errorReturnNotVoid")); //$NON-NLS-1$
        }
        MasterKindOperatorHelper.consumeMaster(dsl);
        MasterKindOperatorHelper.consumeTx(dsl);
        for (ElementRef p : dsl.parametersFrom(2)) {
            if (p.type().isExtra()) {
                dsl.consumeExtraParameter(p);
            } else {
                p.error(Messages.getString("MasterJoinUpdateOperatorDriver.errorExtraParameterInvalidType")); //$NON-NLS-1$
            }
        }
        if (dsl.getInputs().isEmpty() == false) {
            Node txInput = dsl.getInputs().get(dsl.getInputs().size() - 1);
            dsl.addOutput(
                    Document.text(Messages.getString("MasterJoinUpdateOperatorDriver.javadocJoinOutput")), //$NON-NLS-1$
                    dsl.annotation().string(JOINED_PORT),
                    txInput.getType(),
                    txInput.getReference());
            dsl.addOutput(
                    Document.text(Messages.getString("MasterJoinUpdateOperatorDriver.javadocMissOutput")), //$NON-NLS-1$
                    dsl.annotation().string(MISSED_PORT),
                    txInput.getType(),
                    Reference.special(String.valueOf(false)));
        }
        dsl.setSupport(MasterKindOperatorHelper.extractMasterSelection(dsl));
        dsl.requireShuffle();
        return dsl.toDescription();
    }
}
