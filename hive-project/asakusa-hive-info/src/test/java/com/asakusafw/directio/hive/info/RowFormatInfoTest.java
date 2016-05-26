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
package com.asakusafw.directio.hive.info;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Test for {@link RowFormatInfo}.
 */
public class RowFormatInfoTest {

    /**
     * delimited text format.
     */
    @Test
    public void delimited() {
        check(new DelimitedRowFormatInfo.Builder()
                .build());
    }

    /**
     * delimited text format w/ field terminator.
     */
    @Test
    public void delimited_field() {
        check(new DelimitedRowFormatInfo.Builder()
                .fieldsTerminatedBy('\t')
                .build());
    }

    /**
     * delimited text format w/ field terminator.
     */
    @Test
    public void delimited_field_escape() {
        check(new DelimitedRowFormatInfo.Builder()
                .fieldsTerminatedBy('\t').escapedBy('\\')
                .build());
    }

    /**
     * delimited text format w/ collection item terminator.
     */
    @Test
    public void delimited_collection_item_separator() {
        check(new DelimitedRowFormatInfo.Builder()
                .collectionItemsTerminatedBy(':')
                .build());
    }

    /**
     * delimited text format w/ map key terminator.
     */
    @Test
    public void delimited_map_pair_separator() {
        check(new DelimitedRowFormatInfo.Builder()
                .mapKeysTerminatedBy('=')
                .build());
    }

    /**
     * delimited text format w/ line separator.
     */
    @Test
    public void delimited_line_separator() {
        check(new DelimitedRowFormatInfo.Builder()
                .linesTerminatedBy('\r')
                .build());
    }

    /**
     * delimited text format w/ null symbol.
     */
    @Test
    public void delimited_null_symbol() {
        check(new DelimitedRowFormatInfo.Builder()
                .nullDefinedAs('\0')
                .build());
    }

    /**
     * serde format.
     */
    @Test
    public void serde() {
        check(new SerdeRowFormatInfo("com.example.TestSerDe"));
    }

    /**
     * serde format w/ properties.
     */
    @Test
    public void serde_properties() {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("a", "A");
        properties.put("b", "B");
        properties.put("c", "C");
        check(new SerdeRowFormatInfo("com.example.TestSerDe", properties));
    }

    private void check(RowFormatInfo info) {
        Util.check(RowFormatInfo.class, info);
    }
}
