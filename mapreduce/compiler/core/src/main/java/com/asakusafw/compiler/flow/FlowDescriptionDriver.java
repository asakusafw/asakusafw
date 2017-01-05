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
package com.asakusafw.compiler.flow;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * Creates {@link FlowGraph} from {@link FlowDescription}.
 */
public class FlowDescriptionDriver {

    private final List<Object> ports = new ArrayList<>();

    private final Map<String, FlowIn<?>> inputs = new LinkedHashMap<>();

    private final Map<String, FlowOut<?>> outputs = new LinkedHashMap<>();

    /**
     * Returns a new flow input.
     * @param <T> the data type
     * @param name the input name
     * @param importer the importer description for the target input
     * @return the created input
     * @throws IllegalStateException if the input name is conflicted
     * @throws IllegalArgumentException if the input name is invalid, or the parameters are {@code null}
     */
    public <T> In<T> createIn(String name, ImporterDescription importer) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(importer, "importer"); //$NON-NLS-1$
        if (isValidName(name) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("FlowDescriptionDriver.errorInvalidInputName"), //$NON-NLS-1$
                    name));
        }
        if (inputs.containsKey(name)) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("FlowDescriptionDriver.errorConflictInputName"), //$NON-NLS-1$
                    name));
        }
        FlowIn<T> in = new FlowIn<>(new InputDescription(name, importer));
        inputs.put(name, in);
        ports.add(in);
        return in;
    }

    /**
     * Returns a new flow output.
     * @param <T> the data type
     * @param name the output name
     * @param exporter the exporter description for the target output
     * @return the created output
     * @throws IllegalStateException if the output name is conflicted
     * @throws IllegalArgumentException if the output name is invalid, or the parameters are {@code null}
     */
    public <T> Out<T> createOut(String name, ExporterDescription exporter) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(exporter, "exporter"); //$NON-NLS-1$
        if (isValidName(name) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("FlowDescriptionDriver.errorInvalidOutputName"), //$NON-NLS-1$
                    name));
        }
        if (outputs.containsKey(name)) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("FlowDescriptionDriver.errorConflictOutputName"), //$NON-NLS-1$
                    name));
        }
        FlowOut<T> out = new FlowOut<>(new OutputDescription(name, exporter));
        outputs.put(name, out);
        ports.add(out);
        return out;
    }

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z_][0-9A-Za-z_]*"); //$NON-NLS-1$

    /**
     * Returns whether the target name is valid for flow input/output name or not.
     * @param name the input/output name
     * @return {@code true} if the target name is valid, otherwise {@code false}
     */
    public boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        return VALID_NAME.matcher(name).matches();
    }

    /**
     * Returns the previously added flow inputs/outputs by their order.
     * The returned list will only contain {@link FlowIn} (for inputs) and {@link FlowOut} (for outputs).
     * @return the added flow inputs/output
     */
    public List<Object> getPorts() {
        return ports;
    }

    /**
     * Analyzes the target flow description, and returns the corresponded flow graph.
     * @param description the target flow description
     * @return the analyzed flow graph
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public FlowGraph createFlowGraph(FlowDescription description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        description.start();
        FlowGraph result = new FlowGraph(
                description.getClass(),
                Lists.from(inputs.values()),
                Lists.from(outputs.values()));
        inputs.clear();
        outputs.clear();
        return result;
    }
}
