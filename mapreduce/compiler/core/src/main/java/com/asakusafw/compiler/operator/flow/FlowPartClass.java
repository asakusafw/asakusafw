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

import java.util.List;

import javax.lang.model.element.TypeElement;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.OperatorPortDeclaration;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.DocElement;

/**
 * Structural information of flow-part classes.
 */
public class FlowPartClass {

    private final TypeElement element;

    private final List<DocElement> documentation;

    private final List<OperatorPortDeclaration> inputPorts;

    private final List<OperatorPortDeclaration> outputPorts;

    private final List<OperatorPortDeclaration> parameters;

    /**
     * Creates a new instance.
     * @param element the declaration of this flow-part
     * @param documentation the documentation of the operator
     * @param inputPorts the input ports
     * @param outputPorts the output ports
     * @param parameters the user parameters
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public FlowPartClass(
            TypeElement element,
            List<? extends DocElement> documentation,
            List<OperatorPortDeclaration> inputPorts,
            List<OperatorPortDeclaration> outputPorts,
            List<OperatorPortDeclaration> parameters) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(documentation, "documentation"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(inputPorts, "inputPorts"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputPorts, "outputPorts"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(parameters, "parameters"); //$NON-NLS-1$
        this.element = element;
        this.documentation = Lists.from(documentation);
        this.inputPorts = Lists.from(inputPorts);
        this.outputPorts = Lists.from(outputPorts);
        this.parameters = Lists.from(parameters);
    }

    /**
     *
     * Returns the corresponded flow-part class.
     * @return the corresponded flow-part class
     */
    public TypeElement getElement() {
        return element;
    }

    /**
     * Returns the documentation of this flow-part.
     * @return the documentation of this flow-part
     */
    public List<DocElement> getDocumentation() {
        return documentation;
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
}
