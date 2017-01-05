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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.OperatorPortDeclaration.Kind;
import com.asakusafw.compiler.operator.OperatorProcessor.Context;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;
import com.asakusafw.vocabulary.flow.graph.OperatorHelper;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;

/**
 * Structural information of operator methods.
 */
public class OperatorMethodDescriptor {

    private final Class<? extends Annotation> annotationType;

    private final List<DocElement> documentation;

    private final String name;

    private final List<OperatorPortDeclaration> inputPorts;

    private final List<OperatorPortDeclaration> outputPorts;

    private final List<OperatorPortDeclaration> parameters;

    private final List<Expression> attributes;

    /**
     * Creates a new instance.
     * @param annotationType the operator annotation type
     * @param documentation the documentation of the operator
     * @param name the operator name
     * @param inputPorts the input ports
     * @param outputPorts the output ports
     * @param parameters the user parameters
     * @param attributes the operator attributes
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public OperatorMethodDescriptor(
            Class<? extends Annotation> annotationType,
            List<DocElement> documentation,
            String name,
            List<OperatorPortDeclaration> inputPorts,
            List<OperatorPortDeclaration> outputPorts,
            List<OperatorPortDeclaration> parameters,
            List<Expression> attributes) {
        Precondition.checkMustNotBeNull(annotationType, "annotationType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(documentation, "documentation"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(inputPorts, "inputPorts"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputPorts, "outputPorts"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(parameters, "parameters"); //$NON-NLS-1$
        this.annotationType = annotationType;
        this.documentation = Lists.freeze(documentation);
        this.name = name;
        this.inputPorts = Lists.freeze(inputPorts);
        this.outputPorts = Lists.freeze(outputPorts);
        this.parameters = Lists.freeze(parameters);
        this.attributes = Lists.freeze(attributes);
    }

    /**
     * Returns the operator annotation type.
     * @return the operator annotation type
     */
    public Class<? extends Annotation> getAnnotationType() {
        return annotationType;
    }

    /**
     * Returns the documentation of the operator.
     * @return the documentation of the operator
     */
    public List<DocElement> getDocumentation() {
        return documentation;
    }

    /**
     * Returns the operator name.
     * @return the operator name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the input ports.
     * @return the input ports
     */
    public List<OperatorPortDeclaration> getInputPorts() {
        return inputPorts;
    }

    /**
     * Returns the output ports.
     * @return the output ports
     */
    public List<OperatorPortDeclaration> getOutputPorts() {
        return outputPorts;
    }

    /**
     * Returns the user parameters.
     * @return the user parameters
     */
    public List<OperatorPortDeclaration> getParameters() {
        return parameters;
    }

    /**
     * Returns the operator annotations.
     * @return the operator annotations
     */
    public List<Expression> getAttributes() {
        return attributes;
    }

    /**
     * A builder for building {@link OperatorMethodDescriptor}.
     */
    public static class Builder {

        private final Class<? extends Annotation> annotationType;

        private List<DocElement> operatorDescription;

        private final String name;

        private final List<OperatorPortDeclaration> inputPorts;

        private final List<OperatorPortDeclaration> outputPorts;

        private final List<OperatorPortDeclaration> parameters;

        private final List<Expression> attributes;

        private final Context context;

