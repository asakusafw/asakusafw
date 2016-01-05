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

import java.util.List;

import com.asakusafw.testtools.ColumnInfo;
import com.asakusafw.testtools.TestDataHolder;


/**
 * レコードの検査を行うためのインターフェース。
 * @author shinichi.umegane
 */
public interface Inspector {

    /**
     * NG原因を返す。
     * @return 原因の一覧
     */
    List<Cause> getCauses();

    /**
     * 検査結果を取得します。
     * @return 検査結果
     */
    boolean isSuccess();

    /**
     * 指定のテストデータホルダを検査する。
     * @param dataHolder 対象のデータホルダ
     * @return 検査に成功した場合
     */
    boolean inspect(TestDataHolder dataHolder);

    /**
     * 検査に利用するカラム情報の一覧を設定する。
     * @param columnInfos カラム情報の一覧
     */
    void setColumnInfos(List<ColumnInfo> columnInfos);

    /**
     * テストの開始時刻を設定する。
     * @param startTime テストの開始時刻
     */
    void setStartTime(long startTime);

    /**
     * テストの終了時刻を設定する。
     * @param finishTime テストの終了時刻
     */
    void setFinishTime(long finishTime);
}