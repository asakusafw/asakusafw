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
package com.asakusafw.windgate.jdbc;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.resource.ResourceProfile;

/**
 * Test for {@link JdbcProfile}.
 */
public class JdbcProfileTest {

    /**
     * Test database.
     */
    @Rule
    public H2Resource h2 = new H2Resource("testing") {
        @Override
        protected void before() throws Exception {
            executeFile("simple.sql");
        }
    };

    /**
     * Minimum profile.
     * @throws Exception if failed
     */
    @Test
    public void convert() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put(JdbcProfile.KEY_DRIVER, org.h2.Driver.class.getName());
        map.put(JdbcProfile.KEY_URL, h2.getJdbcUrl());

        ResourceProfile rp = toProfile(map);
        JdbcProfile profile = JdbcProfile.convert(rp);
        assertThat(profile.getResourceName(), is(rp.getName()));
        assertThat(profile.getBatchPutUnit(), greaterThan(0L));
        try (Connection conn = profile.openConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO SIMPLE (VALUE) VALUES ('Hello, world!')");
            conn.commit();
        }
        assertThat(h2.count("SIMPLE"), is(1));
    }

    /**
     * Fully specified profile.
     * @throws Exception if failed
     */
    @Test
    public void convert_all() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put(JdbcProfile.KEY_DRIVER, org.h2.Driver.class.getName());
        map.put(JdbcProfile.KEY_URL, h2.getJdbcUrl());
        map.put(JdbcProfile.KEY_USER, "");
        map.put(JdbcProfile.KEY_PASSWORD, "");
        map.put(JdbcProfile.KEY_BATCH_GET_UNIT, "5000");
        map.put(JdbcProfile.KEY_BATCH_PUT_UNIT, "10000");
        map.put(JdbcProfile.KEY_CONNECT_RETRY_COUNT, "3");
        map.put(JdbcProfile.KEY_CONNECT_RETRY_INTERVAL, "10");
        map.put(JdbcProfile.KEY_TRUNCATE_STATEMENT, "DELETE FROM {0}");
        map.put(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello1", "world1");
        map.put(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello2", "world2");
        map.put(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello3", "world3");
        map.put(JdbcProfile.KEY_OPTIMIZATIONS, " O,p, t");

        JdbcProfile profile = JdbcProfile.convert(toProfile(map));
        assertThat(profile.getBatchGetUnit(), is(5000));
        assertThat(profile.getBatchPutUnit(), is(10000L));
        assertThat(profile.getOptimizations(), containsInAnyOrder("O", "p", "t"));

        Map<String, String> extra = new HashMap<>();
        extra.put("hello1", "world1");
        extra.put("hello2", "world2");
        extra.put("hello3", "world3");
        assertThat(profile.getConnectionProperties(), is(extra));

        assertThat(profile.getTruncateStatement("HELLO").trim(), startsWith("DELETE"));

        try (Connection conn = profile.openConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO SIMPLE (VALUE) VALUES ('Hello, world!')");
            conn.commit();
        }
        assertThat(h2.count("SIMPLE"), is(1));
    }

    /**
     * Fully specified profile with parameterized.
     * @throws Exception if failed
     */
    @Test
    public void convert_parameterized() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put(JdbcProfile.KEY_DRIVER, VariableTable.toVariable(JdbcProfile.KEY_DRIVER));
        map.put(JdbcProfile.KEY_URL, VariableTable.toVariable(JdbcProfile.KEY_URL));
        map.put(JdbcProfile.KEY_USER, VariableTable.toVariable(JdbcProfile.KEY_USER));
        map.put(JdbcProfile.KEY_PASSWORD, VariableTable.toVariable(JdbcProfile.KEY_PASSWORD));
        map.put(JdbcProfile.KEY_BATCH_GET_UNIT, VariableTable.toVariable(JdbcProfile.KEY_BATCH_GET_UNIT));
        map.put(JdbcProfile.KEY_BATCH_PUT_UNIT, VariableTable.toVariable(JdbcProfile.KEY_BATCH_PUT_UNIT));
        map.put(JdbcProfile.KEY_CONNECT_RETRY_COUNT, VariableTable.toVariable(JdbcProfile.KEY_CONNECT_RETRY_COUNT));
        map.put(JdbcProfile.KEY_CONNECT_RETRY_INTERVAL, VariableTable.toVariable(JdbcProfile.KEY_CONNECT_RETRY_INTERVAL));
        map.put(JdbcProfile.KEY_TRUNCATE_STATEMENT, VariableTable.toVariable(JdbcProfile.KEY_TRUNCATE_STATEMENT));
        map.put(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello1", VariableTable.toVariable(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello1"));
        map.put(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello2", VariableTable.toVariable(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello2"));
        map.put(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello3", VariableTable.toVariable(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello3"));

        Map<String, String> parameters = new HashMap<>();
        parameters.put(JdbcProfile.KEY_DRIVER, org.h2.Driver.class.getName());
        parameters.put(JdbcProfile.KEY_URL, h2.getJdbcUrl());
        parameters.put(JdbcProfile.KEY_USER, "");
        parameters.put(JdbcProfile.KEY_PASSWORD, "");
        parameters.put(JdbcProfile.KEY_BATCH_GET_UNIT, "5000");
        parameters.put(JdbcProfile.KEY_BATCH_PUT_UNIT, "10000");
        parameters.put(JdbcProfile.KEY_CONNECT_RETRY_COUNT, "3");
        parameters.put(JdbcProfile.KEY_CONNECT_RETRY_INTERVAL, "10");
        parameters.put(JdbcProfile.KEY_TRUNCATE_STATEMENT, "DELETE FROM {0}");
        parameters.put(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello1", "world1");
        parameters.put(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello2", "world2");
        parameters.put(JdbcProfile.KEY_PREFIX_PROPERTIES + "hello3", "world3");

        JdbcProfile profile = JdbcProfile.convert(toProfile(map, parameters));
        assertThat(profile.getBatchGetUnit(), is(5000));
        assertThat(profile.getBatchPutUnit(), is(10000L));

        Map<String, String> extra = new HashMap<>();
        extra.put("hello1", "world1");
        extra.put("hello2", "world2");
        extra.put("hello3", "world3");
        assertThat(profile.getConnectionProperties(), is(extra));

        assertThat(profile.getTruncateStatement("HELLO").trim(), startsWith("DELETE"));

        try (Connection conn = profile.openConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO SIMPLE (VALUE) VALUES ('Hello, world!')");
            conn.commit();
        }
        assertThat(h2.count("SIMPLE"), is(1));
    }

    /**
     * Attempts to convert a profile with empty configuration.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_empty() throws Exception {
        Map<String, String> map = new HashMap<>();
        JdbcProfile.convert(toProfile(map));
    }

    /**
     * Attempts to convert a profile with negative batch put unit.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_negative_batchPutUnit() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put(JdbcProfile.KEY_DRIVER, org.h2.Driver.class.getName());
        map.put(JdbcProfile.KEY_URL, h2.getJdbcUrl());
        map.put(JdbcProfile.KEY_BATCH_PUT_UNIT, "-1");
        JdbcProfile.convert(toProfile(map));
    }

    /**
     * Attempts to convert a profile with invalid batch put unit.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_invalid_batchPutUnit() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put(JdbcProfile.KEY_DRIVER, org.h2.Driver.class.getName());
        map.put(JdbcProfile.KEY_URL, h2.getJdbcUrl());
        map.put(JdbcProfile.KEY_BATCH_PUT_UNIT, "Hello, world!");
        JdbcProfile.convert(toProfile(map));
    }

    /**
     * Attempts to open connection with invalid driver.
     * @throws Exception if failed
     */
    @Test(expected = Exception.class)
    public void openConnection_invalid_driver() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put(JdbcProfile.KEY_DRIVER, ".INVALID");
        map.put(JdbcProfile.KEY_URL, h2.getJdbcUrl());
        JdbcProfile profile = JdbcProfile.convert(toProfile(map));
        try (Connection conn = profile.openConnection()) {
            // do nothing
        }
    }

    /**
     * Attempts to open connection with invalid URL.
     * @throws Exception if failed
     */
    @Test(expected = Exception.class)
    public void openConnection_invalid_url() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put(JdbcProfile.KEY_DRIVER, org.h2.Driver.class.getName());
        map.put(JdbcProfile.KEY_URL, ".INVALID");
        JdbcProfile profile = JdbcProfile.convert(toProfile(map));
        try (Connection conn = profile.openConnection()) {
            // do nothing
        }
    }

    private ResourceProfile toProfile(Map<String, String> map) {
        return toProfile(map, Collections.emptyMap());
    }

    private ResourceProfile toProfile(Map<String, String> map, Map<String, String> params) {
        return new ResourceProfile(
                "jdbc",
                JdbcResourceProvider.class,
                new ProfileContext(getClass().getClassLoader(), new ParameterList(params)),
                map);
    }
}
