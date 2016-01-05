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
import java.util.Arrays;
import java.util.List;

import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.runtime.core.context.RuntimeContext;

/**
 * Releases locks for cache mechanism.
 * This program requires following argument(s):
 * <ol>
 * <li> target name </li>
 * <li> execution ID (optional) </li>
 * </ol>
 * @since 0.2.3
 */
public class ReleaseCacheLock {

    static final Log LOG = new Log(ReleaseCacheLock.class);

    private static final List<String> PROPERTIES = Constants.PROPERTIES_DB;

    /**
     * Program entry.
     * @param args target-name [execution-ID]
     * @throws IllegalArgumentException if program arguments are invalid
     */
    public static void main(String[] args) {
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        if (args.length != 1 && args.length != 2) {
            LOG.error("TG-RELEASECACHELOCK-01003", Arrays.toString(args));
            System.exit(Constants.EXIT_CODE_ERROR);
            return;
        }
        String targetName = args[0];
        String executionId = args.length == 2 ? args[1] : null;
        int initExit = initialize(targetName, executionId);
        if (initExit != Constants.EXIT_CODE_SUCCESS) {
            System.exit(initExit);
        }

        LOG.info("TG-RELEASECACHELOCK-01001", targetName, executionId);
        int exitCode = new ReleaseCacheLock().execute(targetName, executionId);
        LOG.info("TG-RELEASECACHELOCK-01002", targetName, executionId);
        System.exit(exitCode);
    }

    private static int initialize(String targetName, String executionId) {
        if (!BulkLoaderInitializer.initDBServer("ReleaseCacheLock", executionId, PROPERTIES, targetName)) {
            LOG.error("TG-RELEASECACHELOCK-01004", targetName, executionId);
            return Constants.EXIT_CODE_ERROR;
        }
        return Constants.EXIT_CODE_SUCCESS;
    }

    /**
     * Releases cache lock.
     * @param targetName target name
     * @param executionId target execution ID, or {@code null} to release all lock
     * @return exit code
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see Constants#EXIT_CODE_SUCCESS
     * @see Constants#EXIT_CODE_ERROR
     */
    public int execute(String targetName, String executionId) {
        try {
            Connection connection = DBConnection.getConnection();
            try {
                LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
                if (executionId != null) {
                    LOG.info("TG-RELEASECACHELOCK-01005", targetName, executionId);
                    if (RuntimeContext.get().canExecute(repo)) {
                        repo.releaseLock(executionId);
                    }
                } else {
                    LOG.info("TG-RELEASECACHELOCK-01006", targetName);
                    if (RuntimeContext.get().canExecute(repo)) {
                        repo.releaseAllLock();
                    }
                }
            } finally {
                DBConnection.closeConn(connection);
            }
            return Constants.EXIT_CODE_SUCCESS;
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            return Constants.EXIT_CODE_ERROR;
        }
    }
}
