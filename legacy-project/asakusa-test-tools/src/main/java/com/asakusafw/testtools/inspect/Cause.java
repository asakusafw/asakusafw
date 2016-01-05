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

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.value.ValueOption;
import com.asakusafw.testtools.ColumnInfo;

/**
 * 検査が失敗した原因を示すクラス。
 */
public class Cause {

    /**
     * 失敗の原因と、原因を示す文字列を定義する列挙型。
     */
    public enum Type {
        /**
         * 文字列型でないカラムに文字列による比較が指定。
         */
        NOT_STRING_COLUMN("文字列型でないカラムに文字列による比較が指定されています。"),
        /**
         * 時刻型でないカラムの比較条件に「現在時刻」を指定。
         */
        CONDITION_NOW_ON_INVALID_COLUMN("時刻型でないカラムの比較条件に「現在時刻」が指定されている。"),
        /**
         * 「時刻型」、「日次型」のいずれでもないカラムの比較条件に「現在日」を指定。
         */
        CONDITION_TODAY_ON_INVALID_COLUMN("「時刻型」、「日時型」のいずれでもないカラムの比較条件に「現在日」が指定されている。"),
        /**
         * 文字列型でないカラムの比較条件に「部分一致」を指定。
         */
        CONDITION_PARTIAL_ON_INVALID_COLUMN("文字列型でないカラムの比較条件に「部分一致」が指定されている。"),
        /**
         * 現在時刻が設定されていない。
         */
        NOT_IN_TESTING_TIME("現在時刻が設定されていません。"),
        /**
         * 在日が設定されていない。
         */
        NOT_IN_TEST_DAY("現在日が設定されていません。"),
        /**
         * NULL値は許されない。
         */
        NULL_NOT_ALLOWD("NULL値は許されない。"),
        /**
         * 実際のデータに対応する期待データのレコードが存在しない。
         */
        NO_EXPECT_RECORD("実際のデータに対応する期待データのレコードが存在しません。"),
        /**
         * 期待データに対応する実際のデータのレコードが存在しない。
         */
        NO_ACTUAL_RECORD("期待データに対応する実際のデータのレコードが存在しません。"),
        /**
         * 出力データの値が期待する値と異なる。
         */
        COLUMN_VALUE_MISSMATCH("出力データの値が期待する値と異なります。"),
        /**
         * 期待データにキー値が同一のレコードが存在。
         */
        DUPLICATEED_KEY_IN_EXPECT_RECORDS("期待データにキー値が同一のレコードが存在します。"),
        /**
         * 実際の出力データにキー値が同一のレコード存在。
         */
        DUPLICATEED_KEY_IN_ACTUALT_RECORDS("実際の出力データにキー値が同一のレコード存在します。");

        /**
         * 失敗の原因を表す文字列。
         */
        private String message;

        /**
         * インスタンスを生成する。
         * @param message 失敗の原因を表す文字列
         */
        private Type(String message) {
            this.message = message;
        }

        /**
         * 失敗の原因を表す文字列を返す。
         * @return 失敗の原因を表す文字列
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * 失敗の原因。
     */
    private Type type;

    /**
     * 失敗の原因を示す文字列。
     */
    private String message;

    /**
     * 検査に失敗した期待値のレコード。
     */
    private Writable expect;

    /**
     * 検査に失敗した実値のレコード。
     */
    private Writable actual;

    /**
     * 検査に失敗したカラムの情報。
     */
    private ColumnInfo columnInfo;

    /**
     * 検査に失敗したカラムの実値。
     */
    private ValueOption<?> actualVal;

    /**
     * 検査に失敗したカラムの期待値。
     */
    private ValueOption<?> expectVal;

    /**
     * 失敗の原因を取得します。
     * @return 失敗の原因
     */
    public Type getType() {
        return type;
    }

    /**
     * 失敗の原因を示す文字列を取得します。
     * @return 失敗の原因を示す文字列
     */
    public String getMessage() {
        return message;
    }

    /**
     * インスタンスを生成する。
     * @param type 失敗の原因
     * @param additionalMessage 検査対象のレコードや、検査した値を示す文字列
     * @param expect 検査に失敗した期待値のレコード
     * @param actual 検査に失敗した実値のレコード
     * @param expectVal 検査に失敗した期待値
     * @param actualVal 検査に失敗した実値
     * @param columnInfo 検査に失敗したカラムの情報
     */
    public Cause(Type type, String additionalMessage, Writable expect,
            Writable actual, ValueOption<?> expectVal,
            ValueOption<?> actualVal, ColumnInfo columnInfo) {
        this.type = type;
        this.message = type.getMessage() + additionalMessage;
        this.expect = expect;
        this.actual = actual;
        this.expectVal = expectVal;
        this.actualVal = actualVal;
        this.columnInfo = columnInfo;
    }

    /**
     * インスタンスを生成する。
     * @param type 失敗の原因
     * @param keyValueString 検査対象のレコードのキーを表す文字列
     * @param expect 検査に失敗した期待値のレコード
     * @param actual 検査に失敗した実値のレコード
     */
    public Cause(Type type, String keyValueString, Writable expect, Writable actual) {
        this.type = type;
        this.message = type.getMessage() + keyValueString;
        this.expect = expect;
        this.actual = actual;
    }

    /**
     * 検査に失敗した期待値のレコードを取得します。
     * @return 検査に失敗した期待値のレコード
     */
    public Writable getExpect() {
        return expect;
    }

    /**
     * 検査に失敗した実値のレコードを取得します。
     * @return 検査に失敗した実値のレコード
     */
    public Writable getActual() {
        return actual;
    }

    /**
     * 検査に失敗したカラムの情報を取得します。
     * @return 検査に失敗したカラムの情報
     */
    public ColumnInfo getColumnInfo() {
        return columnInfo;
    }

    /**
     * 検査に失敗したカラムの実値を取得します。
     * @return 検査に失敗したカラムの実値
     */
    public ValueOption<?> getActualVal() {
        return actualVal;
    }

    /**
     * 検査に失敗したカラムの期待値を取得します。
     * @return 検査に失敗したカラムの期待値
     */
    public ValueOption<?> getExpectVal() {
        return expectVal;
    }
}
