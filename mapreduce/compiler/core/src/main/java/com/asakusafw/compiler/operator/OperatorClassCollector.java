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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.vocabulary.operator.OperatorHelper;

/**
 * Collects operator classes.
 * @since 0.1.0
 * @version 0.7.0
 */
public class OperatorClassCollector {

    private final OperatorCompilingEnvironment environment;

    private final RoundEnvironment round;

    private final List<TargetMethod> targetMethods;

    private boolean sawError;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @param round the round environment
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public OperatorClassCollector(OperatorCompilingEnvironment environment, RoundEnvironment round) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(round, "round"); //$NON-NLS-1$
        this.environment = environment;
        this.round = round;
        this.targetMethods = new ArrayList<>();
    }

    /**
     * Adds an operator processor for detecting operator methods.
     * @param processor the operator processor
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void add(OperatorProcessor processor) {
        Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
        Class<? extends Annotation> target = processor.getTargetAnnotationType();
        assert target != null;
        TypeElement annotation = environment.getElementUtils().getTypeElement(target.getCanonicalName());
        assert annotation != null;
        TypeMirror annotationType = annotation.asType();

        Set<? extends Element> elements = round.getElementsAnnotatedWith(annotation);
        for (Element element : elements) {
            ExecutableElement method = toOperatorMethodElement(element);
            if (method == null) {
                continue;
            }
            AnnotationMirror annotationMirror = findAnnotation(annotationType, method);
            if (annotationMirror == null) {
                raiseInvalid(element,
                        Messages.getString("OperatorClassCollector.errorMissingAnnotation")); //$NON-NLS-1$
                continue;
            }
            registerMethod(annotationMirror, processor, method);
        }
    }

    private AnnotationMirror findAnnotation(TypeMirror annotationType, Element element) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            if (environment.getTypeUtils().isSameType(annotationType, annotation.getAnnotationType())) {
                return annotation;
            }
        }
        return null;
    }

    private void registerMethod(
            AnnotationMirror annotation,
            OperatorProcessor processor,
            ExecutableElement method) {
        assert annotation != null;
        assert processor != null;
        assert method != null;
        targetMethods.add(new TargetMethod(annotation, method, processor));
    }

    private ExecutableElement toOperatorMethodElement(Element element) {
        assert element != null;
        if (element.getKind() != ElementKind.METHOD) {
            raiseInvalid(element, Messages.getString("OperatorClassCollector.errorNotMethod")); //$NON-NLS-1$
            return null;
        }
        ExecutableElement method = (ExecutableElement) element;
        validateMethodModifiers(method);

        return method;
    }

    private void validateMethodModifiers(ExecutableElement method) {
        assert method != null;
        if (method.getModifiers().contains(Modifier.PUBLIC) == false) {
            raiseInvalid(method, Messages.getString("OperatorClassCollector.errorNotPublicMethod")); //$NON-NLS-1$
        }
        if (method.getModifiers().contains(Modifier.STATIC)) {
            raiseInvalid(method, Messages.getString("OperatorClassCollector.errorStaticMethod")); //$NON-NLS-1$
        }
        if (method.getThrownTypes().isEmpty() == false) {
            raiseInvalid(method, Messages.getString("OperatorClassCollector.errorThrowsMethod")); //$NON-NLS-1$
        }
    }

    private void raiseInvalid(Element member, String message) {
        assert member != null;
        assert message != null;
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
            MessageFormat.format(
                    message,
                    member.getSimpleName()),
            member);
        sawError = true;
    }

    /**
     * Analyzes and returns the previously added operator classes.
     * @return the analyzed operator classes
     * @throws OperatorCompilerException if exception was occurred while analyzing operator classes
     */
    public List<OperatorClass> collect() {
        if (sawError) {
            throw new OperatorCompilerException(null,
                    Messages.getString("OperatorClassCollector.errorFailedToAnalyzeMethod")); //$NON-NLS-1$
        }
        Map<TypeElement, List<TargetMethod>> mapping = new HashMap<>();
        for (TargetMethod target : targetMethods) {
            Maps.addToList(mapping, target.type, target);
        }

        List<OperatorClass> results = new ArrayList<>();
        for (Map.Entry<TypeElement, List<TargetMethod>> entry : mapping.entrySet()) {
            OperatorClass klass = toOperatorClass(entry.getKey(), entry.getValue());
            results.add(klass);
        }

        if (sawError) {
            throw new OperatorCompilerException(null,
                    Messages.getString("OperatorClassCollector.errorFailedToAnalyzeClass")); //$NON-NLS-1$
        }
        return results;
    }

    private OperatorClass toOperatorClass(
            TypeElement type,
            List<TargetMethod> targets) {
        assert type != null;
        assert targets != null;
        validateClassModifiers(type);
        validateConstructorWithNoParameters(type);
        validateMemberNames(type);
        validateCoverage(type, targets);

        OperatorClass result = new OperatorClass(type);
        for (TargetMethod target : targets) {
            result.add(target.annotation, target.method, target.processor);
        }
        return result;
    }

    private void validateClassModifiers(TypeElement type) {
        assert type != null;
        if (type.getKind() != ElementKind.CLASS) {
            raiseInvalidClass(type,
                    Messages.getString("OperatorClassCollector.errorNotClass")); //$NON-NLS-1$
        }
        if (type.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            raiseInvalidClass(type,
                    Messages.getString("OperatorClassCollector.errorEnclosedClass")); //$NON-NLS-1$
        }
        if (type.getTypeParameters().isEmpty() == false) {
            raiseInvalidClass(type,
                    Messages.getString("OperatorClassCollector.errorTypeParameterClass")); //$NON-NLS-1$
        }
        if (type.getModifiers().contains(Modifier.PUBLIC) == false) {
            raiseInvalidClass(type,
                    Messages.getString("OperatorClassCollector.errorNotPublicClass")); //$NON-NLS-1$
        }
        if (type.getModifiers().contains(Modifier.ABSTRACT) == false) {
            raiseInvalidClass(type,
                    Messages.getString("OperatorClassCollector.errorNotAbstractClass")); //$NON-NLS-1$
        }
    }

    private void validateConstructorWithNoParameters(TypeElement type) {
        assert type != null;
        List<ExecutableElement> ctors = ElementFilter.constructorsIn(type.getEnclosedElements());
        if (ctors.isEmpty()) {
            return;
        }
        for (ExecutableElement ctor : ctors) {
            if (ctor.getParameters().isEmpty()
                    && ctor.getTypeParameters().isEmpty()
                    && ctor.getThrownTypes().isEmpty()) {
                return;
            }
        }
        raiseInvalidClass(type,
                Messages.getString("OperatorClassCollector.errorMissingDefaultConstructor")); //$NON-NLS-1$
    }

    private void validateMemberNames(TypeElement type) {
        Map<String, Element> saw = new HashMap<>();
        for (Element member : type.getEnclosedElements()) {
            ElementKind kind = member.getKind();
            if (kind != ElementKind.METHOD
                    && kind.isClass() == false
                    && kind.isInterface() == false) {
                continue;
            }
            if (member.getModifiers().contains(Modifier.PUBLIC) == false) {
                continue;
            }
            String id = member.getSimpleName().toString().toUpperCase();
            if (saw.containsKey(id)) {
                raiseInvalid(member,
                        Messages.getString("OperatorClassCollector.errorConflictMethodName")); //$NON-NLS-1$
            } else {
                saw.put(id, member);
            }
        }
    }

    private void validateCoverage(TypeElement type, List<TargetMethod> targets) {
        assert type != null;
        assert targets != null;

        Set<ExecutableElement> methods = new HashSet<>();
        methods.addAll(ElementFilter.methodsIn(type.getEnclosedElements()));

        Set<ExecutableElement> saw = new HashSet<>();
        for (TargetMethod target : targets) {
            ExecutableElement method = target.method;
            if (saw.contains(method)) {
                raiseInvalid(method,
                        Messages.getString("OperatorClassCollector.errorConflictOperatorAnnotation")); //$NON-NLS-1$
            } else {
                saw.add(method);
                boolean removed = methods.remove(method);
                assert removed : method;
            }
        }
        for (ExecutableElement method : methods) {
            boolean helper = isOperatorHelper(method);
            boolean open = method.getModifiers().contains(Modifier.PUBLIC);
            if (helper && open == false) {
                raiseInvalid(method,
                        Messages.getString("OperatorClassCollector.errorNotPublicHelperMethod")); //$NON-NLS-1$
            } else if (helper == false && open) {
                raiseInvalid(method,
                        Messages.getString("OperatorClassCollector.errorPublicOtherMethod")); //$NON-NLS-1$
            }
        }
    }

    private boolean isOperatorHelper(ExecutableElement method) {
        assert method != null;
        for (AnnotationMirror mirror : method.getAnnotationMirrors()) {
            DeclaredType annotationType = mirror.getAnnotationType();
            Element element = annotationType.asElement();
            if (element != null && element.getAnnotation(OperatorHelper.class) != null) {
                return true;
            }
        }
        return false;
    }

    private void raiseInvalidClass(TypeElement element, String message) {
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
            MessageFormat.format(
                    message,
                    element.getQualifiedName()),
                    element);
        sawError = true;
    }

    private static class TargetMethod {

        final AnnotationMirror annotation;

        final TypeElement type;

        final ExecutableElement method;

        final OperatorProcessor processor;

        TargetMethod(
                AnnotationMirror annotation,
                ExecutableElement method,
                OperatorProcessor processor) {
            assert annotation != null;
            assert method != null;
            assert processor != null;
            this.annotation = annotation;
            this.type = (TypeElement) method.getEnclosingElement();
            this.method = method;
            this.processor = processor;
        }
    }
}
