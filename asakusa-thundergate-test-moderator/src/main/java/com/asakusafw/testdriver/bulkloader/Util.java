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
package com.asakusafw.testdriver.bulkloader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * Utility methods for this package.
 */
final class Util {

    static void truncate(Configuration config, String tableName) throws IOException {
        assert config != null;
        assert tableName != null;
        try {
            Connection conn = config.open();
            try {
                conn.createStatement().execute(MessageFormat.format(
                        "TRUNCATE TABLE {0}",
                        tableName));
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            throw new IOException(MessageFormat.format(
                    "テーブル{0}のtruncateに失敗しました",
                    tableName));
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
