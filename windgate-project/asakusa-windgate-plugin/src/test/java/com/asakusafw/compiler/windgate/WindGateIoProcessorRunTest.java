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
package com.asakusafw.compiler.windgate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.compiler.windgate.testing.model.Simple;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.windgate.Constants;
import com.asakusafw.vocabulary.windgate.WindGateExporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateImporterDescription;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.vocabulary.FileProcess;

/**
 * Test for {@link WindGateIoProcessor} with running Hadoop.
 */
public class WindGateIoProcessorRunTest {

    /**
     * Hadoop runner.
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * Simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        In<Simple> in = tester.input("in1", new Import(Simple.class, "testing", dummy("dummy")));
        Out<Simple> out = tester.output("out1", new Export(Simple.class, "testing", dummy("dummy")));

        JobflowInfo info = tester.compileFlow(new IdentityFlow<Simple>(in, out));

        // extract importer destination
        GateScript importerScript = loadScript(info, "testing", true);
        assertThat(importerScript.getProcesses().size(), is(1));
        ProcessScript<?> importer = getProcess(importerScript, "in1");
        assertThat(importer.getSourceScript().getResourceName(), is("dummy"));
        assertThat(importer.getDrainScript().getResourceName(), is(Constants.HADOOP_FILE_RESOURCE_NAME));
        String importerPath = importer.getDrainScript().getConfiguration().get(FileProcess.FILE.key());
        assertThat(importerPath, is(notNullValue()));
        Location importerLocation = Location.fromPath(importerPath, '/');

        // extract exporter source
        GateScript exporterScript = loadScript(info, "testing", false);
        assertThat(exporterScript.getProcesses().size(), is(1));
        ProcessScript<?> exporter = getProcess(exporterScript, "out1");
        assertThat(exporter.getSourceScript().getResourceName(), is(Constants.HADOOP_FILE_RESOURCE_NAME));
        assertThat(exporter.getDrainScript().getResourceName(), is("dummy"));
        String exporterPath = exporter.getSourceScript().getConfiguration().get(FileProcess.FILE.key());
        assertThat(exporterPath, is(notNullValue()));
        Location exporterLocation = Location.fromPath(exporterPath, '/');
        assertThat(exporterLocation.isPrefix(), is(true));

        ModelOutput<Simple> source = tester.openOutput(Simple.class, importerLocation);
        Simple model = new Simple();
        model.setValueAsString("Hello1, world!");
        source.write(model);
        model.setValueAsString("Hello2, world!");
        source.write(model);
        model.setValueAsString("Hello3, world!");
        source.write(model);
        source.close();

        assertThat(tester.runStages(info), is(true));

        List<Simple> results = tester.getList(Simple.class, exporterLocation);
        assertThat(results.size(), is(3));
        Collections.sort(results, new Comparator<Simple>() {
            @Override
            public int compare(Simple o1, Simple o2) {
                return o1.getValueOption().compareTo(o2.getValueOption());
            }
        });
        assertThat(results.get(0).getValueAsString(), is("Hello1, world!"));
        assertThat(results.get(1).getValueAsString(), is("Hello2, world!"));
        assertThat(results.get(2).getValueAsString(), is("Hello3, world!"));
    }

    private GateScript loadScript(JobflowInfo info, String profile, boolean importer) throws IOException {
        File file = info.getPackageFile();
        ZipFile zip = new ZipFile(file);
        try {
            String location = WindGateIoProcessor.getScriptLocation(importer, profile);
            ZipEntry entry = zip.getEntry(location);
            assertThat(entry, is(notNullValue()));
            InputStream input = zip.getInputStream(entry);
            Properties p = new Properties();
            p.load(input);
            input.close();
            return GateScript.loadFrom("dummy", p, getClass().getClassLoader());
        } finally {
            zip.close();
        }
    }

    private ProcessScript<?> getProcess(GateScript script, String name) {
        for (ProcessScript<?> proc : script.getProcesses()) {
            if (proc.getName().equals(name)) {
                return proc;
            }
        }
        throw new AssertionError(name);
    }

    private DriverScript dummy(String resourceName) {
        return new DriverScript(resourceName, Collections.<String, String>emptyMap());
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
