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
package com.asakusafw.runtime.io;

/**
 * TSVパーサーとエミッターが共通して利用する定数。
 */
public final class TsvConstants {

    /**
     * boolean型の{@code true}を表す文字。
     */
    public static final char BOOLEAN_TRUE = '1';

    /**
     * boolean型の{@code false}を表す文字。
     */
    public static final char BOOLEAN_FALSE = '0';

    /**
     * エスケープシーケンスの先頭文字。
     */
    public static final char ESCAPE_CHAR = '\\';

    /**
     * {@code NULL}カラムを表現するエスケープシーケンスの後続文字。
     */
    public static final char ESCAPE_NULL_COLUMN = 'N';

    /**
     * 水平(HT)タブ文字を表現するエスケープシーケンスの後続文字。
     */
    public static final char ESCAPE_HT = '\t';

    /**
     * 改行(LF)タブ文字を表現するエスケープシーケンスの後続文字。
     */
    public static final char ESCAPE_LF = '\n';

    /**
     * セルの区切り文字。
     */
    public static final char CELL_SEPARATOR = '\t';

    /**
     * レコードの区切り文字。
     */
    public static final char RECORD_SEPARATOR = '\n';

    /**
     * 日付フィールドの区切り文字。
     */
    public static final char DATE_FIELD_SEPARATOR = '-';

    /**
     * 時刻フィールドの区切り文字。
     */
    public static final char TIME_FIELD_SEPARATOR = ':';

    /**
     * 日付と時刻間の区切り文字。
     */
    public static final char DATE_TIME_SEPARATOR = ' ';

    /**
     * 年フィールドの文字列長。
     */
    public static final int YEAR_FIELD_LENGTH = 4;

    /**
     * 月フィールドの文字列長。
     */
    public static final int MONTH_FIELD_LENGTH = 2;

    /**
     * 日フィールドの文字列長。
     */
    public static final int DATE_FIELD_LENGTH = 2;

    /**
     * 時フィールドの文字列長。
     */
    public static final int HOUR_FIELD_LENGTH = 2;

    /**
     * 分フィールドの文字列長。
     */
    public static final int MINUTE_FIELD_LENGTH = 2;

    /**
     * 秒フィールドの文字列長。
     */
    public static final int SECOND_FIELD_LENGTH = 2;

    private TsvConstants() {
        return;
    }
}
