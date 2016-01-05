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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.AbstractImporterPreparator;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.ImporterPreparator;
import com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription;

/**
 * Implementation of {@link ImporterPreparator} for {@link BulkLoadImporterDescription}s.
 * @since 0.2.0
 */
public class BulkLoadImporterPreparator extends AbstractImporterPreparator<BulkLoadImporterDescription> {

    static final Logger LOG = LoggerFactory.getLogger(BulkLoadImporterPreparator.class);

    @Override
    public void truncate(BulkLoadImporterDescription description) throws IOException {
        Configuration conf = Configuration.load(description.getTargetName());
        String tableName = description.getTableName();

        LOG.info("インポート元のテーブル{}の内容を消去します", tableName);
        Util.truncate(conf, tableName);

        String cacheId = description.calculateCacheId();
        if (description.isCacheEnabled() && cacheId != null) {
            LOG.info("キャッシュ{}を消去します: {}",
                    cacheId,
                    description.getClass().getName());
            Util.clearCache(conf, cacheId);
        }
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            BulkLoadImporterDescription description) throws IOException {
        Configuration conf = Configuration.load(description.getTargetName());
        TableInfo<V> info = buildTableInfo(definition, description);
        LOG.info("インポート元の初期値を設定します: テーブル{}", info.getTableName());
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

    private <V> TableInfo<V> buildTableInfo(
            DataModelDefinition<V> definition,
            BulkLoadImporterDescription description) {
        assert definition != null;
        assert description != null;
        return new TableInfo<V>(
                definition,
                description.getTableName(),
                description.getColumnNames(),
                description.isCacheEnabled());
    }
}
