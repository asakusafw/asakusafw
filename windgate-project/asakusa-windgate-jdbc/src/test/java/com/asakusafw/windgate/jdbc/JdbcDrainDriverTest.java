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
package com.asakusafw.windgate.jdbc;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

/**
 * Test for {@link JdbcDrainDriver}.
 */
public class JdbcDrainDriverTest {

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
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        try (Connection conn = h2.open()) {
            JdbcScript<Pair> script = new JdbcScript<>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            try (JdbcDrainDriver<Pair> driver = new JdbcDrainDriver<>(profile(), script, conn, true)) {
                driver.prepare();
                driver.put(new Pair(1, "Hello, world!"));
            }
            test("Hello, world!");
        }
    }

    /**
     * do nothing.
     * @throws Exception if failed
     */
    @Test
    public void empty() throws Exception {
        try (Connection conn = h2.open()) {
            JdbcScript<Pair> script = new JdbcScript<>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            try (JdbcDrainDriver<Pair> driver = new JdbcDrainDriver<>(profile(), script, conn, true)) {
                driver.prepare();
            }
            test();
        }
    }

    /**
     * large case.
     * @throws Exception if failed
     */
    @Test
    public void large() throws Exception {
        try (Connection conn = h2.open()) {
            JdbcScript<Pair> script = new JdbcScript<>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            String[] expected = new String[3333];
            try (JdbcDrainDriver<Pair> driver = new JdbcDrainDriver<>(profile(), script, conn, true)) {
                driver.prepare();
                for (int i = 1; i <= expected.length; i++) {
                    String value = "Hello" + i;
                    expected[i - 1] = value;
                    driver.put(new Pair(i, value));
                }
            }
            test(expected);
        }
    }

    /**
     * large and aligned to put unit.
     * @throws Exception if failed
     */
    @Test
    public void large_align() throws Exception {
        try (Connection conn = h2.open()) {
            JdbcScript<Pair> script = new JdbcScript<>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            String[] expected = new String[1000];
            try (JdbcDrainDriver<Pair> driver = new JdbcDrainDriver<>(profile(), script, conn, true)) {
                driver.prepare();
                for (int i = 1; i <= expected.length; i++) {
                    String value = "Hello" + i;
                    expected[i - 1] = value;
                    driver.put(new Pair(i, value));
                }
            }
            test(expected);
        }
    }

    /**
     * do truncate before put.
     * @throws Exception if failed
     */
    @Test
    public void truncate() throws Exception {
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello, world!')");
        try (Connection conn = h2.open()) {
            JdbcScript<Pair> script = new JdbcScript<>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            try (JdbcDrainDriver<Pair> driver = new JdbcDrainDriver<>(profile(), script, conn, true)) {
                driver.prepare();
                driver.put(new Pair(2, "Other"));
            }
            test("Other");
        }
    }

    /**
     * do truncate before put using delete phrase.
     * @throws Exception if failed
     */
    @Test
    public void truncate_with_delete() throws Exception {
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello, world!')");
        try (Connection conn = h2.open()) {
            conn.setAutoCommit(false);
            JdbcScript<Pair> script = new JdbcScript<>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            JdbcProfile profile = profile();
            profile.setTruncateStatement("DELETE FROM {0}");
            try (JdbcDrainDriver<Pair> driver = new JdbcDrainDriver<>(profile, script, conn, true)) {
                driver.prepare();
                test(new String[]{});
                driver.put(new Pair(2, "Other"));
            }
            test("Other");
        }
    }

    /**
     * do custom truncate before put.
     * @throws Exception if failed
     */
    @Test
    public void custom_truncate() throws Exception {
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (2, 'Hello2')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (3, 'Hello2')");
        try (Connection conn = h2.open()) {
            JdbcScript<Pair> script = new JdbcScript<>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null,
                    "DELETE FROM PAIR WHERE KEY = 2");
            try (JdbcDrainDriver<Pair> driver = new JdbcDrainDriver<>(profile(), script, conn, true)) {
                driver.prepare();
                driver.put(new Pair(2, "Other"));
            }
            test("Hello1", "Other", "Hello2");
        }
    }

    /**
     * Suppresses doing truncate before put.
     * @throws Exception if failed
     */
    @Test
    public void suppress_truncate() throws Exception {
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello, world!')");
        try (Connection conn = h2.open()) {
            JdbcScript<Pair> script = new JdbcScript<>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            try (JdbcDrainDriver<Pair> driver = new JdbcDrainDriver<>(profile(), script, conn, false)) {
                driver.prepare();
                driver.put(new Pair(2, "Other"));
            }
            test("Hello, world!", "Other");
        }
    }

    /**
     * Whether close method suppresses error if driver is already wrong.
     * @throws Exception if failed
     */
    @Test
    public void suppress_error_on_close() throws Exception {
        try (Connection conn = h2.open()) {
            JdbcScript<Pair> script = new JdbcScript<>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            try (JdbcDrainDriver<Pair> driver = new JdbcDrainDriver<>(profile(), script, conn, true)) {
                driver.prepare();
                conn.close();
                try {
                    driver.put(new Pair(1, "Hello, world!"));
                    fail();
                } catch (IOException e) {
                    // ok.
                }
            }
        }
    }

    /**
     * Whether close method aware commit failure.
     * @throws Exception if failed
     */
    @Test
    public void aware_commit_failure() throws Exception {
        try (Connection conn = h2.open()) {
            JdbcScript<Pair> script = new JdbcScript<>(
                    "testing",
                    new PairSupport(),
                    "PAIR",
                    Arrays.asList("KEY", "VALUE"),
                    null);
            try (JdbcDrainDriver<Pair> driver = new JdbcDrainDriver<>(profile(), script, conn, true)) {
                driver.prepare();
                driver.put(new Pair(1, "Hello, world!"));
                conn.close();
                try {
                    driver.close();
                    fail();
                } catch (IOException e) {
                    // ok.
                }
            }
        }
    }

    private void test(String... expected) {
        List<List<Object>> results = h2.query("SELECT VALUE FROM PAIR ORDER BY KEY ASC");
        List<String> actual = new ArrayList<>();
        for (List<Object> row : results) {
            actual.add((String) row.get(0));
        }
        assertThat(actual, is(Arrays.asList(expected)));
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
