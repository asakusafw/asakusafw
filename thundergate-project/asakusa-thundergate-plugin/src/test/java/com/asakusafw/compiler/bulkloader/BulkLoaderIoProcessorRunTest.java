/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import java.io.IOException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.bulkloader.BulkLoaderScript.ExportTable;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.ImportTable;
import com.asakusafw.compiler.bulkloader.testing.model.Ex1;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.thundergate.runtime.property.PropertyLoader;
import com.asakusafw.vocabulary.bulkloader.DbExporterDescription;
import com.asakusafw.vocabulary.bulkloader.DbImporterDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Test for {@link BulkLoaderIoProcessor}.
 */
public class BulkLoaderIoProcessorRunTest {

    /**
     * テストヘルパー。
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * 単純なテスト。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void identity() throws Exception {
        In<Ex1> in = tester.input("ex1", new DbImporterDescription() {
            @Override
            public String getTargetName() {
                return "default";
            }
            @Override
            public Class<?> getModelType() {
                return Ex1.class;
            }
            @Override
            public LockType getLockType() {
                return LockType.TABLE;
            }
        });
        Out<Ex1> out = tester.output("ex1", new DbExporterDescription() {
            @Override
            public String getTargetName() {
                return "default";
            }
            @Override
            public Class<?> getModelType() {
                return Ex1.class;
            }
        });
        JobflowInfo info = tester.compileFlow(new IdentityFlow<Ex1>(in, out));

        BulkLoaderScript script = loadScript(info);
        assertThat(script.getImportTargetTables().size(), is(1));
        assertThat(script.getExportTargetTables().size(), is(1));

        ImportTable itable = script.getImportTargetTables().get(0);
        ExportTable etable = script.getExportTargetTables().get(0);
        assertThat(etable.getSources().size(), is(1));

        ModelOutput<Ex1> source = tester.openOutput(Ex1.class, itable.getDestination());
        Ex1 ex1 = new Ex1();
        ex1.setSid(200);
        ex1.setValue(1);
        source.write(ex1);
        ex1.setSid(300);
        ex1.setValue(2);
        source.write(ex1);
        ex1.setSid(100);
        ex1.setValue(3);
        source.write(ex1);
        source.close();

        assertThat(tester.runStages(info), is(true));

        Location resultLocation = etable.getSources().get(0);
        assertThat(resultLocation.isPrefix(), is(true));
        List<Ex1> results = tester.getList(Ex1.class, resultLocation);
        assertThat(results.size(), is(3));
        assertThat(results.get(0).getValue(), is(3));
        assertThat(results.get(1).getValue(), is(1));
        assertThat(results.get(2).getValue(), is(2));

        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = BulkLoaderIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        assertThat(provider.getImportCommand(context).size(), is(1));
        assertThat(provider.getExportCommand(context).size(), is(1));
        assertThat(provider.getFinalizeCommand(context).size(), is(1));
    }

    /**
     * Capital Caseでのテスト。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void capital() throws Exception {
        In<Ex1> in = tester.input("EX1", new DbImporterDescription() {
            @Override
            public String getTargetName() {
                return "default";
            }
            @Override
            public Class<?> getModelType() {
                return Ex1.class;
            }
            @Override
            public LockType getLockType() {
                return LockType.TABLE;
            }
        });
        Out<Ex1> out = tester.output("EX1", new DbExporterDescription() {
            @Override
            public String getTargetName() {
                return "default";
            }
            @Override
            public Class<?> getModelType() {
                return Ex1.class;
            }
        });
        JobflowInfo info = tester.compileFlow(new IdentityFlow<Ex1>(in, out));

        BulkLoaderScript script = loadScript(info);
        assertThat(script.getImportTargetTables().size(), is(1));
        assertThat(script.getExportTargetTables().size(), is(1));

        ImportTable itable = script.getImportTargetTables().get(0);
        ExportTable etable = script.getExportTargetTables().get(0);
        assertThat(etable.getSources().size(), is(1));

        ModelOutput<Ex1> source = tester.openOutput(Ex1.class, itable.getDestination());
        Ex1 ex1 = new Ex1();
        ex1.setSid(200);
        ex1.setValue(1);
        source.write(ex1);
        ex1.setSid(300);
        ex1.setValue(2);
        source.write(ex1);
        ex1.setSid(100);
        ex1.setValue(3);
        source.write(ex1);
        source.close();

        assertThat(tester.runStages(info), is(true));

        Location resultLocation = etable.getSources().get(0);
        assertThat(resultLocation.isPrefix(), is(true));
        List<Ex1> results = tester.getList(Ex1.class, resultLocation);
        assertThat(results.size(), is(3));
        assertThat(results.get(0).getValue(), is(3));
        assertThat(results.get(1).getValue(), is(1));
        assertThat(results.get(2).getValue(), is(2));

        List<ExternalIoCommandProvider> commands = info.getCommandProviders();
        ExternalIoCommandProvider provider = BulkLoaderIoProcessor.findRelated(commands);
        assertThat(provider, not(nullValue()));

        CommandContext context = new CommandContext("home", "id", "");
        assertThat(provider.getImportCommand(context).size(), is(1));
        assertThat(provider.getExportCommand(context).size(), is(1));
        assertThat(provider.getFinalizeCommand(context).size(), is(1));
    }

    private BulkLoaderScript loadScript(JobflowInfo info) throws IOException {
        PropertyLoader loader = new PropertyLoader(info.getPackageFile(), "default");
        BulkLoaderScript script;
        try {
            List<ImportTable> importers = ImportTable.fromProperties(
                    loader.loadImporterProperties(),
                    getClass().getClassLoader());
            List<ExportTable> exporters = ExportTable.fromProperties(
                    loader.loadExporterProperties(),
                    getClass().getClassLoader());
            script = new BulkLoaderScript(importers, exporters);
        } finally {
            loader.close();
        }
        return script;
    }

}
