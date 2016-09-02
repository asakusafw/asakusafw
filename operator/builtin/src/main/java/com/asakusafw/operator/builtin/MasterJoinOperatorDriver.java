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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.asakusafw.operator.Constants;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.builtin.DslBuilder.AnnotationRef;
import com.asakusafw.operator.builtin.DslBuilder.ElementRef;
import com.asakusafw.operator.builtin.DslBuilder.KeyRef;
import com.asakusafw.operator.builtin.DslBuilder.TypeRef;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Document;
import com.asakusafw.operator.model.OperatorDescription.Node;

/**
 * {@link OperatorDriver} for {@code MasterJoin} annotation.
 */
public class MasterJoinOperatorDriver implements OperatorDriver {

    private static final String JOINED_PORT = "joinedPort"; //$NON-NLS-1$

    private static final String MISSED_PORT = "missedPort"; //$NON-NLS-1$

    @Override
    public ClassDescription getAnnotationTypeName() {
        return Constants.getBuiltinOperatorClass("MasterJoin"); //$NON-NLS-1$
    }

    @Override
    public OperatorDescription analyze(Context context) {
        DslBuilder dsl = new DslBuilder(context);
        if (dsl.method().modifiers().contains(Modifier.ABSTRACT) == false) {
            dsl.method().error(Messages.getString("MasterJoinOperatorDriver.errorNotAbstract")); //$NON-NLS-1$
        }
        if (dsl.result().type().isDataModel() == false) {
            dsl.method().error(Messages.getString("MasterJoinOperatorDriver.errorReturnNotDataModelType")); //$NON-NLS-1$
        }
        if (dsl.isGeneric()) {
            dsl.method().error(Messages.getString("MasterJoinOperatorDriver.errorGeneric")); //$NON-NLS-1$
        }
        if (dsl.sawError()) {
            return null;
        }

        AnnotationRef joined = dsl.result().type().annotation(Constants.TYPE_JOINED);
        if (joined == null) {
            dsl.result().error(Messages.getString("MasterJoinOperatorDriver.errorReturnJoinedModelType")); //$NON-NLS-1$
            return null;
        }
        List<AnnotationRef> terms = joined.annotations("terms"); //$NON-NLS-1$
        if (terms == null || terms.isEmpty()) {
            dsl.result().error(Messages.getString("MasterJoinOperatorDriver.errorJoinedModelMissingTerms")); //$NON-NLS-1$
            return null;
        }

        for (ElementRef p : dsl.parameters(0)) {
            TypeRef type = p.type();
            KeyRef key = null;
            if (type.isDataModel()) {
                boolean saw = false;
                for (Iterator<AnnotationRef> iter = terms.iterator(); iter.hasNext();) {
                    AnnotationRef term = iter.next();
                    TypeRef termType = term.type("source"); //$NON-NLS-1$
                    if (type.isEqualTo(termType)) {
                        iter.remove();
                        AnnotationRef shuffle = term.annotation("shuffle"); //$NON-NLS-1$
                        if (shuffle != null) {
                            key = p.resolveKey(type, shuffle.get());
                        }
                        saw = true;
                        break;
                    }
                }
                if (saw) {
                    dsl.addInput(p.document(), p.name(), type.mirror(), key, p.reference());
                } else {
                    p.error(MessageFormat.format(
                            Messages.getString("MasterJoinOperatorDriver.errorInputNotJoinSourceType"), //$NON-NLS-1$
                            dsl.result().type().dataModel().getSimpleName()));
                }
            } else if (type.isBasic()) {
                p.error(Messages.getString("MasterJoinOperatorDriver.errorParameterBasic")); //$NON-NLS-1$
            } else {
                p.error(Messages.getString("MasterJoinOperatorDriver.errorParameterUnsupportedType")); //$NON-NLS-1$
            }
        }
        if (dsl.getInputs().size() != 2) {
            dsl.method().error(Messages.getString("MasterJoinOperatorDriver.errorInputInvalidCount")); //$NON-NLS-1$
        }
        if (terms.isEmpty()) {
            List<Node> inputs = dsl.getInputs();
            assert inputs.isEmpty() == false;
            Node txInput = inputs.get(inputs.size() - 1);
            dsl.addOutput(
                    dsl.result().document(),
                    dsl.annotation().string(JOINED_PORT),
                    dsl.result().type().mirror(),
                    dsl.result().reference());
            dsl.addOutput(
                    Document.text(Messages.getString("MasterJoinOperatorDriver.javadocMissOutput")), //$NON-NLS-1$
                    dsl.annotation().string(MISSED_PORT),
                    txInput.getType(),
                    txInput.getReference());
        } else {
            if (dsl.sawError() == false) {
                List<TypeRef> types = new ArrayList<>();
                for (AnnotationRef term : terms) {
                    TypeRef type = term.type("source"); //$NON-NLS-1$
                    types.add(type);
                }
                dsl.result().error(MessageFormat.format(
                        Messages.getString("MasterJoinOperatorDriver.errorInputIncomplete"), //$NON-NLS-1$
                        types));
            }
        }
        dsl.setSupport(MasterKindOperatorHelper.extractMasterSelection(dsl));
        dsl.requireShuffle();
        return dsl.toDescription();
    }
}
