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
import java.util.Optional;

import com.asakusafw.info.JobflowInfo;
import com.asakusafw.info.operator.OperatorSpec.OperatorKind;
import com.asakusafw.info.operator.view.OperatorGraphView;
import com.asakusafw.info.operator.view.OperatorView;
import com.asakusafw.info.plan.PlanVertexSpec;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.beust.jcommander.Parameter;

/**
 * Selects vertex.
 * @since 0.10.0
 */
public class VertexSelectorParameter {

    /**
     * The target vertex name.
     */
    @Parameter(
            names = { "--vertex" },
            description = "Target vertex name.",
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
     * Returns the target vertex.
     * @param jobflow the owner jobflow
     * @param plan the execution plan
     * @return the target vertex
     * @throws CommandConfigurationException if there is no such a vertex
     */
    public OperatorView select(JobflowInfo jobflow, OperatorGraphView plan) {
        if (name == null) {
            throw new IllegalStateException();
        }
        return plan.getOperators(OperatorKind.PLAN_VERTEX).stream()
                .filter(it -> ((PlanVertexSpec) it.getSpec()).getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new CommandConfigurationException(MessageFormat.format(
                        "there are no vertex named \"{1}\" in jobflow {0}",
                        Optional.ofNullable(jobflow.getDescriptionClass())
                            .orElse(jobflow.getId()),
                        name)));
    }
}
