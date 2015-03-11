/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Keep a connection of H2 'in memory' Database.
 */
public class H2Resource extends TestWatcher {

    private final String name;

    private Class<?> context;

    private Connection connection;

    /**
     * Creates a new instance.
     * @param name simple name of database
     */
    public H2Resource(String name) {
        this.name = name;
    }

    @Override
    protected void starting(Description description) {
        org.h2.Driver.load();
        this.context = description.getTestClass();
        this.connection = open();
        boolean green = false;
        try {
            leakcheck();
            before();
            green = true;
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            if (green == false) {
                finished(description);
            }
        }
    }

    private void leakcheck() {
        try {
            execute0("CREATE TABLE H2_TEST_DUPCHECK (SID IDENTITY PRIMARY KEY)");
        } catch (SQLException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * runs before executes each test.
     * @throws Exception if failed
     */
    protected void before() throws Exception {
        return;
    }

    /**
     * Creates a new connection.
     * @return the created connection
     */
    public Connection open() {
        try {
            return DriverManager.getConnection("jdbc:h2:mem:" + name);
        } catch (SQLException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns query result columns list.
     * @param sql target SQL
     * @return result rows list that contains columns array
     */
    public List<List<Object>> query(String sql) {
        try {
            return query0(sql);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns query result columns list.
     * @param sql target SQL
     * @return result rows list that contains columns array
     */
    public List<Object> single(String sql) {
        try {
            List<List<Object>> query = query0(sql);
            assertThat(sql, query.size(), is(1));
            return query.get(0);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Count rows in the table.
     * @param table target table
     * @return number of row in the table, or -1 if failed
     */
    public int count(String table) {
        try {
            List<List<Object>> r = query0(MessageFormat.format("SELECT COUNT(*) FROM {0}", table));
            if (r.size() != 1) {
                return -1;
            }
            return ((Number) r.get(0).get(0)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private List<List<Object>> query0(String sql) throws SQLException {
        Statement s = connection.createStatement();
        try {
            ResultSet rs = s.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();
            int size = meta.getColumnCount();
            List<List<Object>> results = new ArrayList<List<Object>>();
            while (rs.next()) {
                Object[] columns = new Object[size];
                for (int i = 0; i < size; i++) {
                    columns[i] = rs.getObject(i + 1);
                }
                results.add(Arrays.asList(columns));
            }
            return results;
        } finally {
            s.close();
        }
    }

    /**
     * Executes DML.
     * @param sql DML
     */
    public void execute(String sql) {
        try {
            execute0(sql);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private void execute0(String sql) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        try {
            ps.execute();
            connection.commit();
        } finally {
            ps.close();
        }
    }

    /**
     * Executes DML in target file.
     * @param sqlFile resource file
     */
    public void executeFile(String sqlFile) {
        String content = load(sqlFile);
        execute(content);
    }

    private String load(String resource) {
        InputStream source = context.getResourceAsStream(resource);
        assertThat(resource, source, is(not(nullValue())));
        try {
            StringBuilder buf = new StringBuilder();
            Reader reader = new InputStreamReader(source, "UTF-8");
            char[] cbuf = new char[1024];
            while (true) {
                int read = reader.read(cbuf);
                if (read < 0) {
                    break;
                }
                buf.append(cbuf, 0, read);
            }
            return buf.toString();
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            try {
                source.close();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }

    @Override
    public void finished(Description description) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new AssertionError(e);
            }
        }
    }
}
