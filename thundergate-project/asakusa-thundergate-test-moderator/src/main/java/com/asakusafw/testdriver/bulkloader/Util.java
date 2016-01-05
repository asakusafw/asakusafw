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
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for this package.
 */
final class Util {

    static final Logger LOG = LoggerFactory.getLogger(Util.class);

    static void truncate(Configuration config, String tableName) throws IOException {
        assert config != null;
        assert tableName != null;
        try {
            Connection conn = config.open();
            try {
                Statement statement = conn.createStatement();
                try {
                    statement.execute(MessageFormat.format(
                            "TRUNCATE TABLE {0}",
                            tableName));
                } finally {
                    statement.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            LOG.warn(MessageFormat.format(
                    "テーブル{0}のtruncateに失敗しました",
                    tableName), e);
        }
    }

    static void clearCache(Configuration config, String cacheId) throws IOException {
        assert config != null;
        assert cacheId != null;
        try {
            boolean committed = false;
            Connection conn = config.open();
            try {
                Statement statement = conn.createStatement();
                try {
                    statement.execute(MessageFormat.format(
                            "DELETE FROM __TG_CACHE_INFO WHERE CACHE_ID = ''{0}''",
                            cacheId));
                    statement.execute(MessageFormat.format(
                            "DELETE FROM __TG_CACHE_LOCK WHERE CACHE_ID = ''{0}''",
                            cacheId));
                    if (conn.getAutoCommit() == false) {
                        conn.commit();
                    }
                    committed = true;
                } finally {
                    statement.close();
                }
            } finally {
                if (committed == false && conn.getAutoCommit() == false) {
                    conn.rollback();
                }
                conn.close();
            }
        } catch (SQLException e) {
            LOG.warn(MessageFormat.format(
                    "キャッシュ{0}の削除に失敗しました",
                    cacheId), e);
        }
    }

    static String join(Iterable<String> list) {
        assert list != null;
        Iterator<String> iterator = list.iterator();
        assert iterator.hasNext();
        StringBuilder buf = new StringBuilder();
        buf.append(iterator.next());
        while (iterator.hasNext()) {
            buf.append(", ");
            buf.append(iterator.next());
        }
        return buf.toString();
    }

    private Util() {
        return;
    }
}
