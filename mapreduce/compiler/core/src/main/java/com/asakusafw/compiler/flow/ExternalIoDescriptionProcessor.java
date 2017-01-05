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

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.OutputFormat;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.jobflow.ExternalIoStage;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * Processes {@link ImporterDescription} and {@link ExporterDescription} in flow DSL
 * for the targeted external I/O component.
 * Each {@link InputDescription} and {@link OutputDescription} in method parameter must have
 * the supported {@link ImporterDescription} and {@link ExporterDescription} which are specified in
 * {@link #getImporterDescriptionType()} and {@link #getExporterDescriptionType()}.
 * @since 0.1.0
 * @version 0.5.1
 */
public abstract class ExternalIoDescriptionProcessor extends FlowCompilingEnvironment.Initialized {

    /**
     * Returns the ID of this processor.
     * @return the processor ID
     * @since 0.5.1
     */
    public abstract String getId();

    /**
     * Returns the target {@link ImporterDescription} type.
     * @return the target {@link ImporterDescription} type
     */
    public abstract Class<? extends ImporterDescription> getImporterDescriptionType();

    /**
     * Returns the target {@link ExporterDescription} type.
     * @return the target {@link ExporterDescription} type
     */
    public abstract Class<? extends ExporterDescription> getExporterDescriptionType();

    /**
     * Validates flow inputs/outputs which are target of this processor.
     * @param inputs the target flow inputs
     * @param outputs the target flow outputs
     * @return {@code true} if the all flow inputs/outputs are valid, otherwise {@code false}
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public abstract boolean validate(List<InputDescription> inputs, List<OutputDescription> outputs);

    /**
     * Returns source information for the input.
     * @param description target description
     * @return resolved information
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract SourceInfo getInputInfo(InputDescription description);

    /**
     * Emits source and resource files which are used in the prologue phase,
     * and returns information of their individual stages.
     * @param context the current context
     * @return information of the prologue stages
     * @throws IOException if error was occurred while generating files
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<ExternalIoStage> emitPrologue(IoContext context) throws IOException {
        return Collections.emptyList();
    }

    /**
     * Emits source and resource files which are used in the epilogue phase,
     * and returns information of their individual stages.
     * @param context the current context
     * @return information of the epilogue stages
     * @throws IOException if error was occurred while generating files
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<ExternalIoStage> emitEpilogue(IoContext context) throws IOException {
        return Collections.emptyList();
    }

    /**
     * Emits source and resource files which are used in the main phase.
     * @param context the current context
     * @throws IOException if error was occurred while generating files
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void emitPackage(IoContext context) throws IOException {
        return;
    }

    /**
     * Returns the command provider for this targeted external I/O component.
     * @param context the current context
     * @return the created command provider
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExternalIoCommandProvider createCommandProvider(IoContext context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        return new ExternalIoCommandProvider();
    }

    /**
     * Represents a context of {@link ExternalIoDescriptionProcessor}.
     * @since 0.1.0
     * @version 0.5.1
     */
    public static class IoContext {

        /**
         * An empty instance.
         * @since 0.5.1
         */
        public static final IoContext EMPTY = new IoContext(Collections.emptyList(), Collections.emptyList());

        private final List<Input> inputs;

        private final List<Output> outputs;

        /**
         * Creates a new instance.
         * @param inputs the target flow inputs
         * @param outputs the target flow outputs
         */
        public IoContext(List<Input> inputs, List<Output> outputs) {
            this.inputs = inputs;
            this.outputs = outputs;
        }

        /**
         * Returns the target flow inputs.
         * @return the target flow inputs
         */
        public List<Input> getInputs() {
            return inputs;
        }

        /**
         * Returns the target flow outputs.
         * @return the target flow outputs
         */
        public List<Output> getOutputs() {
            return outputs;
        }

        /**
         * Returns a new instance which only has information of the target inputs.
         * @return the created instance
         */
        public IoContext getInputContext() {
            return new IoContext(inputs, Collections.emptyList());
        }

        /**
         * Returns a new instance which only has information of the target outputs.
         * @return the created instance
         */
        public IoContext getOutputContext() {
            return new IoContext(Collections.emptyList(), outputs);
        }
    }

    /**
     * A builder for {@link IoContext}.
     * @since 0.5.1
     */
    public static class IoContextBuilder {

        private final Set<Input> inputs = new LinkedHashSet<>();

        private final Set<Output> outputs = new LinkedHashSet<>();

        /**
         * Adds a new output.
         * @param input the input information
         * @return this
         */
        public IoContextBuilder addInput(Input input) {
            inputs.add(input);
            return this;
        }

        /**
         * Adds a new output.
         * @param output the output information
         * @return this
         */
        public IoContextBuilder addOutput(Output output) {
            outputs.add(output);
            return this;
        }

        /**
         * Creates a new instance.
         * @return the created instance
         */
        public IoContext build() {
            return new IoContext(Lists.from(inputs), Lists.from(outputs));
        }
    }

    /**
     * Represents an external output.
     */
    public static class Output {

        private final OutputDescription description;

        private final List<SourceInfo> sources;

        /**
         * Creates a new instance.
         * @param description description of the target flow output
         * @param sources the upstream information of the output
         * @throws IllegalArgumentException the parameters are {@code null}
         */
        public Output(OutputDescription description, List<SourceInfo> sources) {
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(sources, "sources"); //$NON-NLS-1$
            this.description = description;
            this.sources = sources;
        }

        /**
         * Returns description of the target flow output.
         * @return the target flow output description
         */
        public OutputDescription getDescription() {
            return description;
        }

        /**
         * Returns the upstream information of the output.
         * @return the upstream information of the output
         */
        public List<SourceInfo> getSources() {
            return sources;
        }
    }

    /**
     * Represents an external input.
     */
    public static class Input {

        private final InputDescription description;

        private final Class<? extends OutputFormat<?, ?>> format;

        /**
         * Creates a new instance.
         * @param description description of the target flow input
         * @param format the Hadoop input format how to fetch the target input data-set
         * @throws IllegalArgumentException the parameters are {@code null}
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Input(InputDescription description, Class<? extends OutputFormat> format) {
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(format, "format"); //$NON-NLS-1$
            this.description = description;
            this.format = (Class<? extends OutputFormat<?, ?>>) format;
        }

        /**
         * Returns description of the target flow input.
         * @return the target flow input description
         */
        public InputDescription getDescription() {
            return description;
        }

        /**
         * Returns the Hadoop input format for fetching the target input data-set.
         * @return the corresponded Hadoop input format
         */
        public Class<? extends OutputFormat<?, ?>> getFormat() {
            return format;
        }
    }

    /**
     * Represents an upstream data-set information.
     */
    public static class SourceInfo {

        private final Set<Location> locations;

        private final Class<? extends InputFormat<?, ?>> format;

        private final Map<String, String> attributes;

        /**
         * Creates a new instance.
         * @param locations input source locations
         * @param format the Hadoop input format how to fetch the target input data-set
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        @SuppressWarnings({ "rawtypes" })
        public SourceInfo(
                Set<Location> locations,
                Class<? extends InputFormat> format) {
            this(locations, format, Collections.emptyMap());
        }

        /**
         * Creates a new instance.
         * @param locations input source locations
         * @param format the Hadoop input format how to fetch the target input data-set
         * @param attributes attributes for the input format
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public SourceInfo(
                Set<Location> locations,
                Class<? extends InputFormat> format,
                Map<String, String> attributes) {
            Precondition.checkMustNotBeNull(locations, "locations"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(format, "format"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(attributes, "attributes"); //$NON-NLS-1$
            this.locations = locations;
            this.format = (Class<? extends InputFormat<?, ?>>) format;
            this.attributes = Maps.freeze(attributes);
        }

        /**
         * Returns the input source locations.
         * @return the input source locations
         */
        public Set<Location> getLocations() {
            return locations;
        }

        /**
         * Returns the Hadoop input format how to fetch the target input data-set.
         * @return the corresponded Hadoop input format
         */
        public Class<? extends InputFormat<?, ?>> getFormat() {
            return format;
        }

        /**
         * Returns the attributes of {@link #getFormat() the input format}.
         * @return the input format attributes
         */
        public Map<String, String> getAttributes() {
            return attributes;
        }
    }

    /**
     * An abstract super interface of repository of {@link ExternalIoDescriptionProcessor}.
     */
    public interface Repository extends FlowCompilingEnvironment.Initializable {

        /**
         * Returns a {@link ExternalIoDescriptionProcessor} for the target input description.
         * @param description the target flow input description
         * @return the supported processor, or {@code null} if there is no such the processor
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        ExternalIoDescriptionProcessor findProcessor(InputDescription description);

        /**
         * Returns a {@link ExternalIoDescriptionProcessor} for the target output description.
         * @param description the target flow output description
         * @return the supported processor, or {@code null} if there is no such the processor
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        ExternalIoDescriptionProcessor findProcessor(OutputDescription description);
    }
}
