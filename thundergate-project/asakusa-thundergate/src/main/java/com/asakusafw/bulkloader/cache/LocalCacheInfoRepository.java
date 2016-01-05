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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;

/**
 * Repositories in {@link LocalCacheInfo}.
 * This class requires the following two tables.
 * <ul>
 * <li> __TG_CACHE_INFO
 *   <ul>
 *   <li> CACHE_ID [String] PRIMARY KEY</li>
 *   <li> CACHE_TIMESTAMP [TIMESTAMP] NULL</li>
 *   <li> BUILT_TIMESTAMP [TIMESTAMP] NOT NULL</li>
 *   <li> TABLE_NAME [STRING] NOT NULL</li>
 *   <li> REMOTE_PATH [STRING] NOT NULL</li>
 *   <li> ACTIVE [BOOLEAN] NOT NULL</li>
 *   </ul>
 * </li>
 * <li> __TG_CACHE_LOCK
 *   <ul>
 *   <li> CACHE_ID [String] PRIMARY KEY</li>
 *   <li> EXECUTION_ID [STRING] NOT NULL</li>
 *   <li> ACQUIRED [TIMESTAMP] NOT NULL</li>
 *   </ul>
 * </li>
 * </ul>
 * @since 0.2.3
 */
public class LocalCacheInfoRepository {

    static final Log LOG = new Log(LocalCacheInfoRepository.class);

    private final Connection connection;

