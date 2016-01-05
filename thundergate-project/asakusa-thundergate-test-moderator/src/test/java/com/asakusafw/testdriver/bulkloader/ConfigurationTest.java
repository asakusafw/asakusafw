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
import java.sql.Connection;

import org.junit.Rule;
import org.junit.Test;

/**
 * Test for {@link Configuration}.
 */
public class ConfigurationTest {

    /**
     * H2 database.
     */
    @Rule
    public H2Resource h2 = new H2Resource("config") {
        @Override
        protected void before() throws Exception {
            execute("CREATE TABLE TESTING(" +
                    "  SID IDENTITY PRIMARY KEY," +
                    "  NUMBER INT," +
                    "  TEXT VARCHAR(250)" +
                    ")");
        }
    };

    /**
     * Configuration helper.
     */
    @Rule
    public ConfigurationContext context = new ConfigurationContext();

    /**
     * missing config.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void missing() throws Exception {
        Configuration.load("CONFIGURATIONMISSING");
    }

    /**
     * mismatch config.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void mismatch() throws Exception {
        context.put("mismatch", "config");
        Configuration.load("config");
    }

    /**
     * using targetted config.
     * @throws Exception if occur
     */
    @Test
    public void target() throws Exception {
        context.put("config", "config");
        Configuration conf = Configuration.load("config");
        Connection conn = conf.open();
        try {
            conn.createStatement().execute("INSERT INTO TESTING (NUMBER, TEXT) VALUES(1, 'a')");
            conn.commit();
        } finally {
            conn.close();
        }
        assertThat(h2.count("TESTING"), is(1));
    }

    /**
     * using common config.
     * @throws Exception if occur
     */
    @Test
    public void common() throws Exception {
        context.put(null, "config");
        Configuration conf = Configuration.load("config");
        Connection conn = conf.open();
        try {
            conn.createStatement().execute("INSERT INTO TESTING (NUMBER, TEXT) VALUES(1, 'a')");
            conn.commit();
        } finally {
            conn.close();
        }
        assertThat(h2.count("TESTING"), is(1));
    }
}
