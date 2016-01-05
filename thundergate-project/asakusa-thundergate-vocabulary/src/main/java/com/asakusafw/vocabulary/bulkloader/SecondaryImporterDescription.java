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

import java.util.List;


/**
 * DBからのインポーターの処理内容を記述するクラスの基底。
 * <p>
 * サブクラスでは以下のメソッドをオーバーライドして、インポーターの処理方法を記述する。
 * </p>
 * <ul>
 * <li> {@link #getTargetName()} 対象のデータベース </li>
 * <li> {@link #getModelType()} 対象のモデルクラス </li>
 * <li> {@link #getWhere()} インポートする対象の検索条件 (デフォルトは「すべて」) </li>
 * <li> {@link #isCacheEnabled()} キャッシュ利用の有無 (デフォルトは「利用」) </li>
 * <li> {@link #getDataSize()} おおよそのデータサイズ (デフォルトは「不明」) </li>
 * </ul>
 * <p>
 * <p>
 * このクラスを継承するクラスは次のような要件を満たす必要がある。
 * </p>
 * <ul>
 * <li> {@code public}で宣言されている </li>
 * <li> {@code abstract}で宣言されていない </li>
 * <li> 型引数が宣言されていない </li>
 * <li> 明示的なコンストラクターが宣言されていない </li>
 * </ul>
 */
public abstract class SecondaryImporterDescription extends BulkLoadImporterDescription {

    @Override
    public final Mode getMode() {
        return Mode.SECONDARY;
    }

    @Override
    public final LockType getLockType() {
        return LockType.UNUSED;
    }

    /**
     * {@inheritDoc}
     * <p>
     * この実装では、テーブル名をモデルクラスの注釈から自動的に抽出する。
     * 注釈が指定されていない場合は例外がスローされる。
     * </p>
     */
    @Override
    public String getTableName() {
        return AttributeHelper.getTableName(getModelType());
    }

    /**
     * {@inheritDoc}
     * <p>
     * この実装では、カラム名をモデルクラスの注釈から自動的に抽出する。
     * 注釈が指定されていない場合は例外がスローされる。
     * </p>
     */
    @Override
    public List<String> getColumnNames() {
        return AttributeHelper.getColumnNames(getModelType());
    }

    /**
     * {@inheritDoc}
     * <p>
     * この実装では常に{@code false}を返す。
     * このメソッドをオーバーライドすることで、キャッシュの動作を変更できる。
     * </p>
     */
    @Override
    public boolean isCacheEnabled() {
        return false;
    }
}
