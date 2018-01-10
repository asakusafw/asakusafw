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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.asakusafw.info.cli.generate.dot.Drawer.Shape;
import com.asakusafw.info.operator.CoreOperatorSpec;
import com.asakusafw.info.operator.CustomOperatorSpec;
import com.asakusafw.info.operator.FlowOperatorSpec;
import com.asakusafw.info.operator.InputGranularity;
import com.asakusafw.info.operator.InputGroup;
import com.asakusafw.info.operator.InputOperatorSpec;
import com.asakusafw.info.operator.MarkerOperatorSpec;
import com.asakusafw.info.operator.OperatorSpec.OperatorKind;
import com.asakusafw.info.operator.OutputOperatorSpec;
import com.asakusafw.info.operator.UserOperatorSpec;
import com.asakusafw.info.operator.view.InputView;
import com.asakusafw.info.operator.view.OperatorGraphView;
import com.asakusafw.info.operator.view.OperatorView;
import com.asakusafw.info.operator.view.OutputView;
import com.asakusafw.info.plan.PlanInputSpec;
import com.asakusafw.info.plan.PlanOutputSpec;
import com.asakusafw.info.plan.PlanVertexSpec;
import com.asakusafw.info.value.ClassInfo;

class DrawEngine {

    private final Set<Feature> features;

    private final Drawer drawer = new Drawer();

    private final boolean showPort;

    DrawEngine(Collection<Feature> features) {
        this.features = EnumSet.noneOf(Feature.class);
        this.features.addAll(features);
        this.showPort = features.stream().anyMatch(it -> it.showPort);
    }

    void draw(
            PrintWriter writer, OperatorGraphView root,
            int limitDepth, List<String> label,
            Map<String, ?> options,
            Consumer<? super Drawer> extension) {
        drawer.add(root.getRoot(), Shape.GRAPH, label);
        analyzeGraph(root, 1, limitDepth);
        Optional.ofNullable(extension).ifPresent(it -> it.accept(drawer));
        drawer.dump(writer, root.getRoot(), options);
    }

    private void analyzeGraph(OperatorGraphView graph, int currentDepth, int limitDepth) {
        for (OperatorView operator : graph.getOperators()) {
            if (operator.getSpec().getOperatorKind() == OperatorKind.FLOW
                    && currentDepth < limitDepth) {
                analyzeFlowGraph(operator, (FlowOperatorSpec) operator.getSpec());
                analyzeGraph(operator.getElementGraph(), currentDepth + 1, limitDepth);
            } else if (operator.getSpec().getOperatorKind() == OperatorKind.PLAN_VERTEX
                    && operator.getElementGraph().getOperators().isEmpty() == false
                    && currentDepth < limitDepth) {
                analyzePlanVertex(operator, (PlanVertexSpec) operator.getSpec());
                analyzeGraph(operator.getElementGraph(), currentDepth + 1, limitDepth);
            } else {
                analyzeOperator(operator);
            }
            for (OutputView upstream : operator.getOutputs()) {
                for (InputView downstream : upstream.getOpposites()) {
                    drawer.connect(upstream.getEntity(), downstream.getEntity());
                }
            }
        }
        Map<String, OperatorView> vertices = graph.getOperators(OperatorKind.PLAN_VERTEX).stream()
                .collect(Collectors.toMap(
                        it -> ((PlanVertexSpec) it.getSpec()).getName(),
                        Function.identity()));
        vertices.values().forEach(downstream -> ((PlanVertexSpec) downstream.getSpec()).getDependencies().stream()
                .map(vertices::get)
                .filter(it -> it != null)
                .forEach(upstream -> drawer.connect(upstream.getEntity(), downstream.getEntity())));
    }

    private void analyzeFlowGraph(OperatorView operator, FlowOperatorSpec spec) {
        drawer.add(operator.getEntity(), Shape.GRAPH, Optional.ofNullable(spec.getDescriptionClass())
                .map(ClassInfo::getSimpleName));

        OperatorGraphView graph = operator.getElementGraph();
        Map<String, OperatorView> inputs = graph.getOperatorMap(OperatorKind.INPUT);
        operator.getInputs().forEach(it -> drawer.redirect(it.getEntity(), inputs.get(it.getName()).getEntity()));
        Map<String, OperatorView> outputs = graph.getOperatorMap(OperatorKind.OUTPUT);
        operator.getOutputs().forEach(it -> drawer.redirect(it.getEntity(), outputs.get(it.getName()).getEntity()));
    }

    private void analyzePlanVertex(OperatorView operator, PlanVertexSpec spec) {
        drawer.add(operator.getEntity(), Shape.GRAPH, spec.getName());

        OperatorGraphView graph = operator.getElementGraph();
        Map<String, OperatorView> inputs = graph.getOperatorMap(OperatorKind.PLAN_INPUT);
        operator.getInputs().forEach(it -> drawer.redirect(it.getEntity(), inputs.get(it.getName()).getEntity()));
        Map<String, OperatorView> outputs = graph.getOperatorMap(OperatorKind.PLAN_OUTPUT);
        operator.getOutputs().forEach(it -> drawer.redirect(it.getEntity(), outputs.get(it.getName()).getEntity()));
    }

