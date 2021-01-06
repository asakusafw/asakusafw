/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import org.junit.Test;

/**
 * Test for {@code InputDriver}.
 */
public class InputDriverTest {

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
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
     * w/ multiple rows.
     * @throws Exception if failed
     */
    @Test
    public void multiple_rows() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .build();
        String[][] results = collect(def, new String[][] {
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
        String[][] results = collect(def, new String[][] {
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
        String[][] results = collect(def, new String[][] {
            { "A", null, "B", },
        });
        assertThat(results, is(new String[][] {
            { "A", null, "B", },
        }));
    }

    /**
     * less w/ ignore.
     * @throws Exception if failed
     */
    @Test
    public void less_ignore() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .withOnLessInput(ErrorAction.IGNORE)
                .build();
        String[][] results = collect(def, new String[][] {
            { "A", "B", },
            { "C", },
            { "D", "E", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", null, },
            { "D", "E", },
        }));
    }

    /**
     * less w/ ignore.
     * @throws Exception if failed
     */
    @Test
    public void less_report() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .withOnLessInput(ErrorAction.REPORT)
                .build();
        String[][] results = collect(def, new String[][] {
            { "A", "B", },
            { "C", },
            { "D", "E", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", null, },
            { "D", "E", },
        }));
    }

    /**
     * less w/ error.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void less_error() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .build();
        collect(def, new String[][] {
            { "A", "B", },
            { "C", },
        });
    }

    /**
     * more w/ ignore.
     * @throws Exception if failed
     */
    @Test
    public void more_ignore() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withOnMoreInput(ErrorAction.IGNORE)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "A", "B", },
            { "C", "D", "E", },
            { "F", "G", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", "D", },
            { "F", "G", },
        }));
    }

    /**
     * more w/ report.
     * @throws Exception if failed
     */
    @Test
    public void more_report() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withOnMoreInput(ErrorAction.REPORT)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "A", "B", },
            { "C", "D", "E", },
            { "F", "G", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", "D", },
            { "F", "G", },
        }));
    }

    /**
     * more w/ error.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void more_error() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .build();
        collect(def, new String[][] {
            { "A", "B", },
            { "C", "D", "E", },
        });
    }

    /**
     * trim fields.
     * @throws Exception if failed
     */
    @Test
    public void trim_field() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), field(1).withTrimInput(true).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "A", "B", },
            { " C", " D", },
            { "E ", "F ", },
            { " G ", " H ", },
            { " ", " ", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { " C", "D", },
            { "E ", "F", },
            { " G ", "H", },
            { " ", "", },
        }));
    }

    /**
     * trim fields.
     * @throws Exception if failed
     */
    @Test
    public void trim_default() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withTrimInput(true)
                .withField(self(), field(0).withTrimInput(false).build())
                .withField(self(), field(1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "A", "B", },
            { " C", " D", },
            { "E ", "F ", },
            { " G ", " H ", },
            { " ", " ", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { " C", "D", },
            { "E ", "F", },
            { " G ", "H", },
            { " ", "", },
        }));
    }

    /**
     * w/ skip empty.
     * @throws Exception if failed
     */
    @Test
    public void skip_empty() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withSkipEmptyInput(true)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .withField(self(), field(2).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "", "A", "B", "", "", "C", "", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", "C", },
        }));
    }

    /**
     * w/ skip empty for field.
     * @throws Exception if failed
     */
    @Test
    public void skip_empty_field() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .withField(self(), field(2).withSkipEmptyInput(true).build())
                .withField(self(), field(3).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "", "A", "", "B", "", },
        });
        assertThat(results, is(new String[][] {
            { "", "A", "B", "", },
        }));
    }

    /**
     * w/ skip empty + trim.
     * @throws Exception if failed
     */
    @Test
    public void skip_empty_trim() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withSkipEmptyInput(true)
                .withTrimInput(true)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .withField(self(), field(2).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { " ", "A", "B", " ", " ", "C", " ", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", "C", },
        }));
    }

    /**
     * w/ skip empty for field + trim.
     * @throws Exception if failed
     */
    @Test
    public void skip_empty_field_trim() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withTrimInput(true)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .withField(self(), field(2).withSkipEmptyInput(true).build())
                .withField(self(), field(3).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { " ", "A", " ", "B", " ", },
        });
        assertThat(results, is(new String[][] {
            { "", "A", "B", "", },
        }));
    }

    /**
     * w/ malformed field.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void malformed() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), malformField(1).build())
                .withField(self(), field(2).build())
                .build();
        collect(def, new String[][] {
            { "A", "B", "C", },
        });
    }

    /**
     * w/ malformed field.
     * @throws Exception if failed
     */
    @Test
    public void malformed_ignore() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), malformField(1).withOnMalformedInput(ErrorAction.IGNORE).build())
                .withField(self(), field(2).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "A", "B", "C", },
        });
        assertThat(results, is(new String[][] {
            { "A", null, "C", },
        }));
    }

    /**
     * w/ malformed field.
     * @throws Exception if failed
     */
    @Test
    public void malformed_report() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withField(self(), field(0).build())
                .withField(self(), malformField(1).withOnMalformedInput(ErrorAction.REPORT).build())
                .withField(self(), field(2).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "A", "B", "C", },
        });
        assertThat(results, is(new String[][] {
            { "A", null, "C", },
        }));
    }

    /**
     * header w/ force.
     * @throws Exception if failed
     */
    @Test
    public void header_force() throws Exception {
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
     * header w/ force.
     * @throws Exception if failed
     */
    @Test
    public void header_force_only() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.FORCE)
                .withField(self(), field(0).build())
                .withField(self(), field(1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "A", "B", },
        });
        assertThat(results, is(new String[0][]));
    }

    /**
     * header w/ ignore - match.
     * @throws Exception if failed
     */
    @Test
    public void header_ignore_match() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "a", "b", },
            { "A", "B", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
        }));
    }

    /**
     * header w/ ignore - mismatch.
     * @throws Exception if failed
     */
    @Test
    public void header_ignore_mismatch() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "A", "B", },
            { "C", "D", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
            { "C", "D", },
        }));
    }

    /**
     * header w/ ignore - skip empty.
     * @throws Exception if failed
     */
    @Test
    public void header_ignore_skip_empty() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).withSkipEmptyInput(true).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "a", "", "b", },
            { "A", "", "B", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
        }));
    }

    /**
     * header w/ ignore - mismatch.
     * @throws Exception if failed
     */
    @Test
    public void header_ignore_null() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "a", null, },
            { "A", "B", },
        });
        assertThat(results, is(new String[][] {
            { "a", null, },
            { "A", "B", },
        }));
    }

    /**
     * header w/ ignore - less.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void header_ignore_less() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).build())
                .build();
        collect(def, new String[][] {
            { "a", },
        });
    }

    /**
     * header w/ ignore - less.
     * @throws Exception if failed
     */
    @Test
    public void header_ignore_less_ignore() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withOnLessInput(ErrorAction.IGNORE)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "a", },
            { "A", "B", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
        }));
    }

    /**
     * header w/ ignore - less.
     * @throws Exception if failed
     */
    @Test
    public void header_ignore_less_report() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withOnLessInput(ErrorAction.REPORT)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "a", },
            { "A", "B", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
        }));
    }

    /**
     * header w/ ignore - mismatch.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void header_ignore_more() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).build())
                .build();
        collect(def, new String[][] {
            { "a", "b", "c", },
        });
    }

    /**
     * header w/ ignore - mismatch.
     * @throws Exception if failed
     */
    @Test
    public void header_ignore_more_ignore() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withOnMoreInput(ErrorAction.IGNORE)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "a", "b", "c", },
            { "A", "B", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
        }));
    }

    /**
     * header w/ ignore - more.
     * @throws Exception if failed
     */
    @Test
    public void header_ignore_more_report() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withOnMoreInput(ErrorAction.REPORT)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "a", "b", "c", },
            { "A", "B", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
        }));
    }

    /**
     * header w/ ignore - more.
     * @throws Exception if failed
     */
    @Test
    public void header_ignore_more_skip() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withOnMoreInput(ErrorAction.IGNORE)
                .withSkipEmptyInput(true)
                .withField(self(), field("a", 0).build())
                .withField(self(), field("b", 1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { "", "a", "", "b", "", },
            { "", "A", "", "B", "", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
        }));
    }

    /**
     * header w/ ignore - more.
     * @throws Exception if failed
     */
    @Test
    public void header_ignore_more_skip_trim() throws Exception {
        RecordDefinition<String[]> def = RecordDefinition.builder(String[].class)
                .withHeaderType(HeaderType.SKIP)
                .withOnMoreInput(ErrorAction.IGNORE)
                .withSkipEmptyInput(true)
                .withTrimInput(true)
                .withField(self(), field(" a", 0).build())
                .withField(self(), field("b ", 1).build())
                .build();
        String[][] results = collect(def, new String[][] {
            { " ", " a ", " ", " b ", " ", },
            { " ", "A", " ", "B", " ", },
        });
        assertThat(results, is(new String[][] {
            { "A", "B", },
        }));
    }
}
