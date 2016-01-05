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

import java.sql.Connection;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.cache.GetCacheInfoLocal;
import com.asakusafw.bulkloader.cache.LocalCacheInfo;
import com.asakusafw.bulkloader.cache.LocalCacheInfoRepository;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.exception.BulkLoaderReRunnableException;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.thundergate.runtime.cache.CacheInfo;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;

/**
 * Decides import protocol and configures each {@link ImportTargetTableBean table information}.
 * @since 0.2.3
 */
public class ImportProtocolDecide {

    static final Log LOG = new Log(ImportProtocolDecide.class);

    /**
     * Executes this operation.
     * @param bean importer been
     * @throws BulkLoaderSystemException if failed to decide import protocol
     * @throws BulkLoaderReRunnableException if failed to decide import protocol, but is worth retrying operation later
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void execute(ImportBean bean) throws BulkLoaderSystemException, BulkLoaderReRunnableException {
        if (bean == null) {
            throw new IllegalArgumentException("bean must not be null"); //$NON-NLS-1$
        }
        LOG.info("TG-IMPORTER-11001",
                bean.getTargetName(), bean.getBatchId(), bean.getJobflowId(), bean.getExecutionId());
        boolean findCache = false;
        for (String tableName : bean.getImportTargetTableList()) {
            ImportTargetTableBean table = bean.getTargetTable(tableName);
            if (table.getCacheId() == null) {
                setContentProtocol(tableName, table);
            } else {
                findCache = true;
            }
        }
        if (findCache == false) {
            LOG.info("TG-IMPORTER-11003",
                    bean.getTargetName(), bean.getBatchId(), bean.getJobflowId(), bean.getExecutionId());
        } else {
            prepareForCache(bean);
        }
        LOG.info("TG-IMPORTER-11002",
                bean.getTargetName(), bean.getBatchId(), bean.getJobflowId(), bean.getExecutionId());
    }

    /**
     * Cleanup cache for retry the importer operation.
     * @param bean importer been
     * @throws BulkLoaderSystemException if failed to cleanup
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void cleanUpForRetry(ImportBean bean) throws BulkLoaderSystemException {
        if (bean == null) {
            throw new IllegalArgumentException("bean must not be null"); //$NON-NLS-1$
        }
        boolean findCache = false;
        for (String tableName : bean.getImportTargetTableList()) {
            ImportTargetTableBean table = bean.getTargetTable(tableName);
            if (table.getCacheId() != null) {
                findCache = true;
            }
        }
        if (findCache == false) {
            return;
        }
        LOG.info("TG-IMPORTER-11012",
                bean.getTargetName(), bean.getBatchId(), bean.getJobflowId(), bean.getExecutionId());
        Connection connection = DBConnection.getConnection();
        LocalCacheInfoRepository repository = new LocalCacheInfoRepository(connection);
        try {
            repository.releaseLock(bean.getExecutionId());
        } finally {
            DBConnection.closeConn(connection);
        }
    }

    private void prepareForCache(ImportBean bean) throws BulkLoaderSystemException, BulkLoaderReRunnableException {
        assert bean != null;
        boolean succeed = false;
        Connection connection = DBConnection.getConnection();
        LocalCacheInfoRepository repository = new LocalCacheInfoRepository(connection);
        try {
            LOG.info("TG-IMPORTER-11004",
                    bean.getTargetName(), bean.getBatchId(), bean.getJobflowId(), bean.getExecutionId());
            acquireCacheLock(bean, repository);

            LOG.info("TG-IMPORTER-11006",
                    bean.getTargetName(), bean.getBatchId(), bean.getJobflowId(), bean.getExecutionId());
            Map<String, CacheInfo> map = collectRemoteCacheInfo(bean);

            for (String tableName : bean.getImportTargetTableList()) {
                ImportTargetTableBean tableInfo = bean.getTargetTable(tableName);
                String cacheId = tableInfo.getCacheId();
                if (cacheId == null) {
                    assert tableInfo.getImportProtocol() != null;
                    continue;
                }

                CacheInfo currentRemoteInfo = map.get(tableInfo.getDfsFilePath());
                Calendar startTimestamp = computeStartTimestamp(currentRemoteInfo, repository, tableName, tableInfo);

                tableInfo.setStartTimestamp(startTimestamp);
                LocalCacheInfo nextLocalInfo = new LocalCacheInfo(
                        cacheId,
                        null,
                        startTimestamp,
                        tableName,
                        tableInfo.getDfsFilePath());

                ThunderGateCacheSupport model = createDataModelObject(tableName, tableInfo);
                Calendar nextTimestamp = repository.putCacheInfo(nextLocalInfo);
                CacheInfo nextRemoteInfo = new CacheInfo(
                        CacheInfo.FEATURE_VERSION,
                        cacheId,
                        nextTimestamp,
                        tableName,
                        tableInfo.getImportTargetColumns(),
                        model.getClass().getName(),
                        model.__tgc__DataModelVersion());

                FileProtocol.Kind kind = startTimestamp == null
                    ? FileProtocol.Kind.CREATE_CACHE
                    : FileProtocol.Kind.UPDATE_CACHE;
                FileProtocol protocol = new FileProtocol(kind, tableInfo.getDfsFilePath(), nextRemoteInfo);
                tableInfo.setImportProtocol(protocol);
            }
            succeed = true;
        } finally {
            if (succeed == false) {
                repository.releaseLock(bean.getExecutionId());
            }
            DBConnection.closeConn(connection);
        }
    }

    private void acquireCacheLock(
            ImportBean bean,
            LocalCacheInfoRepository repository) throws BulkLoaderSystemException, BulkLoaderReRunnableException {
        assert bean != null;
        assert repository != null;
        for (String tableName : bean.getImportTargetTableList()) {
            ImportTargetTableBean tableInfo = bean.getTargetTable(tableName);
            if (tableInfo.getCacheId() == null) {
                assert tableInfo.getImportProtocol() != null;
                continue;
            }
            boolean locked = repository.tryLock(bean.getExecutionId(), tableInfo.getCacheId(), tableName);
            if (locked == false) {
                throw new BulkLoaderReRunnableException(getClass(), "TG-IMPORTER-11005",
                        tableName,
                        tableInfo.getCacheId());
            }
        }
    }

    private Calendar computeStartTimestamp(
            CacheInfo remoteInfo,
            LocalCacheInfoRepository repository,
            String tableName,
            ImportTargetTableBean tableInfo) throws BulkLoaderSystemException {
        assert repository != null;
        assert tableName != null;
        assert tableInfo != null;
        String cacheId = tableInfo.getCacheId();
        assert cacheId != null;
        if (remoteInfo == null) {
            LOG.info("TG-IMPORTER-11009", tableName, cacheId);
            return null;
        }
        if (remoteInfo.getFeatureVersion().equals(CacheInfo.FEATURE_VERSION) == false) {
            LOG.warn("TG-IMPORTER-11010", tableName, cacheId, MessageFormat.format(
                    "Invalid feature version: expected \"{0}\", but was \"{1}\"",
                    CacheInfo.FEATURE_VERSION, remoteInfo.getFeatureVersion()));
            return null;
        }
        if (remoteInfo.getId().equals(cacheId) == false) {
            LOG.warn("TG-IMPORTER-11010", tableName, cacheId, MessageFormat.format(
                    "Inconsistent cache ID: expected {0}, but was {1}",
                    cacheId, remoteInfo.getId()));
            return null;
        }
        if (remoteInfo.getTableName().equals(tableName) == false) {
            LOG.warn("TG-IMPORTER-11010", tableName, cacheId, MessageFormat.format(
                    "Inconsistent table name: expected {0}, but was {1}",
                    tableName, remoteInfo.getTableName()));
            return null;
        }
        if (remoteInfo.getColumnNames().equals(new HashSet<String>(tableInfo.getImportTargetColumns())) == false) {
            LOG.warn("TG-IMPORTER-11010", tableName, cacheId, MessageFormat.format(
                    "Inconsistent column set: expected {0}, but was {1}",
                    new TreeSet<String>(tableInfo.getImportTargetColumns()), remoteInfo.getColumnNames()));
            return null;
        }

        ThunderGateCacheSupport model = createDataModelObject(tableName, tableInfo);
        if (remoteInfo.getModelClassName().equals(model.getClass().getName()) == false) {
            LOG.warn("TG-IMPORTER-11010", tableName, cacheId, MessageFormat.format(
                    "Inconsistent model class: expected {0}, but was {1}",
                    model.getClass().getName(), remoteInfo.getModelClassName()));
            return null;
        }
        if (remoteInfo.getModelClassVersion() != model.__tgc__DataModelVersion()) {
            LOG.warn("TG-IMPORTER-11010", tableName, cacheId, MessageFormat.format(
                    "Inconsistent model version: expected {0}, but was {1}",
                    model.__tgc__DataModelVersion(), remoteInfo.getModelClassVersion()));
            return null;
        }

        LocalCacheInfo local = repository.getCacheInfo(remoteInfo.getId());
        if (local == null) {
            LOG.info("TG-IMPORTER-11008", tableName, cacheId);
            return null;
        }


        Calendar timestamp = remoteInfo.getTimestamp();
        Calendar localTimestamp = local.getLocalTimestamp();
        if (localTimestamp == null || timestamp.compareTo(localTimestamp) > 0) {
            LOG.warn("TG-IMPORTER-11010", tableName, cacheId, MessageFormat.format(
                    "Inconsistent timestamp: expected is <= {0}, but was {1} (local DB was restored from backup?)",
                    format(localTimestamp), format(timestamp)));
            return null;
        }
        Calendar createdTimestamp = local.getRemoteTimestamp();
        if (createdTimestamp != null && timestamp.compareTo(createdTimestamp) < 0) {
            LOG.warn("TG-IMPORTER-11010", tableName, cacheId, MessageFormat.format(
                    "Inconsistent timestamp: expected is >= {0}, but was {1} (remote FS was restored from backup?)",
                    format(createdTimestamp), format(timestamp)));
            return null;
        }

        LOG.info("TG-IMPORTER-11007",
                tableName, cacheId, format(timestamp));
        return timestamp;
    }

    private String format(Calendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (calendar == null) {
            return formatter.format(new Date(0L));
        } else {
            return formatter.format(calendar.getTime());
        }
    }

    private ThunderGateCacheSupport createDataModelObject(
            String tableName,
            ImportTargetTableBean tableInfo) throws BulkLoaderSystemException {
        assert tableName != null;
        assert tableInfo != null;
        try {
            return tableInfo.getImportTargetType()
                .asSubclass(ThunderGateCacheSupport.class)
                .newInstance();
        } catch (Exception e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-IMPORTER-11011",
                    tableName,
                    tableInfo.getCacheId(),
                    tableInfo.getImportTargetType().getName());
        }
    }

    private void setContentProtocol(String tableName, ImportTargetTableBean table) {
        assert tableName != null;
        assert table != null;
        String remoteLocation = FileNameUtil.createSendImportFileName(tableName);
        FileProtocol protocol = new FileProtocol(FileProtocol.Kind.CONTENT, remoteLocation, null);
        table.setImportProtocol(protocol);
    }

    /**
     * Collects remote cache information related to the import targets.
     * This will return the pairs - {@link ImportTargetTableBean#getDfsFilePath()} and corresponded cache information.
     * If a target table does not use cache feature or related cache did not exist, there will be not in the result.
     * @param bean importer information
     * @return the collected pairs
     * @throws BulkLoaderSystemException if failed to obtain remote cache info
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean bean) throws BulkLoaderSystemException {
        if (bean == null) {
            throw new IllegalArgumentException("bean must not be null"); //$NON-NLS-1$
        }
        GetCacheInfoLocal client = new GetCacheInfoLocal();
        return client.get(bean);
    }
}
