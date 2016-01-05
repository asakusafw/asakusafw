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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.sequencefile.SequenceFileModelOutput;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.Date;
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

/**
 * テストデータを保持するクラス。
 */
public class TestDataHolder {

    /**
     * 入力データ。
     */
    private final List<Writable> source;

    /**
     * 期待する出力データ。
     */
    private final List<Writable> expect;

    /**
     * 実際の出力データ。
     */
    private final List<Writable> actual;

    /**
     * Excelシートのテスト条件。
     */
    private final List<ColumnInfo>  columnInfos;

    /**
     * テストデータを格納するモデルクラス。
     */
    private Class<? extends Writable> modelClass;

    /**
     * DBのテーブル名。
     */
    private final String tablename;

    /**
     * テーブル毎のテスト条件。
     */
    private RowMatchingCondition rowMatchingCondition;

    /**
     * インスタンスを生成する。
     * @param source 結果の一覧
     * @param expect 期待した値の一覧
     * @param columnInfos カラムの情報一覧
     * @param modelClass 元のモデルクラス
     * @param rowMatchingCondition 行の同一性比較条件
     */
    public TestDataHolder(
            List<Writable> source,
            List<Writable> expect,
            List<ColumnInfo> columnInfos,
            Class<? extends Writable> modelClass,
            RowMatchingCondition rowMatchingCondition) {
        super();
        this.source = source;
        this.expect = expect;
        this.actual = new ArrayList<Writable>();
        this.columnInfos = columnInfos;
        this.tablename = columnInfos.get(0).getTableName();
        this.modelClass = modelClass;
        this.rowMatchingCondition = rowMatchingCondition;
    }

    /**
     * 入力データを取得します。
     * @return 入力データ
     */
    public List<Writable> getSource() {
        return source;
    }

    /**
     * 期待する出力データを取得します。
     * @return 期待する出力データ
     */
    public List<Writable> getExpect() {
        return expect;
    }

    /**
     * 実際の出力データを取得します。
     * @return 実際の出力データ
     */
    public List<Writable> getActual() {
        return actual;
    }

    /**
     * Excelシートのテスト条件を取得します。
     * @return Excelシートのテスト条件
     */
    public List<ColumnInfo> getColumnInfos() {
        return columnInfos;
    }

