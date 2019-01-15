/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.operator.method;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.OperatorClass;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.operator.util.AnnotationHelper;
import com.asakusafw.operator.util.ElementHelper;
import com.asakusafw.operator.util.Logger;
import com.asakusafw.operator.util.TypeHelper;

/**
 * Analyzes operator methods.
 */
public class OperatorMethodAnalyzer {

    static final Logger LOG = Logger.get(OperatorMethodAnalyzer.class);

    private static final ClassDescription TYPE_OBJECT = Descriptions.classOf(Object.class);

    private final CompileEnvironment environment;

    private final Map<TypeElement, List<AnnotatedMethod>> operatorClasses;

    /**
     * Creates a new instance.
     * @param environment current compiling environment
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public OperatorMethodAnalyzer(CompileEnvironment environment) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        this.operatorClasses = new HashMap<>();
    }

    /**
     * Registers an operator method declaration to this analyzer.
     * @param annotationDecl target annotation declaration
     * @param methodDecl target method declaration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void register(TypeElement annotationDecl, ExecutableElement methodDecl) {
        Objects.requireNonNull(annotationDecl, "annotationDecl must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(methodDecl, "methodDecl must not be null"); //$NON-NLS-1$
        if (methodDecl.getKind() != ElementKind.METHOD) {
            error(methodDecl, Messages.getString("OperatorMethodAnalyzer.errorMethodInvalid"), //$NON-NLS-1$
                    methodDecl.getSimpleName());
            return;
        }
        AnnotatedMethod method = toAnnotatedMethod(annotationDecl, methodDecl);
        if (method == null) {
            return;
        }
        TypeElement declaring = (TypeElement) methodDecl.getEnclosingElement();
        List<AnnotatedMethod> methods = operatorClasses.get(declaring);
        if (methods == null) {
            methods = new ArrayList<>();
            operatorClasses.put(declaring, methods);
        }
        methods.add(method);
    }

    private AnnotatedMethod toAnnotatedMethod(TypeElement annotationDecl, ExecutableElement methodDecl) {
        assert annotationDecl != null;
        assert methodDecl != null;
        AnnotationMirror annotation = AnnotationHelper.findAnnotation(environment, annotationDecl, methodDecl);
        if (annotation == null) {
            // may not come here
            error(methodDecl, Messages.getString("OperatorMethodAnalyzer.errorMethodInvalidAnnotation"), //$NON-NLS-1$
                    methodDecl.getSimpleName(),
                    annotationDecl.getSimpleName());
            return null;
        }
        OperatorDriver driver = environment.findDriver(annotationDecl);
        if (driver == null) {
            // may not come here
            environment.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                    MessageFormat.format(
                            Messages.getString("OperatorMethodAnalyzer.errorFailLoadOperatorDriver"), //$NON-NLS-1$
                            annotationDecl.getSimpleName()),
                    methodDecl,
                    annotation);
            return null;
        }
        return new AnnotatedMethod(annotation, methodDecl, driver);
    }

    /**
     * Resolves previously {@link #register(TypeElement, ExecutableElement) registered} operator methods.
     * @return resolved operator classes, or {@code null} if no valid operator methods are registered
     */
    public Collection<OperatorClass> resolve() {
        Collection<OperatorClass> results = new ArrayList<>();
        for (Map.Entry<TypeElement, List<AnnotatedMethod>> entry : operatorClasses.entrySet()) {
            OperatorClass resolved = resolve(entry.getKey(), entry.getValue());
            if (resolved != null) {
                results.add(resolved);
            }
        }
        return results;
    }

    private OperatorClass resolve(TypeElement classDecl, List<AnnotatedMethod> methods) {
        assert classDecl != null;
        assert methods != null;
        if (validateClass(classDecl) == false) {
            return null;
        }
        List<AnnotatedMethod> operatorMethods = selectValidMethods(classDecl, methods);
        List<OperatorElement> elements = new ArrayList<>();
        for (AnnotatedMethod method : operatorMethods) {
            OperatorDriver.Context context = new OperatorDriver.Context(environment, method.annotation, method.decl);
            OperatorDescription description = method.driver.analyze(context);
            if (description != null && ElementHelper.validate(environment, method.decl, description) == false) {
                description = null;
            }
            elements.add(new OperatorElement(method.annotation, method.decl, description));
        }
        return new OperatorClass(classDecl, elements);
    }

