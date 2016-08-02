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

import java.util.List;

import javax.lang.model.element.Modifier;

import com.asakusafw.operator.AbstractOperatorDriver;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.builtin.DslBuilder.ElementRef;
import com.asakusafw.operator.builtin.DslBuilder.TypeRef;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.JavaName;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.Reference;

/**
 * {@link OperatorDriver} for {@code Branch} annotation.
 */
public class BranchOperatorDriver extends AbstractOperatorDriver {

    @Override
    public ClassDescription getAnnotationTypeName() {
        return Constants.getBuiltinOperatorClass("Branch"); //$NON-NLS-1$
    }

    @Override
    public OperatorDescription analyze(Context context) {
        DslBuilder dsl = new DslBuilder(context);
        if (dsl.method().modifiers().contains(Modifier.ABSTRACT)) {
            dsl.method().error("This operator method must not be \"abstract\"");
        }
        boolean enumResult = dsl.result().type().isEnum();
        if (enumResult == false) {
            dsl.method().error("This operator method must return enum type");
        }
        for (ElementRef p : dsl.parameters()) {
            TypeRef type = p.type();
            if (type.isDataModel()) {
                if (dsl.getInputs().isEmpty()) {
                    dsl.addInput(p.document(), p.name(), p.type().mirror(), p.reference());
                } else {
                    p.error("This operator must not have multiple data model type parameters");
                }
            } else if (type.isBasic()) {
                dsl.consumeGenericParameter(p);
            } else {
                p.error("This operator's parameters must be either data model type or basic type");
            }
        }
        if (dsl.getInputs().isEmpty() == false && enumResult) {
            List<ElementRef> constants = dsl.result().type().enumConstants();
            if (constants.isEmpty()) {
                dsl.result().error("This operator method must return enum with one or more constants");
            } else {
                Node input = dsl.getInputs().get(0);
                for (ElementRef constant : constants) {
                    JavaName name = JavaName.of(constant.name());
                    dsl.addOutput(
                            constant.document(),
                            name.toMemberName(),
                            input.getType(),
                            Reference.special(constant.name()));
                }
            }
        }
        return dsl.toDescription();
    }
}
