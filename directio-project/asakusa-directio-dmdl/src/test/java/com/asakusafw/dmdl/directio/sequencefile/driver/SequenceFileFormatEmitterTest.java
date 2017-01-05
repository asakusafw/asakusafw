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
package com.asakusafw.dmdl.directio.sequencefile.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.asakusafw.dmdl.directio.common.driver.GeneratorTesterRoot;
import com.asakusafw.dmdl.java.emitter.driver.ObjectDriver;
import com.asakusafw.dmdl.java.emitter.driver.WritableDriver;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.windows.WindowsSupport;

/**
 * Test for {@link SequenceFileFormatEmitter}.
 */
public class SequenceFileFormatEmitterTest extends GeneratorTesterRoot {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    private ClassLoader classLoader;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new SequenceFileFormatEmitter());
        emitDrivers.add(new ObjectDriver());
        emitDrivers.add(new WritableDriver());
        classLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @Override
    @After
    public void tearDown() throws Exception {
        if (classLoader != null) {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    /**
     * A simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File tempFile = folder.newFile("tempfile");
        Path path = new Path(tempFile.toURI());

        ModelLoader loaded = generateJava("simple");
        ModelWrapper model = loaded.newModel("Simple");
        DataFormat<?> support = (DataFormat<?>) loaded.newObject("sequencefile", "SimpleSequenceFileFormat");
        assertThat(support, is(instanceOf(Configurable.class)));
        Thread.currentThread().setContextClassLoader(support.getClass().getClassLoader());

        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(tempFile.toURI(), conf);
        if (support instanceof Configurable) {
            ((Configurable) support).setConf(conf);
        }

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        HadoopFileFormat<Object> unsafe = unsafe(support);

        model.set("value", new Text("Hello, world!"));

        try (ModelOutput<Object> writer = unsafe.createOutput(model.unwrap().getClass(),  fs, path, new Counter())) {
            writer.write(model.unwrap());
        }

        try (ModelInput<Object> reader = unsafe.createInput(
                model.unwrap().getClass(), fs, path, 0, fs.getFileStatus(path).getLen(), new Counter())) {
            Object buffer = loaded.newModel("Simple").unwrap();
            assertThat(reader.readTo(buffer), is(true));
            assertThat(buffer, is(buffer));
            assertThat(reader.readTo(buffer), is(false));
        }
    }

    /**
     * Compile with no attributes.
     * @throws Exception if failed
     */
    @Test
    public void no_attributes() throws Exception {
        ModelLoader loaded = generateJava("no_attributes");
        assertThat(loaded.exists("sequencefile", "NoAttributesSequenceFileFormat"), is(false));
    }

    @SuppressWarnings("unchecked")
    private HadoopFileFormat<Object> unsafe(Object support) {
        return (HadoopFileFormat<Object>) support;
    }
}