    private boolean validateClass(TypeElement type) {
        assert type != null;
        boolean valid = true;
        if (type.getKind() != ElementKind.CLASS) {
            error(type, Messages.getString("OperatorMethodAnalyzer.errorClassNotNotmalClass")); //$NON-NLS-1$
            valid = false;
        }
        if (type.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            error(type, Messages.getString("OperatorMethodAnalyzer.errorClassNotTopLevel"), //$NON-NLS-1$
                    type.getSimpleName());
            valid = false;
        }
        if (type.getTypeParameters().isEmpty() == false) {
            error(type, Messages.getString("OperatorMethodAnalyzer.errorClassGeneric"), //$NON-NLS-1$
                    type.getSimpleName());
            valid = false;
        }
        if (type.getModifiers().contains(Modifier.PUBLIC) == false) {
            error(type, Messages.getString("OperatorMethodAnalyzer.errorClassNotPublic"), //$NON-NLS-1$
                    type.getSimpleName());
            valid = false;
        }
        if (type.getModifiers().contains(Modifier.FINAL)) {
            error(type, Messages.getString("OperatorMethodAnalyzer.errorClassFinal"), //$NON-NLS-1$
                    type.getSimpleName());
            valid = false;
        }
        if (type.getModifiers().contains(Modifier.ABSTRACT) == false) { // optional requirement
            warn(type, Messages.getString("OperatorMethodAnalyzer.warnClassNotAbstract"), //$NON-NLS-1$
                    type.getSimpleName());
        }
        if (type.getSuperclass().getKind() != TypeKind.NONE) { // optional requirement
            Types types = environment.getProcessingEnvironment().getTypeUtils();
            DeclaredType object = environment.findDeclaredType(TYPE_OBJECT);
            if (types.isSameType(type.getSuperclass(), object) == false) {
                warn(type, Messages.getString("OperatorMethodAnalyzer.warnClassExtendNotObjectClass"), //$NON-NLS-1$
                        type.getSimpleName());
            }
        }
        if (type.getInterfaces().isEmpty() == false) { // optional requirement
            warn(type, Messages.getString("OperatorMethodAnalyzer.warnClassImplementInterface"), //$NON-NLS-1$
                    type.getSimpleName());
        }
        List<ExecutableElement> ctors = ElementFilter.constructorsIn(type.getEnclosedElements());
        if (ctors.isEmpty() == false) {
            boolean sawTrivial = false;
            for (ExecutableElement ctor : ctors) {
                if (ctor.getParameters().isEmpty()) {
                    if (ctor.getModifiers().contains(Modifier.PUBLIC) == false) {
                        error(type, Messages.getString("OperatorMethodAnalyzer.errorConstructorNotPublic"), //$NON-NLS-1$
                                type.getSimpleName());
                    }
                    if (ctor.getTypeParameters().isEmpty() == false) {
                        error(type, Messages.getString("OperatorMethodAnalyzer.errorConstructorGeneric"), //$NON-NLS-1$
                                type.getSimpleName());
                    }
                    if (ctor.getThrownTypes().isEmpty() == false) {
                        error(type, Messages.getString("OperatorMethodAnalyzer.errorConstructorException"), //$NON-NLS-1$
                                type.getSimpleName());
                    }
                    sawTrivial = true;
                    break;
                }
            }
            if (sawTrivial == false) {
                error(type, Messages.getString("OperatorMethodAnalyzer.errorConstructorMissing"), //$NON-NLS-1$
                        type.getSimpleName());
                valid = false;
            }
        }
        return valid;
    }

