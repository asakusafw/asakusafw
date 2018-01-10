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

import java.util.List;

import javax.lang.model.element.Modifier;

import com.asakusafw.operator.Constants;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.builtin.DslBuilder.ElementRef;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.JavaName;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.Reference;

/**
 * {@link OperatorDriver} for {@code MasterBranch} annotation.
 */
public class MasterBranchOperatorDriver implements OperatorDriver {

    @Override
    public ClassDescription getAnnotationTypeName() {
        return Constants.getBuiltinOperatorClass("MasterBranch"); //$NON-NLS-1$
    }

    @Override
    public OperatorDescription analyze(Context context) {
        DslBuilder dsl = new DslBuilder(context);
        if (dsl.method().modifiers().contains(Modifier.ABSTRACT)) {
            dsl.method().error(Messages.getString("MasterBranchOperatorDriver.errorAbstract")); //$NON-NLS-1$
        }
        boolean enumResult = dsl.result().type().isEnum();
        if (enumResult == false) {
            dsl.method().error(Messages.getString("MasterBranchOperatorDriver.errorReturnNotEnumType")); //$NON-NLS-1$
        }
        MasterKindOperatorHelper.consumeMaster(dsl);
        MasterKindOperatorHelper.consumeTx(dsl);
        for (ElementRef p : dsl.parametersFrom(2)) {
            if (p.type().isExtra()) {
                dsl.consumeExtraParameter(p);
            } else {
                p.error(Messages.getString("MasterBranchOperatorDriver.errorExtraParameterInvalidType")); //$NON-NLS-1$
            }
        }
        if (dsl.getInputs().isEmpty() == false && enumResult) {
            List<ElementRef> constants = dsl.result().type().enumConstants();
            if (constants.isEmpty()) {
                dsl.result().error(Messages.getString("MasterBranchOperatorDriver.errorReturnEmptyEnumType")); //$NON-NLS-1$
            } else {
                Node txInput = dsl.getInputs().get(dsl.getInputs().size() - 1);
                for (ElementRef constant : constants) {
                    JavaName name = JavaName.of(constant.name());
                    dsl.addOutput(
                            constant.document(),
                            name.toMemberName(),
                            txInput.getType(),
                            Reference.special(constant.name()));
                }
            }
        }
        dsl.setSupport(MasterKindOperatorHelper.extractMasterSelection(dsl));
        dsl.requireShuffle();
        return dsl.toDescription();
    }
}
