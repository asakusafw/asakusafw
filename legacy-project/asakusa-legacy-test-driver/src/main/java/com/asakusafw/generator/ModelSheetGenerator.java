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
package com.asakusafw.generator;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 各種自動生成ツールをキックするランチャー。
 */
@SuppressWarnings("deprecation")
public final class ModelSheetGenerator {

    static final Logger LOG = LoggerFactory.getLogger(ModelSheetGenerator.class);

    /**
     * 各種自動生成ツールを実行します。
     * <p>
     * 以下の処理を行います。
     * ・モデル定義用SQLからDMDLを生成
     * ・モデル定義から該当するテーブル用のテストデータ定義シートを生成
     * ・ThnderGateのロック管理テーブル用DDLを生成
     * </p>
     *
     * @param args asakusa-thundergate-dmdlのReadmeを参照
     * @throws Exception
     *             操作に失敗した場合
     */
    public static void main(String[] args) throws Exception {

        com.asakusafw.dmdl.thundergate.Configuration dmdlTgConf =
            com.asakusafw.dmdl.thundergate.Main.loadConfigurationFromArguments(args);

        com.asakusafw.dmdl.thundergate.model.ModelRepository repository =
            new com.asakusafw.dmdl.thundergate.GenerateTask(dmdlTgConf).call();

        List<com.asakusafw.dmdl.thundergate.model.TableModelDescription> tables =
            repository.allTables();
        String[] tablesArray = new String[tables.size()];

        int i = 0;
        for (com.asakusafw.dmdl.thundergate.model.TableModelDescription tableModelDescription : tables) {
            tablesArray[i] = tableModelDescription.getReference().getSimpleName();
            i++;
        }

        System.setProperty("ASAKUSA_TESTTOOLS_CONF", System.getProperty("ASAKUSA_MODELGEN_JDBC"));
        if ("true".equals(System.getProperty("ASAKUSA_V01_TEMPLATEGEN_RUN"))) {
            String outputDir = System.getProperty("ASAKUSA_TESTDATASHEET_OUTPUT") + "_v01_format";
            File dir = new File(outputDir);
            if (dir.isDirectory() == false && dir.mkdirs() == false) {
                if (dir.isDirectory()) {
                    LOG.warn("Failed to delete output directory: {}", dir);
                }
            }
            System.setProperty("ASAKUSA_TEMPLATEGEN_OUTPUT_DIR", outputDir);
            com.asakusafw.testtools.templategen.Main.main(tablesArray);
        }

        HadoopBulkLoaderDDLGenerator.main(tablesArray);
    }

    private ModelSheetGenerator() {
        return;
    }
}
