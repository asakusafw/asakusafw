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

import java.util.HashSet;
import java.util.List;

import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * バルクローダー(インポート)の処理内容を記述するクラスの基底。
 * <p>
 * このクラスを継承するクラスは次のような要件を満たす必要がある。
 * </p>
 * <ul>
 * <li> {@code public}で宣言されている </li>
 * <li> {@code abstract}で宣言されていない </li>
 * <li> 型引数が宣言されていない </li>
 * <li> 明示的なコンストラクターが宣言されていない </li>
 * </ul>
 * @since 0.1.0
 * @version 0.2.3
 */
public abstract class BulkLoadImporterDescription implements ImporterDescription {

    /**
     * インポーターの動作モードを返す。
     * @return インポーターの動作モード
     */
    public abstract Mode getMode();

    /**
     * インポーターの接続先データベースを表す識別子を返す。
     * <p>
     * 通常、これはデータベース名と同じ文字列である。
     * </p>
     * @return インポーターの接続先データベースを表す識別子
     */
    public abstract String getTargetName();

    /**
     * インポーターが利用する抽出条件をSQLの条件式で返す。
     * <p>
     * 条件式の内部には、バッチ引数を表す変数を含められる。
     * 利用する場合には文字列の中に<code>${バッチ引数名}</code>の形式で含めること。
     * なお、その際の引数の名前は、{@code BatchContext}から参照可能な引数と同じ名前である。
     * </p>
     * <p>
     * 返される文字列は、SQL形式の"WHERE"以降の文字列である必要がある。
     * 特に、上記の変数を利用する場合には、文字列がそのまま展開されてSQLに含められるため、
     * クウォートなどの処理を必要に応じて行うこと。
     * </p>
     * @return インポーターが利用する抽出条件、全範囲を対象とする場合は{@code null}
     */
    public String getWhere() {
        return null;
    }

    /**
     * インポート対象のテーブル名を返す。
     * @return インポート対象のテーブル名
     */
    public abstract String getTableName();

    /**
     * インポート対象のカラム列を返す。
     * @return インポート対象のカラム列
     */
    public abstract List<String> getColumnNames();

    /**
     * インポーターの処理時に行われるロックの種類を返す。
     * @return インポーターの処理時に行われるロックの種類
     */
    public abstract LockType getLockType();

    /**
     * インポーターがキャッシュを利用する場合にのみ{@code true}を返す。
     * <p>
     * このメソッドが{@code true}を返す場合であっても、
     * {@link #getDataSize() データサイズ}の指定によってはキャッシュは利用されない。
     * </p>
     * <p>
     * キャッシュを利用する場合、{@link #getModelType() データモデル}は
     * {@link ThunderGateCacheSupport キャッシュをサポートする}もののみを指定できる。
     * また、{@link #getLockType() ロックの種類}に
     * {@link BulkLoadImporterDescription.LockType#ROW 行ロック}や
     * {@link BulkLoadImporterDescription.LockType#ROW_OR_SKIP 行スキップ}を行うものは指定できない。
     * </p>
     * <p>
     * ジョブフロー作成者は、このメソッドをオーバーライドすることで既定の設定を変更できる。
     * </p>
     * @return インポーターがキャッシュを利用する場合にのみ{@code true}
     * @since 0.1.0
     */
    public abstract boolean isCacheEnabled();

    /**
     * Returns cache ID for this import operation.
     * Clients can override this method to use a custom cache ID.
     * @return the cache ID.
     * @see #isCacheEnabled()
     * @since 0.2.3
     */
    public String calculateCacheId() {
        final long prime = 31;
        long hash = 1;
        hash = hash * prime + hash(getTargetName());
        hash = hash * prime + hash(getModelType().getName());
        hash = hash * prime + hash(getTableName());
        hash = hash * prime + hash(new HashSet<String>(getColumnNames()));
        hash = hash * prime + hash(getWhere());
        return String.format("%016x", hash);
    }

    private int hash(Object object) {
        if (object == null) {
            return 0;
        }
        return object.hashCode();
    }

    @Override
    public DataSize getDataSize() {
        return DataSize.UNKNOWN;
    }

    /**
     * バルクローダーのモード。
     */
    public enum Mode {

        /**
         * プライマリーモード。
         * <p>
         * 全ての機能を利用できる。
         * </p>
         */
        PRIMARY,

        /**
         * セカンダリーモード。
         * <p>
         * あらゆる変更を加えられず、またロックも行えない。
         * </p>
         */
        SECONDARY,
    }

    /**
     * インポート時のロックの種類。
     */
    public enum LockType {

        /**
         * インポート対象のテーブル全体をロックし、失敗したらエラーとする。
         * <p>
         * 強い整合性が必要で、テーブル全体または大半の行にロックを行いたい場合にこの設定を利用できる。
         * </p>
         */
        TABLE,

        /**
         * インポート対象の行のみをロックし、失敗したらエラーとする。
         * <p>
         * 強い整合性が必要で、必要な行のみにロックを行いたい場合にこの設定を利用できる。
         * ただし、キャッシュの対象とする場合にはこの設定は選択できない。
         * </p>
         */
        ROW,

        /**
         * インポート対象の行のみをロックし、失敗したものはインポート対象から除外する。
         * <p>
         * 強い整合性が必要で、かつインクリメンタルに処理を行える場合はこの設定を利用できる。
         * ただし、キャッシュの対象とする場合にはこの設定は選択できない。
         * </p>
         */
        ROW_OR_SKIP,

        /**
         * ロックの有無を確認し、ロックされていたらエラーとする (ロックは取得しない)。
         * <p>
         * トランザクショナルリードが必要で、かつレコードの更新を行わない場合はこの設定を利用できる。
         * </p>
         */
        CHECK,

        /**
         * あらゆるロック操作を行わない。
         * <p>
         * トランザクショナルリードが不要で、かつレコードの更新を行わない場合はこの設定を利用できる。
         * </p>
         */
        UNUSED,
    }
}
