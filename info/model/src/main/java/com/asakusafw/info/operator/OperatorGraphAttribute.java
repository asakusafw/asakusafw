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
package com.asakusafw.info.operator;

import java.util.Objects;

import com.asakusafw.info.Attribute;
import com.asakusafw.info.graph.Node;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An {@link Attribute} which represents details of operator graphs.
 * @since 0.9.2
 */
public class OperatorGraphAttribute implements Attribute {

    static final String ID = "operator-graph";

    private final Node root;

    /**
     * Creates a new instance.
     * @param root the root node
     */
    public OperatorGraphAttribute(Node root) {
        this.root = root;
    }

    @JsonCreator
    static OperatorGraphAttribute restore(
            @JsonProperty(Constants.ID_ID) String id,
            @JsonProperty(Constants.ID_ROOT) Node root) {
        if (Objects.equals(id, ID) == false) {
            throw new IllegalArgumentException();
        }
        return new OperatorGraphAttribute(root);
    }

    @Override
    public String getId() {
        return ID;
    }

    /**
     * Returns the root node.
     * @return the root node
     */
    public Node getRoot() {
        return root;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(root);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(root, ((OperatorGraphAttribute) obj).root);
    }
}
