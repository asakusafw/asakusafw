/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.syntax;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.junit.Test;

import com.asakusafw.directio.hive.info.ColumnInfo;
import com.asakusafw.directio.hive.info.CustomStorageFormatInfo;
import com.asakusafw.directio.hive.info.DelimitedRowFormatInfo;
import com.asakusafw.directio.hive.info.FieldType.TypeName;
import com.asakusafw.directio.hive.info.PlainType;
import com.asakusafw.directio.hive.info.SerdeRowFormatInfo;
import com.asakusafw.directio.hive.info.StorageFormatInfo;
import com.asakusafw.directio.hive.info.TableInfo;

/**
 * Test for {@link HiveQlEmitter}.
 */
public class HiveQlEmitterTest {

    /**
     * simple case.
     * @throws Exception if exists
     */
    @Test
    public void simple() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
            .withColumn("col1", TypeName.INT)
            .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ external.
     * @throws Exception if exists
     */
    @Test
    public void w_external() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
            .withColumn("col1", TypeName.INT)
            .build();
        emit(new SimpleCreateTable(table)
                .withExternal(true));
    }

    /**
     * w/ if not exists.
     * @throws Exception if exists
     */
    @Test
    public void w_if_not_exists() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
            .withColumn("col1", TypeName.INT)
            .build();
        emit(new SimpleCreateTable(table)
                .withSkipPresentTable(true));
    }

    /**
     * w/ dbname.
     * @throws Exception if exists
     */
    @Test
    public void w_dbname() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
            .withColumn("col1", TypeName.INT)
            .build();
        emit(new SimpleCreateTable(table)
                .withDatabaseName("asakusa"));
    }

    /**
     * w/ location.
     * @throws Exception if exists
     */
    @Test
    public void w_location() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
            .withColumn("col1", TypeName.INT)
            .build();
        emit(new SimpleCreateTable(table)
                .withLocation("hdfs://localhost/user/dwh/testing"));
    }

    /**
     * Simple case.
     * @throws Exception if exists
     */
    @Test
    public void w_multiple_fields() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
            .withColumn("col1", TypeName.INT)
            .withColumn("col2", TypeName.STRING)
            .withColumn("col3", TypeName.TIMESTAMP)
            .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ field comment.
     * @throws Exception if exists
     */
    @Test
    public void w_field_comment() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
            .withColumn(new ColumnInfo(
                    "col1",
                    PlainType.of(TypeName.INT),
                    "field comment"))
            .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ table comment.
     * @throws Exception if exists
     */
    @Test
    public void w_table_comment() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
            .withComment("Hello, world!")
            .withColumn("col1", TypeName.INT)
            .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ table properties.
     * @throws Exception if exists
     */
    @Test
    public void w_tblproperties() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
            .withColumn("col1", TypeName.INT)
            .withProperty("orc.compress", "SNAPPY")
            .withProperty("orc.compress.size", "262145")
            .withProperty("orc.stripe.size", "268435457")
            .withProperty("orc.row.index.stride", "20000")
            .withProperty("orc.create.index", "false")
            .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ row format delimited.
     * @throws Exception if exists
     */
    @Test
    public void w_row_format_delimited() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
                .withColumn("col1", TypeName.INT)
                .withRowFormat(new DelimitedRowFormatInfo.Builder()
                        .build())
                .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ row format delimited w/ field separator.
     * @throws Exception if exists
     */
    @Test
    public void w_row_format_delimited_field() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
                .withColumn("col1", TypeName.INT)
                .withRowFormat(new DelimitedRowFormatInfo.Builder()
                        .fieldsTerminatedBy('\t')
                        .build())
                .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ row format delimited w/ field separator + escape.
     * @throws Exception if exists
     */
    @Test
    public void w_row_format_delimited_field_escape() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
                .withColumn("col1", TypeName.INT)
                .withRowFormat(new DelimitedRowFormatInfo.Builder()
                        .fieldsTerminatedBy('\t').escapedBy('\\')
                        .build())
                .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ row format delimited w/ collection separator.
     * @throws Exception if exists
     */
    @Test
    public void w_row_format_delimited_collection() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
                .withColumn("col1", TypeName.INT)
                .withRowFormat(new DelimitedRowFormatInfo.Builder()
                        .collectionItemsTerminatedBy('#')
                        .build())
                .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ row format delimited w/ map separator.
     * @throws Exception if exists
     */
    @Test
    public void w_row_format_delimited_map() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
                .withColumn("col1", TypeName.INT)
                .withRowFormat(new DelimitedRowFormatInfo.Builder()
                        .mapKeysTerminatedBy('\'')
                        .build())
                .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ row format delimited w/ line separator.
     * @throws Exception if exists
     */
    @Test
    public void w_row_format_delimited_line() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
                .withColumn("col1", TypeName.INT)
                .withRowFormat(new DelimitedRowFormatInfo.Builder()
                        .linesTerminatedBy('\n')
                        .build())
                .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ row format delimited w/ null symbol.
     * @throws Exception if exists
     */
    @Test
    public void w_row_format_delimited_null() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
                .withColumn("col1", TypeName.INT)
                .withRowFormat(new DelimitedRowFormatInfo.Builder()
                        .nullDefinedAs('`')
                        .build())
                .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ row format delimited.
     * @throws Exception if exists
     */
    @Test
    public void w_row_format_delimited_all() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
                .withColumn("col1", TypeName.INT)
                .withRowFormat(new DelimitedRowFormatInfo.Builder()
                        .fieldsTerminatedBy(',').escapedBy('\\')
                        .collectionItemsTerminatedBy(':')
                        .mapKeysTerminatedBy('=')
                        .linesTerminatedBy('\n')
                        .nullDefinedAs('\0')
                        .build())
                .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ row format serde.
     * @throws Exception if exists
     */
    @Test
    public void w_row_format_serde() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
            .withColumn("col1", TypeName.INT)
            .withRowFormat(new SerdeRowFormatInfo("com.example.SerDe"))
            .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ row format serde.
     * @throws Exception if exists
     */
    @Test
    public void w_row_format_serde_properties() throws Exception {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("a", "A");
        properties.put("b", "B");
        properties.put("c", "C");
        TableInfo table = new TableInfo.Builder("testing")
                .withColumn("col1", TypeName.INT)
                .withRowFormat(new SerdeRowFormatInfo("com.example.SerDe", properties))
                .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ stored as built-in format.
     * @throws Exception if exists
     */
    @Test
    public void w_sotred_as_builtin() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
            .withColumn("col1", TypeName.INT)
            .withStorageFormat(StorageFormatInfo.FormatKind.ORC)
            .build();
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ stored as custom format.
     * @throws Exception if exists
     */
    @Test
    public void w_sotred_as_custom() throws Exception {
        TableInfo table = new TableInfo.Builder("testing")
                .withColumn("col1", TypeName.INT)
                .withStorageFormat(new CustomStorageFormatInfo("com.example.Input", "com.example.Output"))
                .build();
        emit(new SimpleCreateTable(table));
    }

    private ASTNode emit(HiveCreateTable stmt) throws IOException, ParseException {
        StringBuilder buf = new StringBuilder();
        HiveQlEmitter.emit(stmt, buf);
        String ql = buf.toString();
        System.out.println("====");
        System.out.println(ql);
        ASTNode node = new ParseDriver().parse(ql);
        return node;
    }
}
