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
package com.asakusafw.bulkloader.cache;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Cache information in local.
 * @since 0.2.3
 */
public class LocalCacheInfo {

    private final String id;

    private final Calendar localTimestamp;

    private final Calendar remoteTimestamp;

    private final String tableName;

    private final String path;

    /**
     * Creates a new instance.
     * @param id target cache ID
     * @param localTimestamp attempted to build last modified timestamp (nullable)
     * @param remoteTimestamp actually built last modified timestamp (nullable)
     * @param tableName target table name
     * @param path target path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public LocalCacheInfo(
            String id, Calendar localTimestamp, Calendar remoteTimestamp, String tableName, String path) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        this.id = id;
        this.localTimestamp = copy(localTimestamp);
        this.remoteTimestamp = copy(remoteTimestamp);
        this.tableName = tableName;
        this.path = path;
    }

    /**
     * Returns the cache ID.
     * @return the cache ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the path to the cache directory.
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the target table name.
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Returns the cache last modified timestamp which have been attempted to build.
     * @return the local timestamp, {@code null} if not defined
     */
    public Calendar getLocalTimestamp() {
        return copy(localTimestamp);
    }

    /**
     * Returns the cache last modified timestamp which have been actually built.
     * @return the remote timestamp, {@code null} if not defined
     */
    public Calendar getRemoteTimestamp() {
        return copy(remoteTimestamp);
    }

    private Calendar copy(Calendar timestamp) {
        if (timestamp == null) {
            return null;
        }
        Calendar copy = (Calendar) timestamp.clone();
        copy.set(Calendar.MILLISECOND, 0);
        return copy;
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder builder = new StringBuilder();
        builder.append("LocalCacheInfo [id=");
        builder.append(id);
        builder.append(", localTimestamp=");
        builder.append(localTimestamp == null ? null : formatter.format(localTimestamp.getTime()));
        builder.append(", remoteTimestamp=");
        builder.append(remoteTimestamp == null ? null : formatter.format(remoteTimestamp.getTime()));
        builder.append(", tableName=");
        builder.append(tableName);
        builder.append(", path=");
        builder.append(path);
        builder.append("]");
        return builder.toString();
    }
}
