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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.value.ValueOption;
import com.asakusafw.testtools.ColumnInfo;
import com.asakusafw.testtools.ModelComparator;
import com.asakusafw.testtools.RowMatchingCondition;
import com.asakusafw.testtools.TestDataHolder;
import com.asakusafw.testtools.inspect.Cause.Type;

/**
 * Inspectorの抽象クラス。
 * このクラスを継承してInspecorのを実装する場合、
 * モデルオブジェクトの期待値と実値を比較するメソッド
 * {@link #inspect(Writable, Writable)}のみを実装すれば良い。
 */
public abstract class AbstractInspector implements Inspector {

    /**
     * 全カラムの情報。
     */
    private  List<ColumnInfo> columnInfos;

    /**
     * KEYカラムの情報。
     */
    private final List<ColumnInfo> keyColumnInfos = new ArrayList<ColumnInfo>();

    /**
     * テストの開始時刻。
     */
    private long startTime;


    /**
     * テストの終了時刻。
     */
    private long finishTime = 0;

    /**
     * NG原因のリスト。
     */
    private final List<Cause> causes = new ArrayList<Cause>();


    /**
     * 検査を実行するメソッド。
     * 継承クラスで実装してください。
     * @param expectRow 期待する値のレコード
     * @param actualRow 実値のレコード
     */
    protected abstract void inspect(Writable expectRow, Writable actualRow);


    /**
     * 検査が失敗した原因を設定する。
     * @param type 失敗の原因
     * @param expect 期待する値のレコード
     * @param actual 実値のレコード
     */
    protected final void fail(Type type, Writable expect, Writable actual) {
        String tableNameString = "table = " + columnInfos.get(0).getTableName() + ",";
        String keyValueString;
        if (actual != null) {
            keyValueString = getKeyValueString(actual);
        } else {
            keyValueString = getKeyValueString(expect);
        }
        Cause cause = new Cause(type, tableNameString +  keyValueString, expect, actual);
        causes.add(cause);
    }


    /**
     * 検査が失敗した原因を設定する。
     * @param type 失敗の原因
     * @param expect 期待値のレコード
     * @param actual 実値のレコード
     * @param expectVal 期待する値
     * @param actualVal  実値
     * @param columnInfo NG原因となったカラムの情報
     */
    protected final void fail(Type type, Writable expect, Writable actual,
            ValueOption<?> expectVal, ValueOption<?> actualVal,
            ColumnInfo columnInfo) {
        String format = "table = %s, %s,  column name = %s, expected = %s, actual = %s";
        String additionalMessage = String.format(format, columnInfo.getTableName(),
                getKeyValueString(actual), columnInfo.getColumnName(),
                expectVal.toString(), actualVal.toString());
        Cause cause = new Cause(type, additionalMessage, expect, actual,
                expectVal, actualVal, columnInfo);
        causes.add(cause);
    }

    /**
     * 指定のモデルオブジェクトからキー値を表す文字列を取得する。
     * @param model モデルオブジェクト
     * @return キー値を表す文字列
     */
    private String getKeyValueString(Writable model) {
        StringBuilder sb = new StringBuilder();
        sb.append(" Key = (");
        boolean first = true;
        for (ColumnInfo info : getKeyColumnInfos()) {
            if (info.isKey()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(info.getColumnName());
                sb.append(" = ");
                sb.append(getValue(model, info).toString());
            }
        }
        sb.append(")");
        return sb.toString();
    }


    /**
     * 全カラムの情報を取得します。
     * @return 全カラムの情報
     */
    protected final List<ColumnInfo> getColumnInfos() {
        return columnInfos;
    }

    /**
     * 全カラムの情報を設定します。
     * @param columnInfos 全カラムの情報
     */
    @Override
    public void setColumnInfos(List<ColumnInfo> columnInfos) {
        this.columnInfos = columnInfos;
        for (ColumnInfo info : columnInfos) {
            keyColumnInfos.add(info);
        }
    }

    /**
     * KEYカラムの情報を取得します。
     * @return KEYカラムの情報
     */
    protected final List<ColumnInfo> getKeyColumnInfos() {
        return keyColumnInfos;
    }

    /**
     * テストの開始時刻を設定します。
     * @param startTime テストの開始時刻
     */
    @Override
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * テストの終了時刻(実際には検査の開始時刻)を取得します。
     * @return テストの終了時刻(実際には検査の開始時刻)
     */
    public long getFinishTime() {
        return finishTime;
    }