    /**
     * Creates a new instance.
     * @param connection the JDB connection
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public LocalCacheInfoRepository(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null"); //$NON-NLS-1$
        }
        this.connection = connection;
    }

    /**
     * Obtains a local cache information.
     * @param cacheId target cache ID
     * @return the related cache information, or {@code null} if not found
     * @throws BulkLoaderSystemException if failed to obtain the cache information by storage exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public LocalCacheInfo getCacheInfo(String cacheId) throws BulkLoaderSystemException {
        if (cacheId == null) {
            throw new IllegalArgumentException("cacheId must not be null"); //$NON-NLS-1$
        }
        final String sql = "SELECT CACHE_ID, CACHE_TIMESTAMP, BUILT_TIMESTAMP, TABLE_NAME, REMOTE_PATH "
            + "FROM __TG_CACHE_INFO "
            + "WHERE CACHE_ID = ? AND ACTIVE = TRUE";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            LOG.debugMessage("getting cache info: {0}", cacheId);
            statement = connection.prepareStatement(sql);
            statement.setString(1, cacheId);
            resultSet = statement.executeQuery();
            if (resultSet.next() == false) {
                LOG.debugMessage("cache info not found: {0}", cacheId);
                return null;
            }
            LocalCacheInfo result = toCacheInfoObject(resultSet);
            assert resultSet.next() == false;
            LOG.debugMessage("got cache info: {0}", cacheId);
            return result;
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    getClass(),
                    sql,
                    cacheId);
        } finally {
            DBConnection.closeRs(resultSet);
            DBConnection.closePs(statement);
        }
    }

    /**
     * Puts cache information.
     * Note that {@link LocalCacheInfo#getLocalTimestamp()} will be ignored, and used current clock.
     * @param current the current information
     * @return the last local timestamp
     * @throws BulkLoaderSystemException if failed to update the cache information by storage exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Calendar putCacheInfo(LocalCacheInfo current) throws BulkLoaderSystemException {
        if (current == null) {
            throw new IllegalArgumentException("old must not be null"); //$NON-NLS-1$
        }
        final String sql = "REPLACE "
            + "INTO __TG_CACHE_INFO (CACHE_ID, CACHE_TIMESTAMP, BUILT_TIMESTAMP, TABLE_NAME, REMOTE_PATH, ACTIVE) "
            + "VALUES (?, ?, ?, ?, ?, TRUE)";
        boolean succeed = false;
        PreparedStatement statement = null;
        Calendar last = null;
        try {
            LOG.debugMessage("putting cache info: {0}", current);
            last = getLastUpdated(current.getTableName());
            if (last == null) {
                throw new BulkLoaderSystemException(getClass(), "TG-COMMON-11001", current);
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, current.getId());
            statement.setTimestamp(2, toTimestamp(last));
            statement.setTimestamp(3, toTimestamp(current.getRemoteTimestamp()));
            statement.setString(4, current.getTableName());
            statement.setString(5, current.getPath());
            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new BulkLoaderSystemException(getClass(), "TG-COMMON-11002", current);
            }
            DBConnection.commit(connection);
            succeed = true;
            LOG.debugMessage("put cache info: {0}", toTimestamp(last));
            return last;
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    getClass(),
                    sql,
                    current.getId(),
                    toTimestamp(last),
                    toTimestamp(current.getRemoteTimestamp()),
                    current.getTableName(),
                    current.getPath());
        } finally {
            DBConnection.closePs(statement);
            if (succeed == false) {
                DBConnection.rollback(connection);
            }
        }
    }

    private Calendar getLastUpdated(String tableName) throws SQLException {
        assert connection != null;
        assert tableName != null;
        Statement statement = connection.createStatement();
        ResultSet resultSet = null;
        try {
            LOG.debugMessage("calculating the last modified time for table: {0}", tableName);
            statement.execute(MessageFormat.format("LOCK TABLES {0} READ", tableName));
            resultSet = statement.executeQuery("SELECT NOW()");
            if (resultSet.next() == false) {
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = resultSet.getTimestamp(1, calendar);
            calendar.setTime(timestamp);
            resultSet.close();
            statement.execute("UNLOCK TABLES");
            LOG.debugMessage("calculated the last modified time for table: {0} = {1}", tableName, timestamp);
            return calendar;
        } finally {
            DBConnection.closeRs(resultSet);
            DBConnection.closeStmt(statement);
        }
    }

    /**
     * Deletes cache information for the specified cache ID.
     * The deleted information can be checked by {@link #listDeletedCacheInfo()}.
     * @param cacheId target cache ID
     * @return {@code true} if actually deleted, otherwise {@code false}
     * @throws BulkLoaderSystemException if failed to delete the cache information by storage exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean deleteCacheInfo(String cacheId) throws BulkLoaderSystemException {
        if (cacheId == null) {
            throw new IllegalArgumentException("cacheId must not be null"); //$NON-NLS-1$
        }
        final String sql = "UPDATE __TG_CACHE_INFO "
            + "SET ACTIVE = FALSE "
            + "WHERE CACHE_ID = ? AND ACTIVE = TRUE";
        boolean succeed = false;
        PreparedStatement statement = null;
        try {
            LOG.debugMessage("deleting cache info: {0}", cacheId);
            statement = connection.prepareStatement(sql);
            statement.setString(1, cacheId);
            int rows = statement.executeUpdate();
            DBConnection.commit(connection);
            succeed = true;
            LOG.debugMessage("deleted cache info: {0}, count={1}", cacheId, rows);
            return rows > 0;
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    getClass(),
                    sql,
                    cacheId);
        } finally {
            DBConnection.closePs(statement);
            if (succeed == false) {
                DBConnection.rollback(connection);
            }
        }
    }

    /**
     * Deletes cache information for the specified table name.
     * The deleted information can be checked by {@link #listDeletedCacheInfo()}.
     * @param tableName target table name
     * @return number of deleted entries of cache information
     * @throws BulkLoaderSystemException if failed to delete the cache information by storage exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public int deleteTableCacheInfo(String tableName) throws BulkLoaderSystemException {
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null"); //$NON-NLS-1$
        }
        final String sql = "UPDATE __TG_CACHE_INFO "
            + "SET ACTIVE = FALSE "
            + "WHERE TABLE_NAME = ? AND ACTIVE = TRUE";
        boolean succeed = false;
        PreparedStatement statement = null;
        try {
            LOG.debugMessage("deleting cache info for table: {0}", tableName);
            statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            int rows = statement.executeUpdate();
            DBConnection.commit(connection);
            succeed = true;
            LOG.debugMessage("deleted cache info for table: {0}, count={1}", tableName, rows);
            return rows;
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    getClass(),
                    sql,
                    tableName);
        } finally {
            DBConnection.closePs(statement);
            if (succeed == false) {
                DBConnection.rollback(connection);
            }
        }
    }

    /**
     * Deletes all cache information in this system.
     * The deleted information can be checked by {@link #listDeletedCacheInfo()}.
     * @throws BulkLoaderSystemException if failed to delete the cache information by storage exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void deleteAllCacheInfo() throws BulkLoaderSystemException {
        final String sql = "UPDATE __TG_CACHE_INFO "
            + "SET ACTIVE = FALSE "
            + "WHERE ACTIVE = TRUE";
        boolean succeed = false;
        PreparedStatement statement = null;
        try {
            LOG.debugMessage("deleting all cache info");
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            DBConnection.commit(connection);
            succeed = true;
            LOG.debugMessage("deleted all cache info");
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    getClass(),
                    sql);
        } finally {
            DBConnection.closePs(statement);
            if (succeed == false) {
                DBConnection.rollback(connection);
            }
        }
    }

    /**
     * Obtains a list of deleted cache information.
     * @return a list of found caches
     * @throws BulkLoaderSystemException if failed to obtain the cache information by storage exception
     */
    public List<LocalCacheInfo> listDeletedCacheInfo() throws BulkLoaderSystemException {
        final String sql = "SELECT CACHE_ID, CACHE_TIMESTAMP, BUILT_TIMESTAMP, TABLE_NAME, REMOTE_PATH "
            + "FROM __TG_CACHE_INFO "
            + "WHERE ACTIVE = FALSE";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            LOG.debugMessage("collecting deleted cache info");
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            List<LocalCacheInfo> results = new ArrayList<LocalCacheInfo>();
            while (resultSet.next()) {
                LocalCacheInfo found = toCacheInfoObject(resultSet);
                LOG.debugMessage("found deleted cache info: {0}", found.getId());
                results.add(found);
            }
            LOG.debugMessage("found deleted cache info: count={0}", results.size());
            return results;
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    getClass(),
                    sql);
        } finally {
            DBConnection.closeRs(resultSet);
            DBConnection.closePs(statement);
        }
    }

    /**
     * Completely deletes cache information for the specified cache ID.
     * @param cacheId target cache ID
     * @return {@code true} if actually deleted, otherwise {@code false}
     * @throws BulkLoaderSystemException if failed to delete the cache information by storage exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #deleteCacheInfo(String)
     */
    public boolean deleteCacheInfoCompletely(String cacheId) throws BulkLoaderSystemException {
        if (cacheId == null) {
            throw new IllegalArgumentException("cacheId must not be null"); //$NON-NLS-1$
        }
        final String sql = "DELETE "
            + "FROM __TG_CACHE_INFO "
            + "WHERE CACHE_ID = ? ";
        boolean succeed = false;
        PreparedStatement statement = null;
        try {
            LOG.debugMessage("completely deleting cache info: {0}", cacheId);
            statement = connection.prepareStatement(sql);
            statement.setString(1, cacheId);
            int rows = statement.executeUpdate();
            DBConnection.commit(connection);
            succeed = true;
            LOG.debugMessage("completely deleted cache info: {0}, count={1}", cacheId, rows);
            return rows > 0;
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    getClass(),
                    sql,
                    cacheId);
        } finally {
            DBConnection.closePs(statement);
            if (succeed == false) {
                DBConnection.rollback(connection);
            }
        }
    }

    private LocalCacheInfo toCacheInfoObject(ResultSet resultSet) throws SQLException {
        assert resultSet != null;
        String id = resultSet.getString(1);
        Calendar localTimestamp = Calendar.getInstance();
        Timestamp local = resultSet.getTimestamp(2, localTimestamp);
        if (local == null || local.getTime() == 0) {
            localTimestamp = null;
        } else {
            localTimestamp.setTime(local);
        }
        Calendar remoteTimestamp = Calendar.getInstance();
        Timestamp remote = resultSet.getTimestamp(3, remoteTimestamp);
        if (remote == null || remote.getTime() == 0) {
            remoteTimestamp = null;
        } else {
            remoteTimestamp.setTime(remote);
        }
        String tableName = resultSet.getString(4);
        String path = resultSet.getString(5);
        return new LocalCacheInfo(id, localTimestamp, remoteTimestamp, tableName, path);
    }

    /**
     * Tries to acquire lock for the target cache.
     * @param executionId the current execution ID (as the lock owner)
     * @param cacheId target cache ID
     * @param tableName target table name
     * @return {@code true} if successfully acquired the lock, otherwise {@code false}
     * @throws BulkLoaderSystemException if failed to acquire the lock by storage exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean tryLock(String executionId, String cacheId, String tableName) throws BulkLoaderSystemException {
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        if (cacheId == null) {
            throw new IllegalArgumentException("cacheId must not be null"); //$NON-NLS-1$
        }
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null"); //$NON-NLS-1$
        }
        final String sql = "INSERT IGNORE "
            + "INTO __TG_CACHE_LOCK (CACHE_ID, EXECUTION_ID, ACQUIRED) "
            + "VALUES (?, ?, NOW())";
        boolean succeed = false;
        PreparedStatement statement = null;
        try {
            LOG.debugMessage("trying acquire cache lock: {0}, owner={1}", cacheId, executionId);
            statement = connection.prepareStatement(sql);
            statement.setString(1, cacheId);
            statement.setString(2, executionId);
            int rows = statement.executeUpdate();
            DBConnection.commit(connection);
            succeed = true;
            LOG.debugMessage("tried acquire cache lock: {0}, count={1}", cacheId, rows);
            return rows > 0;
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    getClass(),
                    sql,
                    cacheId, executionId);
        } finally {
            DBConnection.closePs(statement);
            if (succeed == false) {
                DBConnection.rollback(connection);
            }
        }
    }

    /**
     * Releases the cache lock acquired by the specified owner.
     * @param executionId the target execution ID (as the lock owner)
     * @throws BulkLoaderSystemException if failed to acquire the lock by storage exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void releaseLock(String executionId) throws BulkLoaderSystemException {
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        final String sql = "DELETE "
            + "FROM __TG_CACHE_LOCK "
            + "WHERE EXECUTION_ID = ?";
        boolean succeed = false;
        PreparedStatement statement = null;
        try {
            LOG.debugMessage("releasing cache lock: owner={0}", executionId);
            statement = connection.prepareStatement(sql);
            statement.setString(1, executionId);
            int rows = statement.executeUpdate();
            DBConnection.commit(connection);
            succeed = true;
            LOG.debugMessage("released cache lock: owner={0}, count={1}", executionId, rows);
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    getClass(),
                    sql,
                    executionId);
        } finally {
            DBConnection.closePs(statement);
            if (succeed == false) {
                DBConnection.rollback(connection);
            }
        }
    }

    /**
     * Releases all cache lock in this system.
     * @throws BulkLoaderSystemException if failed to acquire the lock by storage exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void releaseAllLock() throws BulkLoaderSystemException {
        final String sql = "DELETE "
            + "FROM __TG_CACHE_LOCK";
        boolean succeed = false;
        PreparedStatement statement = null;
        try {
            LOG.debugMessage("releasing all cache lock");
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            DBConnection.commit(connection);
            succeed = true;
            LOG.debugMessage("released all cache lock");
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    getClass(),
                    sql);
        } finally {
            DBConnection.closePs(statement);
            if (succeed == false) {
                DBConnection.rollback(connection);
            }
        }
    }

    private Timestamp toTimestamp(Calendar calendar) {
        if (calendar == null) {
            return new Timestamp(0L);
        }
        return new Timestamp(calendar.getTimeInMillis());
    }
}
