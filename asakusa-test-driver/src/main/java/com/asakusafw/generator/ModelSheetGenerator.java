/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.generator;

import java.util.List;

import com.asakusafw.modelgen.model.ModelRepository;
import com.asakusafw.modelgen.model.TableModelDescription;

/**
 * 各種自動生成ツールをキックするランチャー。
 */
public final class ModelSheetGenerator {

    /**
     * モデルジェネレータとテストデータ定義シート生成を実行し、HadoopBulkLoaderに必要な付属テーブルを生成します。
     * <p>
     * モデルジェネレータによってフィルタリングされたテーブルのみを
     * テストデータ定義シート、及びHadoopBulkLoaderに必要な付属テーブルの生成対象とします。
     * </p>
     * @param args 無し
     * @throws Exception 操作に失敗した場合
     */
    public static void main(String[] args) throws Exception {

        com.asakusafw.modelgen.Configuration conf =
            com.asakusafw.modelgen.Main.loadConfigurationFromEnvironment();

        ModelRepository repository =
            new com.asakusafw.modelgen.Main(conf).call();

        List<TableModelDescription> tables = repository.allTables();
        String[] tablesArray = new String[tables.size()];

        int i = 0;
        for (TableModelDescription tableModelDescription : tables) {
            tablesArray[i] = tableModelDescription.getReference().getSimpleName();
            i++;
        }

        com.asakusafw.testtools.templategen.Main.main(tablesArray);
        HadoopBulkLoaderDDLGenerator.main(tablesArray);
    }

    private ModelSheetGenerator() {
        return;
    }
}
