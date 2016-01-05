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
package com.asakusafw.testtools;


import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import org.apache.hadoop.io.Writable;
import org.junit.Test;

import test.modelgen.model.AllTypesWNoerr;

import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.testtools.db.DbUtils;
import com.asakusafw.testtools.excel.ExcelUtils;

/**
 * @author shinichi.umegane
 *
 */
public class TestDataHolderTest {

    /**
     * TestDataHolderのインスタンス作成のテスト
     * @throws IOException
     */
    @Test
    public void testNormal() throws Exception {
        String TEST_FILE = "src/test/data/Excel/ExcelUtils/ALLT_TYPES_W_NOERR.xls";
        ExcelUtils excelUtils = new ExcelUtils(TEST_FILE);
        TestDataHolder dataHolder = excelUtils.getTestDataHolder();
        List<Writable> sourceList = dataHolder.getSource();
        List<Writable> expectList = dataHolder.getExpect();

        // sourceList, expectListに正しい値がセットされていることの確認
        testModelObjectList(sourceList);
        testModelObjectList(expectList);

        // DBへのストアとDBからのロード
        Connection conn = null;
        try {
            conn = DbUtils.getConnection();
            // テーブルをDROP/CREATE
            dataHolder.storeToDatabase(conn, true);
            dataHolder.loadFromDatabase(conn);
            // テーブルをTRUNCATE
            dataHolder.storeToDatabase(conn, false);
            dataHolder.loadFromDatabase(conn);

        } finally {
            DbUtils.closeQuietly(conn);
        }
    }


