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
package com.asakusafw.directio.hive.ql;

import java.io.IOException;

import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.junit.Test;

import com.asakusafw.directio.hive.common.DelimitedRowFormatInfo;
import com.asakusafw.directio.hive.common.SimpleFieldInfo;
import com.asakusafw.directio.hive.common.SimpleTableInfo;
import com.asakusafw.directio.hive.ql.HiveCreateTable;
import com.asakusafw.directio.hive.ql.HiveQlEmitter;

/**
 * Test for {@link HiveQlEmitter}.
 */
public class HiveQlEmitterTest {

    /**
     * Simple case.
     * @throws Exception if exists
     */
    @Test
    public void simple() throws Exception {
        SimpleTableInfo table = new SimpleTableInfo("testing")
            .withField(new SimpleFieldInfo("col1", TypeInfoFactory.intTypeInfo));
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ external.
     * @throws Exception if exists
     */
    @Test
    public void w_external() throws Exception {
        SimpleTableInfo table = new SimpleTableInfo("testing")
            .withField(new SimpleFieldInfo("col1", TypeInfoFactory.intTypeInfo));
        emit(new SimpleCreateTable(table)
                .withExternal(true));
    }

    /**
     * w/ if not exists.
     * @throws Exception if exists
     */
    @Test
    public void w_if_not_exists() throws Exception {
        SimpleTableInfo table = new SimpleTableInfo("testing")
            .withField(new SimpleFieldInfo("col1", TypeInfoFactory.intTypeInfo));
        emit(new SimpleCreateTable(table)
                .withSkipPresentTable(true));
    }

    /**
     * w/ dbname.
     * @throws Exception if exists
     */
    @Test
    public void w_dbname() throws Exception {
        SimpleTableInfo table = new SimpleTableInfo("testing")
            .withField(new SimpleFieldInfo("col1", TypeInfoFactory.intTypeInfo));
        emit(new SimpleCreateTable(table)
                .withDatabaseName("asakusa"));
    }

    /**
     * w/ location.
     * @throws Exception if exists
     */
    @Test
    public void w_location() throws Exception {
        SimpleTableInfo table = new SimpleTableInfo("testing")
            .withField(new SimpleFieldInfo("col1", TypeInfoFactory.intTypeInfo));
        emit(new SimpleCreateTable(table)
                .withLocation("hdfs://localhost/user/dwh/testing"));
    }

    /**
     * Simple case.
     * @throws Exception if exists
     */
    @Test
    public void w_multiple_fields() throws Exception {
        SimpleTableInfo table = new SimpleTableInfo("testing")
            .withField(new SimpleFieldInfo("col1", TypeInfoFactory.intTypeInfo))
            .withField(new SimpleFieldInfo("col2", TypeInfoFactory.stringTypeInfo))
            .withField(new SimpleFieldInfo("col3", TypeInfoFactory.timestampTypeInfo));
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ field comment.
     * @throws Exception if exists
     */
    @Test
    public void w_field_comment() throws Exception {
        SimpleTableInfo table = new SimpleTableInfo("testing")
            .withField(new SimpleFieldInfo("col1", TypeInfoFactory.intTypeInfo)
                    .withFieldComment("Hello, world!"));
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ table comment.
     * @throws Exception if exists
     */
    @Test
    public void w_table_comment() throws Exception {
        SimpleTableInfo table = new SimpleTableInfo("testing")
            .withTableComment("Hello, world!")
            .withField(new SimpleFieldInfo("col1", TypeInfoFactory.intTypeInfo));
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ simple row format.
     * @throws Exception if exists
     */
    @Test
    public void w_simple_row_format() throws Exception {
        DelimitedRowFormatInfo format = new DelimitedRowFormatInfo();
        SimpleTableInfo table = new SimpleTableInfo("testing")
            .withField(new SimpleFieldInfo("col1", TypeInfoFactory.intTypeInfo))
            .withRowFormat(format)
            .withFormatName("TEXT");
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ complex row format.
     * @throws Exception if exists
     */
    @Test
    public void w_complex_row_format() throws Exception {
        DelimitedRowFormatInfo format = new DelimitedRowFormatInfo();
        format.setFieldsTerminatedBy("\t");
        format.setEscapedBy("\\");
        format.setCollectionItemsTerminatedBy(":");
        format.setMapKeysTerminatedBy("=");
        format.setLinesTerminatedBy("\n");
        format.setNullDefinedAs("\0");

        SimpleTableInfo table = new SimpleTableInfo("testing")
            .withField(new SimpleFieldInfo("col1", TypeInfoFactory.intTypeInfo))
            .withRowFormat(format)
            .withFormatName("TEXT");
        emit(new SimpleCreateTable(table));
    }

    /**
     * w/ table properties.
     * @throws Exception if exists
     */
    @Test
    public void w_tblproperties() throws Exception {
        SimpleTableInfo table = new SimpleTableInfo("testing")
            .withField(new SimpleFieldInfo("col1", TypeInfoFactory.intTypeInfo))
            .withTableProperty("orc.compress", "SNAPPY")
            .withTableProperty("orc.compress.size", "262145")
            .withTableProperty("orc.stripe.size", "268435457")
            .withTableProperty("orc.row.index.stride", "20000")
            .withTableProperty("orc.create.index", "false");
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
