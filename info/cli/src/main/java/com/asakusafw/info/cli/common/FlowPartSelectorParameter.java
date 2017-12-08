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
package com.asakusafw.info.cli.common;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import com.asakusafw.info.JobflowInfo;
import com.asakusafw.info.operator.FlowOperatorSpec;
import com.asakusafw.info.operator.OperatorSpec.OperatorKind;
import com.asakusafw.info.operator.view.OperatorGraphView;
import com.asakusafw.info.operator.view.OperatorView;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Selects flow-part.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.info.cli.jcommander")
public class FlowPartSelectorParameter {

    /**
     * The target flow-part class name.
     */
    @Parameter(
            names = { "--flow-part", "--flowpart" },
            descriptionKey = "parameter.flow-part-class",
            required = false
    )
    public String name;

    /**
     * Returns whether or not the selector is enabled.
     * @return {@code true} if the selector is enabled, otherwise {@code false}
     */
    public boolean isEnabled() {
        return name != null;
    }

    /**
     * Returns the target flow-part operator view.
     * @param jobflow the owner jobflow
     * @param graph the target graph
     * @return the target operator view
     * @throws CommandConfigurationException if there is no such a flow-part
     */
    public OperatorView select(JobflowInfo jobflow, OperatorGraphView graph) {
        if (name == null) {
            throw new IllegalStateException();
        }
        return findFlowPart(graph, name)
                .orElseThrow(() -> new CommandConfigurationException(MessageFormat.format(
                        "there are no flow part named \"{1}\" in jobflow {0}",
                        Optional.ofNullable(jobflow.getDescriptionClass())
                            .orElse(jobflow.getId()),
                        name)));
    }

    private static Optional<OperatorView> findFlowPart(OperatorGraphView graph, String name) {
        Queue<OperatorView> queue = new LinkedList<>();
        getFlowParts(graph).forEach(queue::offer);
        while (queue.isEmpty() == false) {
            OperatorView next = queue.poll();
            assert next.getSpec() instanceof FlowOperatorSpec;
            if (Optional.ofNullable(((FlowOperatorSpec) next.getSpec()).getDescriptionClass())
                    .filter(it -> name.equals(it.getClassName()) || name.equals(it.getSimpleName()))
                    .isPresent()) {
                return Optional.of(next);
            } else {
                getFlowParts(next.getElementGraph()).forEach(queue::offer);
            }
        }
        return Optional.empty();
    }

    private static Stream<OperatorView> getFlowParts(OperatorGraphView graph) {
        return graph.getOperators(OperatorKind.FLOW).stream();
    }
}
