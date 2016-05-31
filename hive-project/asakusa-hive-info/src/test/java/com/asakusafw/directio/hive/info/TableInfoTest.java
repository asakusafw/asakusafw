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
        check(new TableInfo.Builder("TESTING")
                .withColumn("TESTING", TypeName.INT)
                .build());
    }

    /**
     * w/ multiple columns.
     */
    @Test
    public void multiple_columns() {
        check(new TableInfo.Builder("TESTING")
                .withColumn(new ColumnInfo("A", PlainType.of(TypeName.INT), "key"))
                .withColumn(new ColumnInfo("B", PlainType.of(TypeName.STRING), "string value"))
                .withColumn(new ColumnInfo("C", PlainType.of(TypeName.TIMESTAMP), "timestamp"))
                .build());
    }

    /**
     * w/ comments.
     */
    @Test
    public void comments() {
        check(new TableInfo.Builder("TESTING")
                .withColumn("TESTING", TypeName.INT)
                .withComment("Hello, world!")
                .build());
    }

    /**
     * w/ storage format.
     */
    @Test
    public void storage() {
        check(new TableInfo.Builder("TESTING")
                .withColumn("TESTING", TypeName.INT)
                .withStorageFormat(StorageFormatInfo.FormatKind.PARQUET)
                .build());
    }

    /**
     * w/ properties.
     */
    @Test
    public void properties() {
        check(new TableInfo.Builder("TESTING")
                .withColumn("TESTING", TypeName.INT)
                .withProperty("a", "A")
                .withProperty("b", "B")
                .withProperty("c", "C")
                .build());
    }

    private void check(TableInfo info) {
        Util.check(TableInfo.class, info);
    }
}