    /**
     * モデルオブジェクトのリストに正しい値が含まれていることを確認する
     * @param list テスト対象のモデルオブジェクトのリスト
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    private void testModelObjectList(List<Writable> list) throws Exception {

        // C_BINGINTのテスト
        LongOption longOption = new LongOption();
        testField(list, "getCBigintOption", DATA.NUMERIC_0, longOption.modify(0));
        testField(list, "getCBigintOption", DATA.NUMERIC_1, longOption.modify(1));
        testField(list, "getCBigintOption", DATA.NUMERIC_MINUS1, longOption.modify(-1));
        testField(list, "getCBigintOption", DATA.NUMERIC_MAX, longOption.modify(999999999999999L));
        testField(list, "getCBigintOption", DATA.NUMERIC_MIN, longOption.modify(-999999999999999L));
        testField(list, "getCBigintOption", DATA.NUMERIC_DECIMAL, longOption.setNull());
        testField(list, "getCBigintOption", DATA.NUMERIC_OVER_MAX, longOption.setNull());
        testField(list, "getCBigintOption", DATA.NUMERIC_UNDER_MIN, longOption.setNull());
        testField(list, "getCBigintOption", DATA.NUMERIC_BIG_VALUE, longOption.setNull());
        testField(list, "getCBigintOption", DATA.STRING_0, longOption.modify(0));
        testField(list, "getCBigintOption", DATA.STRING_1, longOption.modify(1));
        testField(list, "getCBigintOption", DATA.STRING_MINUS1, longOption.modify(-1));
        testField(list, "getCBigintOption", DATA.STRING_MAX, longOption.modify(Long.MAX_VALUE));
        testField(list, "getCBigintOption", DATA.STRING_MIN, longOption.modify(Long.MIN_VALUE));
        testField(list, "getCBigintOption", DATA.STRING_DECIMAL, longOption.setNull());
        testField(list, "getCBigintOption", DATA.STRING_OVER_MAX, longOption.setNull());
        testField(list, "getCBigintOption", DATA.STRING_UNDER_MIN, longOption.setNull());
        testField(list, "getCBigintOption", DATA.STRING_BIG_VALUE, longOption.setNull());
        testField(list, "getCBigintOption", DATA.BLANK, longOption.setNull());
        testField(list, "getCBigintOption", DATA.NULL_STRING, longOption.setNull());
        testField(list, "getCBigintOption", DATA.BOOL_TRUE, longOption.modify(1));
        testField(list, "getCBigintOption", DATA.BOOL_FALSE, longOption.modify(0));
        testField(list, "getCBigintOption", DATA.NUMERIC_DATE, longOption.modify(40179));
        testField(list, "getCBigintOption", DATA.DATE_DATE_FMT1, longOption.modify(40179));
        testField(list, "getCBigintOption", DATA.DATE_DATE_FMT2, longOption.modify(40179));
        testField(list, "getCBigintOption", DATA.DATE_DATETIME_FIMT1, longOption.modify(40179));
        testField(list, "getCBigintOption", DATA.DATE_DATETIME_FIMT2, longOption.modify(40179));
        testField(list, "getCBigintOption", DATA.STRING_DATE, longOption.setNull());
        testField(list, "getCBigintOption", DATA.NUMERIC_DATETIME, longOption.setNull());
        testField(list, "getCBigintOption", DATA.DATETIME_DATE_FMT1, longOption.setNull());
        testField(list, "getCBigintOption", DATA.DATETIME_DATE_FMT2, longOption.setNull());
        testField(list, "getCBigintOption", DATA.DATETIME_DATETIME_FIMT1, longOption.setNull());
        testField(list, "getCBigintOption", DATA.DATETIME_DATETIME_FIMT2, longOption.setNull());
        testField(list, "getCBigintOption", DATA.STRING_DATETIME, longOption.setNull());

        // C_INTのテスト
        IntOption intOption = new IntOption();
        testField(list, "getCIntOption", DATA.NUMERIC_0, intOption.modify(0));
        testField(list, "getCIntOption", DATA.NUMERIC_1, intOption.modify(1));
        testField(list, "getCIntOption", DATA.NUMERIC_MINUS1, intOption.modify(-1));
        testField(list, "getCIntOption", DATA.NUMERIC_MAX, intOption.modify(Integer.MAX_VALUE));
        testField(list, "getCIntOption", DATA.NUMERIC_MIN, intOption.modify(Integer.MIN_VALUE));
        testField(list, "getCIntOption", DATA.NUMERIC_DECIMAL, intOption.setNull());
        testField(list, "getCIntOption", DATA.NUMERIC_OVER_MAX, intOption.setNull());
        testField(list, "getCIntOption", DATA.NUMERIC_UNDER_MIN, intOption.setNull());
        testField(list, "getCIntOption", DATA.NUMERIC_BIG_VALUE, intOption.setNull());
        testField(list, "getCIntOption", DATA.STRING_0, intOption.modify(0));
        testField(list, "getCIntOption", DATA.STRING_1, intOption.modify(1));
        testField(list, "getCIntOption", DATA.STRING_MINUS1, intOption.modify(-1));
        testField(list, "getCIntOption", DATA.STRING_MAX, intOption.modify(Integer.MAX_VALUE));
        testField(list, "getCIntOption", DATA.STRING_MIN, intOption.modify(Integer.MIN_VALUE));
        testField(list, "getCIntOption", DATA.STRING_DECIMAL, intOption.setNull());
        testField(list, "getCIntOption", DATA.STRING_OVER_MAX, intOption.setNull());
        testField(list, "getCIntOption", DATA.STRING_UNDER_MIN, intOption.setNull());
        testField(list, "getCIntOption", DATA.STRING_BIG_VALUE, intOption.setNull());
        testField(list, "getCIntOption", DATA.BLANK, intOption.setNull());
        testField(list, "getCIntOption", DATA.NULL_STRING, intOption.setNull());
        testField(list, "getCIntOption", DATA.BOOL_TRUE, intOption.modify(1));
        testField(list, "getCIntOption", DATA.BOOL_FALSE, intOption.modify(0));
        testField(list, "getCIntOption", DATA.NUMERIC_DATE, intOption.modify(40179));
        testField(list, "getCIntOption", DATA.DATE_DATE_FMT1, intOption.modify(40179));
        testField(list, "getCIntOption", DATA.DATE_DATE_FMT2, intOption.modify(40179));
        testField(list, "getCIntOption", DATA.DATE_DATETIME_FIMT1, intOption.modify(40179));
        testField(list, "getCIntOption", DATA.DATE_DATETIME_FIMT2, intOption.modify(40179));
        testField(list, "getCIntOption", DATA.STRING_DATE, intOption.setNull());
        testField(list, "getCIntOption", DATA.NUMERIC_DATETIME, intOption.setNull());
        testField(list, "getCIntOption", DATA.DATETIME_DATE_FMT1, intOption.setNull());
        testField(list, "getCIntOption", DATA.DATETIME_DATE_FMT2, intOption.setNull());
        testField(list, "getCIntOption", DATA.DATETIME_DATETIME_FIMT1, intOption.setNull());
        testField(list, "getCIntOption", DATA.DATETIME_DATETIME_FIMT2, intOption.setNull());
        testField(list, "getCIntOption", DATA.STRING_DATETIME, intOption.setNull());

        // C_SMALLINTのテスト
        ShortOption shortOption = new ShortOption();
        testField(list, "getCSmallintOption", DATA.NUMERIC_0, shortOption.modify((short) 0));
        testField(list, "getCSmallintOption", DATA.NUMERIC_1, shortOption.modify((short) 1));
        testField(list, "getCSmallintOption", DATA.NUMERIC_MINUS1, shortOption.modify((short) -1));
        testField(list, "getCSmallintOption", DATA.NUMERIC_MAX, shortOption.modify(Short.MAX_VALUE));
        testField(list, "getCSmallintOption", DATA.NUMERIC_MIN, shortOption.modify(Short.MIN_VALUE));
        testField(list, "getCSmallintOption", DATA.NUMERIC_DECIMAL, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.NUMERIC_OVER_MAX, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.NUMERIC_UNDER_MIN, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.NUMERIC_BIG_VALUE, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.STRING_0, shortOption.modify((short) 0));
        testField(list, "getCSmallintOption", DATA.STRING_1, shortOption.modify((short) 1));
        testField(list, "getCSmallintOption", DATA.STRING_MINUS1, shortOption.modify((short) -1));
        testField(list, "getCSmallintOption", DATA.STRING_MAX, shortOption.modify(Short.MAX_VALUE));
        testField(list, "getCSmallintOption", DATA.STRING_MIN, shortOption.modify(Short.MIN_VALUE));
        testField(list, "getCSmallintOption", DATA.STRING_DECIMAL, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.STRING_OVER_MAX, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.STRING_UNDER_MIN, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.STRING_BIG_VALUE, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.BLANK, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.NULL_STRING, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.BOOL_TRUE, shortOption.modify((short) 1));
        testField(list, "getCSmallintOption", DATA.BOOL_FALSE, shortOption.modify((short) 0));
        testField(list, "getCSmallintOption", DATA.NUMERIC_DATE, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.DATE_DATE_FMT1, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.DATE_DATE_FMT2, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.DATE_DATETIME_FIMT1, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.DATE_DATETIME_FIMT2, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.STRING_DATE, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.NUMERIC_DATETIME, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.DATETIME_DATE_FMT1, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.DATETIME_DATE_FMT2, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.DATETIME_DATETIME_FIMT1, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.DATETIME_DATETIME_FIMT2, shortOption.setNull());
        testField(list, "getCSmallintOption", DATA.STRING_DATETIME, shortOption.setNull());

        // C_TINYINTのテスト
        ByteOption byteOption = new ByteOption();
        testField(list, "getCTinyintOption", DATA.NUMERIC_0, byteOption.modify((byte) 0));
        testField(list, "getCTinyintOption", DATA.NUMERIC_1, byteOption.modify((byte) 1));
        testField(list, "getCTinyintOption", DATA.NUMERIC_MINUS1, byteOption.modify((byte) -1));
        testField(list, "getCTinyintOption", DATA.NUMERIC_MAX, byteOption.modify(Byte.MAX_VALUE));
        testField(list, "getCTinyintOption", DATA.NUMERIC_MIN, byteOption.modify(Byte.MIN_VALUE));
        testField(list, "getCTinyintOption", DATA.NUMERIC_DECIMAL, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.NUMERIC_OVER_MAX, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.NUMERIC_UNDER_MIN, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.NUMERIC_BIG_VALUE, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.STRING_0, byteOption.modify((byte) 0));
        testField(list, "getCTinyintOption", DATA.STRING_1, byteOption.modify((byte) 1));
        testField(list, "getCTinyintOption", DATA.STRING_MINUS1, byteOption.modify((byte) -1));
        testField(list, "getCTinyintOption", DATA.STRING_MAX, byteOption.modify(Byte.MAX_VALUE));
        testField(list, "getCTinyintOption", DATA.STRING_MIN, byteOption.modify(Byte.MIN_VALUE));
        testField(list, "getCTinyintOption", DATA.STRING_DECIMAL, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.STRING_OVER_MAX, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.STRING_UNDER_MIN, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.STRING_BIG_VALUE, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.BLANK, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.NULL_STRING, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.BOOL_TRUE, byteOption.modify((byte) 1));
        testField(list, "getCTinyintOption", DATA.BOOL_FALSE, byteOption.modify((byte) 0));
        testField(list, "getCTinyintOption", DATA.NUMERIC_DATE, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.DATE_DATE_FMT1, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.DATE_DATE_FMT2, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.DATE_DATETIME_FIMT1, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.DATE_DATETIME_FIMT2, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.STRING_DATE, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.NUMERIC_DATETIME, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.DATETIME_DATE_FMT1, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.DATETIME_DATE_FMT2, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.DATETIME_DATETIME_FIMT1, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.DATETIME_DATETIME_FIMT2, byteOption.setNull());
        testField(list, "getCTinyintOption", DATA.STRING_DATETIME, byteOption.setNull());

        // C_CHARのテスト
        StringOption stringOption = new StringOption();
        testField(list, "getCCharOption", DATA.NUMERIC_0, stringOption.modify("0"));
        testField(list, "getCCharOption", DATA.NUMERIC_1, stringOption.modify("1"));
        testField(list, "getCCharOption", DATA.NUMERIC_MINUS1, stringOption.modify("-1"));
        testField(list, "getCCharOption", DATA.NUMERIC_MAX, stringOption.modify("9.2233720368547697E18"));
        testField(list, "getCCharOption", DATA.NUMERIC_MIN, stringOption.modify("-9.2233720368547697E18"));
        testField(list, "getCCharOption", DATA.NUMERIC_DECIMAL, stringOption.modify("6.54321"));
        testField(list, "getCCharOption", DATA.NUMERIC_OVER_MAX, stringOption.modify("9.2233720368547697E18"));
        testField(list, "getCCharOption", DATA.NUMERIC_UNDER_MIN, stringOption.modify("-9.2233720368547697E18"));
        testField(list, "getCCharOption", DATA.NUMERIC_BIG_VALUE, stringOption.modify("1.23456789012345E19"));
        testField(list, "getCCharOption", DATA.STRING_0, stringOption.modify("0"));
        testField(list, "getCCharOption", DATA.STRING_1, stringOption.modify("1"));
        testField(list, "getCCharOption", DATA.STRING_MINUS1, stringOption.modify("-1"));
        testField(list, "getCCharOption", DATA.STRING_MAX, stringOption.setNull());
        testField(list, "getCCharOption", DATA.STRING_MIN, stringOption.setNull());
        testField(list, "getCCharOption", DATA.STRING_DECIMAL, stringOption.modify("6.54321"));
        testField(list, "getCCharOption", DATA.STRING_OVER_MAX, stringOption.modify("9223372036854775808"));
        testField(list, "getCCharOption", DATA.STRING_UNDER_MIN, stringOption.modify("-9223372036854775809"));
        testField(list, "getCCharOption", DATA.STRING_BIG_VALUE, stringOption.modify("12345678901234567890"));
        testField(list, "getCCharOption", DATA.BLANK, stringOption.setNull());
        testField(list, "getCCharOption", DATA.NULL_STRING, stringOption.modify(""));
        testField(list, "getCCharOption", DATA.BOOL_TRUE, stringOption.modify("1"));
        testField(list, "getCCharOption", DATA.BOOL_FALSE, stringOption.modify("0"));
        testField(list, "getCCharOption", DATA.NUMERIC_DATE, stringOption.modify("40179"));
        testField(list, "getCCharOption", DATA.DATE_DATE_FMT1, stringOption.modify("2010-01-01 00:00:00"));
        testField(list, "getCCharOption", DATA.DATE_DATE_FMT2, stringOption.modify("2010-01-01 00:00:00"));
        testField(list, "getCCharOption", DATA.DATE_DATETIME_FIMT1, stringOption.modify("2010-01-01 00:00:00"));
        testField(list, "getCCharOption", DATA.DATE_DATETIME_FIMT2, stringOption.modify("2010-01-01 00:00:00"));
        testField(list, "getCCharOption", DATA.STRING_DATE, stringOption.modify("2010-01-01"));
        testField(list, "getCCharOption", DATA.NUMERIC_DATETIME, stringOption.modify("40452.50090277778"));
        testField(list, "getCCharOption", DATA.DATETIME_DATE_FMT1, stringOption.modify("2010-10-01 12:01:18"));
        testField(list, "getCCharOption", DATA.DATETIME_DATE_FMT2, stringOption.modify("2010-10-01 12:01:18"));
        testField(list, "getCCharOption", DATA.DATETIME_DATETIME_FIMT1, stringOption.modify("2010-10-01 12:01:18"));
        testField(list, "getCCharOption", DATA.DATETIME_DATETIME_FIMT2, stringOption.modify("2010-10-01 12:01:18"));
        testField(list, "getCCharOption", DATA.STRING_DATETIME, stringOption.modify("2010-10-01 12:01:18"));

        // C_VCHARのテスト
        testField(list, "getCVcharOption", DATA.NUMERIC_0, stringOption.modify("0"));
        testField(list, "getCVcharOption", DATA.NUMERIC_1, stringOption.modify("1"));
        testField(list, "getCVcharOption", DATA.NUMERIC_MINUS1, stringOption.modify("-1"));
        testField(list, "getCVcharOption", DATA.NUMERIC_MAX, stringOption.modify("9.2233720368547697E18"));
        testField(list, "getCVcharOption", DATA.NUMERIC_MIN, stringOption.modify("-9.2233720368547697E18"));
        testField(list, "getCVcharOption", DATA.NUMERIC_DECIMAL, stringOption.modify("6.54321"));
        testField(list, "getCVcharOption", DATA.NUMERIC_OVER_MAX, stringOption.modify("9.2233720368547697E18"));
        testField(list, "getCVcharOption", DATA.NUMERIC_UNDER_MIN, stringOption.modify("-9.2233720368547697E18"));
        testField(list, "getCVcharOption", DATA.NUMERIC_BIG_VALUE, stringOption.modify("1.23456789012345E19"));
        testField(list, "getCVcharOption", DATA.STRING_0, stringOption.modify("0"));
        testField(list, "getCVcharOption", DATA.STRING_1, stringOption.modify("1"));
        testField(list, "getCVcharOption", DATA.STRING_MINUS1, stringOption.modify("-1"));
        testField(list, "getCVcharOption", DATA.STRING_MAX, stringOption.setNull());
        testField(list, "getCVcharOption", DATA.STRING_MIN, stringOption.setNull());
        testField(list, "getCVcharOption", DATA.STRING_DECIMAL, stringOption.modify("6.54321"));
        testField(list, "getCVcharOption", DATA.STRING_OVER_MAX, stringOption.modify("9223372036854775808"));
        testField(list, "getCVcharOption", DATA.STRING_UNDER_MIN, stringOption.modify("-9223372036854775809"));
        testField(list, "getCVcharOption", DATA.STRING_BIG_VALUE, stringOption.modify("12345678901234567890"));
        testField(list, "getCVcharOption", DATA.BLANK, stringOption.setNull());
        testField(list, "getCVcharOption", DATA.NULL_STRING, stringOption.modify(""));
        testField(list, "getCVcharOption", DATA.BOOL_TRUE, stringOption.modify("1"));
        testField(list, "getCVcharOption", DATA.BOOL_FALSE, stringOption.modify("0"));
        testField(list, "getCVcharOption", DATA.NUMERIC_DATE, stringOption.modify("40179"));
        testField(list, "getCVcharOption", DATA.DATE_DATE_FMT1, stringOption.modify("2010-01-01 00:00:00"));
        testField(list, "getCVcharOption", DATA.DATE_DATE_FMT2, stringOption.modify("2010-01-01 00:00:00"));
        testField(list, "getCVcharOption", DATA.DATE_DATETIME_FIMT1, stringOption.modify("2010-01-01 00:00:00"));
        testField(list, "getCVcharOption", DATA.DATE_DATETIME_FIMT2, stringOption.modify("2010-01-01 00:00:00"));
        testField(list, "getCVcharOption", DATA.STRING_DATE, stringOption.modify("2010-01-01"));
        testField(list, "getCVcharOption", DATA.NUMERIC_DATETIME, stringOption.modify("40452.50090277778"));
        testField(list, "getCVcharOption", DATA.DATETIME_DATE_FMT1, stringOption.modify("2010-10-01 12:01:18"));
        testField(list, "getCVcharOption", DATA.DATETIME_DATE_FMT2, stringOption.modify("2010-10-01 12:01:18"));
        testField(list, "getCVcharOption", DATA.DATETIME_DATETIME_FIMT1, stringOption.modify("2010-10-01 12:01:18"));
        testField(list, "getCVcharOption", DATA.DATETIME_DATETIME_FIMT2, stringOption.modify("2010-10-01 12:01:18"));
        testField(list, "getCVcharOption", DATA.STRING_DATETIME, stringOption.modify("2010-10-01 12:01:18"));

        // C_DATETIMEのテスト
        DateTimeOption dateTimeOption = new DateTimeOption();
        DateTime dateTime = new DateTime();
        dateTimeOption.setNull();
        testField(list, "getCDatetimeOption", DATA.NUMERIC_0, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.NUMERIC_1, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.NUMERIC_MINUS1, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.NUMERIC_MAX, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.NUMERIC_MIN, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.NUMERIC_DECIMAL, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.NUMERIC_OVER_MAX, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.NUMERIC_UNDER_MIN, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.NUMERIC_BIG_VALUE, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.STRING_0, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.STRING_1, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.STRING_MINUS1, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.STRING_MAX, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.STRING_MIN, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.STRING_DECIMAL, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.STRING_OVER_MAX, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.STRING_UNDER_MIN, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.STRING_BIG_VALUE, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.BLANK, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.NULL_STRING, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.BOOL_TRUE, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.BOOL_FALSE, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.NUMERIC_DATE, dateTimeOption);
        dateTime.setElapsedSeconds(DateUtil.getDayFromDate(2010, 1, 1) * 86400L);
        dateTimeOption.modify(dateTime);
        testField(list, "getCDatetimeOption", DATA.DATE_DATE_FMT1, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.DATE_DATE_FMT2, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.DATE_DATETIME_FIMT1, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.DATE_DATETIME_FIMT2, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.STRING_DATE, dateTimeOption);
        dateTimeOption.setNull();
        testField(list, "getCDatetimeOption", DATA.NUMERIC_DATETIME, dateTimeOption);
        dateTime.setElapsedSeconds(DateUtil.getDayFromDate(2010, 10, 1) * 86400L + DateUtil.getSecondFromTime(12, 1, 18));
        dateTimeOption.modify(dateTime);
        testField(list, "getCDatetimeOption", DATA.DATETIME_DATE_FMT1, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.DATETIME_DATE_FMT2, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.DATETIME_DATETIME_FIMT1, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.DATETIME_DATETIME_FIMT2, dateTimeOption);
        testField(list, "getCDatetimeOption", DATA.STRING_DATETIME, dateTimeOption);

        // C_DATEのテスト
        DateOption dateOption = new DateOption();
        dateOption.setNull();
        testField(list, "getCDateOption", DATA.NUMERIC_0, dateOption);
        testField(list, "getCDateOption", DATA.NUMERIC_1, dateOption);
        testField(list, "getCDateOption", DATA.NUMERIC_MINUS1, dateOption);
        testField(list, "getCDateOption", DATA.NUMERIC_MAX, dateOption);
        testField(list, "getCDateOption", DATA.NUMERIC_MIN, dateOption);
        testField(list, "getCDateOption", DATA.NUMERIC_DECIMAL, dateOption);
        testField(list, "getCDateOption", DATA.NUMERIC_OVER_MAX, dateOption);
        testField(list, "getCDateOption", DATA.NUMERIC_UNDER_MIN, dateOption);
        testField(list, "getCDateOption", DATA.NUMERIC_BIG_VALUE, dateOption);
        testField(list, "getCDateOption", DATA.STRING_0, dateOption);
        testField(list, "getCDateOption", DATA.STRING_1, dateOption);
        testField(list, "getCDateOption", DATA.STRING_MINUS1, dateOption);
        testField(list, "getCDateOption", DATA.STRING_MAX, dateOption);
        testField(list, "getCDateOption", DATA.STRING_MIN, dateOption);
        testField(list, "getCDateOption", DATA.STRING_DECIMAL, dateOption);
        testField(list, "getCDateOption", DATA.STRING_OVER_MAX, dateOption);
        testField(list, "getCDateOption", DATA.STRING_UNDER_MIN, dateOption);
        testField(list, "getCDateOption", DATA.STRING_BIG_VALUE, dateOption);
        testField(list, "getCDateOption", DATA.BLANK, dateOption);
        testField(list, "getCDateOption", DATA.NULL_STRING, dateOption);
        testField(list, "getCDateOption", DATA.BOOL_TRUE, dateOption);
        testField(list, "getCDateOption", DATA.BOOL_FALSE, dateOption);
        testField(list, "getCDateOption", DATA.NUMERIC_DATE, dateOption);
        dateOption.modify(DateUtil.getDayFromDate(2010, 1, 1));
        testField(list, "getCDateOption", DATA.DATE_DATE_FMT1, dateOption);
        testField(list, "getCDateOption", DATA.DATE_DATE_FMT2, dateOption);
        testField(list, "getCDateOption", DATA.DATE_DATETIME_FIMT1, dateOption);
        testField(list, "getCDateOption", DATA.DATE_DATETIME_FIMT2, dateOption);
        testField(list, "getCDateOption", DATA.STRING_DATE, dateOption);
        dateOption.setNull();
        testField(list, "getCDateOption", DATA.NUMERIC_DATETIME, dateOption);
        testField(list, "getCDateOption", DATA.DATETIME_DATE_FMT1, dateOption);
        testField(list, "getCDateOption", DATA.DATETIME_DATE_FMT2, dateOption);
        testField(list, "getCDateOption", DATA.DATETIME_DATETIME_FIMT1, dateOption);
        testField(list, "getCDateOption", DATA.DATETIME_DATETIME_FIMT2, dateOption);
        testField(list, "getCDateOption", DATA.STRING_DATETIME, dateOption);

        // C_DECIMAL20_0のテスト
        DecimalOption decimalOption = new DecimalOption();
        testField(list, "getCDecimal200Option", DATA.NUMERIC_0, decimalOption.modify(new BigDecimal(0)));
        testField(list, "getCDecimal200Option", DATA.NUMERIC_1, decimalOption.modify(new BigDecimal(1)));
        testField(list, "getCDecimal200Option", DATA.NUMERIC_MINUS1, decimalOption.modify(new BigDecimal(-1)));
        testField(list, "getCDecimal200Option", DATA.NUMERIC_MAX, decimalOption.modify(new BigDecimal(999999999999999L)));
        testField(list, "getCDecimal200Option", DATA.NUMERIC_MIN, decimalOption.modify(new BigDecimal(-999999999999999L)));
        testField(list, "getCDecimal200Option", DATA.NUMERIC_DECIMAL, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.NUMERIC_OVER_MAX, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.NUMERIC_UNDER_MIN, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.NUMERIC_BIG_VALUE, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.STRING_0, decimalOption.modify(new BigDecimal(0)));
        testField(list, "getCDecimal200Option", DATA.STRING_1, decimalOption.modify(new BigDecimal(1)));
        testField(list, "getCDecimal200Option", DATA.STRING_MINUS1, decimalOption.modify(new BigDecimal(-1)));
        testField(list, "getCDecimal200Option", DATA.STRING_MAX, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.STRING_MIN, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.STRING_DECIMAL, decimalOption.modify(new BigDecimal("7")));
        testField(list, "getCDecimal200Option", DATA.STRING_OVER_MAX, decimalOption.modify(new BigDecimal("9223372036854775808")));
        testField(list, "getCDecimal200Option", DATA.STRING_UNDER_MIN, decimalOption.modify(new BigDecimal("-9223372036854775809")));
        testField(list, "getCDecimal200Option", DATA.STRING_BIG_VALUE, decimalOption.modify(new BigDecimal("12345678901234567890")));
        testField(list, "getCDecimal200Option", DATA.BLANK, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.NULL_STRING, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.BOOL_TRUE, decimalOption.modify(new BigDecimal(1)));
        testField(list, "getCDecimal200Option", DATA.BOOL_FALSE, decimalOption.modify(new BigDecimal(0)));
        testField(list, "getCDecimal200Option", DATA.NUMERIC_DATE, decimalOption.modify(new BigDecimal(40179)));
        testField(list, "getCDecimal200Option", DATA.DATE_DATE_FMT1, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.DATE_DATE_FMT2, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.DATE_DATETIME_FIMT1, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.DATE_DATETIME_FIMT2, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.STRING_DATE, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.NUMERIC_DATETIME, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.DATETIME_DATE_FMT1, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.DATETIME_DATE_FMT2, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.DATETIME_DATETIME_FIMT1, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.DATETIME_DATETIME_FIMT2, decimalOption.setNull());
        testField(list, "getCDecimal200Option", DATA.STRING_DATETIME, decimalOption.setNull());

        // C_DECIMAL10_5のテスト
        testField(list, "getCDecimal255Option", DATA.NUMERIC_0, decimalOption.modify(new BigDecimal(0)));
        testField(list, "getCDecimal255Option", DATA.NUMERIC_1, decimalOption.modify(new BigDecimal(1)));
        testField(list, "getCDecimal255Option", DATA.NUMERIC_MINUS1, decimalOption.modify(new BigDecimal(-1)));
        testField(list, "getCDecimal255Option", DATA.NUMERIC_MAX, decimalOption.modify(new BigDecimal(999999999999999L)));
        testField(list, "getCDecimal255Option", DATA.NUMERIC_MIN, decimalOption.modify(new BigDecimal(-999999999999999L)));
        testField(list, "getCDecimal255Option", DATA.NUMERIC_DECIMAL, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.NUMERIC_OVER_MAX, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.NUMERIC_UNDER_MIN, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.NUMERIC_BIG_VALUE, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.STRING_0, decimalOption.modify(new BigDecimal(0)));
        testField(list, "getCDecimal255Option", DATA.STRING_1, decimalOption.modify(new BigDecimal(1)));
        testField(list, "getCDecimal255Option", DATA.STRING_MINUS1, decimalOption.modify(new BigDecimal(-1)));
        testField(list, "getCDecimal255Option", DATA.STRING_MAX, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.STRING_MIN, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.STRING_DECIMAL, decimalOption.modify(new BigDecimal("6.54321")));
        testField(list, "getCDecimal255Option", DATA.STRING_OVER_MAX, decimalOption.modify(new BigDecimal("9223372036854775808")));
        testField(list, "getCDecimal255Option", DATA.STRING_UNDER_MIN, decimalOption.modify(new BigDecimal("-9223372036854775809")));
        testField(list, "getCDecimal255Option", DATA.STRING_BIG_VALUE, decimalOption.modify(new BigDecimal("12345678901234567890")));
        testField(list, "getCDecimal255Option", DATA.BLANK, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.NULL_STRING, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.BOOL_TRUE, decimalOption.modify(new BigDecimal(1)));
        testField(list, "getCDecimal255Option", DATA.BOOL_FALSE, decimalOption.modify(new BigDecimal(0)));
        testField(list, "getCDecimal255Option", DATA.NUMERIC_DATE, decimalOption.modify(new BigDecimal(40179)));
        testField(list, "getCDecimal255Option", DATA.DATE_DATE_FMT1, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.DATE_DATE_FMT2, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.DATE_DATETIME_FIMT1, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.DATE_DATETIME_FIMT2, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.STRING_DATE, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.NUMERIC_DATETIME, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.DATETIME_DATE_FMT1, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.DATETIME_DATE_FMT2, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.DATETIME_DATETIME_FIMT1, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.DATETIME_DATETIME_FIMT2, decimalOption.setNull());
        testField(list, "getCDecimal255Option", DATA.STRING_DATETIME, decimalOption.setNull());
    }

    /**
     * モデルオブジェクトの特定のフィールドの値が正しいことをテストする
     * @param list
     * @param getterName
     * @param data
     * @param type
     * @param expected
     * @throws Exception
     */
    private void testField(List<Writable> list, String getterName,  DATA data, Object expected) throws Exception {
        int index = data.getRownum() - 1; // Excelシートの一行目がリストの最初に入るため -1 する必要がある
        Writable modelObject = list.get(index);
        Method method = modelObject.getClass().getMethod(getterName);
        Object actual = method.invoke(modelObject);
        String format = "テストデータ: %s, getter名 %s";
        String message = String.format(format, data.getComment(), getterName);
        assertEquals("型の一致の確認 " + message, expected.getClass(), actual.getClass());
        assertEquals("値の一致の確認 " + message, expected, actual);
    }


