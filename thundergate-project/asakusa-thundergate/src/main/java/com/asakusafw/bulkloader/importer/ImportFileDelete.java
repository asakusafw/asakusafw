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
package com.asakusafw.bulkloader.importer;

import java.io.File;
import java.util.List;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.log.Log;


/**
 * Import対象ファイルとディレクトリを削除するクラス。
 * @author yuta.shirai
 */
public class ImportFileDelete {

    static final Log LOG = new Log(ImportFileDelete.class);

    /**
     * Importファイル及びディレクトリを削除する。
     * @param bean パラメータを保持するBean
     */
    public void deleteFile(ImportBean bean) {
        // Import対象ファイルを削除
        List<String> list = bean.getImportTargetTableList();
        for (String tableName : list) {
            ImportTargetTableBean targetTable = bean.getTargetTable(tableName);
            File file = targetTable.getImportFile();
            // ファイルが存在する場合は削除する。
            if (file != null && file.exists()) {
                if (!file.delete()) {
                    LOG.warn("TG-IMPORTER-05001", file.getPath());
                }
            }
        }
    }
}
