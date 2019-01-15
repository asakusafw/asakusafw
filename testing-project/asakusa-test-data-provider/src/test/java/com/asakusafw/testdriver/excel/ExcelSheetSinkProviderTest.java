/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.testdriver.excel;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSink;
import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.TestToolRepository;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * Test for {@link ExcelSheetSinkProvider}.
 */
public class ExcelSheetSinkProviderTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<>(Simple.class);

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    /**
     * Load from SPI.
     * @throws Exception if failed
     */
    @Test
    public void spi() throws Exception {
        TestToolRepository repo = new TestToolRepository(getClass().getClassLoader());

        File file = temp.newFile("example.xls");
        file.delete();

        DataModelSinkFactory factory = repo.getDataModelSinkFactory(file.toURI());
        try (DataModelSink sink = factory.createSink(SIMPLE, new TestContext.Empty())) {
            Simple model = new Simple();
            model.text = "expected";
            sink.put(SIMPLE.toReflection(model));
        }
        assertThat(file.exists(), is(true));
    }

    /**
     * Attempt to load from SPI, but its extension is wrong.
     * @throws Exception if failed
     */
    @Test
    public void spi_wrong_extension() throws Exception {
        TestToolRepository repo = new TestToolRepository(getClass().getClassLoader());

        File file = temp.newFile("example.INVALID");
        file.delete();

        DataModelSinkFactory factory = repo.getDataModelSinkFactory(file.toURI());
        try (DataModelSink sink = factory.createSink(SIMPLE, new TestContext.Empty())) {
            sink.close();
            fail();
        } catch (IOException e) {
            // ok.
        }
    }
}
