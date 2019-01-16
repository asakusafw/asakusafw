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
package com.asakusafw.runtime.io.text.driver;

import static com.asakusafw.runtime.io.text.driver.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

/**
 * Test for {@link RecordDefinition}.
 */
public class RecordDefinitionTest {

    /**
     * input - simple case.
     * @throws Exception if failed
     */
    @Test
    public void input() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "Hello, world!", },
        });
        assertThat(results, is(new String[][] {
            { "Hello, world!", },
        }));
    }

    /**
     * input - w/ header.
     * @throws Exception if failed
     */
    @Test
    public void input_header() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.FORCE)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "A", "B", },
            { "C", "D", },
        });
        assertThat(results, is(new String[][] {
            { "C", "D", },
        }));
    }

    /**
     * input - w/ header.
     * @throws Exception if failed
     */
    @Test
    public void input_header_not_head() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.FORCE)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .build();
        String[][] results = collect(def, Collections.emptySet(), new String[][] {
            { "A", "B", },
            { "C", "D", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", "D", },
        }));
    }

    /**
     * output - simple case.
     * @throws Exception if failed
     */
    @Test
    public void output() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .build();
        String[][] results = emit(def, new String[][] {
            { "Hello, world!", },
        });
        assertThat(results, is(new String[][] {
            { "Hello, world!", },
        }));
    }
}
