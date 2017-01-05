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
package com.asakusafw.compiler.operator.flow;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.TypeConstraint;
import com.asakusafw.compiler.operator.OperatorCompilerException;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.asakusafw.compiler.operator.OperatorPortDeclaration;
import com.asakusafw.compiler.operator.PortTypeDescription;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Collects flow-part classes.
 */
public class FlowPartClassCollector {

    private final OperatorCompilingEnvironment environment;

    private final List<FlowPartClass> collected;

    private boolean sawError;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public FlowPartClassCollector(OperatorCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
        this.collected = new ArrayList<>();
        this.sawError = false;
    }

    /**
     * Adds an element that is annotated with flow-part annotation.
     * @param element the target element
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void add(Element element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        if (element.getKind() != ElementKind.CLASS) {
            error(element, Messages.getString("FlowPartClassCollector.errorNotClass")); //$NON-NLS-1$
            return;
        }
        TypeElement typeDecl = (TypeElement) element;
        FlowPartClass result = toFlowPartClass(typeDecl);
        if (result != null) {
            collected.add(result);
        }
    }

    private FlowPartClass toFlowPartClass(Element element) {
        assert element != null;
        if (validateClassModifiers(element) == false) {
            return null;
        }
        TypeElement type = (TypeElement) element;
        ExecutableElement ctor = findConstructor(type);
        if (ctor == null) {
            return null;
        }
        validateConstructorModifiers(ctor);
        FlowPartClass aClass = analyze(type, ctor);
        return aClass;
    }

    private FlowPartClass analyze(TypeElement aClass, ExecutableElement ctor) {
        assert aClass != null;
        assert ctor != null;
        ExecutableAnalyzer analyzer = new ExecutableAnalyzer(environment, ctor);
        List<? extends DocElement> documentation = analyzer.getDocument(aClass);
        List<OperatorPortDeclaration> inputPorts = new ArrayList<>();
        List<OperatorPortDeclaration> outputPorts = new ArrayList<>();
        List<OperatorPortDeclaration> parameters = new ArrayList<>();
        for (int i = 0, n = analyzer.countParameters(); i < n; i++) {
            OperatorPortDeclaration port = analyzePort(analyzer, i);
            if (port == null) {
                continue;
            } else if (port.getKind() == OperatorPortDeclaration.Kind.INPUT) {
                inputPorts.add(port);
            } else if (port.getKind() == OperatorPortDeclaration.Kind.OUTPUT) {
                outputPorts.add(port);
            } else {
                parameters.add(port);
            }
        }
        if (inputPorts.isEmpty() && outputPorts.isEmpty()) {
            analyzer.error(Messages.getString("FlowPartClassCollector.errorMissingIo")); //$NON-NLS-1$
        }
        if (analyzer.hasError()) {
            sawError = true;
            return null;
        }
        outputPorts = inferTypeVariables(analyzer, inputPorts, outputPorts, parameters);
        if (analyzer.hasError()) {
            sawError = true;
            return null;
        }
        return new FlowPartClass(
                aClass,
                documentation,
                inputPorts,
                outputPorts,
                parameters);
    }

    private OperatorPortDeclaration analyzePort(
            ExecutableAnalyzer analyzer, int index) {
        TypeConstraint type = analyzer.getParameterType(index);
        if (type.isIn()) {
            return toPort(OperatorPortDeclaration.Kind.INPUT, analyzer, type, index);
        } else if (type.isOut()) {
            return toPort(OperatorPortDeclaration.Kind.OUTPUT, analyzer, type, index);
        } else {
            return toParameter(analyzer, index, type);
        }
    }

    private List<OperatorPortDeclaration> inferTypeVariables(
            ExecutableAnalyzer analyzer,
            List<OperatorPortDeclaration> inputPorts,
            List<OperatorPortDeclaration> outputPorts,
            List<OperatorPortDeclaration> parameters) {
        assert analyzer != null;
        assert inputPorts != null;
        assert outputPorts != null;
        assert parameters != null;
        List<OperatorPortDeclaration> inferred = new ArrayList<>();
        for (OperatorPortDeclaration output : outputPorts) {
            if (output.getType().getRepresentation().getKind() != TypeKind.TYPEVAR) {
                inferred.add(new OperatorPortDeclaration(
                        output.getKind(),
                        output.getDocumentation(),
                        output.getName(),
                        PortTypeDescription.direct(output.getType().getRepresentation()),
                        output.getParameterPosition(),
                        null));
            } else {
                OperatorPortDeclaration outputType = inferOutputType(output, inputPorts, parameters);
                if (outputType != null) {
                    inferred.add(outputType);
                } else {
                    analyzer.error(
                            output.getParameterPosition(),
                            Messages.getString("FlowPartClassCollector.errorUnboundOutputType"), //$NON-NLS-1$
                            output.getName(),
                            output.getType().getRepresentation());
                }
            }
        }
        return inferred;
    }

    private OperatorPortDeclaration inferOutputType(
            OperatorPortDeclaration output,
            List<OperatorPortDeclaration> inputPorts,
            List<OperatorPortDeclaration> parameters) {
        assert output != null;
        assert inputPorts != null;
        assert parameters != null;
        Types types = environment.getTypeUtils();
        TypeMirror outputType = output.getType().getRepresentation();
        for (OperatorPortDeclaration input : inputPorts) {
            if (types.isSameType(outputType, input.getType().getRepresentation())) {
                return new OperatorPortDeclaration(
                        output.getKind(),
                        output.getDocumentation(),
                        output.getName(),
                        PortTypeDescription.reference(outputType, input.getName()),
                        output.getParameterPosition(),
                        null);
            }
        }
        DeclaredType classType = environment.getDeclaredType(Class.class);
        for (OperatorPortDeclaration param : parameters) {
            // check is form of M<T>
            TypeMirror paramType = param.getType().getRepresentation();
            if (paramType.getKind() != TypeKind.DECLARED) {
                continue;
            }
            DeclaredType declParamType = (DeclaredType) paramType;
            if (declParamType.getTypeArguments().size() != 1) {
                continue;
            }
            // check is <: Class<?>
            if (types.isSameType(environment.getErasure(paramType), classType) == false) {
                continue;
            }
            if (types.isSameType(declParamType.getTypeArguments().get(0), outputType)) {
                return new OperatorPortDeclaration(
                        output.getKind(),
                        output.getDocumentation(),
                        output.getName(),
                        PortTypeDescription.reference(outputType, param.getName()),
                        output.getParameterPosition(),
                        null);
            }
        }
        return null;
    }

    private OperatorPortDeclaration toPort(
            OperatorPortDeclaration.Kind kind,
            ExecutableAnalyzer analyzer,
            TypeConstraint type,
            int index) {
        assert analyzer != null;
        assert type != null;
        TypeConstraint dataType = type.getTypeArgument();
        if (dataType.isModel() == false) {
            analyzer.error(index, Messages.getString("FlowPartClassCollector.errorNotModelIo")); //$NON-NLS-1$
            return null;
        }
        return new OperatorPortDeclaration(
                kind,
                analyzer.getParameterDocument(index),
                analyzer.getParameterName(index),
                PortTypeDescription.reference(dataType.getType(), analyzer.getParameterName(index)),
                index,
                null);
    }

    private OperatorPortDeclaration toParameter(
            ExecutableAnalyzer analyzer,
            int index,
            TypeConstraint type) {
        assert analyzer != null;
        assert type != null;
        if (type.isOperator()) {
            analyzer.error(index, Messages.getString("FlowPartClassCollector.errorOperatorParameter")); //$NON-NLS-1$
            return null;
        }
        return new OperatorPortDeclaration(
                OperatorPortDeclaration.Kind.CONSTANT,
                analyzer.getParameterDocument(index),
                analyzer.getParameterName(index),
                PortTypeDescription.direct(type.getType()),
                index,
                null);
    }

    private ExecutableElement findConstructor(TypeElement type) {
        assert type != null;
        List<ExecutableElement> elements = new ArrayList<>();
        for (Element element : type.getEnclosedElements()) {
            if (element.getKind() == ElementKind.CONSTRUCTOR
                    && element.getModifiers().contains(Modifier.PUBLIC)) {
                elements.add((ExecutableElement) element);
            }
        }
        if (elements.isEmpty()) {
            error(type, Messages.getString("FlowPartClassCollector.errorMissingPublicConstructor")); //$NON-NLS-1$
            return null;
        }
        if (elements.size() >= 2) {
            for (ExecutableElement odd : elements) {
                error(odd, Messages.getString("FlowPartClassCollector.errorExtraPublicConstructor")); //$NON-NLS-1$
            }
            return null;
        }
        return elements.get(0);
    }

    /**
     * Analyzes and returns the previously added flow-part classes.
     * @return the analyzed flow-part classes
     * @throws OperatorCompilerException if exception was occurred while analyzing operator classes
     */
    public List<FlowPartClass> collect() {
        if (sawError) {
            throw new OperatorCompilerException(null,
                    Messages.getString("FlowPartClassCollector.errorFailedToAnalyze")); //$NON-NLS-1$
        }
        return collected;
    }

