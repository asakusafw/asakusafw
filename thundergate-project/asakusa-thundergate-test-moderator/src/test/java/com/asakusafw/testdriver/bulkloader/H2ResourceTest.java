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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

/**
 * Test for {@link H2Resource}.
 */
public class H2ResourceTest {

    /**
     * target.
     */
    @Rule
    public H2Resource h2 = new H2Resource("test") {
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
     * execute via resource.
     * @throws Exception if failed
     */
    @Test
    public void execute_resource() throws Exception {
        h2.execute("INSERT INTO TESTING (NUMBER, TEXT) VALUES(100, 'Hello, world!')");
        List<List<Object>> results = h2.query("SELECT NUMBER, TEXT FROM TESTING");
        assertThat(results.size(), is(1));

        List<Object> columns = results.get(0);
        assertThat(columns.size(), is(2));
        assertThat(columns.get(0), is((Object) 100));
        assertThat(columns.get(1), is((Object) "Hello, world!"));
    }

    /**
     * execute outer resource.
     * @throws Exception if failed
     */
    @Test
    public void execute_direct() throws Exception {
        h2.execute("INSERT INTO TESTING (NUMBER, TEXT) VALUES(100, 'Hello, world!')");

        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        try {
            Statement stmt = conn.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT NUMBER, TEXT FROM TESTING");
                assertThat(rs.next(), is(true));
                assertThat(rs.getObject(1), is((Object) 100));
                assertThat(rs.getObject(2), is((Object) "Hello, world!"));
                assertThat(rs.next(), is(false));
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }
}
