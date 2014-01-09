/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package ${package}.operator;

import java.util.List;

import ${package}.modelgen.dmdl.model.CategorySummary;
import ${package}.modelgen.dmdl.model.ErrorRecord;
import ${package}.modelgen.dmdl.model.ItemInfo;
import ${package}.modelgen.dmdl.model.JoinedSalesInfo;
import ${package}.modelgen.dmdl.model.SalesDetail;
import ${package}.modelgen.dmdl.model.StoreInfo;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.operator.MasterCheck;
import com.asakusafw.vocabulary.operator.MasterJoin;
import com.asakusafw.vocabulary.operator.MasterSelection;
import com.asakusafw.vocabulary.operator.Summarize;
import com.asakusafw.vocabulary.operator.Update;

/**
 * カテゴリ別合計を計算するフローの演算子セット。
 */
public abstract class CategorySummaryOperator {

    /**
     * 売上明細の店舗コードに対応する店舗マスタが存在するかチェックする。
     * @param info 店舗マスタ
     * @param sales 売上明細
     * @return 存在すれば{@code true}
     */
    @MasterCheck
    public abstract boolean checkStore(
            @Key(group = "store_code") StoreInfo info,
            @Key(group = "store_code") SalesDetail sales);

    /**
     * 商品マスタと売上明細を結合する。
     * 結合時にはマスタの適用期間がチェックされる。
     * @param info 商品マスタ
     * @param sales 売上明細
     * @return 結合結果
     * @see #selectAvailableItem(List, SalesDetail)
     */
    @MasterJoin(selection = "selectAvailableItem")
    public abstract JoinedSalesInfo joinItemInfo(ItemInfo info, SalesDetail sales);

    /**
     * {@link #selectAvailableItem(List, SalesDetail)}で利用するバッファ。
     */
    private final Date dateBuffer = new Date();

    /**
     * 商品マスタの一覧から売上明細に適用可能なものを探す。
     * @param candidates 商品マスタの一覧
     * @param sales 売上明細
     * @return 適用可能な商品マスタ、存在しない場合には{@code null}
     */
    @MasterSelection
    public ItemInfo selectAvailableItem(List<ItemInfo> candidates, SalesDetail sales) {
        DateTime dateTime = sales.getSalesDateTime();
        dateBuffer.setElapsedDays(DateUtil.getDayFromDate(
                dateTime.getYear(), dateTime.getMonth(), dateTime.getDay()));
        for (ItemInfo item : candidates) {
            if (item.getBeginDate().compareTo(dateBuffer) <= 0
                    && dateBuffer.compareTo(item.getEndDate()) <= 0) {
                return item;
            }
        }
        return null;
    }

    /**
     * 売上情報をカテゴリ別に集計する。
     * @param info 売上情報
     * @return 集計結果
     */
    @Summarize
    public abstract CategorySummary summarizeByCategory(JoinedSalesInfo info);

    /**
     * エラーレコードにエラーメッセージを設定する。
     * @param record エラーレコード
     * @param message エラーメッセージ
     */
    @Update
    public void setErrorMessage(ErrorRecord record, String message) {
        record.setMessageAsString(message);
    }
}
