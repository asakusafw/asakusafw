/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;

import com.asakusafw.compiler.common.TargetOperator;

/**
 * A skeletal implementation of {@link FlowElementProcessor}.
 */
public abstract class AbstractFlowElementProcessor
        extends FlowCompilingEnvironment.Initialized
        implements FlowElementProcessor {

    private Class<? extends Annotation> targetOperatorAnnotation;

    /**
     * Returns the target annotation type from {@link TargetOperator} annotation.
     * @return the target annotation type, or {@code null} if the sub-class is not annotated with the annotation
     */
    protected Class<? extends Annotation> loadTargetAnnotationType() {
        TargetOperator target = getClass().getAnnotation(TargetOperator.class);
        if (target != null) {
            return target.value();
        } else {
            return null;
        }
    }

    @Override
    public Class<? extends Annotation> getTargetAnnotationType() {
        if (targetOperatorAnnotation == null) {
            this.targetOperatorAnnotation = loadTargetAnnotationType();
            if (targetOperatorAnnotation == null) {
                getEnvironment().error(
                        Messages.getString("AbstractFlowElementProcessor.errorMissingTargetAnnotation"), //$NON-NLS-1$
                        getClass());
                throw new IllegalStateException();
            }
        }
        return targetOperatorAnnotation;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}", //$NON-NLS-1$
                getClass().getName());
    }
}
