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
import java.net.URI;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * Test for {@link TableSourceProvider}.
 * @since 0.2.2
 */
public class TableSourceProviderTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<Simple>(Simple.class);

    /**
     * H2 database.
     */
    @Rule
    public H2Resource h2 = new H2Resource("provider") {

        @Override
        protected void before() throws Exception {
            executeFile("ddl-simple.sql");
            executeFile("ddl-dupcheck.sql");
        }
    };

    /**
     * Configuration helper.
     */
    @Rule
    public ConfigurationContext context = new ConfigurationContext();

    /**
     * single record.
     * @throws Exception if occur
     */
    @Test
    public void single() throws Exception {
        context.put("provider", "provider");

        Simple simple = new Simple();
        simple.number = 100;
        simple.text = "Hello, world!";
        insert(simple, SIMPLE, "SIMPLE");

        TableSourceProvider provider = new TableSourceProvider();
        DataModelSource source = provider.open(SIMPLE, new URI("bulkloader:provider:SIMPLE"), new TestContext.Empty());

        try {
            assertThat(next(source), is(simple));
            assertThat(next(source), is(nullValue()));
        } finally {
            source.close();
        }
    }

    /**
     * multiple records.
     * @throws Exception if occur
     */
    @Test
    public void multiple() throws Exception {
        context.put("provider", "provider");

        Simple s1 = new Simple();
        s1.number = 100;
        s1.text = "Hello, world!";
        insert(s1, SIMPLE, "SIMPLE");

        Simple s2 = new Simple();
        s2.number = 200;
        s2.text = "Hello, world!!!!";
        insert(s2, SIMPLE, "SIMPLE");

        TableSourceProvider provider = new TableSourceProvider();
        DataModelSource source = provider.open(SIMPLE, new URI("bulkloader:provider:SIMPLE"), new TestContext.Empty());

        try {
            assertThat(next(source), is(s1));
            assertThat(next(source), is(s2));
            assertThat(next(source), is(nullValue()));
        } finally {
            source.close();
        }
    }

    /**
     * zero record.
     * @throws Exception if occur
     */
    @Test
    public void zero() throws Exception {
        context.put("provider", "provider");

        TableSourceProvider provider = new TableSourceProvider();
        DataModelSource source = provider.open(SIMPLE, new URI("bulkloader:provider:SIMPLE"), new TestContext.Empty());
        try {
            assertThat(next(source), is(nullValue()));
        } finally {
            source.close();
        }
    }

    /**
     * invalid_scheme.
     * @throws Exception if occur
     */
    @Test
    public void invalid_scheme() throws Exception {
        context.put("provider", "provider");

        TableSourceProvider provider = new TableSourceProvider();
        DataModelSource source = provider.open(SIMPLE, new URI("hoge:provider:SIMPLE"), new TestContext.Empty());
        assertThat(source, is(nullValue()));
    }

    private <T> void insert(T simple, DataModelDefinition<T> def, String table) {
        try {
            TableInfo<T> info = new TableInfo<T>(def, table, Arrays.asList("NUMBER", "TEXT", "C_BOOL", "C_BYTE",
                    "C_SHORT", "C_LONG", "C_FLOAT", "C_DOUBLE", "C_DECIMAL", "C_DATE", "C_TIME", "C_DATETIME"));
            TableOutput<T> output = new TableOutput<T>(info, h2.open());
            try {
                output.write(simple);
            } finally {
                output.close();
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private Simple next(DataModelSource source) throws IOException {
        DataModelReflection next = source.next();
        if (next != null) {
            return SIMPLE.toObject(next);
        }
        return null;
    }

}
