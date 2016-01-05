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
package com.asakusafw.bulkloader.exporter;

import java.io.File;
import java.util.List;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.log.Log;


/**
 * Export対象ファイルとディレクトリを削除するクラス。
 * @author yuta.shirai
 */
public class ExportFileDelete {

    static final Log LOG = new Log(ExportFileDelete.class);

    /**
     * Exportファイル及びディレクトリを削除する。
     * @param bean パラメータを保持するBean
     */
    public void deleteFile(ExporterBean bean) {
        // Export対象ファイルを削除
        List<String> list = bean.getExportTargetTableList();
        for (String tableName : list) {
            ExportTargetTableBean targetTable = bean.getExportTargetTable(tableName);
            List<File> files = targetTable.getExportFiles();
            for (File file : files) {
                // ファイルが存在する場合は削除する
                if (file != null && file.exists()) {
                    if (!file.delete()) {
                        LOG.warn("TG-EXPORTER-05001", file.getPath());
                    }
                }
            }
        }
    }
}
