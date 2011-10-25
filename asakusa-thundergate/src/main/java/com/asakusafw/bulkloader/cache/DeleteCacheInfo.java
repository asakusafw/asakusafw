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
 * Disables each cache information.
 * This requires the one of the following argument sequence:
<table border="1">
<tr>
    <th> subcommand </th>
    <th> target-name </th>
    <th> extra argument </th>
</tr>
<tr>
    <td> {@code "cache"} </td>
    <td> target-name </td>
    <td> {@code cache-id} </td>
</tr>
<tr>
    <td> {@code "table"} </td>
    <td> {@code target-name} </td>
    <td> {@code table-name} </td>
</tr>
<tr>
    <td> {@code "all"} </td>
    <td> {@code target-name} </td>
    <td> <em>nothing</em> </td>
</tr>
</table>
 * @since 0.2.3
 */
public class DeleteCacheInfo {

    private static final Class<?> CLASS = DeleteCacheInfo.class;

    private static final List<String> PROPERTIES = Constants.PROPERTIES_DB;

    /**
     * Program entry.
     * @param args the program arguments (see class documentation)
     * @throws IllegalArgumentException if program arguments are invalid
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(MessageFormat.format(
                    "Invalid arguments for DeleteCacheInfo: {0}",
                    Arrays.toString(args)));
            System.exit(Constants.EXIT_CODE_ERROR);
            return;
        }
        String subCommandName = args[0];
        SubCommand subCommand = SubCommand.find(subCommandName);
        if (subCommand == null) {
            System.err.println(MessageFormat.format(
                    "Unknown subcommand for DeleteCacheInfo: {0}",
                    subCommandName));
            System.exit(Constants.EXIT_CODE_ERROR);
            return;
        }
        String targetName = args[1];

        int initExit = initialize(subCommand, targetName);
        if (initExit != Constants.EXIT_CODE_SUCCESS) {
            System.exit(initExit);
        }
        List<String> subArguments = Arrays.asList(args).subList(2, args.length);
        int exitCode = new DeleteCacheInfo().execute(subCommand, targetName, subArguments);
        System.exit(exitCode);
    }

    private static int initialize(SubCommand subCommand, String targetName) {
        if (!BulkLoaderInitializer.initDBServer("DeleteCacheInfo", subCommand.name(), PROPERTIES, targetName)) {
            Log.log(
                    CLASS,
                    MessageIdConst.RCV_INIT_ERROR,
                    new Date(), targetName, subCommand.name());
            return Constants.EXIT_CODE_ERROR;
        }
        return Constants.EXIT_CODE_SUCCESS;
    }

    /**
     * Deletes cache information.
     * @param subCommand sub command
     * @param targetName target name
     * @param subArguments arguments for the sub command (see the class documentation)
     * @return exit code
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see Constants#EXIT_CODE_SUCCESS
     * @see Constants#EXIT_CODE_ERROR
     */
    public int execute(SubCommand subCommand, String targetName, List<String> subArguments) {
        if (subCommand == null) {
            throw new IllegalArgumentException("subcommand must not be null"); //$NON-NLS-1$
        }
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        if (subArguments == null) {
            throw new IllegalArgumentException("subArguments must not be null"); //$NON-NLS-1$
        }
        if (subCommand.arity != subArguments.size()) {
            System.err.println(MessageFormat.format(
                    "Invalid arguments for DeleteCacheInfo {0} ({1})",
                    subCommand.symbol,
                    subArguments));
            return Constants.EXIT_CODE_ERROR;
        }
        try {
            Connection connection = DBConnection.getConnection();
            try {
                LocalCacheInfoRepository repo = new LocalCacheInfoRepository(connection);
                if (subCommand == SubCommand.CACHE) {
                    String cacheId = subArguments.get(0);
                    // TODO logging
                    boolean deleted = repo.deleteCacheInfo(cacheId);
                    System.out.printf("Deleted cache %s: %s%n", cacheId, deleted);
                } else if (subCommand == SubCommand.TABLE) {
                    String tableName = subArguments.get(0);
                    // TODO logging
                    int deleted = repo.deleteTableCacheInfo(tableName);
                    System.out.printf("Deleted cache for table %s: %s%n", tableName, deleted);
                } else if (subCommand == SubCommand.ALL) {
                    // TODO logging
                    repo.deleteAllCacheInfo();
                    System.out.printf("Deleted all cache%n");
                } else {
                    // unknown subcommand
                    throw new AssertionError(subCommand);
                }
            } finally {
                DBConnection.closeConn(connection);
            }
            return Constants.EXIT_CODE_SUCCESS;
        } catch (BulkLoaderSystemException e) {
            // TODO logging
            e.printStackTrace();
            return Constants.EXIT_CODE_ERROR;
        }
    }

    /**
     * Subcommands for {@link DeleteCacheInfo}.
     * @since 0.2.3
     */
    public enum SubCommand {

        /**
         * Delete single cache.
         */
        CACHE(1),

        /**
         * Delete caches corresponded to the table.
         */
        TABLE(1),

        /**
         * Delete all cache.
         */
        ALL(0),
        ;

        /**
         * Arity of this subcommand (excludes the target-name).
         */
        public final int arity;

        final String symbol;

        private SubCommand(int arity) {
            this.arity = arity;
            this.symbol = name().toLowerCase();
        }

        static SubCommand find(String name) {
            assert name != null;
            for (SubCommand kind : values()) {
                if (kind.symbol.equals(name)) {
                    return kind;
                }
            }
            return null;
        }
    }
}
