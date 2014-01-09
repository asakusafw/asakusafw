/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.JdbcProcess;

/**
 * Test for {@link JdbcResourceManipulator}.
 * @since 0.2.2
 */
public class JdbcResourceManipulatorTest {

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
     * Cleanups source.
     * @throws Exception if failed
     */
    @Test
    public void cleanupSource() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (2, 'Hello2, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (3, 'Hello3, world!')");

        JdbcResourceManipulator manipulator = new JdbcResourceManipulator(profile(), new ParameterList());

        assertThat(h2.count("PAIR"), is(3));
        manipulator.cleanupSource(process);
        assertThat(h2.count("PAIR"), is(0));
    }

    /**
     * Cleanups source with condition.
     * @throws Exception if failed
     */
    @Test
    public void cleanupSource_with_condition() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.CONDITION.key(), "KEY = 10");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (2, 'Hello2, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (3, 'Hello3, world!')");

        JdbcResourceManipulator manipulator = new JdbcResourceManipulator(profile(), new ParameterList());

        assertThat(h2.count("PAIR"), is(3));
        manipulator.cleanupSource(process);
        assertThat(h2.count("PAIR"), is(0));
    }

    /**
     * Cleanups source using modified truncate statement.
     * @throws Exception if failed
     */
    @Test
    public void cleanupSource_modified() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (2, 'Hello2, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (3, 'Hello3, world!')");

        JdbcProfile profile = profile();
        profile.setTruncateStatement("DELETE FROM {0} WHERE KEY = 1");
        JdbcResourceManipulator manipulator = new JdbcResourceManipulator(profile, new ParameterList());

        assertThat(h2.count("PAIR"), is(3));
        manipulator.cleanupSource(process);
        assertThat(h2.count("PAIR"), is(2));
    }

    /**
     * Attempts to cleanup source but the table is missing.
     * @throws Exception if failed
     */
    @Test
    public void cleanupSource_missing_table() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(JdbcProcess.TABLE.key(), "MISSING");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());

        JdbcResourceManipulator manipulator = new JdbcResourceManipulator(profile(), new ParameterList());
        manipulator.cleanupSource(process);
        // green
    }

    /**
     * Cleanups drain.
     * @throws Exception if failed
     */
    @Test
    public void cleanupDrain() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.OPERATION.key(), JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE.value());

        ProcessScript<Pair> process = process(dummy(), new DriverScript("jdbc", conf));

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (2, 'Hello2, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (3, 'Hello3, world!')");

        JdbcResourceManipulator manipulator = new JdbcResourceManipulator(profile(), new ParameterList());

        assertThat(h2.count("PAIR"), is(3));
        manipulator.cleanupDrain(process);
        assertThat(h2.count("PAIR"), is(0));
    }

    /**
     * Cleanups drain using modified truncate statement.
     * @throws Exception if failed
     */
    @Test
    public void cleanupDrain_modified() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.OPERATION.key(), JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE.value());

        ProcessScript<Pair> process = process(dummy(), new DriverScript("jdbc", conf));

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (2, 'Hello2, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (3, 'Hello3, world!')");

        JdbcProfile profile = profile();
        profile.setTruncateStatement("DELETE FROM {0} WHERE KEY IN (1, 2)");
        JdbcResourceManipulator manipulator = new JdbcResourceManipulator(profile, new ParameterList());

        assertThat(h2.count("PAIR"), is(3));
        manipulator.cleanupDrain(process);
        assertThat(h2.count("PAIR"), is(1));
    }

    /**
     * Attempts to cleanup drain but the table is missing.
     * @throws Exception if failed
     */
    @Test
    public void cleanupDrain_missing_table() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(JdbcProcess.TABLE.key(), "__MISSING__");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.OPERATION.key(), JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE.value());

        ProcessScript<Pair> process = process(dummy(), new DriverScript("jdbc", conf));
        JdbcResourceManipulator manipulator = new JdbcResourceManipulator(profile(), new ParameterList());

        manipulator.cleanupDrain(process);
        // green
    }

    /**
     * Test method for {@link JdbcResourceManipulator#createSourceForSource(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void createSourceForSource() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.CONDITION.key(), "KEY > 2"); // should be ignored
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (2, 'Hello2, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (3, 'Hello3, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (4, 'Hello4, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (5, 'Hello5, world!')");

        JdbcResourceManipulator manipulator = new JdbcResourceManipulator(profile(), new ParameterList());

        SourceDriver<Pair> driver = manipulator.createSourceForSource(process);
        try {
            driver.prepare();
            test(driver, "Hello3, world!", "Hello4, world!", "Hello5, world!");
        } finally {
            driver.close();
        }
    }

    /**
     * Test method for {@link JdbcResourceManipulator#createDrainForSource(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void createDrainForSource() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.CONDITION.key(), "KEY > 10"); // should be ignored
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());

        JdbcResourceManipulator manipulator = new JdbcResourceManipulator(profile(), new ParameterList());
        DrainDriver<Pair> driver = manipulator.createDrainForSource(process);
        try {
            driver.prepare();
            driver.put(new Pair(2, "Hello2, world!"));
            driver.put(new Pair(3, "Hello3, world!"));
        } finally {
            driver.close();
        }

        test("Hello1, world!", "Hello2, world!", "Hello3, world!");
    }

    /**
     * Test method for {@link JdbcResourceManipulator#createSourceForDrain(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void createSourceForDrain() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.OPERATION.key(), JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE.value());

        ProcessScript<Pair> process = process(dummy(), new DriverScript("jdbc", conf));

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (2, 'Hello2, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (3, 'Hello3, world!')");

        JdbcResourceManipulator manipulator = new JdbcResourceManipulator(profile(), new ParameterList());

        SourceDriver<Pair> driver = manipulator.createSourceForDrain(process);
        try {
            driver.prepare();
            test(driver, "Hello1, world!", "Hello2, world!", "Hello3, world!");
        } finally {
            driver.close();
        }
    }

    /**
     * Test method for {@link JdbcResourceManipulator#createDrainForDrain(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void createDrainForDrain() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.OPERATION.key(), JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE.value());

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");

        ProcessScript<Pair> process = process(dummy(), new DriverScript("jdbc", conf));

        JdbcResourceManipulator manipulator = new JdbcResourceManipulator(profile(), new ParameterList());
        DrainDriver<Pair> driver = manipulator.createDrainForDrain(process);
        try {
            driver.prepare();
            driver.put(new Pair(2, "Hello2, world!"));
            driver.put(new Pair(3, "Hello3, world!"));
        } finally {
            driver.close();
        }

        test("Hello1, world!", "Hello2, world!", "Hello3, world!");
    }

    private void test(SourceDriver<Pair> source, String... expected) throws IOException {
        List<Pair> results = new ArrayList<Pair>();
        while (source.next()) {
            Pair pair = source.get();
            results.add(new Pair(pair.key, pair.value));
        }
        Collections.sort(results);
        List<String> actual = new ArrayList<String>();
        for (Pair row : results) {
            actual.add(row.value);
        }
        assertThat(actual, is(Arrays.asList(expected)));
    }

    private void test(String... expected) {
        List<List<Object>> results = h2.query("SELECT VALUE FROM PAIR ORDER BY KEY ASC");
        List<String> actual = new ArrayList<String>();
        for (List<Object> row : results) {
            actual.add((String) row.get(0));
        }
        assertThat(actual, is(Arrays.asList(expected)));
    }

    private ProcessScript<Pair> process(DriverScript source, DriverScript drain) {
        return new ProcessScript<Pair>(
                "testing",
                "dummy",
                Pair.class,
                source,
                drain
        );
    }

    private DriverScript dummy() {
        return new DriverScript("dummy", Collections.<String, String>emptyMap());
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
