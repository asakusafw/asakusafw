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
package com.asakusafw.testdriver.bulkloader;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * Test for {@link TableOutput}.
 * @since 0.2.0
 */
public class TableOutputTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<Simple>(Simple.class);

    static final DataModelDefinition<CacheSupport> CACHE = new SimpleDataModelDefinition<CacheSupport>(CacheSupport.class);

    /**
     * H2 database.
     */
    @Rule
    public H2Resource h2 = new H2Resource("config") {
        @Override
        protected void before() throws Exception {
            executeFile("ddl-simple.sql");
        }
    };

    /**
     * nothing to write to.
     * @throws Exception if occur
     */
    @Test
    public void empty() throws Exception {
        TableOutput<Simple> output = new TableOutput<Simple>(info("NUMBER", "TEXT"), h2.open());
        output.close();

        assertThat(h2.count("SIMPLE"), is(0));
    }

    /**
     * write an object.
     * @throws Exception if occur
     */
    @Test
    public void single() throws Exception {
        TableOutput<Simple> output = new TableOutput<Simple>(info("NUMBER", "TEXT"), h2.open());
        try {
            Simple simple = new Simple();
            simple.number = 100;
            simple.text = "Hello, world!";
            output.write(simple);
        } finally {
            output.close();
        }

        assertThat(h2.count("SIMPLE"), is(1));
        List<List<Object>> results = h2.query("SELECT NUMBER, TEXT FROM SIMPLE ORDER BY NUMBER ASC");
        assertThat(results, is(table(
                row(100, "Hello, world!"))));
    }

    /**
     * write multiple objects.
     * @throws Exception if occur
     */
    @Test
    public void multiple() throws Exception {
        TableOutput<Simple> output = new TableOutput<Simple>(info("NUMBER", "TEXT"), h2.open());
        try {
            Simple simple = new Simple();
            simple.number = 100;
            simple.text = "aaa";
            output.write(simple);

            simple.number = 200;
            simple.text = "bbb";
            output.write(simple);

            simple.number = 300;
            simple.text = "ccc";
            output.write(simple);
        } finally {
            output.close();
        }

        assertThat(h2.count("SIMPLE"), is(3));
        List<List<Object>> results = h2.query("SELECT NUMBER, TEXT FROM SIMPLE ORDER BY NUMBER ASC");
        assertThat(results, is(table(
                row(100, "aaa"),
                row(200, "bbb"),
                row(300, "ccc"))));
    }

    /**
     * write object with all supported types.
     * @throws Exception if occur
     */
    @Test
    public void allType() throws Exception {
        Simple simple = new Simple();
        simple.number = 100;
        simple.text = "aaa";
        simple.booleanValue = true;
        simple.byteValue = 2;
        simple.shortValue = 3;
        simple.longValue = 4L;
        simple.floatValue = 1.5f;
        simple.doubleValue = 2.5d;
        simple.bigDecimalValue = new BigDecimal("3.14");

        simple.dateValue = Calendar.getInstance();
        simple.dateValue.clear();
        simple.dateValue.set(2011, 2, 31);

        simple.timeValue = Calendar.getInstance();
        simple.timeValue.clear();
        simple.timeValue.set(Calendar.HOUR_OF_DAY, 13);
        simple.timeValue.set(Calendar.MINUTE, 14);
        simple.timeValue.set(Calendar.SECOND, 15);

        simple.datetimeValue = Calendar.getInstance();
        simple.datetimeValue.clear();
        simple.datetimeValue.set(2000, 0, 2, 3, 4, 5);

        TableOutput<Simple> output = new TableOutput<Simple>(info(
                "NUMBER",
                "TEXT",
                "C_BOOL",
                "C_BYTE",
                "C_SHORT",
                "C_LONG",
                "C_FLOAT",
                "C_DOUBLE",
                "C_DECIMAL",
                "C_DATE",
                "C_TIME",
                "C_DATETIME"
                ), h2.open());
        try {
            output.write(simple);
        } finally {
            output.close();
        }

        assertThat(h2.count("SIMPLE"), is(1));
        List<List<Object>> results = h2.query("SELECT * FROM SIMPLE ORDER BY NUMBER ASC");

        List<Object> row = results.get(0);
        assertThat(row.get(0), is((Object) simple.number));
        assertThat(row.get(1), is((Object) simple.text));
        assertThat(row.get(2), is((Object) simple.booleanValue));
        assertThat(row.get(3), is((Object) simple.byteValue));
        assertThat(row.get(4), is((Object) simple.shortValue));
        assertThat(row.get(5), is((Object) simple.longValue));
        assertThat(row.get(6), isOneOf((Object) simple.floatValue, (Object) simple.floatValue.doubleValue()));
        assertThat(row.get(7), is((Object) simple.doubleValue));
        assertThat(row.get(8), is((Object) simple.bigDecimalValue));
        assertThat(row.get(9), is((Object) new java.sql.Date(simple.dateValue.getTimeInMillis())));
        assertThat(row.get(10), is((Object) new java.sql.Time(simple.timeValue.getTimeInMillis())));
        assertThat(row.get(11), is((Object) new java.sql.Timestamp(simple.datetimeValue.getTimeInMillis())));
    }

    /**
     * write multiple objects.
     * @throws Exception if occur
     */
    @Test
    public void nullValues() throws Exception {
        Simple simple = new Simple();
        simple.number = 100;

        TableOutput<Simple> output = new TableOutput<Simple>(info(
                "NUMBER",
                "TEXT",
                "C_BOOL",
                "C_BYTE",
                "C_SHORT",
                "C_LONG",
                "C_FLOAT",
                "C_DOUBLE",
                "C_DECIMAL",
                "C_DATE",
                "C_TIME",
                "C_DATETIME"
                ), h2.open());
        try {
            output.write(simple);
        } finally {
            output.close();
        }

        assertThat(h2.count("SIMPLE"), is(1));
        List<List<Object>> results = h2.query("SELECT * FROM SIMPLE ORDER BY NUMBER ASC");

        List<Object> row = results.get(0);
        assertThat(row.get(0), is((Object) simple.number));
        for (int i = 1, n = row.size(); i < n; i++) {
            assertThat(String.valueOf(i), row.get(i), is(nullValue()));
        }
    }

    /**
     * implicit timestamp.
     * @throws Exception if occur
     */
    @Test
    public void timestamp() throws Exception {
        TableOutput<CacheSupport> output = new TableOutput<CacheSupport>(
                new TableInfo<CacheSupport>(CACHE, "SIMPLE", Arrays.asList("NUMBER", "TEXT")),
                h2.open());
        try {
            CacheSupport simple = new CacheSupport();
            simple.number = 100;
            simple.text = "Hello, world!";
            output.write(simple);
        } finally {
            output.close();
        }

        assertThat(h2.count("SIMPLE"), is(1));
        List<List<Object>> results = h2.query("SELECT NUMBER, TEXT, C_DATETIME FROM SIMPLE ORDER BY NUMBER ASC");
        assertThat(results.size(), is(1));
        assertThat(results.get(0).get(2), is(notNullValue()));
    }

    /**
     * overwrite timestamp.
     * @throws Exception if occur
     */
    @Test
    public void timestamp_overwrite() throws Exception {
        TableOutput<CacheSupport> output = new TableOutput<CacheSupport>(
                new TableInfo<CacheSupport>(CACHE, "SIMPLE", Arrays.asList("NUMBER", "TEXT", "C_DATETIME")),
                h2.open());
        try {
            CacheSupport simple = new CacheSupport();
            simple.number = 100;
            simple.text = "Hello, world!";
            simple.datetimeValue = Calendar.getInstance();
            simple.datetimeValue.setTimeInMillis(1);
            output.write(simple);
        } finally {
            output.close();
        }

        assertThat(h2.count("SIMPLE"), is(1));
        List<List<Object>> results = h2.query("SELECT C_DATETIME FROM SIMPLE ORDER BY NUMBER ASC");
        assertThat(results.size(), is(1));
        Timestamp timestamp = (Timestamp) results.get(0).get(0);
        assertThat(timestamp, is(notNullValue()));
        assertThat(timestamp.getTime(), is(0L));
    }

    /**
     * suppresses overwrite timestamp.
     * @throws Exception if occur
     */
    @Test
    public void timestamp_suppress_overwrite() throws Exception {
        TableOutput<CacheSupport> output = new TableOutput<CacheSupport>(
                new TableInfo<CacheSupport>(CACHE, "SIMPLE", Arrays.asList("NUMBER", "TEXT", "C_DATETIME"), false),
                h2.open());
        try {
            CacheSupport simple = new CacheSupport();
            simple.number = 100;
            simple.text = "Hello, world!";
            simple.datetimeValue = Calendar.getInstance();
            simple.datetimeValue.setTimeInMillis(1);
            output.write(simple);
        } finally {
            output.close();
        }

        assertThat(h2.count("SIMPLE"), is(1));
        List<List<Object>> results = h2.query("SELECT C_DATETIME FROM SIMPLE ORDER BY NUMBER ASC");
        assertThat(results.size(), is(1));
        Timestamp timestamp = (Timestamp) results.get(0).get(0);
        assertThat(timestamp, is(notNullValue()));
        assertThat(timestamp.getTime(), is(1L));
    }

    /**
     * re-closed.
     * @throws Exception if occur
     */
    @Test
    public void reclose() throws Exception {
        TableOutput<Simple> output = new TableOutput<Simple>(info("NUMBER", "TEXT"), h2.open());
        output.close();
        output.close();
    }

    /**
     * dropped.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void dropCtor() throws Exception {
        h2.execute("DROP TABLE SIMPLE");
        Connection conn = h2.open();
        try {
            TableOutput<Simple> output = new TableOutput<Simple>(info("NUMBER", "TEXT"), conn);
            try {
                Simple simple = new Simple();
                simple.number = 100;
                output.write(simple);
            } finally {
                output.close();
            }

        } finally {
            conn.close();
        }
    }

    /**
     * dropped.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void dropWrite() throws Exception {
        Connection conn = h2.open();
        try {
            TableOutput<Simple> output = new TableOutput<Simple>(info("NUMBER", "TEXT"), conn);
            try {
                h2.execute("DROP TABLE SIMPLE");
                Simple simple = new Simple();
                simple.number = 100;
                output.write(simple);
            } finally {
                output.close();
            }
        } finally {
            conn.close();
        }
    }

    private TableInfo<Simple> info(String... columns) {
        return new TableInfo<Simple>(SIMPLE, "SIMPLE", Arrays.asList(columns));
    }

    private List<List<Object>> table(Object[]... rows) {
        List<List<Object>> results = new ArrayList<List<Object>>();
        for (Object[] row : rows) {
            results.add(Arrays.asList(row));
        }
        return results;
    }

    private Object[] row(Object... cells) {
        return cells;
    }
}
