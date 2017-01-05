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
package com.asakusafw.compiler.trace;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.runtime.trace.DefaultTraceOperator;
import com.asakusafw.trace.model.TraceSetting;
import com.asakusafw.trace.model.TraceSetting.Mode;
import com.asakusafw.trace.model.Tracepoint;
import com.asakusafw.trace.model.Tracepoint.PortKind;
import com.asakusafw.vocabulary.flow.graph.Connectivity;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementPort;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.PortConnection;
import com.asakusafw.vocabulary.operator.Trace;

/**
 * Weaves trace operators into trace-points.
 * @since 0.5.1
 */
public class TracepointWeaver {

    /**
     * The input port name of trace operators.
     */
    public static final String INPUT_PORT_NAME = "in"; //$NON-NLS-1$

    /**
     * The output port name of trace operators.
     */
    public static final String OUTPUT_PORT_NAME = "out"; //$NON-NLS-1$

    private static final String FLOWPART_FACTORY_NAME = "create"; //$NON-NLS-1$

    static final Logger LOG = LoggerFactory.getLogger(TracepointWeaver.class);

    private final Map<String, Map<Tracepoint, TraceSetting>> tracepointsByOperatorClass;

    private final Map<Tracepoint, TraceSetting> rest;

    /**
     * Creates a new instance.
     * @param settings target settings
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TracepointWeaver(Collection<? extends TraceSetting> settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings must not be null"); //$NON-NLS-1$
        }
        HashMap<Tracepoint, TraceSetting> all = new HashMap<>();
        Map<String, Map<Tracepoint, TraceSetting>> map = new HashMap<>();
        for (TraceSetting setting : settings) {
            all.put(setting.getTracepoint(), setting);
            Map<Tracepoint, TraceSetting> entry = map.get(setting.getTracepoint().getOperatorClassName());
            if (entry == null) {
                entry = all;
                map.put(setting.getTracepoint().getOperatorClassName(), entry);
            }
            entry.put(setting.getTracepoint(), setting);
        }
        this.tracepointsByOperatorClass = map;
        this.rest = all;
    }

    /**
     * Weaves trace-points into {@link FlowElement}.
     * @param element the target element
     * @return {@code true} if the target element is modified, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean edit(FlowElement element) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null"); //$NON-NLS-1$
        }
        FlowElementDescription description = element.getDescription();
        switch (description.getKind()) {
        case OPERATOR:
            return edit(element, (OperatorDescription) description);
        case FLOW_COMPONENT:
            return edit(element, (FlowPartDescription) description);
        default:
            return false;
        }
    }

    private boolean edit(FlowElement element, OperatorDescription description) {
        String className = description.getDeclaration().getDeclaring().getName();
        Map<Tracepoint, TraceSetting> settings = tracepointsByOperatorClass.get(className);
        if (settings == null || settings.isEmpty()) {
            return false;
        }
        return edit(element, settings, className, normalizeMethodName(description.getDeclaration().getName()));
    }

    private String normalizeMethodName(String name) {
        return JavaName.of(name).toMemberName();
    }

    private boolean edit(FlowElement element, FlowPartDescription description) {
        String className = description.getFlowGraph().getDescription().getName();
        Map<Tracepoint, TraceSetting> settings = tracepointsByOperatorClass.get(className);
        if (settings == null || settings.isEmpty()) {
            return false;
        }
        return edit(element, settings, className, FLOWPART_FACTORY_NAME);
    }

    private boolean edit(
            FlowElement element,
            Map<Tracepoint, TraceSetting> settings,
            String className, String methodName) {
        boolean modified = false;
        for (FlowElementInput port : element.getInputPorts()) {
            Tracepoint point = new Tracepoint(className, methodName, PortKind.INPUT, port.getDescription().getName());
            if (settings.containsKey(point)) {
                edit(port, settings.get(point));
                rest.remove(point);
                modified = true;
            }
        }
        for (FlowElementOutput port : element.getOutputPorts()) {
            Tracepoint point = new Tracepoint(className, methodName, PortKind.OUTPUT, port.getDescription().getName());
            if (settings.containsKey(point)) {
                edit(port, settings.get(point));
                rest.remove(point);
                modified = true;
            }
        }
        return modified;
    }

    private void edit(FlowElementInput port, TraceSetting setting) {
        LOG.debug("weaving tracepoint ({}): {}", setting.getTracepoint(), port); //$NON-NLS-1$
        Collection<FlowElementOutput> opposites = port.getOpposites();
        edit(setting, port, opposites, Collections.singleton(port));
    }

    private void edit(FlowElementOutput port, TraceSetting setting) {
        LOG.debug("weaving tracepoint ({}): {}", setting.getTracepoint(), port); //$NON-NLS-1$
        Collection<FlowElementInput> opposites = port.getOpposites();
        edit(setting, port, Collections.singleton(port), opposites);
    }

    private void edit(
            TraceSetting setting,
            FlowElementPort port,
            Collection<FlowElementOutput> upstreams,
            Collection<FlowElementInput> downstreams) {
        assert port != null;
        assert upstreams != null;
        assert downstreams != null;
        OperatorDescription.Builder builder = new OperatorDescription.Builder(Trace.class);
        builder.declare(DefaultTraceOperator.class, DefaultTraceOperator.class, "trace"); //$NON-NLS-1$
        builder.declareParameter(Object.class);
        builder.declareParameter(String.class);
        builder.addInput(INPUT_PORT_NAME, port.getDescription().getDataType());
        builder.addOutput(OUTPUT_PORT_NAME, port.getDescription().getDataType());
        builder.addParameter("header", String.class, setting.getTracepoint().toString());
        builder.addAttribute(Connectivity.OPTIONAL);
        if (setting.getMode() == Mode.STRICT) {
            builder.addAttribute(ObservationCount.EXACTLY_ONCE);
            port.disconnectAll();
            FlowElementResolver resolver = builder.toResolver();
            FlowElementInput input = resolver.getInput(INPUT_PORT_NAME);
            for (FlowElementOutput upstream : upstreams) {
                PortConnection.connect(upstream, input);
            }
            FlowElementOutput output = resolver.getOutput(OUTPUT_PORT_NAME);
            for (FlowElementInput downstream : downstreams) {
                PortConnection.connect(output, downstream);
            }
        } else if (setting.getMode() == Mode.IN_ORDER) {
            builder.addAttribute(ObservationCount.AT_LEAST_ONCE);
            port.disconnectAll();
            FlowElementResolver resolver = builder.toResolver();
            FlowElementInput input = resolver.getInput(INPUT_PORT_NAME);
            for (FlowElementOutput upstream : upstreams) {
                PortConnection.connect(upstream, input);
            }
            FlowElementOutput output = resolver.getOutput(OUTPUT_PORT_NAME);
            for (FlowElementInput downstream : downstreams) {
                PortConnection.connect(output, downstream);
            }
        } else if (setting.getMode() == Mode.OUT_OF_ORDER) {
            builder.addAttribute(ObservationCount.AT_LEAST_ONCE);
            FlowElementResolver resolver = builder.toResolver();
            FlowElementInput input = resolver.getInput(INPUT_PORT_NAME);
            for (FlowElementOutput upstream : upstreams) {
                PortConnection.connect(upstream, input);
            }
        }
    }
}
