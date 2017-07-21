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
package com.asakusafw.info.operator.view;

import java.util.List;
import java.util.stream.Collectors;

import com.asakusafw.info.graph.Node;
import com.asakusafw.info.operator.OperatorAttribute;
import com.asakusafw.info.operator.OperatorInfo;
import com.asakusafw.info.operator.OperatorSpec;
import com.asakusafw.info.operator.ParameterInfo;

/**
 * A view of operator operators.
 * @since 0.9.2
 */
public class OperatorView implements OperatorInfo {

    private final OperatorGraphView container;

    private final Node entity;

    private final OperatorInfo info;

    private OperatorGraphView nest;

    OperatorView(OperatorGraphView container, Node entity) {
        this.container = container;
        this.entity = entity;
        this.info = Util.extract(entity, OperatorAttribute.class);
    }

    /**
     * Returns the entity element.
     * @return the entity element
     */
    public Node getEntity() {
        return entity;
    }

    @Override
    public OperatorSpec getSpec() {
        return info.getSpec();
    }

    /**
     * Returns views of input ports.
     * @return the input views
     */
    public List<InputView> getInputs() {
        return entity.getInputs().stream()
                .map(container::resolve)
                .collect(Collectors.toList());
    }

    /**
     * Returns views of output ports.
     * @return the output views
     */
    public List<OutputView> getOutputs() {
        return entity.getOutputs().stream()
                .map(container::resolve)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParameterInfo> getParameters() {
        return info.getParameters();
    }

    /**
     * Returns {@link OperatorGraphView} of this operator.
     * @return the flow view
     */
    public OperatorGraphView getElementGraph() {
        if (nest == null) {
            nest = new OperatorGraphView(entity);
        }
        return nest;
    }

    @Override
    public String toString() {
        return info.toString();
    }
}