    private boolean validateClassModifiers(Element element) {
        assert element != null;
        TypeElement type = (TypeElement) element;
        DeclaredType superType = environment.getDeclaredType(FlowDescription.class);
        if (environment.getTypeUtils().isSubtype(type.asType(), superType) == false) {
            raiseInvalidClass(type, MessageFormat.format(
                    Messages.getString("FlowPartClassCollector.errorInvalidSuperClass"), //$NON-NLS-1$
                    "{0}", //$NON-NLS-1$
                    FlowDescription.class.getName()));
        }
        if (type.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            raiseInvalidClass(type, Messages.getString("FlowPartClassCollector.errorEnclosedClass")); //$NON-NLS-1$
        }
        if (type.getModifiers().contains(Modifier.PUBLIC) == false) {
            raiseInvalidClass(type, Messages.getString("FlowPartClassCollector.errorNotPublic")); //$NON-NLS-1$
        }
        if (type.getModifiers().contains(Modifier.ABSTRACT)) {
            raiseInvalidClass(type, Messages.getString("FlowPartClassCollector.errorAbstract")); //$NON-NLS-1$
        }
        return true;
    }

    private void validateConstructorModifiers(ExecutableElement ctor) {
        assert ctor != null;
        if (ctor.getThrownTypes().isEmpty() == false) {
            error(ctor, Messages.getString("FlowPartClassCollector.errorThrows")); //$NON-NLS-1$
        }
        if (ctor.getTypeParameters().isEmpty() == false) {
            error(ctor, Messages.getString("FlowPartClassCollector.errorTypeParameterConstructor")); //$NON-NLS-1$
        }
    }

    private void raiseInvalidClass(TypeElement element, String message) {
        error(element, MessageFormat.format(
                message,
                element.getQualifiedName()));
    }

    private void error(Element element, String message) {
        assert element != null;
        assert message != null;
        this.environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                message,
                element);
        this.sawError = true;
    }
}
