/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.info.cli.list;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.JobflowInfo;
import com.asakusafw.info.cli.common.JobflowInfoParameter;
import com.asakusafw.info.cli.common.VertexSelectorParameter;
import com.asakusafw.info.operator.OperatorSpec.OperatorKind;
import com.asakusafw.info.operator.UserOperatorSpec;
import com.asakusafw.info.operator.view.OperatorGraphView;
import com.asakusafw.info.operator.view.OperatorView;
import com.asakusafw.info.operator.view.OutputView;
import com.asakusafw.info.plan.PlanAttribute;
import com.asakusafw.info.plan.PlanVertexSpec;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing execution plan.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "plan",
        commandDescriptionKey = "command.generate-list-plan",
        resourceBundle = "com.asakusafw.info.cli.jcommander"
)
public class ListPlanCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(ListPlanCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final JobflowInfoParameter jobflowInfoParameter = new JobflowInfoParameter();

    @ParametersDelegate
    final VertexSelectorParameter vertexSelectorParameter = new VertexSelectorParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());
        try (PrintWriter writer = outputParameter.open()) {
            JobflowInfo jobflow = jobflowInfoParameter.getUniqueJobflow();
            OperatorGraphView plan = jobflow.findAttribute(PlanAttribute.class)
                    .map(OperatorGraphView::new)
                    .filter(it -> it.getOperators(OperatorKind.PLAN_VERTEX).isEmpty() == false)
                    .orElseThrow(() -> new CommandConfigurationException("there are no available execution plans"));
            if (vertexSelectorParameter.isEnabled()) {
                OperatorView target = vertexSelectorParameter.select(jobflow, plan);
                printVertex(writer, 0, target);
            } else {
                plan.getOperators(OperatorKind.PLAN_VERTEX).stream()
                        .sorted(Comparator.comparing((OperatorView it) -> ((PlanVertexSpec) it.getSpec()).getName()))
                        .forEach(it -> {
                            PlanVertexSpec spec = (PlanVertexSpec) it.getSpec();
                            if (verboseParameter.isRequired()) {
                                writer.printf("%s:%n", spec.getName());
                                printVertex(writer, 4, it);
                            } else {
                                writer.println(spec.getName());
                            }
                        });
            }
        }
    }

    private static void printVertex(PrintWriter writer, int indent, OperatorView vertex) {
        PlanVertexSpec spec = (PlanVertexSpec) vertex.getSpec();
        writer.printf("%slabel: %s%n", ListUtil.padding(indent), ListUtil.normalize(spec.getLabel()));
        writer.printf("%sblockers: %s%n", ListUtil.padding(indent), Stream.concat(
                    spec.getDependencies().stream(),
                    vertex.getInputs().stream()
                        .flatMap(p -> p.getOpposites().stream())
                        .map(OutputView::getOwner)
                        .filter(v -> v.getSpec().getOperatorKind() == OperatorKind.PLAN_VERTEX)
                        .map(v -> ((PlanVertexSpec) v.getSpec()).getName()))
                .sorted()
                .distinct()
                .collect(Collectors.joining(", ", "{", "}")));
        ListUtil.printBlock(writer, indent, "operators", vertex.getElementGraph()
                .getOperators(OperatorKind.USER)
                .stream()
                .map(op -> (UserOperatorSpec) op.getSpec())
                .map(it -> String.format("%s#%s(@%s)",
                        it.getDeclaringClass().getName(),
                        it.getMethodName(),
                        it.getAnnotation().getDeclaringClass().getSimpleName()))
                .distinct()
                .collect(Collectors.toList()));
    }
}
