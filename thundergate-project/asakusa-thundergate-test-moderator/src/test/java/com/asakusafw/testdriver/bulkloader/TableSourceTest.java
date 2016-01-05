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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * @since 0.2.0
 */
public class TableSourceTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<Simple>(Simple.class);

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
     * empty.
     * @throws Exception if occur
     */
    @Test
    public void empty() throws Exception {
        TableSource<Simple> source = new TableSource<Simple>(info("NUMBER", "TEXT"), h2.open());
        try {
            assertThat(next(source), is(nullValue()));
        } finally {
            source.close();
        }
    }

    /**
     * single data.
     * @throws Exception if occur
     */
    @Test
    public void single() throws Exception {
        Simple s1 = new Simple();
        s1.number = 100;
        s1.text = "Hello, world!";
        insert(s1);

        TableSource<Simple> source = new TableSource<Simple>(info("NUMBER", "TEXT"), h2.open());
        try {
            assertThat(next(source), is(s1));
            assertThat(next(source), is(nullValue()));
        } finally {
            source.close();
        }
    }

    /**
     * multiple data.
     * @throws Exception if occur
     */
    @Test
    public void multiple() throws Exception {
        Simple s1 = new Simple();
        s1.number = 100;
        s1.text = "aaa";
        insert(s1);

        Simple s2 = new Simple();
        s2.number = 200;
        s2.text = "bbb";
        insert(s2);

        Simple s3 = new Simple();
        s3.number = 300;
        s3.text = "ccc";
        insert(s3);

        TableSource<Simple> source = new TableSource<Simple>(info("NUMBER", "TEXT"), h2.open());
        try {
            LinkedList<Simple> results = new LinkedList<Simple>();
            results.addLast(next(source));
            assertThat(results.getLast(), not(nullValue()));
            results.addLast(next(source));
            assertThat(results.getLast(), not(nullValue()));
            results.addLast(next(source));
            assertThat(results.getLast(), not(nullValue()));
            assertThat(next(source), is(nullValue()));

            Collections.sort(results, new Comparator<Simple>() {
                @Override
                public int compare(Simple o1, Simple o2) {
                    return o1.number.compareTo(o2.number);
                }
            });
            assertThat(results, is(Arrays.asList(s1, s2, s3)));
        } finally {
            source.close();
        }
    }

    /**
     * all property types.
     * @throws Exception if occur
     */
    @Test
    public void allTypes() throws Exception {
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
        insert(simple);

        TableSource<Simple> source = new TableSource<Simple>(all(), h2.open());
        try {
            assertThat(next(source), is(simple));
            assertThat(next(source), is(nullValue()));
        } finally {
            source.close();
        }
    }

    /**
     * null property values.
     * @throws Exception if occur
     */
    @Test
    public void nullValues() throws Exception {
        Simple simple = new Simple();
        simple.number = 100;
        insert(simple);

        TableSource<Simple> source = new TableSource<Simple>(all(), h2.open());
        try {
            assertThat(next(source), is(simple));
            assertThat(next(source), is(nullValue()));
        } finally {
            source.close();
        }
    }

    /**
     * re-closed.
     * @throws Exception if occur
     */
    @Test
    public void reclose() throws Exception {
        TableSource<Simple> source = new TableSource<Simple>(info("NUMBER", "TEXT"), h2.open());
        source.close();
        source.close();
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
            TableSource<Simple> source = new TableSource<Simple>(info("NUMBER", "TEXT"), conn);
            try {
                source.next();
                source.close();
            } finally {
                source.close();
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
    public void dropNext() throws Exception {
        h2.execute("DROP TABLE SIMPLE");
        Connection conn = h2.open();
        try {
            TableSource<Simple> source = new TableSource<Simple>(info("NUMBER", "TEXT"), conn);
            try {
                h2.execute("DROP TABLE SIMPLE");
                source.next();
                source.close();
            } finally {
                source.close();
            }
        } finally {
            conn.close();
        }
    }

    private void insert(Simple simple) {
        try {
            TableOutput<Simple> output = new TableOutput<Simple>(all(), h2.open());
            try {
                output.write(simple);
            } finally {
                output.close();
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private TableInfo<Simple> all() {
        return info(
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
                );
    }

    private Simple next(DataModelSource source) throws IOException {
        DataModelReflection next = source.next();
        if (next != null) {
            return SIMPLE.toObject(next);
        }
        return null;
    }

    private TableInfo<Simple> info(String... columns) {
        return new TableInfo<Simple>(SIMPLE, "SIMPLE", Arrays.asList(columns));
    }
}
