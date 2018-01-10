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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Test for {@link CsvReader}.
 */
public class CsvReaderTest {

    /**
     * Simple case.
     */
    @Test
    public void simple() {
        List<List<String>> rows = parse("Hello,World\r\n");
        assertThat(rows, hasSize(1));

        assertThat(rows.get(0), contains("Hello", "World"));
    }

    /**
     * w/ multiple rows.
     */
    @Test
    public void multiple() {
        List<List<String>> rows = parse("1-1,1-2,1-3\r\n2-1,2-2,2-3\r\n3-1,3-2,3-3\r\n");
        assertThat(rows, hasSize(3));

        assertThat(rows.get(0), contains("1-1", "1-2", "1-3"));
        assertThat(rows.get(1), contains("2-1", "2-2", "2-3"));
        assertThat(rows.get(2), contains("3-1", "3-2", "3-3"));
    }

    /**
     * w/ escaped.
     */
    @Test
    public void escape() {
        List<List<String>> rows = parse("\"Hello, world!\",\"He said \"\"Hello!\"\".\",\"Name:\r\n  Value\"\r\n");
        assertThat(rows, hasSize(1));

        assertThat(rows.get(0), contains("Hello, world!", "He said \"Hello!\".", "Name:\r\n  Value"));
    }

    private List<List<String>> parse(String contents) {
        List<List<String>> results = new ArrayList<>();
        try (CsvReader reader = new CsvReader(new StringReader(contents))) {
            while (reader.next()) {
                results.add(reader.get());
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return results;
    }
}
