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
package ${package}.jobflow;

import org.junit.Test;

import ${package}.jobflow.CategorySummaryJob;
import ${package}.modelgen.table.model.CategorySummary;
import ${package}.modelgen.table.model.ErrorRecord;
import ${package}.modelgen.table.model.ItemInfo;
import ${package}.modelgen.table.model.SalesDetail;
import ${package}.modelgen.table.model.StoreInfo;
import ${package}.util.CountVerifier;
import com.asakusafw.testdriver.JobFlowTester;


/**
 * {@link CategorySummaryJob}のテスト。
 */
public class CategorySummaryJobTest {

    /**
     * 最小限のテスト。
     */
    @Test
    public void simple() {
        run("simple.xls", 0);
    }

    /**
     * カテゴリ別の集計を行うテスト。
     */
    @Test
    public void summarize() {
        run("summarize.xls", 0);
    }

    /**
     * マスタ有効期限のテスト。
     */
    @Test
    public void available_date() {
        run("available_range.xls", 1);
    }

    /**
     * 正しくない店舗情報を含む。
     */
    @Test
    public void invalid_store() {
        run("invalid_store.xls", 1);
    }

    private void run(String dataSet, long errors) {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setBatchArg("date", "testing");

        tester.input("storeInfo", StoreInfo.class)
            .prepare("masters.xls#store_info");
        tester.input("itemInfo", ItemInfo.class)
            .prepare("masters.xls#item_info");

        tester.input("salesDetail", SalesDetail.class)
            .prepare(dataSet + "#sales_detail");
        tester.output("categorySummary", CategorySummary.class)
            .verify(dataSet + "#result", dataSet + "#result_rule");
        tester.output("errorRecord", ErrorRecord.class)
            .verify(CountVerifier.factory(errors));

        tester.runTest(CategorySummaryJob.class);
    }
}