    private void analyzeOperator(OperatorView operator) {
        switch (operator.getSpec().getOperatorKind()) {
        case CORE:
            addCoreVertex(operator, (CoreOperatorSpec) operator.getSpec());
            break;
        case USER:
            addUserVertex(operator, (UserOperatorSpec) operator.getSpec());
            break;
        case INPUT:
            addInputVertex(operator, (InputOperatorSpec) operator.getSpec());
            break;
        case OUTPUT:
            addOutputVertex(operator, (OutputOperatorSpec) operator.getSpec());
            break;
        case FLOW:
            addFlowVertex(operator, (FlowOperatorSpec) operator.getSpec());
            break;
        case MARKER:
            addMarkerVertex(operator, (MarkerOperatorSpec) operator.getSpec());
            break;
        case CUSTOM:
            addCustomVertex(operator, (CustomOperatorSpec) operator.getSpec());
            break;
        case PLAN_VERTEX:
            addPlanVertex(operator, (PlanVertexSpec) operator.getSpec());
            break;
        case PLAN_INPUT:
            addPlanInput(operator, (PlanInputSpec) operator.getSpec());
            break;
        case PLAN_OUTPUT:
            addPlanOutput(operator, (PlanOutputSpec) operator.getSpec());
            break;
        default:
            throw new AssertionError();
        }
    }

    private void addCoreVertex(OperatorView operator, CoreOperatorSpec spec) {
        List<String> body = new ArrayList<>();
        body.add('@' + spec.getCategory().getAnnotationType().getSimpleName());
        body.addAll(analyzeBody(operator));
        if (showPort) {
            analyzeOperatorAsRecord(operator, body);
        } else {
            drawer.add(operator.getEntity(), Shape.BOX, body);
        }
    }

    private void addUserVertex(OperatorView operator, UserOperatorSpec spec) {
        List<String> body = new ArrayList<>();
        body.add('@' + spec.getAnnotation().getDeclaringClass().getSimpleName());
        body.add(spec.getDeclaringClass().getSimpleName());
        body.add(spec.getMethodName());
        body.addAll(analyzeBody(operator));
        if (showPort) {
            analyzeOperatorAsRecord(operator, body);
        } else {
            drawer.add(operator.getEntity(), Shape.BOX, body);
        }
    }

    private void addInputVertex(OperatorView operator, InputOperatorSpec spec) {
        List<String> body = new ArrayList<>();
        Optional.ofNullable(spec.getDescriptionClass())
            .ifPresent(it -> body.add("@Import"));
        body.add(spec.getName());
        if (features.contains(Feature.PORT_TYPE)) {
            operator.getOutputs().stream()
                .findAny()
                .map(OutputView::getDataType)
                .map(ClassInfo::getSimpleName)
                .ifPresent(body::add);
        }
        if (features.contains(Feature.EXTERNAL_IO_CLASS)) {
            Optional.ofNullable(spec.getDescriptionClass())
                .map(ClassInfo::getSimpleName)
                .ifPresent(body::add);
        }
        body.addAll(analyzeBody(operator));
        drawer.add(operator.getEntity(), Shape.INPUT, body);
    }

    private void addOutputVertex(OperatorView operator, OutputOperatorSpec spec) {
        List<String> body = new ArrayList<>();
        Optional.ofNullable(spec.getDescriptionClass())
            .ifPresent(it -> body.add("@Export"));
        body.add(spec.getName());
        if (features.contains(Feature.PORT_TYPE)) {
            operator.getInputs().stream()
                .findAny()
                .map(InputView::getDataType)
                .map(ClassInfo::getSimpleName)
                .ifPresent(body::add);
        }
        if (features.contains(Feature.EXTERNAL_IO_CLASS)) {
            Optional.ofNullable(spec.getDescriptionClass())
                .map(ClassInfo::getSimpleName)
                .ifPresent(body::add);
        }
        body.addAll(analyzeBody(operator));
        drawer.add(operator.getEntity(), Shape.OUTPUT, body);
    }

    private void addFlowVertex(OperatorView operator, FlowOperatorSpec spec) {
        List<String> body = new ArrayList<>();
        body.add("@FlowPart");
        Optional.ofNullable(spec.getDescriptionClass())
            .map(ClassInfo::getSimpleName)
            .ifPresent(body::add);
        body.addAll(analyzeBody(operator));
        if (showPort) {
            analyzeOperatorAsRecord(operator, body);
        } else {
            drawer.add(operator.getEntity(), Shape.BOX, body);
        }
    }

