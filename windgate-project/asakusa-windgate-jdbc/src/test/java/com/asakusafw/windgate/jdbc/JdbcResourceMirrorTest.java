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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.RuntimeContext.ExecutionMode;
import com.asakusafw.runtime.core.context.RuntimeContextKeeper;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.JdbcProcess;

/**
 * Test for {@link JdbcResourceMirror}.
 */
public class JdbcResourceMirrorTest {

    /**
     * Keeps runtime context.
     */
    @Rule
    public final RuntimeContextKeeper rc = new RuntimeContextKeeper();

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
     * Simple source test.
     * @throws Exception if failed
     */
    @Test
    public void source_simple() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello, world!')");
        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            resource.prepare(script);
            try (SourceDriver<Pair> source = resource.createSource(process)) {
                source.prepare();
                test(source, "Hello, world!");
            }
        }
    }

    /**
     * Many objects from source.
     * @throws Exception if failed
     */
    @Test
    public void source_many() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (2, 'Hello2, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (3, 'Hello3, world!')");

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            resource.prepare(script);
            try (SourceDriver<Pair> source = resource.createSource(process)) {
                source.prepare();
                test(source, "Hello1, world!", "Hello2, world!", "Hello3, world!");
            }
        }
    }

    /**
     * Source with condition.
     * @throws Exception if failed
     */
    @Test
    public void source_condition() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.CONDITION.key(), "KEY > 3");

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (2, 'Hello2, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (3, 'Hello3, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (4, 'Hello4, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (5, 'Hello5, world!')");

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            resource.prepare(script);
            try (SourceDriver<Pair> source = resource.createSource(process)) {
                source.prepare();
                test(source, "Hello4, world!", "Hello5, world!");
            }
        }
    }

    /**
     * Source with parameterized condition.
     * @throws Exception if failed
     */
    @Test
    public void source_condition_parameterized() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.CONDITION.key(), "KEY <= ${max}");

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (1, 'Hello1, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (2, 'Hello2, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (3, 'Hello3, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (4, 'Hello4, world!')");
        h2.execute("INSERT INTO PAIR (KEY, VALUE) VALUES (5, 'Hello5, world!')");

        try (JdbcResourceMirror resource = new JdbcResourceMirror(
                profile(),
                new ParameterList(Collections.singletonMap("max", "2")))) {
            resource.prepare(script);
            try (SourceDriver<Pair> source = resource.createSource(process)) {
                source.prepare();
                test(source, "Hello1, world!", "Hello2, world!");
            }
        }
    }

    /**
     * source test in simulated.
     * @throws Exception if failed
     */
    @Test
    public void source_sim() throws Exception {
        RuntimeContext.set(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION));
        h2.execute("DROP TABLE PAIR;");

        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            assertThat(RuntimeContext.get().canExecute(resource), is(true));
            resource.prepare(script);
            try (SourceDriver<Pair> source = resource.createSource(process)) {
                assertThat(RuntimeContext.get().canExecute(source), is(false));
            }
        }
    }

    /**
     * Source with invalid parameterized condition.
     * @throws Exception if failed
     */
    @Test
    public void source_condition_invalid_parameterized() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.CONDITION.key(), "KEY <= ${INVALID}");

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * Source with invalid model.
     * @throws Exception if failed
     */
    @Test
    public void source_invalid_model() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), VoidSupport.class.getName());

        ProcessScript<Void> process = new ProcessScript<>(
                "invalid",
                "testing",
                Void.class,
                new DriverScript("jdbc", conf),
                dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * Simple drain test.
     * @throws Exception if failed
     */
    @Test
    public void drain_simple() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.OPERATION.key(), JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE.value());

        ProcessScript<Pair> process = process(dummy(), new DriverScript("jdbc", conf));
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            resource.prepare(script);
            try (DrainDriver<Pair> drain = resource.createDrain(process)) {
                drain.prepare();
                drain.put(new Pair(1, "Hello, world!"));
            }
            test("Hello, world!");
        }
    }

    /**
     * Many objects into drain.
     * @throws Exception if failed
     */
    @Test
    public void drain_many() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.OPERATION.key(), JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE.value());

        ProcessScript<Pair> process = process(dummy(), new DriverScript("jdbc", conf));
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            resource.prepare(script);
            try (DrainDriver<Pair> drain = resource.createDrain(process)) {
                drain.prepare();
                drain.put(new Pair(1, "Hello1, world!"));
                drain.put(new Pair(2, "Hello2, world!"));
                drain.put(new Pair(3, "Hello3, world!"));
            }
            test("Hello1, world!", "Hello2, world!", "Hello3, world!");
        }
    }

    /**
     * drain test in simulated.
     * @throws Exception if failed
     */
    @Test
    public void drain_sim() throws Exception {
        RuntimeContext.set(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION));

        h2.execute("DROP TABLE PAIR;");

        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.OPERATION.key(), JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE.value());

        ProcessScript<Pair> process = process(dummy(), new DriverScript("jdbc", conf));
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            assertThat(RuntimeContext.get().canExecute(resource), is(true));
            resource.prepare(script);
            try (DrainDriver<Pair> drain = resource.createDrain(process)) {
                assertThat(RuntimeContext.get().canExecute(drain), is(false));
            }
        }
    }

    /**
     * With invalid operation (missing).
     * @throws Exception if failed
     */
    @Test
    public void invalid_drain_operation_missing() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(dummy(), new DriverScript("jdbc", conf));
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * With invalid operation (missing).
     * @throws Exception if failed
     */
    @Test
    public void invalid_drain_operation_unknown() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());
        conf.put(JdbcProcess.OPERATION.key(), "__INVALID");

        ProcessScript<Pair> process = process(dummy(), new DriverScript("jdbc", conf));
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * With invalid table (unspecified).
     * @throws Exception if failed
     */
    @Test
    public void invalid_table_missing() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * With invalid table (empty).
     * @throws Exception if failed
     */
    @Test
    public void invalid_table_empty() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * With invalid columns (missing).
     * @throws Exception if failed
     */
    @Test
    public void invalid_columns_missing() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * With invalid columns (empty).
     * @throws Exception if failed
     */
    @Test
    public void invalid_columns_empty() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * With invalid support (missing).
     * @throws Exception if failed
     */
    @Test
    public void invalid_support_missing() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * With invalid support (unknown classs).
     * @throws Exception if failed
     */
    @Test
    public void invalid_support_unknown() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), "__INVALID");

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * With invalid support (inconsistent support class).
     * @throws Exception if failed
     */
    @Test
    public void invalid_support_class() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), Pair.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * With invalid support (cannot create objects).
     * @throws Exception if failed
     */
    @Test
    public void invalid_support_failnew() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), SupportWithPrivateConstructor.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * With invalid support (inconsistent).
     * @throws Exception if failed
     */
    @Test
    public void invalid_support_inconsistent() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "KEY,VALUE");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), VoidSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * With invalid support (invalid columns).
     * @throws Exception if failed
     */
    @Test
    public void invalid_support_unsupported() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(JdbcProcess.TABLE.key(), "PAIR");
        conf.put(JdbcProcess.COLUMNS.key(), "VALUE,KEY");
        conf.put(JdbcProcess.JDBC_SUPPORT.key(), PairSupport.class.getName());

        ProcessScript<Pair> process = process(new DriverScript("jdbc", conf), dummy());
        GateScript script = script(process);

        try (JdbcResourceMirror resource = new JdbcResourceMirror(profile(), new ParameterList())) {
            try {
                resource.prepare(script);
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    private void test(SourceDriver<Pair> source, String... expected) throws IOException {
        List<Pair> results = new ArrayList<>();
        while (source.next()) {
            Pair pair = source.get();
            results.add(new Pair(pair.key, pair.value));
        }
        Collections.sort(results);
        List<String> actual = new ArrayList<>();
        for (Pair row : results) {
            actual.add(row.value);
        }
        assertThat(actual, is(Arrays.asList(expected)));
    }

    private void test(String... expected) {
        List<List<Object>> results = h2.query("SELECT VALUE FROM PAIR ORDER BY KEY ASC");
        List<String> actual = new ArrayList<>();
        for (List<Object> row : results) {
            actual.add((String) row.get(0));
        }
        assertThat(actual, is(Arrays.asList(expected)));
    }

    private GateScript script(ProcessScript<?>... processes) {
        return new GateScript("testing", Arrays.<ProcessScript<?>>asList(processes));
    }

    private ProcessScript<Pair> process(DriverScript source, DriverScript drain) {
        return new ProcessScript<>(
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
