/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.info.hive;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.asakusafw.info.InfoSerDe;

/**
 * Test for {@link RowFormatInfo}.
 */
public class RowFormatInfoTest {

    /**
     * delimited text format.
     */
    @Test
    public void delimited() {
        InfoSerDe.checkRestore(RowFormatInfo.class,
                new DelimitedRowFormatInfo.Builder()
                        .build());
    }

    /**
     * delimited text format w/ field terminator.
     */
    @Test
    public void delimited_field() {
        InfoSerDe.checkRestore(RowFormatInfo.class,
                new DelimitedRowFormatInfo.Builder()
                        .fieldsTerminatedBy('\t')
                        .build());
    }

    /**
     * delimited text format w/ field terminator.
     */
    @Test
    public void delimited_field_escape() {
        InfoSerDe.checkRestore(RowFormatInfo.class,
                new DelimitedRowFormatInfo.Builder()
                        .fieldsTerminatedBy('\t').escapedBy('\\')
                        .build());
    }

    /**
     * delimited text format w/ collection item terminator.
     */
    @Test
    public void delimited_collection_item_separator() {
        InfoSerDe.checkRestore(RowFormatInfo.class,
                new DelimitedRowFormatInfo.Builder()
                        .collectionItemsTerminatedBy(':')
                        .build());
    }

    /**
     * delimited text format w/ map key terminator.
     */
    @Test
    public void delimited_map_pair_separator() {
        InfoSerDe.checkRestore(RowFormatInfo.class,
                new DelimitedRowFormatInfo.Builder()
                        .mapKeysTerminatedBy('=')
                        .build());
    }

    /**
     * delimited text format w/ line separator.
     */
    @Test
    public void delimited_line_separator() {
        InfoSerDe.checkRestore(RowFormatInfo.class,
                new DelimitedRowFormatInfo.Builder()
                        .linesTerminatedBy('\r')
                        .build());
    }

    /**
     * delimited text format w/ null symbol.
     */
    @Test
    public void delimited_null_symbol() {
        InfoSerDe.checkRestore(RowFormatInfo.class,
                new DelimitedRowFormatInfo.Builder()
                        .nullDefinedAs('\0')
                        .build());
    }

    /**
     * serde format.
     */
    @Test
    public void serde() {
        InfoSerDe.checkRestore(RowFormatInfo.class, new SerdeRowFormatInfo("com.example.TestSerDe"));
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
        InfoSerDe.checkRestore(RowFormatInfo.class,
                new SerdeRowFormatInfo("com.example.TestSerDe", properties));
    }
}
