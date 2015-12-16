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
package com.asakusafw.vocabulary.windgate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;
import com.asakusafw.windgate.core.vocabulary.JdbcProcess;

/**
 * Test for {@link JdbcExporterDescription}.
 */
public class JdbcExporterDescriptionTest {

    /**
     * Simple case.
     */
    @Test
    public void simple() {
        Mock mock = new Mock(String.class, "testing", StringSupport.class, "TESTING", "VALUE");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        Map<String, String> conf = script.getConfiguration();
        assertThat(conf.size(), is(4));
        assertThat(conf.get(JdbcProcess.TABLE.key()), is("TESTING"));
        assertThat(conf.get(JdbcProcess.COLUMNS.key()), equalToIgnoringWhiteSpace("VALUE"));
        assertThat(conf.get(JdbcProcess.JDBC_SUPPORT.key()), is(StringSupport.class.getName()));
        assertThat(conf.get(JdbcProcess.CUSTOM_TRUNCATE.key()), is(nullValue()));
        assertThat(conf.get(JdbcProcess.OPERATION.key()), is(not(nullValue())));
        assertThat(script.getParameterNames(), hasSize(0));
    }

    /**
     * Multiple columns.
     */
    @Test
    public void multiple_columns() {
        Mock mock = new Mock(String.class, "testing", StringSupport.class, "TESTING",
                "A", "B", "C");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        Map<String, String> conf = script.getConfiguration();
        assertThat(conf.size(), is(4));
        assertThat(conf.get(JdbcProcess.TABLE.key()), is("TESTING"));
        assertThat(conf.get(JdbcProcess.COLUMNS.key()), equalToIgnoringWhiteSpace("A, B, C"));
        assertThat(conf.get(JdbcProcess.JDBC_SUPPORT.key()), is(StringSupport.class.getName()));
        assertThat(conf.get(JdbcProcess.CUSTOM_TRUNCATE.key()), is(nullValue()));
        assertThat(conf.get(JdbcProcess.OPERATION.key()), is(not(nullValue())));
        assertThat(script.getParameterNames(), hasSize(0));
    }

    /**
     * w/ custom truncate.
     */
    @Test
    public void w_custom_truncate() {
        Mock mock = new Mock(String.class, "testing", StringSupport.class, "TESTING", "VALUE")
            .withCustomTruncate("CUSTOM");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        Map<String, String> conf = script.getConfiguration();
        assertThat(conf.size(), is(5));
        assertThat(conf.get(JdbcProcess.TABLE.key()), is("TESTING"));
        assertThat(conf.get(JdbcProcess.COLUMNS.key()), equalToIgnoringWhiteSpace("VALUE"));
        assertThat(conf.get(JdbcProcess.JDBC_SUPPORT.key()), is(StringSupport.class.getName()));
        assertThat(conf.get(JdbcProcess.CUSTOM_TRUNCATE.key()), is("CUSTOM"));
        assertThat(conf.get(JdbcProcess.OPERATION.key()), is(not(nullValue())));
        assertThat(script.getParameterNames(), hasSize(0));
    }

    /**
     * w/ parameters.
     */
    @Test
    public void parameters() {
        Mock mock = new Mock(String.class, "testing", StringSupport.class, "TESTING", "VALUE")
            .withCustomTruncate("CUSTOM WITH ${var}");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        Map<String, String> conf = script.getConfiguration();
        assertThat(conf.size(), is(5));
        assertThat(conf.get(JdbcProcess.TABLE.key()), is("TESTING"));
        assertThat(conf.get(JdbcProcess.COLUMNS.key()), equalToIgnoringWhiteSpace("VALUE"));
        assertThat(conf.get(JdbcProcess.JDBC_SUPPORT.key()), is(StringSupport.class.getName()));
        assertThat(conf.get(JdbcProcess.CUSTOM_TRUNCATE.key()), is("CUSTOM WITH ${var}"));
        assertThat(conf.get(JdbcProcess.OPERATION.key()), is(not(nullValue())));
        assertThat(script.getParameterNames(), containsInAnyOrder("var"));
    }

    /**
     * Table not specified.
     */
    @Test(expected = IllegalStateException.class)
    public void no_tables() {
        Mock mock = new Mock(String.class, "testing", StringSupport.class, null, "VALUE");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        script.getConfiguration();
    }

    /**
     * Table is empty.
     */
    @Test(expected = IllegalStateException.class)
    public void empty_table() {
        Mock mock = new Mock(String.class, "testing", StringSupport.class, "", "VALUE");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        script.getConfiguration();
    }

