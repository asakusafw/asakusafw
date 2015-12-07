/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.flow.graph;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.vocabulary.flow.Source;

/**
 * A description of user/code operator.
 * @since 0.1.0
 * @version 0.5.1
 */
public class OperatorDescription implements FlowElementDescription {

    private final FlowElementDescription origin;

    private final Declaration declaration;

    private final List<FlowElementPortDescription> inputPorts;

    private final List<FlowElementPortDescription> outputPorts;

    private final List<FlowResourceDescription> resources;

    private final List<Parameter> parameters;

    private final Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> attributes;

    private String name;

    /**
     * Creates a new instance.
     * @param declaration information of the operator declaration
     * @param inputPorts information of the input ports
     * @param outputPorts information of the output ports
     * @param resources information of the external resources
     * @param parameters information of the user parameters
     * @param attributes the attributes
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public OperatorDescription(
            Declaration declaration,
            List<FlowElementPortDescription> inputPorts,
            List<FlowElementPortDescription> outputPorts,
            List<FlowResourceDescription> resources,
            List<Parameter> parameters,
            List<FlowElementAttribute> attributes) {
        this(null, declaration, inputPorts, outputPorts, resources, parameters, attributes);
    }

    /**
     * Creates a new instance.
     * @param origin the original description (nullable)
     * @param declaration information of the operator declaration
     * @param inputPorts information of the input ports
     * @param outputPorts information of the output ports
     * @param resources information of the external resources
     * @param parameters information of the user parameters
     * @param attributes the attributes
     * @throws IllegalArgumentException if some parameters are {@code null}
     * @since 0.5.1
     */
    public OperatorDescription(
            FlowElementDescription origin,
            Declaration declaration,
            List<FlowElementPortDescription> inputPorts,
            List<FlowElementPortDescription> outputPorts,
            List<FlowResourceDescription> resources,
            List<Parameter> parameters,
            List<FlowElementAttribute> attributes) {
        if (declaration == null) {
            throw new IllegalArgumentException("declaration must not be null"); //$NON-NLS-1$
        }
        if (inputPorts == null) {
            throw new IllegalArgumentException("inputPorts must not be null"); //$NON-NLS-1$
        }
        if (outputPorts == null) {
            throw new IllegalArgumentException("outputPorts must not be null"); //$NON-NLS-1$
        }
        if (resources == null) {
            throw new IllegalArgumentException("resources must not be null"); //$NON-NLS-1$
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        this.origin = origin == null ? this : origin;
        this.declaration = declaration;
        this.inputPorts = Collections.unmodifiableList(new ArrayList<>(inputPorts));
        this.outputPorts = Collections.unmodifiableList(new ArrayList<>(outputPorts));
        this.resources = Collections.unmodifiableList(new ArrayList<>(resources));
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        this.attributes = new HashMap<>();
        for (FlowElementAttribute attribute : attributes) {
            this.attributes.put(attribute.getDeclaringClass(), attribute);
        }
    }

    @Override
    public FlowElementKind getKind() {
        return FlowElementKind.OPERATOR;
    }

    @Override
    public FlowElementDescription getOrigin() {
        return origin;
    }

    /**
     * Returns information of the operator declaration.
     * @return information of the operator declaration
     */
    public Declaration getDeclaration() {
        return declaration;
    }

    @Override
    public String getName() {
        if (name == null) {
            return MessageFormat.format(
                "{0}.{1}", //$NON-NLS-1$
                declaration.getDeclaring().getSimpleName(),
                declaration.getName());
        }
        return name;
    }

    @Override
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        this.name = name;
    }

    @Override
    public List<FlowElementPortDescription> getInputPorts() {
        return inputPorts;
    }

    @Override
    public List<FlowElementPortDescription> getOutputPorts() {
        return outputPorts;
    }

    @Override
    public List<FlowResourceDescription> getResources() {
        return resources;
    }

