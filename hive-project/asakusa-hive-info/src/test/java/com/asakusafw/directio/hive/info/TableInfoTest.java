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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.asakusafw.directio.hive.info.FieldType.TypeName;

/**
 * Test for {@link TableInfo}.
 */
public class TableInfoTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        check(new TableInfo(
                "TESTING",
                Arrays.asList(new ColumnInfo[] {
                        new ColumnInfo("TESTING", PlainType.of(TypeName.INT), null)
                }),
                null,
                null,
                null,
                null));
    }

    /**
     * w/ multiple columns.
     */
    @Test
    public void multiple_columns() {
        check(new TableInfo(
                "TESTING",
                Arrays.asList(new ColumnInfo[] {
                        new ColumnInfo("A", PlainType.of(TypeName.INT), "key"),
                        new ColumnInfo("B", PlainType.of(TypeName.STRING), "string value"),
                        new ColumnInfo("C", PlainType.of(TypeName.TIMESTAMP), "timestamp"),
                }),
                null,
                null,
                null,
                null));
    }

    /**
     * w/ comments.
     */
    @Test
    public void comments() {
        check(new TableInfo(
                "TESTING",
                Arrays.asList(new ColumnInfo[] {
                        new ColumnInfo("TESTING", PlainType.of(TypeName.INT), null)
                }),
                "Hello, world!",
                null,
                null,
                null));
    }

    /**
     * w/ storage format.
     */
    @Test
    public void storage() {
        check(new TableInfo(
                "TESTING",
                Arrays.asList(new ColumnInfo[] {
                        new ColumnInfo("TESTING", PlainType.of(TypeName.INT), null)
                }),
                null,
                null,
                BuiltinStorageFormatInfo.of(StorageFormatInfo.FormatKind.PARQUET),
                null));
    }

    /**
     * w/ properties.
     */
    @Test
    public void properties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("a", "A");
        properties.put("b", "B");
        properties.put("c", "C");
        check(new TableInfo(
                "TESTING",
                Arrays.asList(new ColumnInfo[] {
                        new ColumnInfo("TESTING", PlainType.of(TypeName.INT), null)
                }),
                null,
                null,
                null,
                properties));
    }

    private void check(TableInfo info) {
        Util.check(TableInfo.class, info);
    }
}
