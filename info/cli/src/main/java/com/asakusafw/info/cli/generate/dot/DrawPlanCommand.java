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
package com.asakusafw.info.cli.generate.dot;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.JobflowInfo;
import com.asakusafw.info.cli.common.JobflowInfoParameter;
import com.asakusafw.info.cli.common.VertexSelectorParameter;
import com.asakusafw.info.cli.generate.dot.DrawEngine.Feature;
import com.asakusafw.info.operator.view.OperatorGraphView;
import com.asakusafw.info.operator.view.OperatorView;
import com.asakusafw.info.plan.PlanAttribute;
import com.asakusafw.info.plan.PlanVertexSpec;
import com.asakusafw.info.value.ClassInfo;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for generating DOT script about execution plan graphs.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "plan",
        commandDescription = "Generates execution plan graph as Graphviz DOT script"
)
public class DrawPlanCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(DrawPlanCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final JobflowInfoParameter jobflowInfoParameter = new JobflowInfoParameter();

    @ParametersDelegate
    final ShowAllParameter verboseParameter = new ShowAllParameter();

    @ParametersDelegate
    final VertexSelectorParameter vertexSelectorParameter = new VertexSelectorParameter();

    @ParametersDelegate
    final DotParameter graphvizParameter = new DotParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @Parameter(
            names = { "--show-operator", },
            description = "Displays operators in each vertice.",
            arity = 0,
            required = false)
    boolean showOperator = false;

    @Parameter(
            names = { "--show-argument", },
            description = "Displays operator arguments.",
            arity = 0,
            required = false)
    boolean showArgument = false;

    @Parameter(
            names = { "--show-edge-operation", },
            description = "Displays operations on edges.",
            arity = 0,
            required = false)
    boolean showEdgeOperation = false;

    @Parameter(
            names = { "--show-io", },
            description = "Displays external I/O class names.",
            arity = 0,
            required = false)
    boolean showExternalIo = false;

    @Parameter(
            names = { "--show-name", },
            description = "Displays operator port names.",
            arity = 0,
            required = false)
    boolean showPortName = false;

    @Parameter(
            names = { "--show-type", },
            description = "Displays data types.",
            arity = 0,
            required = false)
    boolean showType = false;

    @Parameter(
            names = { "--show-key", },
            description = "Displays grouping keys.",
            arity = 0,
            required = false)
    boolean showKey = false;

    @Override
    public void run() {
        try (PrintWriter writer = outputParameter.open()) {
            JobflowInfo jobflow = jobflowInfoParameter.getUniqueJobflow();
            OperatorGraphView graph = jobflow.findAttribute(PlanAttribute.class)
                    .map(OperatorGraphView::new)
                    .orElseThrow(() -> new CommandConfigurationException("there are no available execution plans"));
            List<String> label = new ArrayList<>();
            label.add(Optional.ofNullable(jobflow.getDescriptionClass())
                    .map(ClassInfo::of)
                    .map(ClassInfo::getSimpleName)
                    .orElse(jobflow.getId()));
            int depth;
            if (vertexSelectorParameter.isEnabled()) {
                OperatorView v = vertexSelectorParameter.select(jobflow, graph);
                graph = v.getElementGraph();
                if (graph.getOperators().isEmpty()) {
                    throw new CommandConfigurationException(MessageFormat.format(
                            "there are no available operators in vertex \"{1}\" in jobflow {0}",
                            Optional.ofNullable(jobflow.getDescriptionClass())
                            .orElse(jobflow.getId()),
                            ((PlanVertexSpec) v.getSpec()).getName()));
                }
                PlanVertexSpec spec = (PlanVertexSpec) v.getSpec();
                label.add(spec.getName());
                depth = 1;
            } else  {
                depth = showOperator
                        || showArgument
                        || showExternalIo
                        || showPortName
                        || showType
                        || showKey
                        || verboseParameter.isRequired() ? 2 : 1;
            }
            Set<Feature> features = extractFeatures();
            DrawEngine engine = new DrawEngine(features);
            engine.draw(writer, graph, depth, label, graphvizParameter.getOptions(), null);
        }
    }

    private Set<Feature> extractFeatures() {
        if (verboseParameter.isRequired()) {
            return EnumSet.allOf(Feature.class);
        }
        Set<DrawEngine.Feature> results = EnumSet.noneOf(Feature.class);
        if (showArgument) {
            results.add(Feature.ARGUMENT);
        }
        if (showExternalIo) {
            results.add(Feature.EXTERNAL_IO_CLASS);
        }
        if (showEdgeOperation) {
            results.add(Feature.EDGE_OPERATION);
        }
        if (showPortName) {
            results.add(Feature.PORT_NAME);
        }
        if (showType) {
            results.add(Feature.PORT_TYPE);
            results.add(Feature.EDGE_TYPE);
        }
        if (showKey) {
            results.add(Feature.PORT_KEY);
            results.add(Feature.EDGE_KEY);
        }
        return results;
    }
}
