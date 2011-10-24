/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.compiler.bulkloader;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.compiler.bulkloader.testing.model.Ex1;
import com.asakusafw.compiler.bulkloader.testing.model.MockErrorModel;
import com.asakusafw.compiler.bulkloader.testing.model.MockTableModel;
import com.asakusafw.compiler.bulkloader.testing.model.MockUnionModel;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.vocabulary.bulkloader.BulkLoadExporterDescription;
import com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription;
import com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription.LockType;
import com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription.Mode;
import com.asakusafw.vocabulary.bulkloader.DupCheckDbExporterDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Test for {@link BulkLoaderIoProcessor}.
 */
public class BulkLoaderIoProcessorTest {

    /**
     * テンポラリフォルダ。
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * 正常系。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void ok() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Ex1> in1 = flow.createIn("in1", new Import(Mode.PRIMARY, "default", LockType.ROW));
        In<Ex1> in2 = flow.createIn("in2", new Import(Mode.PRIMARY, "default", LockType.ROW_OR_SKIP));
        Out<Ex1> out1 = flow.createOut("out1", new Export("default"));
        Out<Ex1> out2 = flow.createOut("out2", new Export("default"));
        FlowDescription desc = new DualIdentityFlow<Ex1>(in1, in2, out1, out2);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = BulkLoaderIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        assertThat(provider.getImportCommand(context).size(), is(1));
        assertThat(provider.getExportCommand(context).size(), is(1));
        assertThat(provider.getFinalizeCommand(context).size(), is(1));
    }

    /**
     * 重複チェックつき。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void dupCheck() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Ex1> in = flow.createIn("in", new DirectImporterDescription(MockUnionModel.class, "a/a"));
        Out<Ex1> out = flow.createOut("out", new DupCheckDbExporterDescription() {
            @Override
            public Class<?> getModelType() {
                return MockUnionModel.class;
            }
            @Override
            public String getTargetName() {
                return "primary";
            }
            @Override
            protected Class<?> getNormalModelType() {
                return MockTableModel.class;
            }
            @Override
            protected Class<?> getErrorModelType() {
                return MockErrorModel.class;
            }
            @Override
            protected String getErrorCodeValue() {
                return "DUPLICATED";
            }
            @Override
            protected String getErrorCodeColumnName() {
                return "E";
            }
            @Override
            protected List<String> getCheckColumnNames() {
                return Arrays.asList("A");
            }
        });
        FlowDescription desc = new IdentityFlow<Ex1>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = BulkLoaderIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        assertThat(provider.getImportCommand(context).size(), is(1));
        assertThat(provider.getExportCommand(context).size(), is(1));
        assertThat(provider.getFinalizeCommand(context).size(), is(1));
    }

    /**
     * Columnsにない項目がTargetColumnsに含まれる。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void invalidNormalTargetColumns() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<MockUnionModel> in = flow.createIn("in", new DirectImporterDescription(MockUnionModel.class, "a/a"));
        Out<MockUnionModel> out = flow.createOut("out", new DupCheckDbExporterDescription() {
            @Override
            public Class<?> getModelType() {
                return MockUnionModel.class;
            }
            @Override
            public String getTargetName() {
                return "asakusa";
            }
            @Override
            protected Class<?> getNormalModelType() {
                return MockTableModel.class;
            }
            @Override
            protected Class<?> getErrorModelType() {
                return MockErrorModel.class;
            }
            @Override
            protected String getErrorCodeValue() {
                return "ERROR";
            }
            @Override
            protected String getErrorCodeColumnName() {
                return "E";
            }
            @Override
            public List<String> getTargetColumnNames() {
                return Arrays.asList("UNKNOWN");
            }
            @Override
            protected List<String> getCheckColumnNames() {
                return Arrays.asList("B", "C");
            }
        });
        FlowDescription desc = new IdentityFlow<MockUnionModel>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * Columnsにない項目がErrorColumnsに含まれる。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void invalidErrorTargetColumns() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<MockUnionModel> in = flow.createIn("in", new DirectImporterDescription(MockUnionModel.class, "a/a"));
        Out<MockUnionModel> out = flow.createOut("out", new DupCheckDbExporterDescription() {
            @Override
            public Class<?> getModelType() {
                return MockUnionModel.class;
            }
            @Override
            public String getTargetName() {
                return "asakusa";
            }
            @Override
            protected Class<?> getNormalModelType() {
                return MockTableModel.class;
            }
            @Override
            protected Class<?> getErrorModelType() {
                return MockErrorModel.class;
            }
            @Override
            protected String getErrorCodeValue() {
                return "ERROR";
            }
            @Override
            protected String getErrorCodeColumnName() {
                return "E";
            }
            @Override
            protected List<String> getErrorColumnNames() {
                return Arrays.asList("UNKNOWN");
            }
            @Override
            protected List<String> getCheckColumnNames() {
                return Arrays.asList("B", "C");
            }
        });
        FlowDescription desc = new IdentityFlow<MockUnionModel>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * Columnsにない項目がTargetColumnsに含まれる。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void invalidCheckColumns() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<MockUnionModel> in = flow.createIn("in", new DirectImporterDescription(MockUnionModel.class, "a/a"));
        Out<MockUnionModel> out = flow.createOut("out", new DupCheckDbExporterDescription() {
            @Override
            public Class<?> getModelType() {
                return MockUnionModel.class;
            }
            @Override
            public String getTargetName() {
                return "asakusa";
            }
            @Override
            protected Class<?> getNormalModelType() {
                return MockTableModel.class;
            }
            @Override
            protected Class<?> getErrorModelType() {
                return MockErrorModel.class;
            }
            @Override
            protected String getErrorCodeValue() {
                return "ERROR";
            }
            @Override
            protected String getErrorCodeColumnName() {
                return "E";
            }
            @Override
            protected List<String> getCheckColumnNames() {
                return Arrays.asList("UNKNOWN");
            }
        });
        FlowDescription desc = new IdentityFlow<MockUnionModel>(in, out);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * 補助インポーターを利用する正常系。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void using_secondary() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Ex1> in1 = flow.createIn("in1", new Import(Mode.PRIMARY, "default", LockType.CHECK));
        In<Ex1> in2 = flow.createIn("in2", new Import(Mode.SECONDARY, "secondary", LockType.UNUSED));
        Out<Ex1> out1 = flow.createOut("out1", new Export("default"));
        Out<Ex1> out2 = flow.createOut("out2", new Export("default"));
        FlowDescription desc = new DualIdentityFlow<Ex1>(in1, in2, out1, out2);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = BulkLoaderIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        assertThat(provider.getImportCommand(context).size(), is(2));
        assertThat(provider.getExportCommand(context).size(), is(1));
        assertThat(provider.getFinalizeCommand(context).size(), is(1));
    }

    /**
     * インポーターが存在しない。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void no_primary_importers() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Ex1> in1 = flow.createIn("in1", new DirectImporterDescription(Ex1.class, "a/a"));
        Out<Ex1> out1 = flow.createOut("out1", new Export("primary"));
        FlowDescription desc = new IdentityFlow<Ex1>(in1, out1);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = BulkLoaderIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        assertThat(provider.getImportCommand(context).size(), is(1));
        assertThat(provider.getExportCommand(context).size(), is(1));
        assertThat(provider.getFinalizeCommand(context).size(), is(1));
    }

    /**
     * エクスポーターが存在しない。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void no_exporters() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Ex1> in1 = flow.createIn("in1", new Import(Mode.PRIMARY, "primary", LockType.TABLE));
        Out<Ex1> out1 = flow.createOut("out1", new DirectExporterDescription(Ex1.class, "a/a-*"));
        FlowDescription desc = new IdentityFlow<Ex1>(in1, out1);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = BulkLoaderIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        assertThat(provider.getImportCommand(context).size(), is(1));
        assertThat(provider.getExportCommand(context).size(), is(1));
        assertThat(provider.getFinalizeCommand(context).size(), is(1));
    }

    /**
     * エクスポーターが存在しない。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void only_secondary() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Ex1> in1 = flow.createIn("in1", new Import(Mode.SECONDARY, "secondary", LockType.UNUSED));
        Out<Ex1> out1 = flow.createOut("out1", new DirectExporterDescription(Ex1.class, "a/a-*"));
        FlowDescription desc = new IdentityFlow<Ex1>(in1, out1);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = BulkLoaderIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        assertThat(provider.getImportCommand(context).size(), is(1));
        assertThat(provider.getExportCommand(context).size(), is(0));
        assertThat(provider.getFinalizeCommand(context).size(), is(0));
    }

    /**
     * 補助インポーターでロックを指定。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void lock_in_secondary() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Ex1> in1 = flow.createIn("in1", new Import(Mode.SECONDARY, "secondary", LockType.ROW));
        Out<Ex1> out1 = flow.createOut("out1", new Export("primary"));
        FlowDescription desc = new IdentityFlow<Ex1>(in1, out1);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * プライマリインポーターの指定が複数ある。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void mutiple_primary() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Ex1> in1 = flow.createIn("in1", new Import(Mode.PRIMARY, "p1", LockType.ROW));
        In<Ex1> in2 = flow.createIn("in2", new Import(Mode.PRIMARY, "p2", LockType.ROW));
        Out<Ex1> out1 = flow.createOut("out1", new Export("p1"));
        Out<Ex1> out2 = flow.createOut("out2", new Export("p1"));
        FlowDescription desc = new DualIdentityFlow<Ex1>(in1, in2, out1, out2);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * プライマリ以外でエクスポーターを起動する。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void inconsistent_exporter() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Ex1> in1 = flow.createIn("in1", new Import(Mode.PRIMARY, "a", LockType.ROW));
        In<Ex1> in2 = flow.createIn("in2", new Import(Mode.PRIMARY, "a", LockType.ROW));
        Out<Ex1> out1 = flow.createOut("out1", new Export("b"));
        Out<Ex1> out2 = flow.createOut("out2", new Export("b"));
        FlowDescription desc = new DualIdentityFlow<Ex1>(in1, in2, out1, out2);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * エクスポーターが複数存在する。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void multiple_exporter() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Ex1> in1 = flow.createIn("in1", new Import(Mode.PRIMARY, "a", LockType.ROW));
        In<Ex1> in2 = flow.createIn("in2", new Import(Mode.PRIMARY, "a", LockType.ROW));
        Out<Ex1> out1 = flow.createOut("out1", new Export("a"));
        Out<Ex1> out2 = flow.createOut("out2", new Export("b"));
        FlowDescription desc = new DualIdentityFlow<Ex1>(in1, in2, out1, out2);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * 補助インポーターが昇格する。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void upgrade_secondary() throws Exception {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Ex1> in1 = flow.createIn("in1", new Import(Mode.PRIMARY, "a", LockType.ROW));
        In<Ex1> in2 = flow.createIn("in2", new Import(Mode.SECONDARY, "a", LockType.UNUSED));
        Out<Ex1> out1 = flow.createOut("out1", new Export("a"));
        Out<Ex1> out2 = flow.createOut("out2", new Export("a"));
        FlowDescription desc = new DualIdentityFlow<Ex1>(in1, in2, out1, out2);

        JobflowInfo info = compile(flow, desc);
        assertThat(info, not(nullValue()));
        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = BulkLoaderIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        assertThat("２回にわけない", provider.getImportCommand(context).size(), is(1));
        assertThat(provider.getExportCommand(context).size(), is(1));
        assertThat(provider.getFinalizeCommand(context).size(), is(1));
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
                    Collections.<File>emptyList(),
                    getClass().getClassLoader(),
                    FlowCompilerOptions.load(System.getProperties()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static class Import extends BulkLoadImporterDescription {

        private final Mode mode;

        private final String target;

        private final LockType lock;

        Import(Mode mode, String target, LockType lock) {
            this.mode = mode;
            this.target = target;
            this.lock = lock;
        }

        @Override
        public Mode getMode() {
            return mode;
        }

        @Override
        public String getTargetName() {
            return target;
        }

        @Override
        public LockType getLockType() {
            return lock;
        }

        @Override
        public Class<?> getModelType() {
            return Ex1.class;
        }

        @Override
        public String getTableName() {
            return "EX1";
        }

        @Override
        public List<String> getColumnNames() {
            return Arrays.asList("SID");
        }

        @Override
        public boolean isCacheEnabled() {
            return false;
        }
    }

    static class Export extends BulkLoadExporterDescription {

        private final String target;

        Export(String target) {
            this.target = target;
        }

        @Override
        public Class<?> getTableModelClass() {
            return Ex1.class;
        }

        @Override
        public String getTargetName() {
            return target;
        }

        @Override
        public Class<?> getModelType() {
            return Ex1.class;
        }

        @Override
        public String getTableName() {
            return "EX1";
        }

        @Override
        public List<String> getColumnNames() {
            return Arrays.asList("SID");
        }

        @Override
        public List<String> getTargetColumnNames() {
            return Arrays.asList("SID");
        }

        @Override
        public List<String> getPrimaryKeyNames() {
            return Arrays.asList("SID");
        }
    }
}
