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
package com.asakusafw.vocabulary.bulkloader;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.asakusafw.vocabulary.bulkloader.BulkLoadExporterDescription.DuplicateRecordCheck;

/**
 * Test for {@link DupCheckDbExporterDescription}.
 */
public class DupCheckDbExporterDescriptionTest {

    /**
     * 例外テーブルを利用。
     */
    @Test
    public void modelType() {
        BulkLoadExporterDescription desc = new MockDupCheckDbExporterDescription();
        assertThat(desc.getModelType(), equalTo((Object) MockUnionModel.class));
    }

    /**
     * テーブル名は正常テーブル。
     */
    @Test
    public void tableName() {
        BulkLoadExporterDescription desc = new MockDupCheckDbExporterDescription();
        assertThat(desc.getTableName(), is("MOCK"));
    }

    /**
     * テーブル名は正常テーブル。
     */
    @Test(expected = RuntimeException.class)
    public void tableName_invalid() {
        BulkLoadExporterDescription desc = new InvalidDupCheckDbExporterDescription();
        desc.getTableName();
    }

    /**
     * 例外テーブルのカラム一覧。
     */
    @Test
    public void columnNames() {
        BulkLoadExporterDescription desc = new MockDupCheckDbExporterDescription();
        assertThat(desc.getColumnNames(), is(Arrays.asList("A", "B", "C", "D", "X")));
    }

    /**
     * 例外テーブルのカラム一覧。
     */
    @Test(expected = RuntimeException.class)
    public void columnNames_invalid() {
        BulkLoadExporterDescription desc = new InvalidDupCheckDbExporterDescription();
        desc.getColumnNames();
    }

    /**
     * 正常テーブルのカラム一覧。
     */
    @Test
    public void normalColumnNames() {
        BulkLoadExporterDescription desc = new MockDupCheckDbExporterDescription();
        assertThat(desc.getTargetColumnNames(), is(Arrays.asList("A", "B", "C")));
    }

    /**
     * 正常テーブルのカラム一覧。
     */
    @Test(expected = RuntimeException.class)
    public void normalColumnNames_invalid() {
        BulkLoadExporterDescription desc = new InvalidDupCheckDbExporterDescription();
        desc.getTargetColumnNames();
    }

    /**
     * 主キーのプロパティ名一覧。
     */
    @Test
    public void primaryKeyNames() {
        BulkLoadExporterDescription desc = new MockDupCheckDbExporterDescription();
        assertThat(desc.getPrimaryKeyNames(), is(Arrays.asList("a")));
    }

    /**
     * 主キーのプロパティ名一覧。
     */
    @Test(expected = RuntimeException.class)
    public void primaryKeyNames_invalid() {
        BulkLoadExporterDescription desc = new InvalidDupCheckDbExporterDescription();
        desc.getPrimaryKeyNames();
    }

    /**
     * 重複チェックの情報一覧。
     */
    @Test
    public void duplicateRecordCheck() {
        BulkLoadExporterDescription desc = new MockDupCheckDbExporterDescription();
        DuplicateRecordCheck dup = desc.getDuplicateRecordCheck();
        assertThat(dup, not(nullValue()));
        assertThat(dup.getTableName(), is("MOCK_ERROR"));
        assertThat(dup.getColumnNames(), is(Arrays.asList("A", "B", "C", "D")));
        assertThat(dup.getCheckColumnNames(), is(Arrays.asList("A")));
        assertThat(dup.getErrorCodeColumnName(), is("E"));
        assertThat(dup.getErrorCodeValue(), is("DUP!"));
    }

    /**
     * 重複チェックの情報一覧。
     */
    @Test(expected = RuntimeException.class)
    public void duplicateRecordCheck_invalid() {
        BulkLoadExporterDescription desc = new InvalidDupCheckDbExporterDescription();
        desc.getDuplicateRecordCheck();
    }
}
