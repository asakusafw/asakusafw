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
package com.asakusafw.testtools.inspect;

import static org.junit.Assert.*;

import org.junit.Test;

import test.modelgen.model.AllTypesWNoerr;

import com.asakusafw.modelgen.source.MySqlDataType;
import com.asakusafw.testtools.ColumnInfo;
import com.asakusafw.testtools.NullValueCondition;
import com.asakusafw.testtools.inspect.Cause.Type;

public class CauseTest {
    // テストに使用するCauseクラスのオブジェクトを各コンストラクタごとに１つづつ作成
    // 各テストメソッドでgetterで正しい値が取得できるかを確認する。

    private static final String MSG1 = "msg1";
    private static final String MSG2 = "msg2";
    private static AllTypesWNoerr expect = new AllTypesWNoerr();
    private static AllTypesWNoerr actual = new AllTypesWNoerr();
    private static ColumnInfo columnInfo = new ColumnInfo("TBL1", "COL1", "CMT", MySqlDataType.CHAR, 5, 0, 0, true, false, null, NullValueCondition.NORMAL);
    private static Cause cause1 = new Cause(Type.COLUMN_VALUE_MISSMATCH, MSG1,
            expect, actual);;
    private static Cause cause2 = new Cause(
            Type.CONDITION_NOW_ON_INVALID_COLUMN, MSG2, expect, actual,
            expect.getCBigintOption(), actual.getCCharOption(), columnInfo);;

    @Test
    public void testGetType() {
        assertEquals(Type.COLUMN_VALUE_MISSMATCH, cause1.getType());
        assertEquals(Type.CONDITION_NOW_ON_INVALID_COLUMN, cause2.getType());
    }

    @Test
    public void testGetMessage() {
        assertTrue(cause1.getMessage().contains(MSG1));
        assertTrue(cause2.getMessage().contains(MSG2));
    }

    @Test
    public void testGetExpect() {
        assertEquals(expect, cause1.getExpect());
        assertEquals(expect, cause2.getExpect());
    }

    @Test
    public void testGetActual() {
        assertEquals(actual, cause1.getActual());
        assertEquals(actual, cause2.getActual());
    }

    @Test
    public void testGetColumnInfo() {
        assertEquals(null,cause1.getColumnInfo());
        assertEquals(columnInfo, cause2.getColumnInfo());
    }

    @Test
    public void testGetActualVal() {
        assertNull(cause1.getActualVal());
        assertEquals(actual.getCCharOption(), cause2.getActualVal());
    }

    @Test
    public void testGetExpectVal() {
        assertNull(null,cause1.getActualVal());
        assertEquals(actual.getCBigintOption(), cause2.getExpectVal());
    }

}
