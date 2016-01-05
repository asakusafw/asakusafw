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
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;
import com.asakusafw.vocabulary.bulkloader.DbImporterDescription;

/**
 * Test for {@link BulkLoadImporterPreparator}.
 * @since 0.2.0
 */
public class BulkLoadImporterPreparatorTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<Simple>(Simple.class);

    static final DbImporterDescription NORMAL = new DbImporterDescription() {
        @Override
        public Class<?> getModelType() {
            return Simple.class;
        }

        @Override
        public String getTargetName() {
            return "importer";
        }

        @Override
        public LockType getLockType() {
            return LockType.UNUSED;
        }
    };

    static final DbImporterDescription CACHED = new DbImporterDescription() {
        @Override
        public Class<?> getModelType() {
            return Simple.class;
        }

        @Override
        public String getTargetName() {
            return "importer";
        }

        @Override
        public LockType getLockType() {
            return LockType.UNUSED;
        }

        @Override
        public boolean isCacheEnabled() {
            return true;
        }

        @Override
        public String calculateCacheId() {
            return "testing";
        }
    };

    static final DbImporterDescription MISSING = new DbImporterDescription() {
        @Override
        public Class<?> getModelType() {
            return Simple.class;
        }

        @Override
        public String getTargetName() {
            return "importer";
        }

        @Override
        public String getTableName() {
            return "INVALID";
        }

        @Override
        public LockType getLockType() {
            return LockType.UNUSED;
        }
    };

    /**
     * H2 database.
     */
    @Rule
    public H2Resource h2 = new H2Resource("importer") {
        @Override
        protected void before() throws Exception {
            executeFile("ddl-simple.sql");
        }
    };

    /**
     * Configuration helper.
     */
    @Rule
    public ConfigurationContext context = new ConfigurationContext();

    /**
     * truncate.
     * @throws Exception if occur
     */
    @Test
    public void truncate() throws Exception {
        Simple simple = new Simple();
        simple.number = 100;
        simple.text = "Hello, world!";
        insert(simple);

        context.put("importer", "importer");

        BulkLoadImporterPreparator prep = new BulkLoadImporterPreparator();

        assertThat(h2.count("SIMPLE"), is(1));
        prep.truncate(NORMAL);

        assertThat(h2.count("SIMPLE"), is(0));
    }

    /**
     * truncate cache infomation.
     * @throws Exception if occur
     */
    @Test
    public void truncateWithCache() throws Exception {
        Simple simple = new Simple();
        simple.number = 100;
        simple.text = "Hello, world!";
        insert(simple);

        context.put("importer", "importer");

        h2.executeFile("ddl-thundergate.sql");
        h2.execute(
                "INSERT " +
                "INTO __TG_CACHE_INFO (CACHE_ID, CACHE_TIMESTAMP, BUILT_TIMESTAMP, TABLE_NAME, REMOTE_PATH) " +
                "VALUES ('testing', NOW(), NOW(), 'SIMPLE', '/remote/path')");
        h2.execute(
                "INSERT " +
                "INTO __TG_CACHE_INFO (CACHE_ID, CACHE_TIMESTAMP, BUILT_TIMESTAMP, TABLE_NAME, REMOTE_PATH) " +
                "VALUES ('other', NOW(), NOW(), 'SIMPLE', '/remote/path')");
        h2.execute(
                "INSERT " +
                "INTO __TG_CACHE_LOCK (CACHE_ID, EXECUTION_ID, ACQUIRED) " +
                "VALUES ('testing', 'running', NOW())");
        h2.execute(
                "INSERT " +
                "INTO __TG_CACHE_LOCK (CACHE_ID, EXECUTION_ID, ACQUIRED) " +
                "VALUES ('other', 'running', NOW())");

        BulkLoadImporterPreparator prep = new BulkLoadImporterPreparator();

        assertThat(h2.count("SIMPLE"), is(1));
        assertThat(h2.count("__TG_CACHE_INFO"), is(2));
        assertThat(h2.count("__TG_CACHE_LOCK"), is(2));
        prep.truncate(CACHED);

        assertThat(h2.count("SIMPLE"), is(0));
        assertThat(h2.count("__TG_CACHE_INFO"), is(1));
        assertThat(h2.count("__TG_CACHE_LOCK"), is(1));
    }

    /**
     * truncate cache infomation but cache table is not found.
     * @throws Exception if occur
     */
    @Test
    public void truncateWithCache_butNoCacheFeature() throws Exception {
        Simple simple = new Simple();
        simple.number = 100;
        simple.text = "Hello, world!";
        insert(simple);

        context.put("importer", "importer");

        BulkLoadImporterPreparator prep = new BulkLoadImporterPreparator();

        assertThat(h2.count("SIMPLE"), is(1));
        prep.truncate(CACHED);

        assertThat(h2.count("SIMPLE"), is(0));
    }

    /**
     * output.
     * @throws IOException if occur
     */
    @Test
    public void output() throws IOException {
        context.put("importer", "importer");
        BulkLoadImporterPreparator prep = new BulkLoadImporterPreparator();
        ModelOutput<Simple> output = prep.createOutput(SIMPLE, NORMAL);
        try {
            Simple simple = new Simple();
            simple.number = 100;
            simple.text = "Hello, world!";
            output.write(simple);
        } finally {
            output.close();
        }

        assertThat(h2.count("SIMPLE"), is(1));
        List<Object> row = h2.single("SELECT NUMBER, TEXT FROM SIMPLE");
        assertThat(row, is(Arrays.<Object>asList(100, "Hello, world!")));
    }

    /**
     * attempted to output to invalid table.
     * @throws IOException if occur
     */
    @Test(expected = IOException.class)
    public void output_missing() throws IOException {
        context.put("importer", "importer");
        BulkLoadImporterPreparator prep = new BulkLoadImporterPreparator();
        ModelOutput<Simple> output = prep.createOutput(SIMPLE, MISSING);
        output.close();
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

    private TableInfo<Simple> info(String... columns) {
        return new TableInfo<Simple>(SIMPLE, "SIMPLE", Arrays.asList(columns));
    }
}
