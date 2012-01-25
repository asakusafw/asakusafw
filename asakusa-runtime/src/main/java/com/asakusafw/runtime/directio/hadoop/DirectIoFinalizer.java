/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.hadoop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;

import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.OutputTransactionContext;

/**
 * Finalizes Direct I/O staging area.
 * @since 0.2.5
 */
public final class DirectIoFinalizer extends Configured implements Tool {

    static final Log LOG = LogFactory.getLog(DirectIoFinalizer.class);

    private DirectDataSourceRepository repository;

    /**
     * Creates a new instance.
     */
    public DirectIoFinalizer() {
        return;
    }

    /**
     * Creates a new instance for testing.
     * @param repository repository
     */
    DirectIoFinalizer(DirectDataSourceRepository repository) {
        this.repository = repository;
    }

    @Override
    public int run(String[] args) {
        if (args.length == 0) {
            try {
                finalizeAll();
                return 0;
            } catch (Exception e) {
                LOG.error("Failed to finalize staging area", e);
                return 1;
            }
        } else if (args.length == 1) {
            String executionId = args[0];
            try {
                finalizeSingle(executionId);
                return 0;
            } catch (Exception e) {
                LOG.error(MessageFormat.format(
                        "Failed to finalize staging area (executionId={0})",
                        executionId), e);
                return 1;
            }
        } else {
            LOG.error(MessageFormat.format(
                    "Invalid arguments: {0}",
                    Arrays.toString(args)));
            System.err.println(MessageFormat.format(
                    "Usage: hadoop {0} [execution-id] -conf <datasource-conf.xml>",
                    getClass().getName()));
            return 1;
        }
    }

    /**
     * Finalizes all Direct I/O transactions.
     * @return the number of actually finalized transactions
     * @throws IOException if failed to finalize by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public int finalizeAll() throws IOException, InterruptedException {
        LOG.info("Start finalizing all Direct I/O transactions");
        List<String> executionIds = findIndoubtExecutionIds();
        LOG.info(MessageFormat.format(
                "Found {0} in-doubt Direct I/O transaction(s)",
                executionIds.size()));
        int count = 0;
        for (String executionId : executionIds) {
            boolean finalized = finalizeSingle(executionId);
            if (finalized) {
                count++;
            }
        }
        LOG.info(MessageFormat.format(
                "Finish finalizing {0} Direct I/O transaction(s)",
                count));
        return count;
    }

    /**
     * Finalizes Direct I/O transaction for an execution.
     * @param executionId target execution ID
     * @return {@code true} if successfully finalized, or {@code false} if already finalized
     * @throws IOException if failed to finalize by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean finalizeSingle(String executionId) throws IOException, InterruptedException {
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        LOG.info(MessageFormat.format(
                "Start finalizing Direct I/O transaction (executionId={0})",
                executionId));
        boolean finalized = doFinalize(executionId);
        if (finalized) {
            LOG.info(MessageFormat.format(
                    "Finish finalizing Direct I/O transaction (executionId={0})",
                    executionId));
        } else {
            LOG.info(MessageFormat.format(
                    "Direct I/O transaction is already completed (executionId={0})",
                    executionId));
        }
        return finalized;
    }

    private List<String> findIndoubtExecutionIds() throws IOException {
        List<FileStatus> list = new ArrayList<FileStatus>(HadoopDataSourceUtil.findAllCommitMarkFiles(getConf()));
        Collections.sort(list, new Comparator<FileStatus>() {
            @Override
            public int compare(FileStatus o1, FileStatus o2) {
                long t1 = o1.getModificationTime();
                long t2 = o2.getModificationTime();
                if (t1 < t2) {
                    return -1;
                } else if (t1 > t2) {
                    return +1;
                }
                return 0;
            }
        });
        List<String> results = new ArrayList<String>();
        for (FileStatus stat : list) {
            Path path = stat.getPath();
            String executionId = HadoopDataSourceUtil.getCommitMarkExecutionId(path);
            if (executionId != null) {
                results.add(executionId);
            }
        }
        return results;
    }

    private boolean doFinalize(String executionId) throws IOException, InterruptedException {
        assert executionId != null;
        Path commitMark = HadoopDataSourceUtil.getCommitMarkPath(getConf(), executionId);
        FileSystem fs = commitMark.getFileSystem(getConf());
        if (fs.exists(commitMark) == false) {
            return false;
        }
        boolean succeed = true;
        DirectDataSourceRepository repo = getRepository();
        for (String containerPath : repo.getContainerPaths()) {
            String datasourceId = repo.getRelatedId(containerPath);
            try {
                DirectDataSource datasource = repo.getRelatedDataSource(containerPath);
                OutputTransactionContext context = HadoopDataSourceUtil.createContext(executionId, datasourceId);
                doFinalize(context, datasource);
            } catch (IOException e) {
                succeed = false;
                LOG.error(MessageFormat.format(
                        "Failed to finalize transaction (datastoreId={0}, executionId={1})",
                        datasourceId,
                        executionId));
            }
        }
        if (succeed) {
            LOG.info(MessageFormat.format(
                    "Deleting commit mark (executionId={0}, path={1})",
                    executionId,
                    commitMark));
            try {
                if (fs.delete(commitMark, true) == false) {
                    LOG.warn(MessageFormat.format(
                            "Failed to delte commit mark (executionId={0}, path={1})",
                            executionId,
                            commitMark));
                }
            } catch (FileNotFoundException e) {
                LOG.warn(MessageFormat.format(
                        "Failed to delte commit mark (executionId={0}, path={1})",
                        executionId,
                        commitMark), e);
            }
            return true;
        } else {
            throw new IOException(MessageFormat.format(
                    "Failed to finalize some transaction (executionId={0});"
                    + " if you want to ignore this transaction, please delete {1} manually.",
                    executionId,
                    commitMark));
        }
    }

    private synchronized DirectDataSourceRepository getRepository() {
        if (repository == null) {
            LOG.info("Start initializing Direct I/O data stores");
            repository = HadoopDataSourceUtil.loadRepository(getConf());
            LOG.info("Finish initializing Direct I/O data stores");
        }
        return repository;
    }

    private void doFinalize(
            OutputTransactionContext context,
            DirectDataSource datasource) throws IOException, InterruptedException {
        assert context != null;
        assert datasource != null;
        datasource.commitTransactionOutput(context);
        datasource.cleanupTransactionOutput(context);
    }
}