    private List<AnnotatedMethod> selectValidMethods(TypeElement type, List<AnnotatedMethod> methods) {
        assert type != null;
        assert methods != null;
        List<AnnotatedMethod> results = new ArrayList<>();
        Map<ExecutableElement, AnnotatedMethod> sawMethods = new HashMap<>();
        Map<String, AnnotatedMethod> sawNameIds = new HashMap<>();
        for (AnnotatedMethod target : methods) {
            ExecutableElement method = target.decl;
            boolean valid = true;
            if (method.getModifiers().contains(Modifier.PUBLIC) == false) {
                error(method, Messages.getString("OperatorMethodAnalyzer.errorMethodNotPublic"), //$NON-NLS-1$
                        type.getSimpleName(), method.getSimpleName());
                valid = false;
            }
            if (method.getModifiers().contains(Modifier.STATIC)) {
                error(method, Messages.getString("OperatorMethodAnalyzer.errorMethodStatic"), //$NON-NLS-1$
                        type.getSimpleName(), method.getSimpleName());
                valid = false;
            }
            if (sawMethods.containsKey(method)) {
                AnnotatedMethod conflict = sawMethods.get(method);
                environment.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        MessageFormat.format(
                                Messages.getString("OperatorMethodAnalyzer.errorMethodAmbiguousOperatorAnnotation"), //$NON-NLS-1$
                                type.getSimpleName(), method.getSimpleName(),
                                conflict.annotation.getAnnotationType().asElement().getSimpleName(),
                                target.annotation.getAnnotationType().asElement().getSimpleName()),
                        method,
                        target.annotation);
                valid = false;
            } else {
                sawMethods.put(method, target);
                String nameId = toNameId(method.getSimpleName());
                if (sawNameIds.containsKey(nameId)) {
                    AnnotatedMethod conflict = sawNameIds.get(nameId);
                    error(method, Messages.getString("OperatorMethodAnalyzer.errorMethodNameConflict"), //$NON-NLS-1$
                            type.getSimpleName(), method.getSimpleName(),
                            conflict.decl.getSimpleName());
                    valid = false;
                } else {
                    sawNameIds.put(nameId, target);
                }
            }
            if (valid) {
                results.add(target);
            }
        }

        for (ExecutableElement method : ElementFilter.methodsIn(type.getEnclosedElements())) {
            if (method.getModifiers().contains(Modifier.PUBLIC) == false) {
                continue;
            }
            if (sawMethods.containsKey(method)) {
                continue;
            }
            if (hasOperatorHelper(method)) {
                continue;
            }
            warn(method, Messages.getString("OperatorMethodAnalyzer.warnMethodPublicOtherThanOperator"), //$NON-NLS-1$
                    type.getSimpleName(),
                    method.getSimpleName());
        }
        return results;
    }

    private boolean hasOperatorHelper(ExecutableElement method) {
        for (AnnotationMirror annotation : method.getAnnotationMirrors()) {
            if (TypeHelper.isOperatorHelper(environment, annotation.getAnnotationType())) {
                return true;
            }
        }
        return false;
    }

    private static String toNameId(Name simpleName) {
        assert simpleName != null;
        StringBuilder buf = new StringBuilder();
        for (int i = 0, n = simpleName.length(); i < n; i++) {
            char c = simpleName.charAt(i);
            if (c != '_') {
                buf.append(Character.toLowerCase(c));
            }
        }
        return buf.toString();
    }

    private void warn(Element element, String pattern, Object... arguments) {
        switch (environment.getWarningAction()) {
        case IGNORE:
            message(Diagnostic.Kind.NOTE, element, pattern, arguments);
            break;
        case REPORT:
            message(Diagnostic.Kind.WARNING, element, pattern, arguments);
            break;
        case FAIL:
            message(Diagnostic.Kind.ERROR, element, pattern, arguments);
            break;
        default:
            break;
        }
    }

    private void error(Element element, String pattern, Object... arguments) {
        message(Diagnostic.Kind.ERROR, element, pattern, arguments);
    }

    private void message(Diagnostic.Kind kind, Element element, String pattern, Object... arguments) {
        assert kind != null;
        assert element != null;
        assert pattern != null;
        assert arguments != null;
        String message = arguments.length == 0 ? pattern : MessageFormat.format(pattern, arguments);
        if (kind == Diagnostic.Kind.NOTE) {
            LOG.debug(message);
        } else {
            environment.getProcessingEnvironment().getMessager().printMessage(kind, message, element);
        }
    }

    private static final class AnnotatedMethod {

        final AnnotationMirror annotation;

        final ExecutableElement decl;

        final OperatorDriver driver;

        AnnotatedMethod(AnnotationMirror annotation, ExecutableElement method, OperatorDriver driver) {
            assert annotation != null;
            assert method != null;
            assert driver != null;
            this.annotation = annotation;
            this.decl = method;
            this.driver = driver;
        }
    }
}
