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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.runtime.core.context.RuntimeContext;

/**
 * Deletes disposed cache storages.
 * This program requires following argument:
 * <ol>
 * <li> target name </li>
 * </ol>
 * @since 0.2.3
 */
public class GcCacheStorage {

    static final Log LOG = new Log(GcCacheStorage.class);

    private static final List<String> PROPERTIES = Constants.PROPERTIES_DB;

    /**
     * Program entry.
     * @param args target-name
     * @throws IllegalArgumentException if program arguments are invalid
     */
    public static void main(String[] args) {
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        if (args.length != 1) {
            LOG.error("TG-GCCACHE-01003", Arrays.toString(args));
            System.exit(Constants.EXIT_CODE_ERROR);
            return;
        }
        String targetName = args[0];
        String executionId = UUID.randomUUID().toString();
        int initExit = initialize(targetName, executionId);
        if (initExit != Constants.EXIT_CODE_SUCCESS) {
            System.exit(initExit);
        }

        LOG.info("TG-GCCACHE-01001", targetName, executionId);
        int exitCode = new GcCacheStorage().execute(targetName, executionId);
        LOG.info("TG-GCCACHE-01002", targetName, executionId);
        System.exit(exitCode);
    }

    private static int initialize(String targetName, String executionId) {
        if (!BulkLoaderInitializer.initDBServer("GcCacheStorage", executionId, PROPERTIES, targetName)) {
            LOG.error("TG-GCCACHE-01004", targetName, executionId);
            return Constants.EXIT_CODE_ERROR;
        }
        return Constants.EXIT_CODE_SUCCESS;
    }

    /**
     * Deletes disposed cache storages.
     * @param targetName target name
     * @param executionId target execution ID (for cache lock)
     * @return exit code
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see Constants#EXIT_CODE_SUCCESS
     * @see Constants#EXIT_CODE_WARNING
     * @see Constants#EXIT_CODE_ERROR
     */
    public int execute(String targetName, String executionId) {
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        try {
            Connection connection = DBConnection.getConnection();
            try {
                LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
                boolean succeed;
                if (RuntimeContext.get().isSimulation()) {
                    succeed = true;
                } else {
                    succeed = execute(repo, targetName, executionId);
                }
                if (succeed) {
                    LOG.info("TG-GCCACHE-01005", targetName);
                    return Constants.EXIT_CODE_SUCCESS;
                } else {
                    LOG.warn("TG-GCCACHE-01006", targetName);
                    return Constants.EXIT_CODE_WARNING;
                }
            } finally {
                DBConnection.closeConn(connection);
            }
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            LOG.error("TG-GCCACHE-01007", targetName);
            return Constants.EXIT_CODE_ERROR;
        }
    }

    private boolean execute(
            LocalCacheInfoRepository repo,
            String targetName,
            String executionId) throws BulkLoaderSystemException {
        assert repo != null;
        assert targetName != null;
        assert executionId != null;

        LOG.info("TG-GCCACHE-01008", targetName);
        List<LocalCacheInfo> deleted = repo.listDeletedCacheInfo();
        if (deleted.isEmpty()) {
            LOG.info("TG-GCCACHE-01009", targetName);
            return true;
        }

        boolean green = true;
        try {
            LOG.info("TG-GCCACHE-01010", targetName, executionId);
            List<LocalCacheInfo> locked = new ArrayList<LocalCacheInfo>();
            for (LocalCacheInfo info : deleted) {
                LOG.debugMessage("Trying to acquire a cache lock: cacheId={0}, targetName={1}, executionId={2}",
                        info.getId(), targetName, executionId);
                if (repo.tryLock(executionId, info.getId(), info.getTableName())) {
                    locked.add(info);
                } else {
                    LOG.info("TG-GCCACHE-01014", targetName, info.getId(), info.getTableName());
                    green = false;
                }
            }
            DeleteCacheStorageLocal client = getClient();
            Map<String, FileProtocol.Kind> results = client.delete(locked, targetName);
            int count = 0;
            for (LocalCacheInfo info : locked) {
                FileProtocol.Kind result = results.get(info.getPath());
                if (result == FileProtocol.Kind.RESPONSE_DELETED
                        || result == FileProtocol.Kind.RESPONSE_NOT_FOUND) {
                    LOG.info("TG-GCCACHE-01011", targetName, info.getId(), info.getTableName());
                    repo.deleteCacheInfoCompletely(info.getId());
                    count++;
                } else {
                    LOG.info("TG-GCCACHE-01015", targetName, info.getId(), info.getTableName(), info.getPath());
                    green = false;
                }
            }
            LOG.info("TG-GCCACHE-01012", targetName, count);
        } finally {
            LOG.info("TG-GCCACHE-01013", targetName, executionId);
            repo.releaseLock(executionId);
        }
        return green;
    }

    /**
     * Returns an instance of {@link DeleteCacheStorageLocal}.
     * @return the instance
     */
    protected DeleteCacheStorageLocal getClient() {
        return new DeleteCacheStorageLocal();
    }
}
