/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.compiler.yaess.testing.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.runtime.stage.input.TemporaryInputFormat;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * Mock implementation for {@link ExternalIoDescriptionProcessor}.
 */
public class MockIoDescriptionProcessor extends ExternalIoDescriptionProcessor {

    @Override
    public Class<? extends ImporterDescription> getImporterDescriptionType() {
        return MockImporterDescription.class;
    }

    @Override
    public Class<? extends ExporterDescription> getExporterDescriptionType() {
        return MockExporterDescription.class;
    }

    @Override
    public boolean validate(List<InputDescription> inputs, List<OutputDescription> outputs) {
        return true;
    }

    @Override
    public SourceInfo getInputInfo(InputDescription description) {
        Set<Location> locations = Sets.create();
        locations.add(getEnvironment().getTargetLocation().append(description.getName()));
        return new SourceInfo(locations, TemporaryInputFormat.class);
    }

    @Override
    public List<CompiledStage> emitPrologue(IoContext context) throws IOException {
        ModelFactory f = getEnvironment().getModelFactory();
        return Arrays.asList(new CompiledStage(
                Models.toName(f, "com.example.MockPrologue"),
                "prologue"));
    }

    @Override
    public List<CompiledStage> emitEpilogue(IoContext context) throws IOException {
        ModelFactory f = getEnvironment().getModelFactory();
        return Arrays.asList(new CompiledStage(
                Models.toName(f, "com.example.MockEpilogue"),
                "epilogue"));
    }

    @Override
    public ExternalIoCommandProvider createCommandProvider(IoContext context) {
        return new CommandProvider();
    }

    static class CommandProvider extends ExternalIoCommandProvider {

        private static final long serialVersionUID = 1L;

        @Override
        public String getName() {
            return "mock";
        }

        @Override
        public List<Command> getInitializeCommand(CommandContext context) {
            return Arrays.asList(new Command[] {
                    new Command(Arrays.asList(new String[] {
                            "initialize",
                    }), "mock", null, Collections.<String, String>emptyMap())
            });
        }

        @Override
        public List<Command> getImportCommand(CommandContext context) {
            return Arrays.asList(new Command[] {
                    new Command(Arrays.asList(new String[] {
                            "import",
                    }), "mock", "mock", Collections.<String, String>emptyMap())
            });
        }

        @Override
        public List<Command> getExportCommand(CommandContext context) {
            return Arrays.asList(new Command[] {
                    new Command(Arrays.asList(new String[] {
                            "export",
                    }), "mock", "mock", Collections.<String, String>emptyMap())
            });
        }

        @Override
        public List<Command> getFinalizeCommand(CommandContext context) {
            return Arrays.asList(new Command[] {
                    new Command(Arrays.asList(new String[] {
                            "finalize",
                    }), "mock", null, Collections.<String, String>emptyMap())
            });
        }
    }
}