    /**
     *
     * Returns information of the user parameters.
     * @return information of the user parameters
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public <T extends FlowElementAttribute> T getAttribute(Class<T> attributeClass) {
        if (attributeClass == null) {
            throw new IllegalArgumentException("attributeClass must not be null"); //$NON-NLS-1$
        }
        Object attribute = attributes.get(attributeClass);
        return attributeClass.cast(attribute);
    }

    /**
     * Returns the attributes of the operator.
     * @return the attributes
     */
    public Set<FlowElementAttribute> getAttributes() {
        return new HashSet<>(attributes.values());
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}#{1}(@{2})", //$NON-NLS-1$
                getDeclaration().getDeclaring().getSimpleName(),
                getDeclaration().getName(),
                getDeclaration().getAnnotationType().getSimpleName());
    }

    /**
     * Represents a declaration of operator.
     */
    public static class Declaration {

        private final Class<? extends Annotation> annotationType;

        private final Class<?> declaring;

        private final Class<?> implementing;

        private final String name;

        private final List<Class<?>> parameterTypes;

        /**
         * Creates a new instance.
         * @param annotationType the operator annotation type
         * @param declaring the declaring class (a.k.a. operator class)
         * @param implementing the implementation class
         * @param name the operator method name
         * @param parameterTypes the erased parameter types of the operator method
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Declaration(
                Class<? extends Annotation> annotationType,
                Class<?> declaring,
                Class<?> implementing,
                String name,
                List<Class<?>> parameterTypes) {
            if (annotationType == null) {
                throw new IllegalArgumentException("annotationType must not be null"); //$NON-NLS-1$
            }
            if (declaring == null) {
                throw new IllegalArgumentException("declaring must not be null"); //$NON-NLS-1$
            }
            if (implementing == null) {
                throw new IllegalArgumentException("implementing must not be null"); //$NON-NLS-1$
            }
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
            }
            this.annotationType = annotationType;
            this.declaring = declaring;
            this.implementing = implementing;
            this.name = name;
            this.parameterTypes = parameterTypes;
        }

        /**
         * Returns the operator annotation type.
         * @return the operator annotation type
         */
        public Class<? extends Annotation> getAnnotationType() {
            return annotationType;
        }

        /**
         * Returns the declaring class.
         * @return the declaring class
         */
        public Class<?> getDeclaring() {
            return declaring;
        }

        /**
         * Returns the implementation class.
         * @return the implementation class
         */
        public Class<?> getImplementing() {
            return implementing;
        }

        /**
         * Returns the operator method name.
         * @return the operator method name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the erased parameter types of the operator method.
         * @return the parameter types
         */
        public List<Class<?>> getParameterTypes() {
            return parameterTypes;
        }

        /**
         * Returns the reflective object of the target operator method.
         * @return the reflective object, or {@code null} if there is no such a corresponding method
         */
        public Method toMethod() {
            Class<?>[] params = parameterTypes.toArray(new Class<?>[parameterTypes.size()]);
            try {
                return declaring.getMethod(name, params);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0}#{1}({2})", //$NON-NLS-1$
                    declaring.getName(),
                    name,
                    parameterTypes);
        }
    }

    /**
     * Represents a user parameter.
     */
    public static class Parameter {

        private final String name;

        private final Type type;

        private final Object value;

        /**
         * Creates a new instance.
         * @param name the parameter name
         * @param type the parameter type
         * @param value the parameter value ({@code nullable})
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Parameter(String name, Type type, Object value) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (type == null) {
                throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
            }
            this.name = name;
            this.type = type;
            this.value = value;
        }

        /**
         * Returns the parameter name.
         * @return the parameter name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the parameter type.
         * @return the parameter type
         */
        public Type getType() {
            return type;
        }

        /**
         * Returns the parameter value.
         * @return the parameter value, or {@code null} if the target value is just {@code null}
         */
        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0}[{1}]={2}", //$NON-NLS-1$
                    getName(),
                    getType(),
                    getValue());
        }
    }

    /**
     * A builder for building {@link OperatorDescription}.
     * @since 0.1.0
     * @version 0.5.1
     */
    public static class Builder {

        private FlowElementDescription origin;

        private final Class<? extends Annotation> annotationType;

        private Class<?> declaring;

        private Class<?> implementing;

        private String name;

        private final List<Class<?>> parameterTypes;

        private final List<FlowElementPortDescription> inputPorts;

        private final List<FlowElementPortDescription> outputPorts;

        private final List<FlowResourceDescription> resources;

        private final List<Parameter> parameters;

        private final List<FlowElementAttribute> attributes;

        /**
         * Creates a new instance.
         * @param annotationType the operator annotation type
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Builder(Class<? extends Annotation> annotationType) {
            if (annotationType == null) {
                throw new IllegalArgumentException("annotationType must not be null"); //$NON-NLS-1$
            }
            this.annotationType = annotationType;
            this.parameterTypes = new ArrayList<>();
            this.inputPorts = new ArrayList<>();
            this.outputPorts = new ArrayList<>();
            this.resources = new ArrayList<>();
            this.parameters = new ArrayList<>();
            this.attributes = new ArrayList<>();
        }

        /**
         * Sets information of operator method declaration.
         * Note that, clients should use {@link #declareParameter(Class)} to add method parameters.
         * @param operatorClass the operator class
         * @param implementorClass the implementation class
         * @param methodName the operator method name
         * @return this
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Builder declare(Class<?> operatorClass, Class<?> implementorClass, String methodName) {
            if (operatorClass == null) {
                throw new IllegalArgumentException("operatorClass must not be null"); //$NON-NLS-1$
            }
            if (implementorClass == null) {
                throw new IllegalArgumentException("implementorClass must not be null"); //$NON-NLS-1$
            }
            if (methodName == null) {
                throw new IllegalArgumentException("methodName must not be null"); //$NON-NLS-1$
            }
            if (this.declaring != null) {
                throw new IllegalStateException();
            }
            this.declaring = operatorClass;
            this.implementing = implementorClass;
            this.name = methodName;
            return this;
        }

        /**
         * Sets the original description for the building description.
         * @param origin the original description, or {@code null} if the building one is the origin
         */
        public void setOrigin(FlowElementDescription origin) {
            this.origin = origin;
        }

        /**
         * Adds a parameter of the target operator method.
         * @param parameterType the erased parameter type
         * @return this
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Builder declareParameter(Class<?> parameterType) {
            if (parameterType == null) {
                throw new IllegalArgumentException("parameterType must not be null"); //$NON-NLS-1$
            }
            this.parameterTypes.add(parameterType);
            return this;
        }

        /**
         * Adds a new input port of the target operator.
         * @param portName the port name
         * @param dataType the data type
         * @return this
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Builder addInput(String portName, Type dataType) {
            if (portName == null) {
                throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
            }
            if (dataType == null) {
                throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
            }
            inputPorts.add(new FlowElementPortDescription(
                    portName,
                    dataType,
                    PortDirection.INPUT));
            return this;
        }

        /**
         * Adds a new input port of the target operator.
         * @param portName the port name
         * @param typeReference a source that has the data type as same to the creating port
         * @return this
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Builder addInput(String portName, Source<?> typeReference) {
            if (portName == null) {
                throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
            }
            if (typeReference == null) {
                throw new IllegalArgumentException("typeReference must not be null"); //$NON-NLS-1$
            }
            return addInput(
                    portName,
                    typeReference.toOutputPort().getDescription().getDataType());
        }

        /**
         * Adds a new input port of the target operator.
         * @param portName the port name
         * @param dataType the data type
         * @param key information of the shuffle operation
         * @return this
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Builder addInput(String portName, Type dataType, ShuffleKey key) {
            if (portName == null) {
                throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
            }
            if (dataType == null) {
                throw new IllegalArgumentException("typeReference must not be null"); //$NON-NLS-1$
            }
            if (key == null) {
                throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
            }
            inputPorts.add(new FlowElementPortDescription(
                    portName,
                    dataType,
                    key));
            return this;
        }

        /**
         * Adds a new input port of the target operator.
         * @param portName the port name
         * @param typeReference a source that has the data type as same to the creating port
         * @param key information of the shuffle operation
         * @return this
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Builder addInput(String portName, Source<?> typeReference, ShuffleKey key) {
            if (portName == null) {
                throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
            }
            if (typeReference == null) {
                throw new IllegalArgumentException("typeReference must not be null"); //$NON-NLS-1$
            }
            if (key == null) {
                throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
            }
            return addInput(portName, typeReference.toOutputPort().getDescription().getDataType(), key);
        }

        /**
         * Adds a new output port of the target operator.
         * @param portName the port name
         * @param dataType the data type
         * @return this
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Builder addOutput(String portName, Type dataType) {
            if (portName == null) {
                throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
            }
            if (dataType == null) {
                throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
            }
            outputPorts.add(new FlowElementPortDescription(
                    portName,
                    dataType,
                    PortDirection.OUTPUT));
            return this;
        }

        /**
         * Adds a new output port of the target operator.
         * @param portName the port name
         * @param typeReference a source that has the data type as same to the creating port
         * @return this
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Builder addOutput(String portName, Source<?> typeReference) {
            if (portName == null) {
                throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
            }
            if (typeReference == null) {
                throw new IllegalArgumentException("typeReference must not be null"); //$NON-NLS-1$
            }
            return addOutput(portName, typeReference.toOutputPort().getDescription().getDataType());
        }

        /**
         * Adds a new external resource.
         * @param resource the external resource
         * @return this
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Builder addResource(FlowResourceDescription resource) {
            if (resource == null) {
                throw new IllegalArgumentException("resource must not be null"); //$NON-NLS-1$
            }
            resources.add(resource);
            return this;
        }

        /**
         * Adds a new user parameter.
         * @param parameterName the parameter name
         * @param parameterType the parameter value
         * @param argument the actual parameter argument, or {@code null} if the argument is just {@code null}
         * @return this
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Builder addParameter(String parameterName, Type parameterType, Object argument) {
            if (parameterName == null) {
                throw new IllegalArgumentException("parameterName must not be null"); //$NON-NLS-1$
            }
            if (parameterType == null) {
                throw new IllegalArgumentException("parameterType must not be null"); //$NON-NLS-1$
            }
            parameters.add(new Parameter(parameterName, parameterType, argument));
            return this;
        }

        /**
         * Adds an attribute.
         * @param attribute the attribute
         * @return this
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Builder addAttribute(FlowElementAttribute attribute) {
            if (attribute == null) {
                throw new IllegalArgumentException("attribute must not be null"); //$NON-NLS-1$
            }
            attributes.add(attribute);
            return this;
        }

        /**
         * Creates a new description object from the previously information.
         * @return the created description
         */
        public OperatorDescription toDescription() {
            return new OperatorDescription(
                    origin,
                    new Declaration(
                            annotationType,
                            declaring,
                            implementing,
                            name,
                            parameterTypes),
                    inputPorts,
                    outputPorts,
                    resources,
                    parameters,
                    attributes);
        }

        /**
         * Creates a new {@link FlowElementResolver} object from the previously information.
         * @return the created object
         */
        public FlowElementResolver toResolver() {
            return new FlowElementResolver(toDescription());
        }
    }
}