    /**
     * Columns not specified.
     */
    @Test(expected = IllegalStateException.class)
    public void no_columns() {
        Mock mock = new Mock(String.class, "testing", StringSupport.class, "TESTING", (String[]) null);
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        script.getConfiguration();
    }

    /**
     * Columns are empty.
     */
    @Test(expected = IllegalStateException.class)
    public void empty_columns() {
        Mock mock = new Mock(String.class, "testing", StringSupport.class, "TESTING");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        script.getConfiguration();
    }

    /**
     * Contains empty column.
     */
    @Test(expected = IllegalStateException.class)
    public void has_empty_column() {
        Mock mock = new Mock(String.class, "testing", StringSupport.class, "TESTING", "");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        script.getConfiguration();
    }

    /**
     * Contains null column.
     */
    @Test(expected = IllegalStateException.class)
    public void has_null_column() {
        Mock mock = new Mock(String.class, "testing", StringSupport.class, "TESTING", "VALUE", null);
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        script.getConfiguration();
    }

    /**
     * JDBC Support not specified.
     */
    @Test(expected = IllegalStateException.class)
    public void no_support() {
        Mock mock = new Mock(String.class, "testing", null, "TESTING", "VALUE");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        script.getConfiguration();
    }

    /**
     * Inconsistent JDBC support.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_type_support() {
        Mock mock = new Mock(String.class, "testing", VoidSupport.class, "TESTING", "VALUE");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        script.getConfiguration();
    }

    /**
     * Columns are not supported.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_columns() {
        Mock mock = new Mock(String.class, "testing", NullSupport.class, "TESTING", "VALUE");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        script.getConfiguration();
    }

    /**
     * Inconsistent JDBC support.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_support_class() {
        Mock mock = new Mock(String.class, "testing", InvalidSupport.class, "TESTING", "VALUE");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        script.getConfiguration();
    }

    /**
     * custom truncate statement is empty.
     */
    @Test(expected = IllegalStateException.class)
    public void empty_custom_truncate() {
        Mock mock = new Mock(String.class, "testing", StringSupport.class, "TESTING", "VALUE")
            .withCustomTruncate(" ");
        DriverScript script = mock.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.JDBC_RESOURCE_NAME));
        script.getConfiguration();
    }

    /**
     * String support.
     */
    public static class StringSupport extends MockJdbcSupport<String> {
        @Override
        public Class<String> getSupportedType() {
            return String.class;
        }
    }

    /**
     * Void support.
     */
    public static class VoidSupport extends MockJdbcSupport<Void> {
        @Override
        public Class<Void> getSupportedType() {
            return Void.class;
        }
    }

    /**
     * Supports nothing.
     */
    public static class NullSupport extends MockJdbcSupport<Object> {
        @Override
        public Class<Object> getSupportedType() {
            return Object.class;
        }
        @Override
        public boolean isSupported(List<String> columnNames) {
            return false;
        }
    }

    /**
     * Invalid support.
     */
    public static class InvalidSupport extends MockJdbcSupport<Object> {
        private InvalidSupport() {
            return;
        }
        @Override
        public Class<Object> getSupportedType() {
            return Object.class;
        }
    }

    static class Mock extends JdbcExporterDescription {

        private final Class<?> modelType;
        private final String profileName;
        private final Class<? extends DataModelJdbcSupport<?>> jdbcSupport;
        private final String tableName;
        private final List<String> columnNames;
        private String customTruncate;

        Mock(
                Class<?> modelType,
                String profileName,
                Class<? extends DataModelJdbcSupport<?>> jdbcSupport,
                String tableName,
                String... columnNames) {
            this.modelType = modelType;
            this.profileName = profileName;
            this.jdbcSupport = jdbcSupport;
            this.tableName = tableName;
            if (columnNames != null) {
                this.columnNames = Arrays.asList(columnNames);
            } else {
                this.columnNames = null;
            }
        }

        @Override
        public Class<?> getModelType() {
            return modelType;
        }

        @Override
        public String getProfileName() {
            return profileName;
        }

        @Override
        public Class<? extends DataModelJdbcSupport<?>> getJdbcSupport() {
            return jdbcSupport;
        }

        @Override
        public String getTableName() {
            return tableName;
        }

        @Override
        public List<String> getColumnNames() {
            return columnNames;
        }

        @Override
        public String getCustomTruncate() {
            if (customTruncate == null) {
                return super.getCustomTruncate();
            }
            return customTruncate;
        }

        public Mock withCustomTruncate(String value) {
            this.customTruncate = value;
            return this;
        }
    }
}
