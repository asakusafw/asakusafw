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

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.asakusafw.operator.AbstractOperatorDriver;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.builtin.DslBuilder.AnnotationRef;
import com.asakusafw.operator.builtin.DslBuilder.ElementRef;
import com.asakusafw.operator.builtin.DslBuilder.TypeRef;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.OperatorDescription;

/**
 * {@link OperatorDriver} for {@code Split} annotation.
 */
public class SplitOperatorDriver extends AbstractOperatorDriver {

    @Override
    public ClassDescription getAnnotationTypeName() {
        return Constants.getBuiltinOperatorClass("Split"); //$NON-NLS-1$
    }

    @Override
    public OperatorDescription analyze(Context context) {
        DslBuilder dsl = new DslBuilder(context);
        if (dsl.method().modifiers().contains(Modifier.ABSTRACT) == false) {
            dsl.method().error("This operator method must be \"abstract\"");
        }
        if (dsl.result().type().isVoid() == false) {
            dsl.method().error("This operator method must return \"void\"");
        }

        ElementRef p0 = dsl.parameter(0);
        if (p0.type().isDataModel() == false) {
            p0.error("The first parameter must be a data model type");
        }
        if (dsl.isGeneric()) {
            dsl.method().error("This operator must not have any type parameters");
        }
        if (dsl.sawError()) {
            return null;
        }
        AnnotationRef joined = p0.type().annotation(Constants.TYPE_JOINED);
        if (joined == null) {
            p0.error("The first parameter type must be a \"Joined data model\"");
            return null;
        }
        List<AnnotationRef> terms = joined.annotations("terms"); //$NON-NLS-1$
        if (terms == null || terms.isEmpty()) {
            p0.error("The first parameter type is invalid joind data model (\"terms\" is not declared?)");
            return null;
        }
        LinkedList<TypeRef> sourceTypes = new LinkedList<>();
        for (AnnotationRef term : terms) {
            sourceTypes.add(term.type("source")); //$NON-NLS-1$
        }

        dsl.addInput(p0.document(), p0.name(), p0.type().mirror(), p0.reference());

        for (ElementRef p : dsl.parameters(1)) {
            TypeRef type = p.type();
            if (type.isResult()) {
                TypeRef arg = type.arg(0);
                if (arg.isDataModel() == false) {
                    p.error("Output Result parameter must have a data model type in its type argument");
                } else {
                    boolean saw = false;
                    for (Iterator<TypeRef> iter = sourceTypes.iterator(); iter.hasNext();) {
                        TypeRef next = iter.next();
                        if (arg.isEqualTo(next)) {
                            iter.remove();
                            saw = true;
                            break;
                        }
                    }
                    if (saw) {
                        dsl.addOutput(p.document(), p.name(), arg.mirror(), p.reference());
                    } else {
                        p.error("This output type is not a valid join source");
                    }
                }
            } else if (type.isDataModel()) {
                p.error("Output parameter must have Result type");
            } else if (type.isBasic()) {
                p.error("This operator cannot have any basic arguments");
            } else {
                p.error("Rest of parameters must be Result type");
            }
        }
        if (dsl.sawError() == false && sourceTypes.isEmpty() == false) {
            p0.error(MessageFormat.format(
                    "Some join source types do not appeared in parameter: {0}",
                    sourceTypes));
        }
        return dsl.toDescription();
    }
}
