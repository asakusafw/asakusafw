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
package com.asakusafw.bulkloader.importer;

import java.sql.Connection;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.cache.GetCacheInfoLocal;
import com.asakusafw.bulkloader.cache.LocalCacheInfo;
import com.asakusafw.bulkloader.cache.LocalCacheInfoRepository;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.common.MessageIdConst;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.thundergate.runtime.cache.CacheInfo;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;

/**
 * Decides import protocol and configures each {@link ImportTargetTableBean table information}.
 * @since 0.2.3
 */
public class ImportProtocolDecide {

    /**
     * Executes this operation.
     * @param bean importer been
     * @throws BulkLoaderSystemException if failed to decide import protocol
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void execute(ImportBean bean) throws BulkLoaderSystemException {
        if (bean == null) {
            throw new IllegalArgumentException("bean must not be null"); //$NON-NLS-1$
        }
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
            return;
        }
        prepareForCache(bean);
    }


    private void prepareForCache(ImportBean bean) throws BulkLoaderSystemException {
        assert bean != null;
        boolean succeed = false;
        Connection connection = DBConnection.getConnection();
        LocalCacheInfoRepository repository = new LocalCacheInfoRepository(connection);
        try {
            acquireCacheLock(bean, repository);

            Map<String, CacheInfo> map = collectRemoteCacheInfo(bean);

            for (String tableName : bean.getImportTargetTableList()) {
                ImportTargetTableBean tableInfo = bean.getTargetTable(tableName);
                String cacheId = tableInfo.getCacheId();
                if (cacheId == null) {
                    assert tableInfo.getImportProtocol() != null;
                    continue;
                }

                ThunderGateCacheSupport model = createDataModelObject(tableName, tableInfo);

                CacheInfo currentRemoteInfo = map.get(tableInfo.getDfsFilePath());
                Calendar startTimestamp = computeStartTimestamp(currentRemoteInfo, repository, tableName, tableInfo);

                FileProtocol.Kind kind = startTimestamp == null
                    ? FileProtocol.Kind.CREATE_CACHE
                    : FileProtocol.Kind.UPDATE_CACHE;
                tableInfo.setStartTimestamp(startTimestamp);
                LocalCacheInfo nextLocalInfo = new LocalCacheInfo(
                        cacheId,
                        null,
                        startTimestamp,
                        tableName,
                        tableInfo.getDfsFilePath());

                Calendar nextTimestamp = repository.putCacheInfo(nextLocalInfo);
                CacheInfo nextRemoteInfo = new CacheInfo(
                        CacheInfo.FEATURE_VERSION,
                        cacheId,
                        nextTimestamp,
                        tableName,
                        tableInfo.getImportTargetColumns(),
                        model.getClass().getName(),
                        model.__tgc__DataModelVersion());

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
            LocalCacheInfoRepository repository) throws BulkLoaderSystemException {
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
                throw new BulkLoaderSystemException(
                        getClass(),
                        // TODO logging
                        MessageIdConst.IMP_CACHE_ERROR,
                        bean.getJobnetEndTime(),
                        "?",
                        bean.getTargetName(),
                        bean.getBatchId(),
                        bean.getJobflowId(),
                        bean.getExecutionId());
            }
        }
    }

    private Calendar computeStartTimestamp(
            CacheInfo info,
            LocalCacheInfoRepository repository,
            String tableName,
            ImportTargetTableBean tableInfo) throws BulkLoaderSystemException {
        assert repository != null;
        assert tableName != null;
        assert tableInfo != null;
        if (info == null) {
            return null;
        }
        if (info.getFeatureVersion().equals(CacheInfo.FEATURE_VERSION) == false) {
            return null;
        }
        if (info.getId().equals(tableInfo.getCacheId()) == false) {
            return null;
        }
        if (info.getTableName().equals(tableName) == false) {
            return null;
        }
        if (info.getColumnNames().equals(new HashSet<String>(tableInfo.getImportTargetColumns())) == false) {
            return null;
        }

        ThunderGateCacheSupport model = createDataModelObject(tableName, tableInfo);
        if (info.getModelClassName().equals(model.getClass().getName()) == false) {
            return null;
        }
        if (info.getModelClassVersion() != model.__tgc__DataModelVersion()) {
            return null;
        }

        LocalCacheInfo local = repository.getCacheInfo(info.getId());
        if (local == null) {
            return null;
        }
        Calendar timestamp = info.getTimestamp();
        Calendar localTimestamp = local.getLocalTimestamp();
        if (localTimestamp == null || timestamp.compareTo(localTimestamp) > 0) {
            return null;
        }
        Calendar createdTimestamp = local.getRemoteTimestamp();
        if (createdTimestamp != null && timestamp.compareTo(createdTimestamp) < 0) {
            return null;
        }

        return timestamp;
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
            throw new BulkLoaderSystemException(
                    e,
                    getClass(),
                    // TODO logging
                    MessageIdConst.IMP_EXCEPRION);
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
     * @throws BulkLoaderSystemException
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
