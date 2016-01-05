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
package com.asakusafw.testdriver.html;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.DifferenceSink;
import com.asakusafw.testdriver.core.DifferenceSinkFactory;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.TestToolRepository;
import com.asakusafw.testdriver.excel.Simple;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * Test for {@link HtmlDifferenceSinkProvider}.
 */
public class HtmlDifferenceSinkProviderTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<>(Simple.class);

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    /**
     * Load the provider via SPI.
     * @throws Exception if failed
     */
    @Test
    public void spi() throws Exception {
        TestToolRepository repo = new TestToolRepository(getClass().getClassLoader());

        File file = temp.newFile("example.html");
        file.delete();

        DifferenceSinkFactory factory = repo.getDifferenceSinkFactory(file.toURI());
        try (DifferenceSink sink = factory.createSink(SIMPLE, new TestContext.Empty())) {
            Simple expected = new Simple();
            expected.text = "expected";
            Simple actual = new Simple();
            actual.text = "actual";
            sink.put(new Difference(
                    SIMPLE.toReflection(expected),
                    SIMPLE.toReflection(actual),
                    "testing"));
        }

        assertThat(file.exists(), is(true));
    }

    /**
     * Attempt to load the provider via SPI, but its extension is wrong.
     * @throws Exception if failed
     */
    @Test
    public void spi_wrong_extension() throws Exception {
        TestToolRepository repo = new TestToolRepository(getClass().getClassLoader());

        File file = temp.newFile("example.invalid");
        file.delete();

        DifferenceSinkFactory factory = repo.getDifferenceSinkFactory(file.toURI());
        try (DifferenceSink sink = factory.createSink(SIMPLE, new TestContext.Empty())) {
            sink.close();
            fail();
        } catch (IOException e) {
            // ok.
        }
    }
}