    /**
     * 各行にどのようなテストデータが定義されているかを表す列挙型
     */
    private enum DATA {
        NUMERIC_0("数値の0", 1),
        NUMERIC_1("数値の1", 2),
        NUMERIC_MINUS1("数値の-1", 3),
        NUMERIC_MAX("表現可能な最大値(数値)", 4),
        NUMERIC_MIN("表現可能な最小値(数値)", 5),
        NUMERIC_DECIMAL("小数値(数値)", 6),
        NUMERIC_OVER_MAX("表現可能な最大値+1(数値)", 7),
        NUMERIC_UNDER_MIN("表現可能な最小値-1(数値)", 8),
        NUMERIC_BIG_VALUE("大きな値(数値)", 9),
        STRING_0("文字列の0", 10),
        STRING_1("文字列の1", 11),
        STRING_MINUS1("文字列の-1", 12),
        STRING_MAX("表現可能な最大値(文字列)", 13),
        STRING_MIN("表現可能な最小値(文字列)", 14),
        STRING_DECIMAL("小数値(文字列)", 15),
        STRING_OVER_MAX("表現可能な最大値+1(文字列)", 16),
        STRING_UNDER_MIN("表現可能な最小値-1(文字列)", 17),
        STRING_BIG_VALUE("大きな値(数値)", 18),
        BLANK("ブランク", 19),
        NULL_STRING("空文字列", 20),
        BOOL_TRUE("BOOL値(TRUE)", 21),
        BOOL_FALSE("BOOL値(FALSE)", 22),
        NUMERIC_DATE("日付(数値フォーマット)", 23),
        DATE_DATE_FMT1("日付(日付フォーマット1)", 24),
        DATE_DATE_FMT2("日付(日付フォーマット2)", 25),
        DATE_DATETIME_FIMT1("日付(時刻フォーマット1)", 26),
        DATE_DATETIME_FIMT2("日付(時刻フォーマット2)", 27),
        STRING_DATE("日付(文字列)", 28),
        NUMERIC_DATETIME("時刻(数値フォーマット)", 29),
        DATETIME_DATE_FMT1("時刻(日付フォーマット1)", 30),
        DATETIME_DATE_FMT2("時刻(日付フォーマット2)", 31),
        DATETIME_DATETIME_FIMT1("時刻(時刻フォーマット1)", 32),
        DATETIME_DATETIME_FIMT2("時刻(時刻フォーマット2)", 33),
        STRING_DATETIME("時刻(文字列)", 34);

        /**
         * コメント
         */
        private final String comment;

        /**
         * EXCELシートの行位置
         */
        private final int rownum;

        private DATA(String comment, int rownum) {
            this.comment = comment;
            this.rownum = rownum;
        }

        public String getComment() {
            return comment;
        }

        public int getRownum() {
            return rownum;
        }
    }


    /**
     * sortメソッドのテスト
     * @throws Exception
     */
    @Test
    public void testSort() throws Exception {
        String TEST_FILE = "src/test/data/Excel/ExcelUtils/SORT_TEST.xls";
        ExcelUtils excelUtils = new ExcelUtils(TEST_FILE);
        TestDataHolder dataHolder = excelUtils.getTestDataHolder();
        dataHolder.sort();
        List<Writable> expectList = dataHolder.getExpect();
        int expectValue = 0;
        for(Writable modelObject: expectList) {
            expectValue++;
            AllTypesWNoerr a = (AllTypesWNoerr) modelObject;
            int actual = Integer.parseInt( a.getCTagOption().getAsString());
            assertEquals(expectValue, actual);
        }

    }
}
