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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.BatchInfo;
import com.asakusafw.info.JobflowInfo;
import com.asakusafw.info.cli.common.BatchInfoParameter;
import com.asakusafw.info.cli.common.HelpParameter;
import com.asakusafw.info.cli.common.OutputParameter;
import com.asakusafw.info.cli.draw.Drawer.Shape;
import com.asakusafw.info.graph.Node;
import com.asakusafw.info.task.TaskInfo;
import com.asakusafw.info.task.TaskListAttribute;
import com.asakusafw.info.value.ClassInfo;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for generating DOT script about jobflow graphs.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "jobflow",
        commandDescription = "Generates jobflow graph as Graphviz DOT script"
)
public class DrawJobflowCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(DrawJobflowCommand.class);

    @ParametersDelegate
    HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    BatchInfoParameter batchInfoParameter = new BatchInfoParameter();

    @ParametersDelegate
    ShowAllParameter verboseParameter = new ShowAllParameter();

    @ParametersDelegate
    GraphvizParameter graphvizParameter = new GraphvizParameter();

    @ParametersDelegate
    OutputParameter outputParameter = new OutputParameter();

    @Parameter(
            names = { "--show-task", },
            description = "Displays task graph.",
            arity = 0,
            required = false)
    boolean showTask = false;

    @Parameter(
            names = { "--show-type", },
            description = "Displays description type.",
            arity = 0,
            required = false)
    boolean showType = false;

    @Override
    public void run() {
        try (PrintWriter writer = outputParameter.open()) {
            BatchInfo info = batchInfoParameter.load();
            new Engine().draw(writer, info);
        }
    }

    private class Engine {

        private final Drawer drawer = new Drawer();

        Engine() {
            return;
        }

        void draw(PrintWriter writer, BatchInfo info) {
            Node root = new Node();
            addBatch(info, root);
            drawer.dump(writer, root, graphvizParameter.getOptions());
        }

        private void addBatch(BatchInfo info, Node node) {
            List<String> label = new ArrayList<>();
            if (verboseParameter.isRequired() || showType) {
                Optional.ofNullable(info.getDescriptionClass())
                    .map(ClassInfo::of)
                    .map(ClassInfo::getSimpleName)
                    .ifPresent(label::add);
            }
            label.add(info.getId());
            Map<String, Node> jobflows = info.getJobflows().stream()
                    .collect(Collectors.toMap(
                            JobflowInfo::getId,
                            it -> node.newElement()
                                .withInput(null)
                                .withOutput(null)
                                .configure(e -> addJobflow(it, e))));
            info.getJobflows().forEach(it -> it.getBlockerIds().stream()
                    .map(jobflows::get)
                    .filter(blocker -> blocker != null)
                    .forEach(blocker -> blocker.getOutput(0).connect(jobflows.get(it.getId()).getInput(0))));

            node.getWires().forEach(it -> drawer.connect(it.getSource(), it.getDestination()));
            drawer.add(node, Shape.GRAPH, label);
        }

        private void addJobflow(JobflowInfo info, Node node) {
            List<String> label = new ArrayList<>();
            if (verboseParameter.isRequired() || showType) {
                Optional.ofNullable(info.getDescriptionClass())
                    .map(ClassInfo::of)
                    .map(ClassInfo::getSimpleName)
                    .ifPresent(label::add);
            }
            label.add(info.getId());
            if (verboseParameter.isRequired() || showTask) {
                Map<TaskInfo.Phase, List<TaskInfo>> phases = info.findAttribute(TaskListAttribute.class)
                        .map(TaskListAttribute::getPhases)
                        .orElse(Collections.emptyMap());
                List<Node> elements = phases.entrySet().stream()
                        .map(e -> node.newElement()
                                .withInput(null)
                                .withOutput(null)
                                .configure(n -> addPhase(e.getKey(), e.getValue(), n)))
                        .collect(Collectors.toList());
                Node last = null;
                for (Node e : elements) {
                    if (last != null) {
                        last.getOutput(0).connect(e.getInput(0));
                    }
                    last = e;
                }
            }
            node.getWires().forEach(it -> drawer.connect(it.getSource(), it.getDestination()));
            drawer.add(node, Shape.GRAPH, label);
        }

        private void addPhase(TaskInfo.Phase phase, List<TaskInfo> tasks, Node node) {
            Map<String, Node> map = tasks.stream()
                    .collect(Collectors.toMap(
                            TaskInfo::getId,
                            it -> node.newElement()
                                .withInput(null)
                                .withOutput(null)
                                .configure(e -> addTask(it, e))));
            tasks.forEach(succ -> succ.getBlockers().stream()
                    .map(map::get)
                    .filter(pred -> pred != null)
                    .forEach(pred -> pred.getOutput(0).connect(map.get(succ.getId()).getInput(0))));
            if (map.size() >= 1) {
                Node begin = node.newElement().withOutput(null);
                drawer.add(begin, Shape.POINT, Collections.emptyList());

                Node end = node.newElement().withInput(null);
                drawer.add(end, Shape.POINT, Collections.emptyList());

                tasks.stream()
                    .filter(it -> it.getBlockers().isEmpty())
                    .map(TaskInfo::getId)
                    .map(map::get)
                    .forEach(it -> begin.getOutput(0).connect(it.getInput(0)));

                Set<String> blockers = tasks.stream()
                        .flatMap(it -> it.getBlockers().stream())
                        .collect(Collectors.toSet());
                tasks.stream()
                    .map(TaskInfo::getId)
                    .filter(it -> blockers.contains(it) == false)
                    .map(map::get)
                    .forEach(it -> it.getOutput(0).connect(end.getInput(0)));
            }

            node.getWires().forEach(it -> drawer.connect(it.getSource(), it.getDestination()));
            drawer.add(node, Shape.GRAPH, Collections.singletonList(phase.getSymbol()));
        }

        private void addTask(TaskInfo info, Node node) {
            List<String> label = new ArrayList<>();
            Optional.ofNullable(info.getModuleName()).ifPresent(label::add);
            Optional.ofNullable(info.getProfileName()).map(it -> String.format("@%s", it)).ifPresent(label::add);
            drawer.add(node, Shape.BOX, label);
        }
    }
}
