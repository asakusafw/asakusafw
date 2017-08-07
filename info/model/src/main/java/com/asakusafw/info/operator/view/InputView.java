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

import com.asakusafw.info.graph.Input;
import com.asakusafw.info.operator.InputAttribute;
import com.asakusafw.info.operator.InputGranularity;
import com.asakusafw.info.operator.InputGroup;
import com.asakusafw.info.operator.InputInfo;
import com.asakusafw.info.value.ClassInfo;

/**
 * A view of operator inputs.
 * @since 0.9.2
 */
public class InputView implements InputInfo {

    private final OperatorGraphView container;

    private final Input entity;

    private final InputInfo info;

    InputView(OperatorGraphView container, Input entity) {
        this.container = container;
        this.entity = entity;
        this.info = Util.extract(entity, InputAttribute.class);
    }

    /**
     * Returns the entity element.
     * @return the entity element
     */
    public Input getEntity() {
        return entity;
    }

    /**
     * Returns the owner of this port.
     * @return the owner
     */
    public OperatorView getOwner() {
        return container.resolve(entity.getParent());
    }

    /**
     * Returns the opposite ports.
     * @return the opposite ports
     */
    public List<OutputView> getOpposites() {
        return entity.getOpposites().stream()
                .map(container::resolve)
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return info.getName();
    }

    @Override
    public ClassInfo getDataType() {
        return info.getDataType();
    }

    @Override
    public InputGranularity getGranulatity() {
        return info.getGranulatity();
    }

    @Override
    public InputGroup getGroup() {
        return info.getGroup();
    }

    @Override
    public String toString() {
        return info.toString();
    }
}
