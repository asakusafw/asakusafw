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
package com.asakusafw.compiler.operator;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;

/**
 * An skeletal implementation of {@link OperatorProcessor}.
 * Sub-classes should use {@link TargetOperator} annotation to declare the target operator kind.
 * @since 0.1.0
 * @version 0.7.0
 */
public abstract class AbstractOperatorProcessor implements OperatorProcessor {

    private OperatorCompilingEnvironment environment;

    private Class<? extends Annotation> targetOperatorAnnotation;

    @Override
    public void initialize(OperatorCompilingEnvironment env) {
        Precondition.checkMustNotBeNull(env, "env"); //$NON-NLS-1$
        this.environment = env;
        TargetOperator target = getClass().getAnnotation(TargetOperator.class);
        if (target != null) {
            this.targetOperatorAnnotation = target.value();
        } else {
            env.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                MessageFormat.format(
                    Messages.getString("AbstractOperatorProcessor.errorMissingAnnotation"), //$NON-NLS-1$
                    getClass().getName(),
                    TargetOperator.class.getName()));
        }
    }

    @Override
    public Class<? extends Annotation> getTargetAnnotationType() {
        return targetOperatorAnnotation;
    }

    @Override
    public synchronized AnnotationMirror getOperatorAnnotation(ExecutableElement element) {
        TypeMirror targetType = environment
                .getElementUtils()
                .getTypeElement(targetOperatorAnnotation.getName())
                .asType();
        Types types = environment.getTypeUtils();
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            if (types.isSameType(annotation.getAnnotationType(), targetType)) {
                return annotation;
            }
        }
        return null;
    }

    @Override
    public List<? extends TypeBodyDeclaration> implement(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        if (context.element.getModifiers().contains(Modifier.ABSTRACT) == false) {
            return Collections.emptyList();
        }
        return override(context);
    }

    /**
     * Returns an implementation method of the target operator method.
     * @param context the current context
     * @return the implementation method body, or {@code null} if the target operator is not valid
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    protected List<? extends TypeBodyDeclaration> override(Context context) {
        return null;
    }
}
