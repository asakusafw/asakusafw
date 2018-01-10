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
package com.asakusafw.info.cli.generate.dot;

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
import com.asakusafw.info.cli.common.JobflowInfoParameter;
import com.asakusafw.info.cli.generate.dot.DrawEngine.Feature;
import com.asakusafw.info.operator.FlowOperatorSpec;
import com.asakusafw.info.operator.OperatorGraphAttribute;
import com.asakusafw.info.operator.view.OperatorGraphView;
import com.asakusafw.info.operator.view.OperatorView;
import com.asakusafw.info.value.ClassInfo;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for generating DOT script about operator graphs.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "operator",
        commandDescriptionKey = "command.generate-dot-operator",
        resourceBundle = "com.asakusafw.info.cli.jcommander"
)
public class DrawOperatorCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(DrawOperatorCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final JobflowInfoParameter jobflowInfoParameter = new JobflowInfoParameter();

    @ParametersDelegate
    final ShowAllParameter verboseParameter = new ShowAllParameter();

    @ParametersDelegate
    final FlowPartSelectorParameter flowPartSelectorParameter = new FlowPartSelectorParameter();

    @ParametersDelegate
    final DotParameter graphvizParameter = new DotParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @Parameter(
            names = { "--depth", },
            descriptionKey = "parameter.depth",
            arity = 1,
            required = false)
    int limitDepth = Integer.MAX_VALUE;

    @Parameter(
            names = { "--show-argument", },
            descriptionKey = "parameter.show-argument",
            arity = 0,
            required = false)
    boolean showArgument = false;

    @Parameter(
            names = { "--show-io", },
            descriptionKey = "parameter.show-io-class",
            arity = 0,
            required = false)
    boolean showExternalIo = false;

    @Parameter(
            names = { "--show-name", },
            descriptionKey = "parameter.show-operator-port-name",
            arity = 0,
            required = false)
    boolean showPortName = false;

    @Parameter(
            names = { "--show-key", },
            descriptionKey = "parameter.show-group-key",
            arity = 0,
            required = false)
    boolean showPortKey = false;

    @Parameter(
            names = { "--show-type", },
            descriptionKey = "parameter.show-data-type",
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
