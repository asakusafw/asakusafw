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

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowGraphRewriter;
import com.asakusafw.compiler.flow.plan.FlowGraphUtil;
import com.asakusafw.trace.io.TraceSettingSerializer;
import com.asakusafw.trace.model.TraceSetting;
import com.asakusafw.trace.model.Tracepoint;
import com.asakusafw.trace.model.Tracepoint.PortKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;
import com.asakusafw.vocabulary.flow.util.CoreOperators;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
import com.asakusafw.vocabulary.operator.Trace;

/**
 * Weaves {@link Trace} operators into target {@link FlowGraph}.
 * @since 0.5.1
 */
public class TracepointWeaveRewriter extends FlowCompilingEnvironment.Initialized implements FlowGraphRewriter {

    /**
     * The compiler option property key of tracepoint settings.
     */
    public static final String KEY_COMPILER_OPTION = "tracepoint"; //$NON-NLS-1$

    static final Logger LOG = LoggerFactory.getLogger(TracepointWeaveRewriter.class);

    @Override
    public Phase getPhase() {
        return Phase.LATER_DEBUG;
    }

    @Override
    public boolean rewrite(FlowGraph graph) throws RewriteException {
        Collection<? extends TraceSetting> settings = extractTraceSettings();
        return rewrite(graph, settings);
    }

    private Collection<? extends TraceSetting> extractTraceSettings() throws RewriteException {
        FlowCompilerOptions options = getEnvironment().getOptions();
        String attribute = options.getExtraAttribute(KEY_COMPILER_OPTION);
        if (attribute == null) {
            return Collections.emptyList();
        }
        Collection<? extends TraceSetting> loaded;
        try {
            loaded = TraceSettingSerializer.deserialize(attribute);
        } catch (RuntimeException e) {
            String keyName = options.getExtraAttributeKeyName(KEY_COMPILER_OPTION);
            throw new RewriteException(MessageFormat.format(
                    Messages.getString("TracepointWeaveRewriter.errorExtractTracepoints"), //$NON-NLS-1$
                    keyName), e);
        }
        return normalize(loaded);
    }

    static boolean rewrite(FlowGraph graph, Collection<? extends TraceSetting> settings) {
        if (settings.isEmpty()) {
            return false;
        }
        LOG.info("Weaving trace operators: {}", graph.getDescription().getName()); //$NON-NLS-1$
        TracepointWeaver weaver = new TracepointWeaver(settings);
        return rewrite(weaver, graph);
    }

    private static boolean rewrite(TracepointWeaver editor, FlowGraph graph) {
        boolean modify = false;
        LinkedList<FlowGraph> work = new LinkedList<>();
        work.add(graph);
        while (work.isEmpty() == false) {
            FlowGraph flow = work.removeFirst();
            Set<FlowElement> elements = FlowGraphUtil.collectElements(flow);
            for (FlowElement element : elements) {
                modify |= editor.edit(element);
                FlowElementDescription description = element.getDescription();
                if (description.getKind() == FlowElementKind.FLOW_COMPONENT) {
                    work.addLast(((FlowPartDescription) description).getFlowGraph());
                }
            }
        }
        return modify;
    }

    @Override
    public Name resolve(FlowResourceDescription resource) throws RewriteException {
        return null;
    }

    private Collection<? extends TraceSetting> normalize(
            Collection<? extends TraceSetting> settings) throws RewriteException {
        ClassLoader loader = getEnvironment().getServiceClassLoader();
        List<TraceSetting> results = new ArrayList<>();
        for (TraceSetting setting : settings) {
            Tracepoint orig = setting.getTracepoint();
            Class<?> operatorClass;
            try {
                operatorClass = loader.loadClass(orig.getOperatorClassName());
            } catch (ClassNotFoundException e) {
                throw new RewriteException(MessageFormat.format(
                        Messages.getString("TracepointWeaveRewriter.errorLoadOperatorClass"), //$NON-NLS-1$
                        orig.getOperatorClassName()), e);
            }
            Tracepoint normalized = createTracepoint(
                    operatorClass, orig.getOperatorMethodName(),
                    orig.getPortKind(), orig.getPortName());
            results.add(new TraceSetting(normalized, setting.getMode(), setting.getAttributes()));
        }
        return results;
    }

