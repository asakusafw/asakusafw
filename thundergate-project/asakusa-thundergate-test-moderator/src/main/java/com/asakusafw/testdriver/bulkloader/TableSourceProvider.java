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
import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceProvider;
import com.asakusafw.testdriver.core.TestContext;

/**
 * Provides {@link DataModelSource} from Database Table.
 * This accepts URI: {@code bulkloader:<target_name>:<table_name>}
 *
 * @since 0.2.2
 */
public class TableSourceProvider implements DataModelSourceProvider {

    static final Logger LOG = LoggerFactory.getLogger(TableSourceProvider.class);

    private static final String SCHEME = "bulkloader";

    @Override
    public <T> DataModelSource open(
            DataModelDefinition<T> definition,
            URI source,
            TestContext context) throws IOException {
        String scheme = source.getScheme();
        if (scheme == null || !scheme.equals(SCHEME)) {
            LOG.debug("Not a Bulkloader URI: {}", source);
            return null;
        }
        LOG.info("DBテーブルをデータソースに利用します: {}", source);

        String[] pathArray = source.toString().split(":");
        String targetName = pathArray[1];
        String tableName = pathArray[2];

        Configuration conf = Configuration.load(targetName);
        Connection conn = null;
        ResultSet res = null;
        try {
            conn = conf.open();
            DatabaseMetaData meta = conn.getMetaData();
            res = meta.getColumns(null, null, tableName, "%");
            List<String> columnList = new ArrayList<String>();
            while (res.next()) {
                columnList.add(res.getString("COLUMN_NAME"));
            }
            TableInfo<T> table = new TableInfo<T>(definition, tableName, columnList);
            return new TableSource<T>(table, conn);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
