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

/**
 * Test for {@link DbExporterDescription}.
 */
public class DbExporterDescriptionTest {

    /**
     * テーブル名の抽出。
     */
    @Test
    public void tableName() {
        DbExporterDescription desc = new MockDbExporterDescription();
        assertThat(desc.getTableName(), is("MOCK"));
    }

    /**
     * テーブル名の抽出。
     */
    @Test(expected = RuntimeException.class)
    public void tableName_invalid() {
        DbExporterDescription desc = new InvalidDbExporterDescription();
        desc.getTableName();
    }

    /**
     * カラム名の抽出。
     */
    @Test
    public void columnNames() {
        DbExporterDescription desc = new MockDbExporterDescription();
        assertThat(desc.getColumnNames(), is(Arrays.asList("A", "B", "C")));
    }

    /**
     * カラム名の抽出。
     */
    @Test(expected = RuntimeException.class)
    public void columnNames_invalid() {
        DbExporterDescription desc = new InvalidDbExporterDescription();
        desc.getColumnNames();
    }

    /**
     * カラム名の抽出。
     */
    @Test
    public void normalColumnNames() {
        DbExporterDescription desc = new MockDbExporterDescription();
        assertThat(desc.getTargetColumnNames(), is(Arrays.asList("A", "B", "C")));
    }

    /**
     * カラム名の抽出。
     */
    @Test(expected = RuntimeException.class)
    public void normalCames_invalid() {
        DbExporterDescription desc = new InvalidDbExporterDescription();
        desc.getTargetColumnNames();
    }

    /**
     * 主キーの抽出。
     */
    @Test
    public void primaryKeyNames() {
        DbExporterDescription desc = new MockDbExporterDescription();
        assertThat(desc.getPrimaryKeyNames(), is(Arrays.asList("a")));
    }

    /**
     * 主キーの抽出。
     */
    @Test(expected = RuntimeException.class)
    public void primaryKeyNames_invalid() {
        DbExporterDescription desc = new InvalidDbExporterDescription();
        desc.getPrimaryKeyNames();
    }

    /**
     * 重複チェック情報。
     */
    @Test
    public void duplicateRecordCheck() {
        DbExporterDescription desc = new MockDbExporterDescription();
        assertThat(desc.getDuplicateRecordCheck(), is(nullValue()));
    }
}
