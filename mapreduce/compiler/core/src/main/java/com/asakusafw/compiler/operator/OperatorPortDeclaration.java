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

import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;

/**
 * Structural information of operator ports.
 */
public class OperatorPortDeclaration {

    private final Kind kind;

    private final List<DocElement> documentation;

    private final String name;

    private final PortTypeDescription type;

    private final Integer position;

    private final ShuffleKey shuffleKey;

    /**
     * Creates a new instance.
     * @param kind the port kind
     * @param documentation the documentation of the port
     * @param name the port name
     * @param type the port type
     * @param position the declaring parameter index (nullable)
     * @param shuffleKey the shuffle key (nullable)
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public OperatorPortDeclaration(
            Kind kind,
            List<? extends DocElement> documentation,
            String name,
            PortTypeDescription type,
            Integer position,
            ShuffleKey shuffleKey) {
        Precondition.checkMustNotBeNull(kind, "kind"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(documentation, "documentation"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        this.kind = kind;
        this.documentation = Lists.freeze(documentation);
        this.name = name;
        this.type = type;
        this.position = position;
        this.shuffleKey = shuffleKey;
    }

    /**
     * Returns the parameter index in the declaring method of this port.
     * @return the parameter index, or {@code null} if the port is not derived from a method parameter
     */
    public Integer getParameterPosition() {
        return position;
    }

    /**
     * Returns the port kind.
     * @return the port kind
     */
    public Kind getKind() {
        return this.kind;
    }

    /**
     * Returns the documentation element of the target port.
     * @return the documentation element, or {@code null} if the target port does not have any documentation
     */
    public List<DocElement> getDocumentation() {
        return this.documentation;
    }

    /**
     * Returns the port name.
     * @return the port name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the port type.
     * @return the port type
     */
    public PortTypeDescription getType() {
        return this.type;
    }

    /**
     * Returns the shuffle key for the target port.
     * @return the shuffle key, or {@code null} if the target port does not have shuffle key information
     */
    public ShuffleKey getShuffleKey() {
        return shuffleKey;
    }

    /**
     * Represents kinds of operator port.
     */
    public enum Kind {

        /**
         * The input.
         */
        INPUT,

        /**
         * The output.
         */
        OUTPUT,

        /**
         * The user parameter.
         */
        CONSTANT,
    }
}