    static Tracepoint createTracepoint(
            Class<?> operatorClass, String methodName, PortKind portKind, String portName) throws RewriteException {
        assert operatorClass != null;
        assert methodName != null;
        assert portKind != null;
        assert portName != null;
        Class<?> factoryClass = findFactoryClass(operatorClass);
        OperatorFactory factory = factoryClass.getAnnotation(OperatorFactory.class);
        assert factory != null;
        Method factoryMethod = findFactoryMethod(factoryClass, methodName);
        OperatorInfo info = factoryMethod.getAnnotation(OperatorInfo.class);
        assert info != null;
        if (portKind == PortKind.INPUT) {
            boolean found = false;
            for (OperatorInfo.Input port : info.input()) {
                if (port.name().equals(portName)) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                List<String> list = new ArrayList<>();
                for (OperatorInfo.Input port : info.input()) {
                    list.add(port.name());
                }
                throw new RewriteException(MessageFormat.format(
                        Messages.getString("TracepointWeaveRewriter.errorUnknownInputPort"), //$NON-NLS-1$
                        factoryClass.getName(),
                        factoryMethod.getName(),
                        portName,
                        list));
            }
        } else {
            boolean found = false;
            for (OperatorInfo.Output port : info.output()) {
                if (port.name().equals(portName)) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                List<String> list = new ArrayList<>();
                for (OperatorInfo.Output port : info.output()) {
                    list.add(port.name());
                }
                throw new RewriteException(MessageFormat.format(
                        Messages.getString("TracepointWeaveRewriter.errorUnknownOutputPort"), //$NON-NLS-1$
                        factoryClass.getName(),
                        factoryMethod.getName(),
                        portName,
                        list));
            }
        }
        return new Tracepoint(factory.value().getName(), factoryMethod.getName(), portKind, portName);
    }

    private static Class<?> findFactoryClass(Class<?> operatorOrFactoryClass) throws RewriteException {
        assert operatorOrFactoryClass != null;
        if (operatorOrFactoryClass == CoreOperators.class || operatorOrFactoryClass == CoreOperatorFactory.class) {
            throw new RewriteException(Messages.getString("TracepointWeaveRewriter.errorCoreOperator")); //$NON-NLS-1$
        }
        if (operatorOrFactoryClass.isAnnotationPresent(OperatorFactory.class)) {
            return operatorOrFactoryClass;
        } else {
            ClassLoader classLoader = operatorOrFactoryClass.getClassLoader();
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            Class<?> factoryClass;
            String factoryClassName = operatorOrFactoryClass.getName() + "Factory"; //$NON-NLS-1$
            try {
                factoryClass =  classLoader.loadClass(factoryClassName);
            } catch (ClassNotFoundException e) {
                throw new RewriteException(MessageFormat.format(
                        Messages.getString("TracepointWeaveRewriter.errorOperatorFactory"), //$NON-NLS-1$
                        operatorOrFactoryClass.getName(),
                        factoryClassName));
            }
            if (factoryClass.isAnnotationPresent(OperatorFactory.class) == false) {
                throw new RewriteException(MessageFormat.format(
                        Messages.getString("TracepointWeaveRewriter.errorOperatorFactoryAnnotation"), //$NON-NLS-1$
                        operatorOrFactoryClass.getName(),
                        factoryClassName));
            }
            return factoryClass;
        }
    }

    private static Method findFactoryMethod(Class<?> factoryClass, String methodName) throws RewriteException {
        assert factoryClass != null;
        assert methodName != null;
        for (Method method : factoryClass.getMethods()) {
            if (method.getName().equalsIgnoreCase(methodName)) {
                if (method.isAnnotationPresent(OperatorInfo.class)) {
                    return method;
                }
            }
        }
        throw new RewriteException(MessageFormat.format(
                Messages.getString("TracepointWeaveRewriter.errorOperatorFactoryMethod"), //$NON-NLS-1$
                factoryClass.getName(),
                methodName));
    }
}