        /**
         * Creates a new instance.
         * @param annotationType the target operator annotation
         * @param context the current context
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Builder(
                Class<? extends Annotation> annotationType,
                OperatorProcessor.Context context) {
            Precondition.checkMustNotBeNull(annotationType, "annotationType"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
            this.context = context;
            this.annotationType = annotationType;
            this.name = context.element.getSimpleName().toString();
            this.operatorDescription = new ArrayList<>();
            this.inputPorts = new ArrayList<>();
            this.outputPorts = new ArrayList<>();
            this.parameters = new ArrayList<>();
            this.attributes = new ArrayList<>();
        }

        /**
         * Returns an input name which has the specified type variable.
         * @param type target type
         * @return the matched input name, or {@code null} if not exists or is not a projective model
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public String findInput(TypeMirror type) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            if (type.getKind() != TypeKind.TYPEVAR) {
                return null;
            }
            Types types = context.environment.getTypeUtils();
            for (OperatorPortDeclaration input : inputPorts) {
                if (types.isSameType(type, input.getType().getRepresentation())) {
                    return input.getName();
                }
            }
            return null;
        }

        /**
         * Sets a documentation for the operator.
         * @param description the document
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public void setDocumentation(List<? extends DocElement> description) {
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            this.operatorDescription = Lists.from(description);
        }

        /**
         * Adds an input port.
         * @param documentation the port documentation
         * @param varName the port name
         * @param type the port type
         * @param position the original parameter index, or {@code null} if it was not derived from any parameters
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public void addInput(
                List<? extends DocElement> documentation,
                String varName,
                TypeMirror type,
                Integer position) {
            addInput(documentation, varName, type, position, null);
        }

        /**
         * Adds an input port.
         * @param documentation the port documentation
         * @param varName the port name
         * @param type the port type
         * @param position the original parameter index, or {@code null} if it was not derived from any parameters
         * @param shuffleKey the shuffle key
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public void addInput(
                List<? extends DocElement> documentation,
                String varName,
                TypeMirror type,
                Integer position,
                ShuffleKey shuffleKey) {
            Precondition.checkMustNotBeNull(documentation, "documentation"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(varName, "varName"); //$NON-NLS-1$
            inputPorts.add(new OperatorPortDeclaration(
                    Kind.INPUT,
                    documentation,
                    varName,
                    PortTypeDescription.reference(type, varName),
                    position,
                    shuffleKey));
        }

        /**
         * Adds an output port.
         * @param documentation the port documentation
         * @param varName the port name
         * @param type the port type
         * @param correspondedInputName the port name which has the same type to this port
         * @param position the original parameter index, or {@code null} if it was not derived from any parameters
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public void addOutput(
                List<? extends DocElement> documentation,
                String varName,
                TypeMirror type,
                String correspondedInputName,
                Integer position) {
            Precondition.checkMustNotBeNull(documentation, "documentation"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(varName, "varName"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            PortTypeDescription typeDesc;
            if (correspondedInputName != null) {
                typeDesc = PortTypeDescription.reference(type, correspondedInputName);
            } else {
                typeDesc = PortTypeDescription.direct(type);
            }
            outputPorts.add(new OperatorPortDeclaration(
                    Kind.OUTPUT,
                    documentation,
                    varName,
                    typeDesc,
                    position,
                    null));
        }

        /**
         * Adds an output port.
         * @param documentation the port documentation
         * @param varName the port name
         * @param type the port type
         * @param correspondedInputName the port name which has the same type to this port
         * @param position the original parameter index, or {@code null} if it was not derived from any parameters
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public void addOutput(
                String documentation,
                String varName,
                TypeMirror type,
                String correspondedInputName,
                Integer position) {
            Precondition.checkMustNotBeNull(documentation, "documentation"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(varName, "varName"); //$NON-NLS-1$
            List<? extends DocElement> elements = Collections.emptyList();
            if (documentation != null) {
                elements = Collections.singletonList(Models.getModelFactory()
                    .newDocText(documentation));
            }
            addOutput(elements, varName, type, correspondedInputName, position);
        }

        /**
         * Adds an user parameter.
         * @param documentation the documentation
         * @param varName the variable name
         * @param type the variable type
         * @param position the original parameter index, or {@code null} if it was not derived from any parameters
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public void addParameter(
                List<? extends DocElement> documentation,
                String varName,
                TypeMirror type,
                Integer position) {
            Precondition.checkMustNotBeNull(documentation, "documentation"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(varName, "varName"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            parameters.add(new OperatorPortDeclaration(
                    Kind.CONSTANT,
                    documentation,
                    varName,
                    PortTypeDescription.direct(type),
                    position,
                    null));
        }

        /**
         * Adds an operator attribute.
         * @param attribute the target attribute
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public void addAttribute(Expression attribute) {
            Precondition.checkMustNotBeNull(attribute, "attribute"); //$NON-NLS-1$
            attributes.add(attribute);
        }

        /**
         * Adds an attribute.
         * @param constant the {@link FlowElementAttribute}
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public void addAttribute(Enum<? extends FlowElementAttribute> constant) {
            Precondition.checkMustNotBeNull(constant, "constant"); //$NON-NLS-1$
            ModelFactory f = context.environment.getFactory();
            ImportBuilder ib = context.importer;
            Expression attribute = new TypeBuilder(f, ib.toType(constant.getDeclaringClass()))
                .field(constant.name())
                .toExpression();
            addAttribute(attribute);
        }

        /**
         * Adds an operator helper method.
         * @param helperMethod the operator helper method
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public void addOperatorHelper(ExecutableElement helperMethod) {
            Precondition.checkMustNotBeNull(helperMethod, "helperMethod"); //$NON-NLS-1$
            ModelFactory f = context.environment.getFactory();
            ImportBuilder ib = context.importer;
            Jsr269 conv = new Jsr269(f);
            List<Expression> parameterTypeLiterals = new ArrayList<>();
            for (VariableElement parameter : helperMethod.getParameters()) {
                TypeMirror type = context.environment.getErasure(parameter.asType());
                parameterTypeLiterals.add(new TypeBuilder(f, ib.resolve(conv.convert(type)))
                    .dotClass()
                    .toExpression());
            }
            Expression attribute = new TypeBuilder(f, ib.toType(OperatorHelper.class))
                .newObject(new Expression[] {
                        // name
                        Models.toLiteral(f, helperMethod.getSimpleName().toString()),
                        // parameter types
                        new TypeBuilder(f, ib.toType(Arrays.class))
                            .method("asList", new TypeBuilder(f, ib.toType(Class.class)) //$NON-NLS-1$
                                .parameterize(f.newWildcard())
                                .array(1)
                                .newArray(f.newArrayInitializer(parameterTypeLiterals))
                                .toExpression())
                            .toExpression()
                }).toExpression();
            addAttribute(attribute);
        }

        /**
         * Builds a {@link OperatorMethodDescriptor}.
         * @return the created object
         */
        public OperatorMethodDescriptor toDescriptor() {
            return new OperatorMethodDescriptor(
                    annotationType,
                    operatorDescription,
                    name,
                    inputPorts,
                    outputPorts,
                    parameters,
                    attributes);
        }
    }
}
