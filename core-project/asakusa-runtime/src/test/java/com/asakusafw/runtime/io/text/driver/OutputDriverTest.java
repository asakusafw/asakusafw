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
package com.asakusafw.runtime.io.text.driver;

import static com.asakusafw.runtime.io.text.driver.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import com.asakusafw.runtime.io.text.UnmappableOutput.ErrorCode;

/**
 * Test for {@code OutputDriver}.
 */
public class OutputDriverTest {


    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
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

    /**
     * w/ multiple rows.
     * @throws Exception if failed
     */
    @Test
    public void multiple_rows() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .build();
        String[][] results = emit(def, new String[][] {
            { "A", },
            { "B", },
            { "C", },
        });
        assertThat(results, is(new String[][] {
            { "A", },
            { "B", },
            { "C", },
        }));
    }

    /**
     * w/ multiple columns.
     * @throws Exception if failed
     */
    @Test
    public void multiple_columns() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .withField(self(), field(2).build())
                .build();
        String[][] results = emit(def, new String[][] {
            { "A", "B", "C", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", "C", },
        }));
    }

    /**
     * w/ null value.
     * @throws Exception if failed
     */
    @Test
    public void null_value() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .withField(self(), field(2).build())
                .build();
        String[][] results = emit(def, new String[][] {
            { "A", null, "B", },
        });
        assertThat(results, is(new String[][] {
            { "A", null, "B", },
        }));
    }

    /**
     * w/ unmappable.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void unmappable() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .build();
        emit(def, new String[][] {
            { "A", "B", },
            { "C", ErrorCode.UNDEFINED_NULL_SEQUENCE.name(), },
        });
    }

    /**
     * w/ unmappable - ignore.
     * @throws Exception if failed
     */
    @Test
    public void unmappable_ignore() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withOnUnmappableOutput(ErrorAction.IGNORE)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .build();
        String[][] results = emit(def, new String[][] {
            { "A", "B", },
            { "C", ErrorCode.UNDEFINED_NULL_SEQUENCE.name(), },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", ErrorCode.UNDEFINED_NULL_SEQUENCE.name(), },
        }));
    }

    /**
     * w/ unmappable - report.
     * @throws Exception if failed
     */
    @Test
    public void unmappable_report() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withOnUnmappableOutput(ErrorAction.REPORT)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .build();
        String[][] results = emit(def, new String[][] {
            { "A", "B", },
            { "C", ErrorCode.UNDEFINED_NULL_SEQUENCE.name(), },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", ErrorCode.UNDEFINED_NULL_SEQUENCE.name(), },
        }));
    }

    /**
     * w/ unmappable - each field.
     * @throws Exception if failed
     */
    @Test
    public void unmappable_field() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), field(1).withOnUnmappableOutput(ErrorAction.IGNORE).build())
                .build();
        String[][] results = emit(def, new String[][] {
            { "A", "B", },
            { "C", ErrorCode.UNDEFINED_NULL_SEQUENCE.name(), },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", ErrorCode.UNDEFINED_NULL_SEQUENCE.name(), },
        }));
    }

    /**
     * w/ unmappable - all messages.
     * @throws Exception if failed
     */
    @Test
    public void unmappable_messages() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withOnUnmappableOutput(ErrorAction.REPORT)
                .withField(self(), field(0).build())
                .build();
        String[][] values = Arrays.stream(ErrorCode.values())
                .map(c -> new String[] { c.name() })
                .toArray(String[][]::new);
        String[][] results = emit(def, values);
        assertThat(results, is(values));
    }

    /**
     * w/ header.
     * @throws Exception if failed
     */
    @Test
    public void header() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.FORCE)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).build())
                .build();
        String[][] results = emit(def, new String[][] {
            { "A", "B", },
            { "C", "D", },
        });
        assertThat(results, is(new String[][] {
            { "a", "b", },
            { "A", "B", },
            { "C", "D", },
        }));
    }

    /**
     * w/ header - unmappable.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void header_unmappable() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.FORCE)
                .withField(self(), field("a", 0).build())
                .withField(self(), field(ErrorCode.CONFLICT_ESCAPE_SEQUENCE.name(), 1).build())
                .build();
        emit(def, new String[][] {
            { "A", "B", },
        });
    }

    /**
     * w/ header - unmappable.
     * @throws Exception if failed
     */
    @Test
    public void header_unmappable_ignore() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.FORCE)
                .withOnUnmappableOutput(ErrorAction.IGNORE)
                .withField(self(), field("a", 0).build())
                .withField(self(), field(ErrorCode.CONFLICT_ESCAPE_SEQUENCE.name(), 1).build())
                .build();
        String[][] results = emit(def, new String[][] {
            { "A", "B", },
        });
        assertThat(results, is(new String[][] {
            { "a", ErrorCode.CONFLICT_ESCAPE_SEQUENCE.name(), },
            { "A", "B", },
        }));
    }

    /**
     * w/ header - unmappable.
     * @throws Exception if failed
     */
    @Test
    public void header_unmappable_report() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.FORCE)
                .withOnUnmappableOutput(ErrorAction.REPORT)
                .withField(self(), field("a", 0).build())
                .withField(self(), field(ErrorCode.CONFLICT_ESCAPE_SEQUENCE.name(), 1).build())
                .build();
        String[][] results = emit(def, new String[][] {
            { "A", "B", },
        });
        assertThat(results, is(new String[][] {
            { "a", ErrorCode.CONFLICT_ESCAPE_SEQUENCE.name(), },
            { "A", "B", },
        }));
    }

    /**
     * w/ header - unmappable.
     * @throws Exception if failed
     */
    @Test
    public void header_unmappable_field() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.FORCE)
                .withOnUnmappableOutput(ErrorAction.IGNORE)
                .withField(self(), field("a", 0).build())
                .withField(self(), field(ErrorCode.CONFLICT_ESCAPE_SEQUENCE.name(), 1)
                        .withOnUnmappableOutput(ErrorAction.ERROR) // ignored for header
                        .build())
                .build();
        String[][] results = emit(def, new String[][] {
            { "A", "B", },
        });
        assertThat(results, is(new String[][] {
            { "a", ErrorCode.CONFLICT_ESCAPE_SEQUENCE.name(), },
            { "A", "B", },
        }));
    }
}
