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
package com.asakusafw.info.cli.draw;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.JobflowInfo;
import com.asakusafw.info.cli.common.FlowPartSelectorParameter;
import com.asakusafw.info.cli.common.HelpParameter;
import com.asakusafw.info.cli.common.JobflowInfoParameter;
import com.asakusafw.info.cli.common.OutputParameter;
import com.asakusafw.info.cli.draw.DrawEngine.Feature;
import com.asakusafw.info.operator.FlowOperatorSpec;
import com.asakusafw.info.operator.OperatorGraphAttribute;
import com.asakusafw.info.operator.view.OperatorGraphView;
import com.asakusafw.info.operator.view.OperatorView;
import com.asakusafw.info.value.ClassInfo;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for generating DOT script about operator graphs.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "operator",
        commandDescription = "Generates operator graph as Graphviz DOT script"
)
public class DrawOperatorCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(DrawOperatorCommand.class);

    @ParametersDelegate
    HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    JobflowInfoParameter jobflowInfoParameter = new JobflowInfoParameter();

    @ParametersDelegate
    ShowAllParameter verboseParameter = new ShowAllParameter();

    @ParametersDelegate
    FlowPartSelectorParameter flowPartSelectorParameter = new FlowPartSelectorParameter();

    @ParametersDelegate
    GraphvizParameter graphvizParameter = new GraphvizParameter();

    @ParametersDelegate
    OutputParameter outputParameter = new OutputParameter();

    @Parameter(
            names = { "--depth", },
            description = "Limit number of graph depth.",
            arity = 1,
            required = false)
    int limitDepth = Integer.MAX_VALUE;

    @Parameter(
            names = { "--show-argument", },
            description = "Displays operator arguments.",
            arity = 0,
            required = false)
    boolean showArgument = false;

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
            names = { "--show-key", },
            description = "Displays grouping key of operator ports.",
            arity = 0,
            required = false)
    boolean showPortKey = false;

    @Parameter(
            names = { "--show-type", },
            description = "Displays data type of operator ports.",
            arity = 0,
            required = false)
    boolean showPortType = false;

    @Override
    public void run() {
        try (PrintWriter writer = outputParameter.open()) {
            JobflowInfo jobflow = jobflowInfoParameter.getUniqueJobflow();
            OperatorGraphView graph = jobflow.findAttribute(OperatorGraphAttribute.class)
                    .map(OperatorGraphView::new)
                    .orElseThrow(() -> new IllegalStateException("there are no available operators"));
            List<String> label = new ArrayList<>();
            label.add(Optional.ofNullable(jobflow.getDescriptionClass())
                    .map(ClassInfo::of)
                    .map(ClassInfo::getSimpleName)
                    .orElse(jobflow.getId()));
            if (flowPartSelectorParameter.isEnabled()) {
                OperatorView fp = flowPartSelectorParameter.select(jobflow, graph);
                graph = fp.getElementGraph();
                FlowOperatorSpec spec = (FlowOperatorSpec) fp.getSpec();
                Optional.of(spec.getDescriptionClass())
                .map(ClassInfo::getSimpleName)
                .ifPresent(label::add);
            }
            Set<Feature> features = extractFeatures();
            DrawEngine engine = new DrawEngine(features);
            engine.draw(writer, graph, limitDepth, label, graphvizParameter.getOptions(), null);
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
        if (showPortName) {
            results.add(Feature.PORT_NAME);
        }
        if (showPortKey) {
            results.add(Feature.PORT_KEY);
        }
        if (showPortType) {
            results.add(Feature.PORT_TYPE);
        }
        return results;
    }
}
