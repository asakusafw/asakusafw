/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.utils.io.csv;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link CsvWriter}.
 */
public class CsvWriterTest {

    /**
     * Simple test case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        StringBuilder buf = new StringBuilder();
        try (CsvWriter writer = new CsvWriter(buf)) {
            writer.putField("Hello");
            writer.putField("World");
            writer.putEndOfRecord();
        }
        assertThat(buf.toString(), is("Hello,World\r\n"));
    }

    /**
     * Multiple records.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        StringBuilder buf = new StringBuilder();
        try (CsvWriter writer = new CsvWriter(buf)) {
            writer.putField("1-1");
            writer.putField("1-2");
            writer.putField("1-3");
            writer.putEndOfRecord();
            writer.putField("2-1");
            writer.putField("2-2");
            writer.putField("2-3");
            writer.putEndOfRecord();
            writer.putField("3-1");
            writer.putField("3-2");
            writer.putField("3-3");
            writer.putEndOfRecord();
        }
        assertThat(buf.toString(), is("1-1,1-2,1-3\r\n2-1,2-2,2-3\r\n3-1,3-2,3-3\r\n"));
    }

    /**
     * With special characters.
     * @throws Exception if failed
     */
    @Test
    public void escape() throws Exception {
        StringBuilder buf = new StringBuilder();
        try (CsvWriter writer = new CsvWriter(buf)) {
            writer.putField("Hello, world!");
            writer.putField("He said \"Hello!\".");
            writer.putField("Name:\r\n  Value");
            writer.putEndOfRecord();
        }
        assertThat(buf.toString(), is("\"Hello, world!\",\"He said \"\"Hello!\"\".\",\"Name:\r\n  Value\"\r\n"));
    }
}
