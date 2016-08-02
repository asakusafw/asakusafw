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

import com.asakusafw.operator.AbstractOperatorDriver;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.builtin.DslBuilder.ElementRef;
import com.asakusafw.operator.builtin.DslBuilder.KeyRef;
import com.asakusafw.operator.builtin.DslBuilder.TypeRef;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Document;

/**
 * {@link OperatorDriver} for {@code Fold} annotation.
 */
public class FoldOperatorDriver extends AbstractOperatorDriver {

    private static final String OUTPUT_PORT = "outputPort"; //$NON-NLS-1$

    private static final String PARTIAL_AGGREGATION = "partialAggregation"; //$NON-NLS-1$

    @Override
    public ClassDescription getAnnotationTypeName() {
        return Constants.getBuiltinOperatorClass("Fold"); //$NON-NLS-1$
    }

    @Override
    public OperatorDescription analyze(Context context) {
        DslBuilder dsl = new DslBuilder(context);
        if (dsl.method().modifiers().contains(Modifier.ABSTRACT)) {
            dsl.method().error("This operator method must not be \"abstract\"");
        }
        if (dsl.result().type().isVoid() == false) {
            dsl.method().error("This operator method must return \"void\"");
        }
        ElementRef p0 = dsl.parameter(0);
        ElementRef p1 = dsl.parameter(1);
        if (p0.type().isDataModel() == false) {
            p0.error("The first parameter of this operator must be a data model type");
        } else if (p1.type().isDataModel() == false) {
            p1.error("The second parameter of this operator must be a data model type");
        } else if (p0.type().isEqualTo(p1.type()) == false) {
            p1.error("The second parameter type must be same to the first parameter type");
        } else {
            KeyRef key = p0.resolveKey(p0.type());
            dsl.addInput(p1.document(), p1.name(), p1.type().mirror(), key, p0.reference());
            dsl.addOutput(
                    Document.text("folding result"),
                    dsl.annotation().string(OUTPUT_PORT),
                    p0.type().mirror(),
                    p0.reference());
        }
        for (ElementRef p : dsl.parameters(2)) {
            TypeRef type = p.type();
            if (type.isBasic()) {
                dsl.consumeGenericParameter(p);
            } else {
                p.error("Rest of this operator's parameters must be basic type");
            }
        }
        dsl.requireShuffle();
        dsl.addAttribute(dsl.annotation().constant(PARTIAL_AGGREGATION));
        return dsl.toDescription();
    }
}
