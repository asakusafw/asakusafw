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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;
import com.asakusafw.testtools.ColumnInfo;
import com.asakusafw.testtools.inspect.Cause.Type;

/**
 * 標準的な{@link Inspector}の実装。
 */
public final class DefaultInspector extends AbstractInspector {

    @Override
    protected void inspect(Writable expectRow, Writable actualRow) {
        for (ColumnInfo columnInfo : getColumnInfos()) {
            Method method;
            try {
                method = expectRow.getClass().getMethod(columnInfo.getGetterName());
                ValueOption<?> expectVal = (ValueOption<?>) method.invoke(expectRow);
                ValueOption<?> actualVal = (ValueOption<?>) method.invoke(actualRow);
                inspect(expectRow, actualRow, expectVal, actualVal, columnInfo);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 指定のレコードに対する値を検査する。
     * @param expect 期待値のレコード(エラーメッセージの作成に使用)
     * @param actual 実値のレコード(エラーメッセージの作成に使用)
     * @param expectVal 期待値
     * @param actualVal 実値
     * @param columnInfo カラム情報
     */
    private void inspect(
            Writable expect,
            Writable actual,
            ValueOption<?> expectVal,
            ValueOption<?> actualVal,
            ColumnInfo columnInfo) {
        if (actualVal.isNull()) {
            switch (columnInfo.getNullValueCondition()) {
            case NULL_IS_NG:
                fail(Type.COLUMN_VALUE_MISSMATCH, expect, actual, expectVal, actualVal, columnInfo);
                return;
            case NULL_IS_OK:
                return;
            case NORMAL:
            case NOT_NULL_IS_NG:
            case NOT_NULL_IS_OK:
                break; // 次に進む
            default:
                throw new RuntimeException("Unsupported test condition");
            }
        }
        if (!actualVal.isNull()) {
            switch (columnInfo.getNullValueCondition()) {
            case NOT_NULL_IS_NG:
                fail(Type.COLUMN_VALUE_MISSMATCH, expect, actual, expectVal, actualVal, columnInfo);
                return;
            case NOT_NULL_IS_OK:
                return;
            case NORMAL:
            case NULL_IS_OK:
            case NULL_IS_NG:
                break; // 次に進む
            default:
                throw new RuntimeException("Unsupported test condition");
            }
        }
        switch (columnInfo.getColumnMatchingCondition()) {
        case EXACT: // 完全一致
            inspectExact(expect, actual, expectVal, actualVal, columnInfo);
            break;
        case NONE: // 検査しない場合常に成功
            return;
        case NOW: // 現在時刻
            inspectNow(expect, actual, expectVal, actualVal, columnInfo);
            break;
        case TODAY: // 現在日
            inspectToday(expect, actual, expectVal, actualVal, columnInfo);
            break;
        case PARTIAL: // 部分一致
            inspectPartialt(expect, actual, expectVal, actualVal, columnInfo);
            break;
        default:
            throw new RuntimeException("Unsupported test Condition");
        }
    }

    /**
     * 指定のレコードに対する値を厳密に検査する。
     * @param expect 期待値のレコード(エラーメッセージの作成に使用)
     * @param actual 実値のレコード(エラーメッセージの作成に使用)
     * @param expectVal 期待値
     * @param actualVal 実値
     * @param columnInfo カラム情報
     */
    private void inspectExact(
            Writable expect,
            Writable actual,
            ValueOption<?> expectVal,
            ValueOption<?> actualVal,
            ColumnInfo columnInfo) {
        if (!expectVal.equals(actualVal)) {
            fail(Type.COLUMN_VALUE_MISSMATCH, expect, actual, expectVal, actualVal, columnInfo);
            return;
        } else {
            return;
        }
    }

    /**
     * 指定のレコードに対する値を部分的に検査する。
     * @param expect 期待値のレコード(エラーメッセージの作成に使用)
     * @param actual 実値のレコード(エラーメッセージの作成に使用)
     * @param expectVal 期待値
     * @param actualVal 実値
     * @param columnInfo カラム情報
     */
    private void inspectPartialt(
            Writable expect,
            Writable actual,
            ValueOption<?> expectVal,
            ValueOption<?> actualVal,
            ColumnInfo columnInfo) {
        // StringOptionにキャスト
        StringOption actualStringOption;
        StringOption expectStringOption;
        if (actualVal instanceof StringOption) {
            actualStringOption = (StringOption) actualVal;
            expectStringOption = (StringOption) expectVal;
        } else {
            fail(Type.CONDITION_PARTIAL_ON_INVALID_COLUMN, expect, actual, expectVal, actualVal, columnInfo);
            return;
        }
        // まず完全一致をテストする(期待値と実値が両方ともNULLまたはNULL文字列の場合も
        // 完全一致とみなされるため、以降期待値と実値が両方ともNULLまたはNULL文字列の場合を
        // 考慮しなくてもよい
        if (actualStringOption.equals(expectStringOption)) {
            return;
        }
        // 期待値、実地のどちらかがNULLかNULL文字列の場合、検証を失敗させる
        if (actualStringOption.isNull()
                || expectStringOption.isNull()
                || actualStringOption.getAsString().length() == 0
                || expectStringOption.getAsString().length() == 0) {
            fail(Type.COLUMN_VALUE_MISSMATCH, expect, actual, expectVal, actualVal, columnInfo);
            return;
        }
        String actualString = actualStringOption.getAsString();
        String expectString = expectStringOption.getAsString();
        if (actualString.indexOf(expectString) == -1) {
            fail(Type.COLUMN_VALUE_MISSMATCH, expect, actual, expectVal, actualVal, columnInfo);
            return;
        } else {
            return;
        }
    }

    /**
     * 「現在」に関する項目を検査する。
     * @param expect 期待値のレコード(エラーメッセージの作成に使用)
     * @param actual 実値のレコード(エラーメッセージの作成に使用)
     * @param expectVal 期待値
     * @param actualVal 実値
     * @param columnInfo カラム情報
     */
    private void inspectNow(
            Writable expect,
            Writable actual,
            ValueOption<?> expectVal,
            ValueOption<?> actualVal,
            ColumnInfo columnInfo) {
        Calendar cal = Calendar.getInstance();
        if (actualVal instanceof DateTimeOption) {
            if (actualVal.isNull()) {
                fail(Type.NOT_IN_TESTING_TIME, expect, actual, expectVal, actualVal, columnInfo);
                return;
            }
            DateTimeOption dateTimeOption = (DateTimeOption) actualVal;
            DateTime dateTime = dateTimeOption.get();
            int y = dateTime.getYear();
            int m = dateTime.getMonth();
            int d = dateTime.getDay();
            int h = dateTime.getHour();
            int min = dateTime.getMinute();
            int s = dateTime.getSecond();
            cal.clear();
            cal.set(y, m - 1, d, h, min, s);
        } else {
            fail(Type.CONDITION_NOW_ON_INVALID_COLUMN, expect, actual, expectVal, actualVal, columnInfo);
            return;
        }
        long time = cal.getTimeInMillis();
        // getStartTime()はミリ秒の値を持つのに対し、timeはミリ秒の値を持たない
        // この影響を排除するため、startTime - 1秒の値を使用して比較する
        if (getStartTime() - 1000 <= time && time <= getFinishTime()) {
            return;
        } else {
            fail(Type.NOT_IN_TESTING_TIME, expect, actual, expectVal, actualVal, columnInfo);
            return;
        }
    }

    /**
     * 「本日」に関する項目を検査する。
     * @param expect 期待値のレコード(エラーメッセージの作成に使用)
     * @param actual 実値のレコード(エラーメッセージの作成に使用)
     * @param expectVal 期待値
     * @param actualVal 実値
     * @param columnInfo カラム情報
     */
    private void inspectToday(
            Writable expect,
            Writable actual,
            ValueOption<?> expectVal,
            ValueOption<?> actualVal,
            ColumnInfo columnInfo) {
        Calendar cal = Calendar.getInstance();
        if (actualVal instanceof DateOption) {
            if (actualVal.isNull()) {
                fail(Type.NOT_IN_TEST_DAY, expect, actual, expectVal, actualVal, columnInfo);
                return;
            }
            DateOption dateOption = (DateOption) actualVal;
            Date date = dateOption.get();
            cal.clear();
            cal.set(date.getYear(), date.getMonth() - 1, date.getDay());
        } else if (actualVal instanceof DateTimeOption) {
            if (actualVal.isNull()) {
                fail(Type.NOT_IN_TEST_DAY, expect, actual, expectVal, actualVal, columnInfo);
                return;
            }
            DateTimeOption dateTimeOption = (DateTimeOption) actualVal;
            DateTime dateTime = dateTimeOption.get();
            int y = dateTime.getYear();
            int m = dateTime.getMonth();
            int d = dateTime.getDay();
            int h = dateTime.getHour();
            int min = dateTime.getMinute();
            int s = dateTime.getSecond();
            cal.clear();
            cal.set(y, m - 1, d, h, min, s);
        } else {
            fail(Type.CONDITION_TODAY_ON_INVALID_COLUMN, expect, actual, expectVal, actualVal, columnInfo);
            return;
        }
        Calendar startDay = Calendar.getInstance();
        startDay.setTimeInMillis(getStartTime());
        truncateCalendar(startDay);
        Calendar finishDay = Calendar.getInstance();
        finishDay.setTimeInMillis(getFinishTime());
        truncateCalendar(finishDay);
        finishDay.add(Calendar.DAY_OF_MONTH, 1);
        long time = cal.getTimeInMillis();
        if (startDay.getTimeInMillis() <= time && time < finishDay.getTimeInMillis()) {
            return;
        } else {
            fail(Type.NOT_IN_TEST_DAY, expect, actual, expectVal, actualVal, columnInfo);
            return;
        }
    }

    /**
     * 指定したカレンダーオブジェクトの時分秒を切り捨てる。
     * @param cal 対象のカレンダー
     */
    private void truncateCalendar(Calendar cal) {
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        cal.clear();
        cal.set(y, m, d);
    }
}

