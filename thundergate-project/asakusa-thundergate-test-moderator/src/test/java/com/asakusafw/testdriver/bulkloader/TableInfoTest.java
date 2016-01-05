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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * Test for {@link TableInfo}.
 * @since 0.2.0
 */
public class TableInfoTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<Simple>(Simple.class);

    static final DataModelDefinition<CacheSupport> CACHE = new SimpleDataModelDefinition<CacheSupport>(CacheSupport.class);

    /**
     * simple.
     */
    @Test
    public void simple() {
        TableInfo<Simple> info = new TableInfo<Simple>(SIMPLE, "SMPL", Arrays.asList(new String[] {
                "NUMBER"
        }));
        assertThat(info.getDefinition(), is(SIMPLE));
        assertThat(info.getTableName(), is("SMPL"));
        assertThat(info.getTimestampColumn(), is(nullValue()));
        Map<String, PropertyName> map = info.getColumnsToProperties();
        assertThat(map.size(), is(1));
        assertThat(map.get("NUMBER"), is(name("number")));
    }

    /**
     * ordered.
     */
    @Test
    public void ordered() {
        TableInfo<Simple> info = new TableInfo<Simple>(SIMPLE, "SMPL", Arrays.asList(new String[] {
                "C_BYTE",
                "C_SHORT",
                "C_FLOAT",
                "C_DOUBLE",
        }));
        assertThat(info.getDefinition(), is(SIMPLE));
        assertThat(info.getTableName(), is("SMPL"));
        Map<String, PropertyName> map = info.getColumnsToProperties();
        assertThat(map.size(), is(4));
        assertThat(map.get("C_BYTE"), is(name("byte_value")));
        assertThat(map.get("C_SHORT"), is(name("short_value")));
        assertThat(map.get("C_FLOAT"), is(name("float_value")));
        assertThat(map.get("C_DOUBLE"), is(name("double_value")));
        List<String> names =  new ArrayList<String>(map.keySet());
        assertThat(names, is(Arrays.asList("C_BYTE", "C_SHORT", "C_FLOAT", "C_DOUBLE")));
    }

    /**
     * infer original name.
     */
    @Test
    public void infer() {
        TableInfo<Simple> info = new TableInfo<Simple>(SIMPLE, "SMPL", Arrays.asList(new String[] {
                "INFER_ORIGINAL_NAME"
        }));
        assertThat(info.getDefinition(), is(SIMPLE));
        assertThat(info.getTableName(), is("SMPL"));
        Map<String, PropertyName> map = info.getColumnsToProperties();
        assertThat(map.size(), is(1));
        assertThat(map.get("INFER_ORIGINAL_NAME"), is(name("infer_original_name")));
    }

    /**
     * infer original name.
     */
    @Test
    public void skip() {
        TableInfo<Simple> info = new TableInfo<Simple>(SIMPLE, "SMPL", Arrays.asList(new String[] {
                "C_INTEGER"
        }));
        assertThat(info.getDefinition(), is(SIMPLE));
        assertThat(info.getTableName(), is("SMPL"));
        Map<String, PropertyName> map = info.getColumnsToProperties();
        assertThat(map.size(), is(0));
    }

    /**
     * unknown columns.
     */
    @Test
    public void unknown() {
        TableInfo<Simple> info = new TableInfo<Simple>(SIMPLE, "SMPL", Arrays.asList(new String[] {
                "UNKNOWN_COLUMN"
        }));
        assertThat(info.getDefinition(), is(SIMPLE));
        assertThat(info.getTableName(), is("SMPL"));
        Map<String, PropertyName> map = info.getColumnsToProperties();
        assertThat(map.size(), is(0));
    }

    /**
     * timestamp column.
     */
    @Test
    public void timestamp() {
        TableInfo<CacheSupport> info = new TableInfo<CacheSupport>(CACHE, "SMPL", Arrays.asList(new String[] {
                "NUMBER"
        }));
        assertThat(info.getTimestampColumn(), is("C_DATETIME"));
    }

    private Matcher<PropertyName> name(final String words) {
        return new BaseMatcher<PropertyName>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof PropertyName) {
                    PropertyName other = (PropertyName) o;
                    PropertyName name = PropertyName.newInstance(words.split("_"));
                    return other.equals(name);
                }
                return false;
            }

            @Override
            public void describeTo(Description d) {
                d.appendText(words);
            }
        };
    }
}