    /**
     * 入力データをDBに書き出す。
     * @param conn データベースコネクション
     * @param createTable trueのときテーブルをdrop/createし、falseのときtruncateする
     * @throws SQLException DBへの書き出しに失敗した場合
     */
    public void storeToDatabase(Connection conn, boolean createTable) throws SQLException {
        // テーブルの作成
        if (createTable) {
            DbUtils.dropTable(conn, tablename);
            DbUtils.createTable(conn, columnInfos);
        } else {
            DbUtils.truncateTable(conn, tablename);
        }

        // INSERT用のSQL文を生成
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(tablename);
        for (int i = 0; i < columnInfos.size(); i++) {
            if (i == 0) {
                sb.append("(");
            } else {
                sb.append(", ");
            }
            sb.append(columnInfos.get(i).getColumnName());
        }
        sb.append(") ");

        for (int i = 0; i < columnInfos.size(); i++) {
            if (i == 0) {
                sb.append(" VALUES(?");
            } else {
                sb.append(", ?");
            }
        }
        sb.append(")");
        String sql = sb.toString();

        // データのインサート
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            for (Writable model : source) {
                setModelValue(ps, model);
                ps.executeUpdate();
            }
        } finally {
            DbUtils.closeQuietly(ps);
        }
    }


    /**
     * PreparedStatementにmodelObjectの値を設定する。
     * @param ps PreparedStetement
     * @param model Modelオブジェクト
     * @throws SQLException パラメーターの設定に失敗した場合
     */
    private void setModelValue(PreparedStatement ps, Writable model) throws SQLException {
        int parameterIndex = 0;
        for (ColumnInfo info : columnInfos) {
            parameterIndex++;
            Object vo = null;
            try {
                Method method = modelClass.getMethod(info.getGetterName());
                vo = method.invoke(model);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            switch (info.getDataType()) {
            case LONG:
                LongOption longOption = (LongOption) vo;
                if (longOption.isNull()) {
                    ps.setNull(parameterIndex, Types.BIGINT);
                } else {
                    ps.setLong(parameterIndex, longOption.get());
                }
                break;
            case INT:
                IntOption intOption = (IntOption) vo;
                if (intOption.isNull()) {
                    ps.setNull(parameterIndex, Types.INTEGER);
                } else {
                    ps.setInt(parameterIndex, intOption.get());
                }
                break;
            case SMALL_INT:
                ShortOption shortOption = (ShortOption) vo;
                if (shortOption.isNull()) {
                    ps.setNull(parameterIndex, Types.SMALLINT);
                } else {
                    ps.setInt(parameterIndex, shortOption.get());
                }
                break;
            case TINY_INT:
                ByteOption byteOption = (ByteOption) vo;
                if (byteOption.isNull()) {
                    ps.setNull(parameterIndex, Types.TINYINT);
                } else {
                    ps.setByte(parameterIndex, byteOption.get());
                }
                break;
            case CHAR:
                StringOption charStringOption = (StringOption) vo;
                if (charStringOption.isNull()) {
                    ps.setNull(parameterIndex, Types.CHAR);
                } else {
                    ps.setString(parameterIndex, charStringOption.getAsString());
                }
                break;
            case VARCHAR:
                StringOption vcharStringOption = (StringOption) vo;
                if (vcharStringOption.isNull()) {
                    ps.setNull(parameterIndex, Types.VARCHAR);
                } else {
                    ps.setString(parameterIndex, vcharStringOption.getAsString());
                }
                break;
            case TIMESTAMP:
            case DATETIME:
                DateTimeOption dateTimeOption = (DateTimeOption) vo;
                if (dateTimeOption.isNull()) {
                    ps.setNull(parameterIndex, Types.TIMESTAMP);
                } else {
                    DateTime dateTime = dateTimeOption.get();
                    Calendar cal = Calendar.getInstance();
                    int y = dateTime.getYear();
                    int m = dateTime.getMonth();
                    int d = dateTime.getDay();
                    int h = dateTime.getHour();
                    int min = dateTime.getMinute();
                    int s = dateTime.getSecond();
                    cal.clear();
                    cal.set(y, m - 1, d, h, min, s);
                    Timestamp ts = new Timestamp(cal.getTimeInMillis());
                    ps.setTimestamp(parameterIndex, ts);
                }
                break;
            case DATE:
                DateOption dateOption = (DateOption) vo;
                if (dateOption.isNull()) {
                    ps.setNull(parameterIndex, Types.DATE);
                } else {
                    Date date = dateOption.get();
                    Calendar cal = Calendar.getInstance();
                    int y = date.getYear();
                    int m = date.getMonth();
                    int d = date.getDay();
                    cal.clear();
                    cal.set(y, m - 1, d, 0, 0, 0);
                    java.sql.Date sqlDate = new java.sql.Date(cal.getTimeInMillis());
                    ps.setDate(parameterIndex, sqlDate);
                }
                break;
            case DECIMAL:
                DecimalOption decimalOption = (DecimalOption) vo;
                if (decimalOption.isNull()) {
                    ps.setNull(parameterIndex, Types.DECIMAL);
                } else {
                    ps.setBigDecimal(parameterIndex, decimalOption.get());
                }
                break;
            default:
                throw new RuntimeException("Unsupported data type: " + info.getDataType());
            }
        }
    }

    /**
     * DBテーブルの値を実際の出力データとして取り込む。
     * @param conn DBコネクション
     * @throws SQLException 取り込みに失敗した場合
     */
    public void loadFromDatabase(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (ColumnInfo info : columnInfos) {
            if (info == columnInfos.get(0)) {
                sb.append("SELECT ");
            } else {
                sb.append(", ");
            }
            sb.append(info.getColumnName());
        }
        sb.append(" FROM ");
        sb.append(tablename);
        String sql = sb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            actual.clear();
            while (rs.next()) {
                Writable model;
                try {
                    model = getModelFromResultSet(rs);
                    actual.add(model);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
        }
    }


    /**
     * ResultSetの値をmodelオブジェクトに設定する。
     * @param rs ResultSet
     * @return ResultSetの現在の行を表すモデルオブジェクト
     * @throws InstantiationException モデルオブジェクトの生成に失敗した場合
     * @throws IllegalAccessException モデルオブジェクトの必要なメンバを利用できなかった場合
     * @throws NoSuchMethodException モデルオブジェクトの必要なメソッドを参照できなかった場合
     * @throws InvocationTargetException モデルオブジェクトのメソッド実行に失敗した場合
     * @throws SQLException ResultSetから値を取得できなかった場合
     */
    private Writable getModelFromResultSet(ResultSet rs)
            throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, SQLException {
        Writable model = modelClass.newInstance();
        int columnIndex = 0;
        for (ColumnInfo info : columnInfos) {
            columnIndex++;
            String name = info.getSetterName();
            switch (info.getDataType()) {
            case LONG:
                LongOption longOption = new LongOption();
                long l = rs.getLong(columnIndex);
                if (rs.wasNull()) {
                    longOption.setNull();
                } else {
                    longOption.modify(l);
                }
                modelClass.getMethod(name, LongOption.class).invoke(model, longOption);
                break;
            case INT:
                IntOption intOption = new IntOption();
                int i = rs.getInt(columnIndex);
                if (rs.wasNull()) {
                    intOption.setNull();
                } else {
                    intOption.modify(i);
                }
                modelClass.getMethod(name, IntOption.class).invoke(model, intOption);
                break;
            case SMALL_INT:
                ShortOption shortOption = new ShortOption();
                short sv = rs.getShort(columnIndex);
                if (rs.wasNull()) {
                    shortOption.setNull();
                } else {
                    shortOption.modify(sv);
                }
                modelClass.getMethod(name, ShortOption.class).invoke(model, shortOption);
                break;
            case TINY_INT:
                ByteOption byteOption = new ByteOption();
                Byte b = rs.getByte(columnIndex);
                if (rs.wasNull()) {
                    byteOption.setNull();
                } else {
                    byteOption.modify(b);
                }
                modelClass.getMethod(name, ByteOption.class).invoke(model, byteOption);
                break;
            case CHAR:
            case VARCHAR:
                StringOption stringOption = new StringOption();
                String str = rs.getString(columnIndex);
                if (rs.wasNull()) {
                    stringOption.setNull();
                } else {
                    stringOption.modify(str);
                }
                modelClass.getMethod(name, StringOption.class).invoke(model, stringOption);
                break;
            case TIMESTAMP:
            case DATETIME:
                DateTimeOption dateTimeOption = new DateTimeOption();
                Timestamp timestamp = rs.getTimestamp(columnIndex);
                if (rs.wasNull()) {
                    dateTimeOption.setNull();
                } else {
                    Calendar dateTimeCal = Calendar.getInstance();
                    dateTimeCal.setTime(timestamp);
                    int y = dateTimeCal.get(Calendar.YEAR);
                    int m = dateTimeCal.get(Calendar.MONTH);
                    int d = dateTimeCal.get(Calendar.DAY_OF_MONTH);
                    int h = dateTimeCal.get(Calendar.HOUR_OF_DAY);
                    int min = dateTimeCal.get(Calendar.MINUTE);
                    int s = dateTimeCal.get(Calendar.SECOND);
                    int days = com.asakusafw.runtime.value.DateUtil.getDayFromDate(y, m + 1, d);
                    int secs = com.asakusafw.runtime.value.DateUtil.getSecondFromTime(h, min, s);
                    DateTime dt = new DateTime();
                    dt.setElapsedSeconds((long) days * 86400 + secs);
                    dateTimeOption.modify(dt);
                }
                modelClass.getMethod(name, DateTimeOption.class).invoke(model, dateTimeOption);
                break;
            case DATE:
                DateOption dateOption = new DateOption();
                java.sql.Date sqlDate = rs.getDate(columnIndex);
                if (rs.wasNull()) {
                    dateOption.setNull();
                } else {
                    Calendar dateCal = Calendar.getInstance();
                    dateCal.setTime(sqlDate);
                    int y = dateCal.get(Calendar.YEAR);
                    int m = dateCal.get(Calendar.MONTH);
                    int d = dateCal.get(Calendar.DAY_OF_MONTH);
                    int days = DateUtil.getDayFromDate(y, m + 1, d);
                    dateOption.modify(days);
                }
                modelClass.getMethod(name, DateOption.class).invoke(model, dateOption);
                break;
            case DECIMAL:
                DecimalOption decimalOption = new DecimalOption();
                BigDecimal bigDecimal = rs.getBigDecimal(columnIndex);
                if (rs.wasNull()) {
                    decimalOption.setNull();
                } else {
                    decimalOption.modify(bigDecimal);
                }
                modelClass.getMethod(name, DecimalOption.class).invoke(model, decimalOption);
                break;
            default:
                throw new RuntimeException("Unsupported data type: " + info.getDataType());
            }
        }
        return model;
    }

    /**
     * 出力データをシーケンスファイルに書き出す。
     * @param writer 書き出し先
     * @throws IOException 書き出しに失敗した場合
     * @deprecated Use {@link #store(ModelOutput)} instead
     */
    @Deprecated
    public void store(SequenceFile.Writer writer) throws IOException {
        store(new SequenceFileModelOutput<Writable>(writer));
    }

    /**
     * Store dataset into the specified output.
     * @param output target output
     * @throws IOException if failed to store
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.5
     */
    public void store(ModelOutput<?> output) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        @SuppressWarnings("unchecked")
        ModelOutput<Writable> unsafe = (ModelOutput<Writable>) output;
        for (Writable model : source) {
            unsafe.write(model);
        }
    }

    /**
     * 実データをシーケンスファイルから読み込む。
     * @param reader 読み込み元
     * @throws IOException 読み込みに失敗した場合
     * @deprecated Use {@link #load(ModelInput)} instead
     */
    @Deprecated
    public void load(SequenceFile.Reader reader) throws IOException {
        NullWritable key = NullWritable.get();
        for (;;) {
            Writable model;
            try {
                model = modelClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (reader.next(key, model)) {
                actual.add(model);
            } else {
                break;
            }
        }
    }

    /**
     * Loads dataset from the specified input.
     * @param input source input
     * @throws IOException if failed to load
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.5
     */
    public void load(ModelInput<?> input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null"); //$NON-NLS-1$
        }
        @SuppressWarnings("unchecked")
        ModelInput<Writable> unsafe = (ModelInput<Writable>) input;
        for (;;) {
            Writable model;
            try {
                model = modelClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (unsafe.readTo(model)) {
                actual.add(model);
            } else {
                break;
            }
        }
    }

    /**
     * 期待する出力データ、実際の出力データをソートする。
     */
    public void sort() {
        Comparator<Writable> comparator = new ModelComparator<Writable>(columnInfos, modelClass);
        Collections.sort(expect, comparator);
        Collections.sort(actual, comparator);
    }


    /**
     * テストデータを格納するモデルクラスを取得します。
     * @return テストデータを格納するモデルクラス
     */
    public Class<? extends Writable> getModelClass() {
        return modelClass;
    }


    /**
     * テストデータを格納するモデルクラスを設定します。
     * @param modelClass テストデータを格納するモデルクラス
     */
    public void setModelClass(Class<? extends Writable> modelClass) {
        this.modelClass = modelClass;
    }


    /**
     * DBのテーブル名を取得します。
     * @return DBのテーブル名
     */
    public String getTablename() {
        return tablename;
    }


    /**
     * テーブル毎のテスト条件を取得します。
     * @return テーブル毎のテスト条件
     */
    public RowMatchingCondition getRowMatchingCondition() {
        return rowMatchingCondition;
    }


    /**
     * テーブル毎のテスト条件を設定します。
     * @param rowMatchingCondition テーブル毎のテスト条件
     */
    public void setRowMatchingCondition(RowMatchingCondition rowMatchingCondition) {
        this.rowMatchingCondition = rowMatchingCondition;
    }
}