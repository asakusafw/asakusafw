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
package com.asakusafw.testdriver.bulkloader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.AbstractExporterRetriever;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.ExporterRetriever;
import com.asakusafw.vocabulary.bulkloader.BulkLoadExporterDescription;
import com.asakusafw.vocabulary.bulkloader.BulkLoadExporterDescription.DuplicateRecordCheck;

/**
 * Implementation of {@link ExporterRetriever} for {@link BulkLoadExporterDescription}s.
 * @since 0.2.0
 */
public class BulkLoadExporterRetriever extends AbstractExporterRetriever<BulkLoadExporterDescription> {

    static final Logger LOG = LoggerFactory.getLogger(BulkLoadExporterRetriever.class);

    @Override
    public void truncate(BulkLoadExporterDescription description) throws IOException {
        Configuration conf = Configuration.load(description.getTargetName());
        truncate(conf, description.getTableName());
        if (description.getDuplicateRecordCheck() != null) {
            truncate(conf, description.getDuplicateRecordCheck().getTableName());
        }
    }

    private void truncate(Configuration conf, String tableName) throws IOException {
        assert conf != null;
        assert tableName != null;
        LOG.info("エクスポート先のテーブル{}の内容を消去します", tableName);
        Util.truncate(conf, tableName);
    }

    @Override
    public <V> ModelOutput<V> createOutput(DataModelDefinition<V> definition,
            BulkLoadExporterDescription description) throws IOException {
        Configuration conf = Configuration.load(description.getTargetName());
        TableInfo<V> info = buildTableInfo(definition, description);
        LOG.info("エクスポート先の初期値を設定します: テーブル{}", info.getTableName());
        LOG.debug("Opening output: {}", info);
        Connection conn = conf.open();
        boolean green = false;
        try {
            ModelOutput<V> output = new TableOutput<V>(info, conn);
            green = true;
            return output;
        } finally {
            if (green == false) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOG.warn("error occurred while closing database connection", e);
                }
            }
        }
    }

    @Override
    public <V> DataModelSource createSource(DataModelDefinition<V> definition,
            BulkLoadExporterDescription description) throws IOException {
        Configuration conf = Configuration.load(description.getTargetName());
        TableInfo<V> info = buildTableInfo(definition, description);
        LOG.info("エクスポート結果を取得します: テーブル{}", info.getTableName());
        LOG.debug("Opening results: {}", info);
        Connection conn = conf.open();
        boolean green = false;
        try {
            DataModelSource source = new TableSource<V>(info, conn);
            green = true;
            return source;
        } finally {
            if (green == false) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOG.warn("error occurred while closing database connection", e);
                }
            }
        }
    }

    private <V> TableInfo<V> buildTableInfo(
            DataModelDefinition<V> definition,
            BulkLoadExporterDescription description) throws IOException {
        assert definition != null;
        assert description != null;
        if (isNormalTarget(definition, description)) {
            return new TableInfo<V>(
                    definition,
                    description.getTableName(),
                    description.getTargetColumnNames());
        } else {
            DuplicateRecordCheck dup = description.getDuplicateRecordCheck();
            List<String> columns = dup.getColumnNames();
            if (columns.contains(dup.getErrorCodeColumnName()) == false) {
                // restore error code column name
                columns = new ArrayList<String>(columns);
                columns.add(dup.getErrorCodeColumnName());
            }
            return new TableInfo<V>(
                    definition,
                    dup.getTableName(),
                    columns);
        }
    }

    private boolean isNormalTarget(DataModelDefinition<?> definition,
            BulkLoadExporterDescription description) throws IOException {
        assert definition != null;
        assert description != null;
        LOG.debug("Detecting export target");
        Class<?> modelClass = definition.getModelClass();
        DuplicateRecordCheck dupcheck = description.getDuplicateRecordCheck();
        if (dupcheck != null && modelClass == dupcheck.getTableModelClass()) {
            return false;
        } else if (modelClass == description.getTableModelClass()) {
            return true;
        } else {
            throw new IOException(MessageFormat.format(
                    "エクスポート記述{0}はテーブルモデル{1}を対象としません",
                    description.getClass().getName(),
                    modelClass.getName()));
        }
    }
}
