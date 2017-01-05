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
package com.asakusafw.compiler.windgate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.Command;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.compiler.windgate.testing.model.Simple;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.windgate.WindGateExporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateImporterDescription;
import com.asakusafw.windgate.core.DriverScript;

/**
 * Test for {@link WindGateIoProcessor}.
 */
public class WindGateIoProcessorTest {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in = flow.createIn("in1", new Import(Simple.class, "testing", dummy()));
        Out<Simple> out = flow.createOut("out1", new Export(Simple.class, "testing", dummy()));

        FlowDescription desc = new IdentityFlow<>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = WindGateIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        List<Command> importer = provider.getImportCommand(context);
        List<Command> exporter = provider.getExportCommand(context);
        List<Command> finalizer = provider.getFinalizeCommand(context);
        assertThat(importer.size(), is(1));
        assertThat(exporter.size(), is(1));
        assertThat(finalizer.size(), is(1));
        assertThat(mode(importer, "testing"), is(WindGateIoProcessor.OPT_BEGIN));
        assertThat(mode(exporter, "testing"), is(WindGateIoProcessor.OPT_END));
        assertThat(find(finalizer, "testing"), is(notNullValue()));
    }

    /**
     * Different profiles.
     * @throws Exception if failed
     */
    @Test
    public void different_profile() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in = flow.createIn("in1", new Import(Simple.class, "testing", dummy()));
        Out<Simple> out = flow.createOut("out1", new Export(Simple.class, "other", dummy()));

        FlowDescription desc = new IdentityFlow<>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = WindGateIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        List<Command> importer = provider.getImportCommand(context);
        List<Command> exporter = provider.getExportCommand(context);
        List<Command> finalizer = provider.getFinalizeCommand(context);
        assertThat(importer.size(), is(1));
        assertThat(exporter.size(), is(1));
        assertThat(finalizer.size(), is(2));
        assertThat(mode(importer, "testing"), is(WindGateIoProcessor.OPT_ONESHOT));
        assertThat(mode(exporter, "other"), is(WindGateIoProcessor.OPT_ONESHOT));
        assertThat(find(finalizer, "testing"), is(notNullValue()));
        assertThat(find(finalizer, "other"), is(notNullValue()));
    }

    /**
     * Multiple I/O with same profile.
     * @throws Exception if failed
     */
    @Test
    public void same_profile() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in1 = flow.createIn("in1", new Import(Simple.class, "testing", dummy()));
        In<Simple> in2 = flow.createIn("in2", new Import(Simple.class, "testing", dummy()));
        Out<Simple> out1 = flow.createOut("out1", new Export(Simple.class, "testing", dummy()));
        Out<Simple> out2 = flow.createOut("out2", new Export(Simple.class, "testing", dummy()));

        FlowDescription desc = new DualIdentityFlow<>(in1, in2, out1, out2);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = WindGateIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        List<Command> importer = provider.getImportCommand(context);
        List<Command> exporter = provider.getExportCommand(context);
        List<Command> finalizer = provider.getFinalizeCommand(context);
        assertThat(importer.size(), is(1));
        assertThat(exporter.size(), is(1));
        assertThat(finalizer.size(), is(1));
        assertThat(mode(importer, "testing"), is(WindGateIoProcessor.OPT_BEGIN));
        assertThat(mode(exporter, "testing"), is(WindGateIoProcessor.OPT_END));
        assertThat(find(finalizer, "testing"), is(notNullValue()));
    }

    /**
     * Multiple I/O with different profiles in input.
     * @throws Exception if failed
     */
    @Test
    public void mutlti_profile_input() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in1 = flow.createIn("in1", new Import(Simple.class, "testing", dummy()));
        In<Simple> in2 = flow.createIn("in2", new Import(Simple.class, "other", dummy()));
        Out<Simple> out1 = flow.createOut("out1", new Export(Simple.class, "testing", dummy()));
        Out<Simple> out2 = flow.createOut("out2", new Export(Simple.class, "testing", dummy()));

        FlowDescription desc = new DualIdentityFlow<>(in1, in2, out1, out2);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = WindGateIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        List<Command> importer = provider.getImportCommand(context);
        List<Command> exporter = provider.getExportCommand(context);
        List<Command> finalizer = provider.getFinalizeCommand(context);
        assertThat(importer.size(), is(2));
        assertThat(exporter.size(), is(1));
        assertThat(finalizer.size(), is(2));
        assertThat(mode(importer, "testing"), is(WindGateIoProcessor.OPT_BEGIN));
        assertThat(mode(importer, "other"), is(WindGateIoProcessor.OPT_ONESHOT));
        assertThat(mode(exporter, "testing"), is(WindGateIoProcessor.OPT_END));
        assertThat(find(finalizer, "testing"), is(notNullValue()));
        assertThat(find(finalizer, "other"), is(notNullValue()));
    }

    /**
     * Multiple I/O with different profiles in input.
     * @throws Exception if failed
     */
    @Test
    public void multi_profile_output() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in1 = flow.createIn("in1", new Import(Simple.class, "testing", dummy()));
        In<Simple> in2 = flow.createIn("in2", new Import(Simple.class, "testing", dummy()));
        Out<Simple> out1 = flow.createOut("out1", new Export(Simple.class, "testing", dummy()));
        Out<Simple> out2 = flow.createOut("out2", new Export(Simple.class, "other", dummy()));

        FlowDescription desc = new DualIdentityFlow<>(in1, in2, out1, out2);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = WindGateIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        List<Command> importer = provider.getImportCommand(context);
        List<Command> exporter = provider.getExportCommand(context);
        List<Command> finalizer = provider.getFinalizeCommand(context);
        assertThat(importer.size(), is(1));
        assertThat(exporter.size(), is(2));
        assertThat(finalizer.size(), is(2));
        assertThat(mode(importer, "testing"), is(WindGateIoProcessor.OPT_BEGIN));
        assertThat(mode(exporter, "testing"), is(WindGateIoProcessor.OPT_END));
        assertThat(mode(exporter, "other"), is(WindGateIoProcessor.OPT_ONESHOT));
        assertThat(find(finalizer, "testing"), is(notNullValue()));
        assertThat(find(finalizer, "other"), is(notNullValue()));
    }

    /**
     * Multiple I/O with different profiles in input and output.
     * @throws Exception if failed
     */
    @Test
    public void multi_profile_inout() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in1 = flow.createIn("in1", new Import(Simple.class, "testing", dummy()));
        In<Simple> in2 = flow.createIn("in2", new Import(Simple.class, "other", dummy()));
        Out<Simple> out1 = flow.createOut("out1", new Export(Simple.class, "testing", dummy()));
        Out<Simple> out2 = flow.createOut("out2", new Export(Simple.class, "other", dummy()));

        FlowDescription desc = new DualIdentityFlow<>(in1, in2, out1, out2);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = WindGateIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        List<Command> importer = provider.getImportCommand(context);
        List<Command> exporter = provider.getExportCommand(context);
        List<Command> finalizer = provider.getFinalizeCommand(context);
        assertThat(importer.size(), is(2));
        assertThat(exporter.size(), is(2));
        assertThat(finalizer.size(), is(2));
        assertThat(mode(importer, "testing"), is(WindGateIoProcessor.OPT_BEGIN));
        assertThat(mode(importer, "other"), is(WindGateIoProcessor.OPT_BEGIN));
        assertThat(mode(exporter, "testing"), is(WindGateIoProcessor.OPT_END));
        assertThat(mode(exporter, "other"), is(WindGateIoProcessor.OPT_END));
        assertThat(find(finalizer, "testing"), is(notNullValue()));
        assertThat(find(finalizer, "other"), is(notNullValue()));
    }

    /**
     * Importer script is invalid.
     * @throws Exception if failed
     */
    @Test
    public void invalid_script_importer() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in = flow.createIn("in1", new Import(Simple.class, "testing", null));
        Out<Simple> out = flow.createOut("out1", new Export(Simple.class, "testing", dummy()));

        FlowDescription desc = new IdentityFlow<>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * Exporter script is invalid.
     * @throws Exception if failed
     */
    @Test
    public void invalid_script_exporter() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in = flow.createIn("in1", new Import(Simple.class, "testing", dummy()));
        Out<Simple> out = flow.createOut("out1", new Export(Simple.class, "testing", null));

        FlowDescription desc = new IdentityFlow<>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * importer description w/o profile name.
     * @throws Exception if failed
     */
    @Test
    public void importer_wo_profile() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in = flow.createIn("in1", new Import(Simple.class, null, dummy()));
        Out<Simple> out = flow.createOut("out1", new Export(Simple.class, "testing", dummy()));

        FlowDescription desc = new IdentityFlow<>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, nullValue());
    }

    /**
     * importer description w/o profile name.
     * @throws Exception if failed
     */
    @Test
    public void importer_w_empty_profile() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in = flow.createIn("in1", new Import(Simple.class, "", dummy()));
        Out<Simple> out = flow.createOut("out1", new Export(Simple.class, "testing", dummy()));

        FlowDescription desc = new IdentityFlow<>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, nullValue());
    }

    /**
     * exporter description w/o profile name.
     * @throws Exception if failed
     */
    @Test
    public void exporter_wo_profile() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in = flow.createIn("in1", new Import(Simple.class, "testing", dummy()));
        Out<Simple> out = flow.createOut("out1", new Export(Simple.class, null, dummy()));

        FlowDescription desc = new IdentityFlow<>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, nullValue());
    }

    /**
     * exporter description w/o profile name.
     * @throws Exception if failed
     */
    @Test
    public void exporter_w_empty_profile() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Simple> in = flow.createIn("in1", new Import(Simple.class, "testing", dummy()));
        Out<Simple> out = flow.createOut("out1", new Export(Simple.class, "", dummy()));

        FlowDescription desc = new IdentityFlow<>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, nullValue());
    }

    JobflowInfo compile(FlowDescriptionDriver flow, FlowDescription desc) {
        try {
            return DirectFlowCompiler.compile(
                    flow.createFlowGraph(desc),
                    "test",
                    "test",
                    "com.example",
                    Location.fromPath("target/testing", '/'),
                    folder.newFolder("build"),
                    Collections.emptyList(),
                    getClass().getClassLoader(),
                    FlowCompilerOptions.load(System.getProperties()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String mode(List<Command> commands, String profile) {
        Command found = find(commands, profile);
        assertThat(profile, found, is(notNullValue()));
        // mode argument
        return found.getCommandTokens().get(2);
    }

    private Command find(List<Command> commands, String profile) {
        for (Command cmd : commands) {
            List<String> tokens = cmd.getCommandTokens();
            // profile argument
            if (tokens.get(1).equals(profile)) {
                return cmd;
            }
        }
        return null;
    }

    private DriverScript dummy() {
        return new DriverScript("example", Collections.emptyMap());
    }

    static final class Import extends WindGateImporterDescription {

        private final Class<?> modelType;

        private final String profileName;

        private final DriverScript driverScript;

        Import(Class<?> modelType, String profileName, DriverScript driverScript) {
            this.modelType = modelType;
            this.profileName = profileName;
            this.driverScript = driverScript;
        }

        @Override
        public Class<?> getModelType() {
            return modelType;
        }

        @Override
        public String getProfileName() {
            return profileName;
        }

        @Override
        public DriverScript getDriverScript() {
            return driverScript;
        }
    }

    static final class Export extends WindGateExporterDescription {

        private final Class<?> modelType;

        private final String profileName;

        private final DriverScript driverScript;

        Export(Class<?> modelType, String profileName, DriverScript driverScript) {
            this.modelType = modelType;
            this.profileName = profileName;
            this.driverScript = driverScript;
        }

        @Override
        public Class<?> getModelType() {
            return modelType;
        }

        @Override
        public String getProfileName() {
            return profileName;
        }

        @Override
        public DriverScript getDriverScript() {
            return driverScript;
        }
    }
}
