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

import ${package}.modelgen.table.model.CategorySummary;
import com.asakusafw.vocabulary.bulkloader.DbExporterDescription;

/**
 * カテゴリ別集計結果をThunderGate/Exporterでエクスポートする。
 * エクスポート対象テーブルは {@code CATEGORY_SUMMARY}。
 */
public class CategorySummaryToJdbc extends DbExporterDescription {

    @Override
    public String getTargetName() {
        return "asakusa";
    }

    @Override
    public Class<?> getModelType() {
        return CategorySummary.class;
    }

}