    /**
     * テストの終了時刻を設定します。
     * このメソッドは単体テストでの使用を想定しています。
     * このメッソドを呼び出さなかった場合、またはこのメソッドで0を
     * 設定した場合、テストの終了時刻にinspet()メソッドを呼び出した
     * 時刻が設定されます。
     * @param finishTime テストの終了時刻(実際には検査の開始時刻)
     */
    @Override
    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public final List<Cause> getCauses() {
        return causes;
    }

    /**
     * 指定のモデルオブジェクトの指定のカラムの値を取得する。
     * @param model モデルオブジェクト
     * @param info カラム情報
     * @return カラムの値
     */
    protected final ValueOption<?> getValue(Writable model, ColumnInfo info) {
        ValueOption<?> vo = null;
        try {
            Method method = model.getClass().getMethod(info.getGetterName());
            vo = (ValueOption<?>) method.invoke(model);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return vo;
    }

    @Override
    public boolean isSuccess() {
        return causes.isEmpty();
    }

    /**
     * テストの開始時刻します。
     * @return テストの開始時刻
     */
    protected long getStartTime() {
        return startTime;
    }

    @Override
    public boolean inspect(TestDataHolder dataHolder) {
        // テスト終了時刻が設定されていない場合、テスト終了時刻に現在時刻を指定
        if (finishTime == 0) {
            finishTime = System.currentTimeMillis();
        }

        // 始めにデータホルダが保持するデータをソートする
        dataHolder.sort();

        // 検査しないと指定されたテーブルに対しては常に成功を返す
        if (dataHolder.getRowMatchingCondition() == RowMatchingCondition.NONE) {
            return true;
        }

        // 各テーブルに対し、検査を実施する
        Comparator<Writable> comparator = new ModelComparator<Writable>(columnInfos, dataHolder.getModelClass());

        // 期待データのキーが重複していないかの検査
        List<Writable> expect = dataHolder.getExpect();
        for (int i = 1, n = expect.size(); i < n; i++) {
            if (comparator.compare(expect.get(i - 1), expect.get(i)) == 0) {
                fail(Type.DUPLICATEED_KEY_IN_EXPECT_RECORDS, expect.get(i), null);
            }
        }

        // 実際の出力データのキーが重複してないかの検査
        List<Writable> actual = dataHolder.getActual();
        for (int i = 1, n = actual.size(); i < n; i++) {
            if (comparator.compare(actual.get(i - 1), actual.get(i)) == 0) {
                fail(Type.DUPLICATEED_KEY_IN_ACTUALT_RECORDS, null, actual.get(i));
            }
        }
        if (!isSuccess()) {
            return false; // キーの重複がある場合検査を失敗させ、以降の処理を実行しない
        }

        // 各レコードを比較
        Iterator<Writable> expectIterator = expect.iterator();
        Iterator<Writable> actualIterator = actual.iterator();
        Writable expectRow = null;
        Writable actualRow = null;
        for (;;) {
            // expect, actualのどちらかしか存在しない場合
            if (!expectIterator.hasNext() && expectRow == null) {
                if (actualRow != null) {
                    fail(Type.NO_EXPECT_RECORD, null, actualRow);
                }
                while (actualIterator.hasNext()) {
                    actualRow = actualIterator.next();
                    if (dataHolder.getRowMatchingCondition() == RowMatchingCondition.EXACT) {
                        fail(Type.NO_EXPECT_RECORD, null, actualRow);
                    }
                }
                // 比較終了
                break;
            }
            if (!actualIterator.hasNext() && actualRow == null) {
                if (expectRow != null) {
                    fail(Type.NO_ACTUAL_RECORD, expectRow, null);
                }
                while (expectIterator.hasNext()) {
                    expectRow = expectIterator.next();
                    fail(Type.NO_ACTUAL_RECORD, expectRow, null);
                }
                // 比較終了
                break;
            }

            if (expectRow == null) {
                expectRow = expectIterator.next();
            }
            if (actualRow == null) {
                actualRow = actualIterator.next();
            }
            int result = comparator.compare(expectRow, actualRow);
            if (result < 0) {
                // 期待データに対応する実際の出力データが存在しない
                fail(Type.NO_ACTUAL_RECORD, expectRow, null);
                expectRow = null;
            } else if (result == 0) {
                inspect(expectRow, actualRow);
                expectRow = null;
                actualRow = null;
            } else {
                // 実際のデータに対応する期待データが存在しない
                if (dataHolder.getRowMatchingCondition() == RowMatchingCondition.EXACT) {
                    fail(Type.NO_EXPECT_RECORD,  null, actualRow);
                }
                actualRow = null;
            }
        }
        return true;
    }

    /**
     * テスト結果をクリアする。
     */
    public void clear() {
        finishTime = 0;
        causes.clear();
    }
}
