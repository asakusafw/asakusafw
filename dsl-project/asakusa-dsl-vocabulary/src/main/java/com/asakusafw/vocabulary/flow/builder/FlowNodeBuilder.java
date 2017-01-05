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
package com.asakusafw.vocabulary.flow.builder;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;

/**
 * Builds fragment graph node internally.
 * @since 0.9.0
 */
public class FlowNodeBuilder extends FlowElementBuilder {

    private static final Object INVALID_ARGUMENT = new Object() {
        @Override
        public String toString() {
            return "INVALID"; //$NON-NLS-1$
        }
    };

    private final Constructor<? extends FlowDescription> constructor;

    /**
     * Creates a new instance for operator method.
     * @param flowClass flow class
     * @param parameterTypes flow-part constructor parameter types
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowNodeBuilder(Class<? extends FlowDescription> flowClass, Class<?>... parameterTypes) {
        try {
            this.constructor = flowClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Failed to detect flow description constructor (class={0}, parameters={1})",
                    flowClass.getName(),
                    Arrays.toString(parameterTypes)), e);
        }
    }

    /**
     * Returns the target constructor.
     * @return the constructor
     */
    public Constructor<? extends FlowDescription> getConstructor() {
        return constructor;
    }

    @Override
    protected FlowElementDescription build(
            List<PortInfo> inputPorts,
            List<PortInfo> outputPorts,
            List<DataInfo> arguments,
            List<AttributeInfo> attributes) {
        FlowPartDescription.Builder builder = new FlowPartDescription.Builder(constructor.getDeclaringClass());
        Map<String, FlowIn<?>> inputMap = new LinkedHashMap<>();
        Map<String, FlowOut<?>> outputMap = new LinkedHashMap<>();
        Map<String, Object> dataMap = new LinkedHashMap<>();
        for (PortInfo info : inputPorts) {
            if (info.getKey() != null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "flow-part cannot accept shuffle key: {0}({1})",
                        constructor.getDeclaringClass().getName(),
                        info.getName()));
            }
            if (info.getExtern() != null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "flow-part cannot accept external input: {0}({1})",
                        constructor.getDeclaringClass().getName(),
                        info.getName()));
            }
            FlowIn<?> port = builder.addInput(info.getName(), info.getType());
            inputMap.put(info.getName(), port);
        }
        for (PortInfo info : outputPorts) {
            if (info.getKey() != null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "flow-part cannot accept shuffle key: {0}({1})",
                        constructor.getDeclaringClass().getName(),
                        info.getName()));
            }
            if (info.getExtern() != null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "flow-part cannot accept external output: {0}({1})",
                        constructor.getDeclaringClass().getName(),
                        info.getName()));
            }
            FlowOut<?> port = builder.addOutput(info.getName(), info.getType());
            outputMap.put(info.getName(), port);
        }
        for (DataInfo info : arguments) {
            Data data = info.getData();
            Object value;
            switch (data.getKind()) {
            case CONSTANT: {
                Constant c = (Constant) data;
                value = c.getValue();
                builder.addParameter(info.getName(), c.getType(), value);
                break;
            }
            default:
                throw new AssertionError(data);
            }
            dataMap.put(info.getName(), value);
        }
        if (attributes.isEmpty() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "flow-part cannot accept attributes: {0}({1})",
                    constructor.getDeclaringClass().getName(),
                    attributes));
        }
        Object[] constructorArguments = computeConstructoArguments(
                inputPorts, outputPorts, arguments,
                inputMap, outputMap, dataMap);
        try {
            FlowDescription instance = constructor.newInstance(constructorArguments);
            instance.start();
        } catch (Exception e) {
            throw new IllegalStateException(MessageFormat.format(
                    "error occurred while processing flow-part: {0}",
                    constructor.getDeclaringClass().getName()), e);
        }
        return builder.toDescription();
    }

    private Object[] computeConstructoArguments(
            List<PortInfo> inputPorts,
            List<PortInfo> outputPorts,
            List<DataInfo> arguments,
            Map<String, FlowIn<?>> inputMap,
            Map<String, FlowOut<?>> outputMap,
            Map<String, Object> dataMap) {
        Object[] results = new Object[constructor.getParameterTypes().length];
        Arrays.fill(results, INVALID_ARGUMENT);
        for (PortInfo info : inputPorts) {
            put("input", results, info, inputMap);
        }
        for (PortInfo info : outputPorts) {
            put("output", results, info, outputMap);
        }
        for (DataInfo info : arguments) {
            put("data", results, info, dataMap);
        }
        for (int index = 0; index < results.length; index++) {
            if (results[index] == INVALID_ARGUMENT) {
                throw new IllegalStateException(MessageFormat.format(
                        "flow-part constructor argument is not completed: {0}(@{1})",
                        constructor.getDeclaringClass().getName(),
                        index));
            }
        }
        return results;
    }

    private void put(String label, Object[] results, EdgeInfo<?> info, Map<String, ?> valueMap) {
        Integer index = info.getParameterIndex();
        if (index == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "flow-part {0} must have parameter index information: {1}({2})",
                    label,
                    constructor.getDeclaringClass().getName(),
                    info.getName()));
        }
        if (results[index] != INVALID_ARGUMENT) {
            throw new IllegalStateException(MessageFormat.format(
                    "flow-part constructor argument is duplicated: {0}(@{1}={2})",
                    constructor.getDeclaringClass().getName(),
                    index,
                    info.getName()));
        }
        if (valueMap.containsKey(info.getName()) == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "flow-part constructor argument is unknown: {0}(@{1}={2})",
                    constructor.getDeclaringClass().getName(),
                    index,
                    info.getName()));
        }
        Object value = valueMap.get(info.getName());
        results[index] = value;
    }
}
