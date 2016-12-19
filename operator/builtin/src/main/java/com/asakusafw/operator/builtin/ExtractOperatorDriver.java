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
import com.asakusafw.operator.builtin.DslBuilder.TypeRef;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.OperatorDescription;

/**
 * {@link OperatorDriver} for {@code Extract} annotation.
 */
public class ExtractOperatorDriver implements OperatorDriver {

    @Override
    public ClassDescription getAnnotationTypeName() {
        return Constants.getBuiltinOperatorClass("Extract"); //$NON-NLS-1$
    }

    @Override
    public OperatorDescription analyze(Context context) {
        DslBuilder dsl = new DslBuilder(context);
        if (dsl.method().modifiers().contains(Modifier.ABSTRACT)) {
            dsl.method().error(Messages.getString("ExtractOperatorDriver.errorAbstract")); //$NON-NLS-1$
        }
        if (dsl.result().type().isVoid() == false) {
            dsl.method().error(Messages.getString("ExtractOperatorDriver.errorReturnNotVoid")); //$NON-NLS-1$
        }
        for (ElementRef p : dsl.parameters()) {
            TypeRef type = p.type();
            if (type.isDataModel()) {
                if (dsl.getInputs().isEmpty()) {
                    dsl.addInput(p.document(), p.name(), p.type().mirror(), p.reference());
                } else {
                    p.error(Messages.getString("ExtractOperatorDriver.errorInputTooMany")); //$NON-NLS-1$
                }
            } else if (type.isResult()) {
                TypeRef arg = type.arg(0);
                if (arg.isDataModel()) {
                    dsl.addOutput(p.document(), p.name(), arg.mirror(), p.reference());
                } else {
                    p.error(Messages.getString("ExtractOperatorDriver.errorOutputNotDataModelResultType")); //$NON-NLS-1$
                }
            } else if (type.isExtra()) {
                dsl.consumeExtraParameter(p);
            } else {
                p.error(Messages.getString("ExtractOperatorDriver.errorParameterUnsupportedType")); //$NON-NLS-1$
            }
        }
        return dsl.toDescription();
    }
}
