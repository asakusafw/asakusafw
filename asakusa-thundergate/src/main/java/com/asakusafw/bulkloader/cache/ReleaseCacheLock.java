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
package com.asakusafw.bulkloader.cache;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.MessageIdConst;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;

/**
 * Releases locks for cache mechanism.
 * @since 0.2.3
 */
public class ReleaseCacheLock {

    private static final Class<?> CLASS = GetCacheInfoRemote.class;

    private static final List<String> PROPERTIES = Constants.PROPERTIES_DB;

    /**
     * Program entry.
     * @param args target-name [execution-ID]
     * @throws IllegalArgumentException if program arguments are invalid
     */
    public static void main(String[] args) {
        if (args.length != 1 && args.length != 2) {
            System.err.println(MessageFormat.format(
                    "Invalid arguments for ReleaseCacheLock: {0}",
                    Arrays.toString(args)));
            System.exit(Constants.EXIT_CODE_ERROR);
            return;
        }
        String targetName = args[0];
        String executionId = args.length == 2 ? args[1] : null;
        int initExit = initialize(targetName, executionId);
        if (initExit != Constants.EXIT_CODE_SUCCESS) {
            System.exit(initExit);
        }
        int exitCode = new ReleaseCacheLock().execute(executionId);
        System.exit(exitCode);
    }

    private static int initialize(String targetName, String executionId) {
        if (!BulkLoaderInitializer.initDBServer("ReleaseCacheLock", executionId, PROPERTIES, targetName)) {
            Log.log(
                    CLASS,
                    MessageIdConst.RCV_INIT_ERROR,
                    new Date(), targetName, executionId);
            return Constants.EXIT_CODE_ERROR;
        }
        return Constants.EXIT_CODE_SUCCESS;
    }

    /**
     * Releases cache lock.
     * @param executionId target execution ID, or {@code null} to release all lock
     * @return exit code
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see Constants#EXIT_CODE_SUCCESS
     * @see Constants#EXIT_CODE_ERROR
     */
    public int execute(String executionId) {
        try {
            Connection connection = DBConnection.getConnection();
            try {
                LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
                if (executionId != null) {
                    // TODO logging
                    repo.releaseLock(executionId);
                } else {
                    // TODO logging
                    repo.releaseAllLock();
                }
            } finally {
                DBConnection.closeConn(connection);
            }
            return Constants.EXIT_CODE_SUCCESS;
        } catch (BulkLoaderSystemException e) {
            // TODO logging
            return Constants.EXIT_CODE_ERROR;
        }
    }
}
