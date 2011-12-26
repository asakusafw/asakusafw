/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.windgate.jdbc;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

/**
 * Test for {@link JdbcSourceDriver}.
 */
public class JdbcSourceDriverTest {

    /**
     * Test database.
     */
    @Rule
    public H2Resource h2 = new H2Resource("testing") {
        @Override
        protected void before() throws Exception {
            executeFile("pair.sql");
        }
    };

    /**
     * simple access.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello, world!')");
        Connection conn = h2.open();
        try {
            JdbcScript<Pair> script = new JdbcScript<Pair>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            JdbcSourceDriver<Pair> driver = new JdbcSourceDriver<Pair>(profile(), script, conn, new Pair());
            driver.prepare();
            List<String> values = values(driver);
            driver.close();

            assertThat(values, is(Arrays.asList("Hello, world!")));
        } finally {
            conn.close();
        }
    }

    /**
     * Empty rows.
     * @throws Exception if failed
     */
    @Test
    public void empty() throws Exception {
        Connection conn = h2.open();
        try {
            JdbcScript<Pair> script = new JdbcScript<Pair>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            JdbcSourceDriver<Pair> driver = new JdbcSourceDriver<Pair>(profile(), script, conn, new Pair());
            driver.prepare();
            List<String> values = values(driver);
            driver.close();

            assertThat(values, is(Arrays.<String>asList()));
        } finally {
            conn.close();
        }
    }

    /**
     * many rows.
     * @throws Exception if failed
     */
    @Test
    public void large() throws Exception {
        List<String> answer = new ArrayList<String>();
        for (int i = 0; i < 3000; i++) {
            String value = MessageFormat.format(
                    "Hello{0}",
                    String.valueOf(i + 1));
            answer.add(value);
            h2.execute(MessageFormat.format(
                    "INSERT INTO PAIR (KEY, VALUE) VALUES ({0}, ''{1}'')",
                    String.valueOf(i + 1),
                    value));
        }
        Connection conn = h2.open();
        try {
            JdbcScript<Pair> script = new JdbcScript<Pair>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            JdbcSourceDriver<Pair> driver = new JdbcSourceDriver<Pair>(profile(), script, conn, new Pair());
            driver.prepare();
            List<String> values = values(driver);
            driver.close();

            assertThat(values, is(answer));
        } finally {
            conn.close();
        }
    }

    /**
     * with condition.
     * @throws Exception if failed
     */
    @Test
    public void condition() throws Exception {
        for (int i = 0; i < 100; i++) {
            String value = MessageFormat.format(
                    "Hello{0}",
                    String.valueOf(i + 1));
            h2.execute(MessageFormat.format(
                    "INSERT INTO PAIR (KEY, VALUE) VALUES ({0}, ''{1}'')",
                    String.valueOf(i + 1),
                    value));
        }
        Connection conn = h2.open();
        try {
            JdbcScript<Pair> script = new JdbcScript<Pair>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    "KEY BETWEEN 80 AND 81");
            JdbcSourceDriver<Pair> driver = new JdbcSourceDriver<Pair>(profile(), script, conn, new Pair());
            driver.prepare();
            List<String> values = values(driver);
            driver.close();

            assertThat(values, is(Arrays.asList("Hello80", "Hello81")));
        } finally {
            conn.close();
        }
    }

    /**
     * Whether close method suppresses error if driver is already wrong.
     * @throws Exception if failed
     */
    @Test
    public void suppress_error_on_close() throws Exception {
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello, world!')");
        Connection conn = h2.open();
        try {
            JdbcScript<Pair> script = new JdbcScript<Pair>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            JdbcSourceDriver<Pair> driver = new JdbcSourceDriver<Pair>(profile(), script, conn, new Pair());
            driver.prepare();
            conn.close();
            try {
                driver.next();
                fail();
            } catch (IOException e) {
                // ok.
            }
            driver.close();
        } finally {
            conn.close();
        }
    }

    private List<String> values(JdbcSourceDriver<Pair> driver) throws IOException {
        List<Pair> results = new ArrayList<Pair>();
        while (driver.next()) {
            Pair got = driver.get();
            Pair copy = new Pair();
            copy.key = got.key;
            copy.value = got.value;
            results.add(copy);
        }
        Collections.sort(results);

        List<String> values = new ArrayList<String>();
        for (Pair p : results) {
            values.add(p.value);
        }
        return values;
    }

    private JdbcProfile profile() {
        return new JdbcProfile(
                "jdbc",
                null,
                org.h2.Driver.class.getName(),
                h2.getJdbcUrl(),
                null,
                null,
                100);
    }
}