    private void addMarkerVertex(OperatorView operator, MarkerOperatorSpec spec) {
        drawer.add(operator.getEntity(), Shape.POINT, Collections.emptyList());
    }

    private void addCustomVertex(OperatorView operator, CustomOperatorSpec spec) {
        List<String> body = new ArrayList<>();
        body.add(spec.getCategory());
        body.addAll(analyzeBody(operator));
        if (showPort) {
            analyzeOperatorAsRecord(operator, body);
        } else {
            drawer.add(operator.getEntity(), Shape.ROUNDED_BOX, body);
        }
    }

    private void addPlanVertex(OperatorView operator, PlanVertexSpec spec) {
        List<String> body = new ArrayList<>();
        body.add(spec.getName());
        Optional.ofNullable(spec.getLabel()).ifPresent(body::add);
        drawer.add(operator.getEntity(), Shape.ROUNDED_BOX, body);
    }

    private void addPlanInput(OperatorView operator, PlanInputSpec spec) {
        List<String> body = analyzePlanInput(operator, spec);
        body.addAll(analyzeBody(operator));
        drawer.add(operator.getEntity(), Shape.ROUNDED_BOX, body);
    }

    private List<String> analyzePlanInput(OperatorView operator, PlanInputSpec spec) {
        List<String> body = new ArrayList<>();
        body.add(spec.getExchange().toString());
        if (features.contains(Feature.EDGE_TYPE)) {
            operator.getInputs().stream()
                .findAny()
                .map(InputView::getDataType)
                .map(ClassInfo::getSimpleName)
                .ifPresent(body::add);
        }
        if (features.contains(Feature.EDGE_KEY)) {
            Optional.ofNullable(spec.getGroup())
                .map(InputGroup::toString)
                .ifPresent(body::add);
        }
        return body;
    }

    private void addPlanOutput(OperatorView operator, PlanOutputSpec spec) {
        List<String> body = analyzePlanOutput(operator, spec);
        body.addAll(analyzeBody(operator));
        drawer.add(operator.getEntity(), Shape.ROUNDED_BOX, body);
    }

    private List<String> analyzePlanOutput(OperatorView operator, PlanOutputSpec spec) {
        List<String> body = new ArrayList<>();
        body.add(spec.getExchange().toString());
        if (features.contains(Feature.EDGE_OPERATION)) {
            body.addAll(spec.getExtraOperations());
        }
        if (features.contains(Feature.EDGE_TYPE)) {
            operator.getInputs().stream()
                .findAny()
                .map(InputView::getDataType)
                .map(ClassInfo::getSimpleName)
                .ifPresent(body::add);
        }
        if (features.contains(Feature.EDGE_KEY)) {
            Optional.ofNullable(spec.getGroup())
                .map(InputGroup::toString)
                .ifPresent(body::add);
        }
        return body;
    }

    private void analyzeOperatorAsRecord(OperatorView operator, List<String> body) {
        drawer.add(operator.getEntity(), Shape.RECORD, body);
        operator.getInputs().forEach(this::analyzeMemberInput);
        operator.getOutputs().forEach(this::analyzeMemberOutput);
    }

    private List<String> analyzeBody(OperatorView operator) {
        List<String> results = new ArrayList<>();
        if (features.contains(Feature.ARGUMENT)) {
            operator.getParameters().stream()
                .map(it -> String.format("%s: %s", it.getName(), it.getValue().getObject()))
                .forEachOrdered(results::add);
        }
        return results;
    }

    private void analyzeMemberInput(InputView port) {
        List<String> results = new ArrayList<>();
        if (features.contains(Feature.PORT_NAME)) {
            results.add(port.getName());
        }
        if (features.contains(Feature.PORT_TYPE)) {
            results.add(port.getDataType().getSimpleName());
        }
        if (features.contains(Feature.PORT_KEY)) {
            Optional.ofNullable(port.getGranulatity())
                .map(InputGranularity::toString)
                .ifPresent(results::add);
            Optional.ofNullable(port.getGroup())
                .map(InputGroup::toString)
                .ifPresent(results::add);
        }
        drawer.add(port.getEntity(), results);
    }

    private void analyzeMemberOutput(OutputView port) {
        List<String> results = new ArrayList<>();
        if (features.contains(Feature.PORT_NAME)) {
            results.add(port.getName());
        }
        if (features.contains(Feature.PORT_TYPE)) {
            results.add(port.getDataType().getSimpleName());
        }
        drawer.add(port.getEntity(), results);
    }

    enum Feature {

        ARGUMENT(false),

        EXTERNAL_IO_CLASS(false),

        PORT_NAME(true),

        PORT_TYPE(true),

        PORT_KEY(true),

        EDGE_OPERATION(false),

        EDGE_TYPE(false),

        EDGE_KEY(false),
        ;

        final boolean showPort;

        Feature(boolean showPort) {
            this.showPort = showPort;
        }
    }
}
