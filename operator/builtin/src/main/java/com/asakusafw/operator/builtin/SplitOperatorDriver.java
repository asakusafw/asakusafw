/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
public class SplitOperatorDriver implements OperatorDriver {

    @Override
    public ClassDescription getAnnotationTypeName() {
        return Constants.getBuiltinOperatorClass("Split"); //$NON-NLS-1$
    }

    @Override
    public OperatorDescription analyze(Context context) {
        DslBuilder dsl = new DslBuilder(context);
        if (dsl.method().modifiers().contains(Modifier.ABSTRACT) == false) {
            dsl.method().error(Messages.getString("SplitOperatorDriver.errorNotAbstract")); //$NON-NLS-1$
        }
        if (dsl.result().type().isVoid() == false) {
            dsl.method().error(Messages.getString("SplitOperatorDriver.errorReturnNotVoid")); //$NON-NLS-1$
        }

        ElementRef p0 = dsl.parameter(0);
        if (p0.type().isDataModel() == false) {
            p0.error(Messages.getString("SplitOperatorDriver.errorFirstParameterNotDataModelType")); //$NON-NLS-1$
        }
        if (dsl.isGeneric()) {
            dsl.method().error(Messages.getString("SplitOperatorDriver.errorGeneric")); //$NON-NLS-1$
        }
        if (dsl.sawError()) {
            return null;
        }
        AnnotationRef joined = p0.type().annotation(Constants.TYPE_JOINED);
        if (joined == null) {
            p0.error(Messages.getString("SplitOperatorDriver.errorFirstParameterNotJoinedModelType")); //$NON-NLS-1$
            return null;
        }
        List<AnnotationRef> terms = joined.annotations("terms"); //$NON-NLS-1$
        if (terms == null || terms.isEmpty()) {
            p0.error(Messages.getString("SplitOperatorDriver.errorJoinedModelMissingTerms")); //$NON-NLS-1$
            return null;
        }
        LinkedList<TypeRef> sourceTypes = new LinkedList<>();
        for (AnnotationRef term : terms) {
            sourceTypes.add(term.type("source")); //$NON-NLS-1$
        }

        dsl.addInput(p0.document(), p0.name(), p0.type().mirror(), p0.reference());

        for (ElementRef p : dsl.parametersFrom(1)) {
            TypeRef type = p.type();
            if (type.isResult()) {
                TypeRef arg = type.arg(0);
                if (arg.isDataModel() == false) {
                    p.error(Messages.getString("SplitOperatorDriver.errorOutputNotDataModelResultType")); //$NON-NLS-1$
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
                        p.error(MessageFormat.format(
                                Messages.getString("SplitOperatorDriver.errorOutputNotJoinSourceType"), //$NON-NLS-1$
                                p0.type().dataModel().getSimpleName()));
                    }
                }
            } else if (type.isDataModel()) {
                p.error(Messages.getString("SplitOperatorDriver.errorOutputDataModelType")); //$NON-NLS-1$
            } else if (type.isBasic()) { // unsupported
                p.error(Messages.getString("SplitOperatorDriver.errorOutputBasicType")); //$NON-NLS-1$
            } else {
                p.error(Messages.getString("SplitOperatorDriver.errorParameterUnsupportedType")); //$NON-NLS-1$
            }
        }
        if (dsl.sawError() == false && sourceTypes.isEmpty() == false) {
            p0.error(MessageFormat.format(
                    Messages.getString("SplitOperatorDriver.errorOutputIncomplete"), //$NON-NLS-1$
                    sourceTypes));
        }
        return dsl.toDescription();
    }
}
