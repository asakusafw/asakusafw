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
package com.asakusafw.runtime.directio.hadoop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.OutputTransactionContext;

/**
 * Edits Direct I/O transactions.
 * @since 0.2.5
 */
public final class DirectIoTransactionEditor extends Configured {

    static final Log LOG = LogFactory.getLog(DirectIoTransactionEditor.class);

    private DirectDataSourceRepository repository;

    /**
     * Creates a new instance.
     */
    public DirectIoTransactionEditor() {
        return;
    }

    /**
     * Creates a new instance.
     * @param repository repository, or {@code null} if create repository from Configuration
     */
    public DirectIoTransactionEditor(DirectDataSourceRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the corresponded transaction information to the execution ID.
     * @param executionId target ID
     * @return the corresponded transaction information, or {@code null} if does not exist
     * @throws IOException if failed to obtain information
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TransactionInfo get(String executionId) throws IOException {
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        Path path = HadoopDataSourceUtil.getTransactionInfoPath(getConf(), executionId);
        try {
            FileStatus status = path.getFileSystem(getConf()).getFileStatus(path);
            return toInfoObject(status);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Lists Direct I/O transaction information.
     * @return transactions, or an empty list if not exist
     * @throws IOException if failed to obtain transactions information
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<TransactionInfo> list() throws IOException {
        LOG.info("Start listing Direct I/O transactions");
        List<FileStatus> list = new ArrayList<>(HadoopDataSourceUtil.findAllTransactionInfoFiles(getConf()));
        if (list.isEmpty()) {
            LOG.info("There are no Direct I/O transactions");
            return Collections.emptyList();
        }
        Collections.sort(list, new Comparator<FileStatus>() {
            @Override
            public int compare(FileStatus o1, FileStatus o2) {
                return Long.compare(o1.getModificationTime(), o2.getModificationTime());
            }
        });
        LOG.info(MessageFormat.format(
                "Start extracting {0} Direct I/O commit information",
                list.size()));
        List<TransactionInfo> results = new ArrayList<>();
        for (FileStatus stat : list) {
            TransactionInfo commitObject = toInfoObject(stat);
            if (commitObject != null) {
                results.add(commitObject);
            }
        }
        LOG.info("Finish listing Direct I/O transactions");
        return results;
    }

    /**
     * Applies Direct I/O transaction for an execution.
     * @param executionId target execution ID
     * @return {@code true} if successfully applied, or {@code false} if nothing to do
     * @throws IOException if failed to apply by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean apply(String executionId) throws IOException, InterruptedException {
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        LOG.info(MessageFormat.format(
                "Start applying Direct I/O transaction (executionId={0})",
                executionId));
        boolean applied = doApply(executionId);
        if (applied) {
            LOG.info(MessageFormat.format(
                    "Finish applying Direct I/O transaction (executionId={0})",
                    executionId));
        } else {
            LOG.info(MessageFormat.format(
                    "Direct I/O transaction is already completed (executionId={0})",
                    executionId));
        }
        return applied;
    }

    /**
     * Aborts Direct I/O transaction for an execution.
     * @param executionId target execution ID
     * @return {@code true} if successfully aborted, or {@code false} if nothing to do
     * @throws IOException if failed to abort by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean abort(String executionId) throws IOException, InterruptedException {
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        LOG.info(MessageFormat.format(
                "Start aborting Direct I/O transaction (executionId={0})",
                executionId));
        boolean aborted = doAbort(executionId);
        if (aborted) {
            LOG.info(MessageFormat.format(
                    "Finish aborting Direct I/O transaction (executionId={0})",
                    executionId));
        } else {
            LOG.info(MessageFormat.format(
                    "Direct I/O transaction is already completed (executionId={0})",
                    executionId));
        }
        return aborted;
    }

    private TransactionInfo toInfoObject(FileStatus stat) throws IOException {
        assert stat != null;
        Path path = stat.getPath();
        String executionId = HadoopDataSourceUtil.getTransactionInfoExecutionId(path);
        long timestamp = stat.getModificationTime();
        List<String> comment = new ArrayList<>();
        Path commitMarkPath = HadoopDataSourceUtil.getCommitMarkPath(getConf(), executionId);
        FileSystem fs = path.getFileSystem(getConf());
        boolean committed = fs.exists(commitMarkPath);
        try (FSDataInputStream input = fs.open(path);
                Scanner scanner = new Scanner(new InputStreamReader(input, HadoopDataSourceUtil.COMMENT_CHARSET))) {
            while (scanner.hasNextLine()) {
                comment.add(scanner.nextLine());
            }
        } catch (IOException e) {
            comment.add(e.toString());
        }

        return new TransactionInfo(executionId, timestamp, committed, comment);
    }

    private boolean doApply(String executionId) throws IOException, InterruptedException {
        assert executionId != null;
        Path transactionInfo = HadoopDataSourceUtil.getTransactionInfoPath(getConf(), executionId);
        Path commitMark = HadoopDataSourceUtil.getCommitMarkPath(getConf(), executionId);
        FileSystem fs = commitMark.getFileSystem(getConf());
        if (fs.exists(transactionInfo) == false) {
            return false;
        }
        boolean succeed = true;
        if (fs.exists(commitMark) == false) {
            // FIXME cleanup
            return false;
        }
        DirectDataSourceRepository repo = getRepository();
        for (String containerPath : repo.getContainerPaths()) {
            String datasourceId = repo.getRelatedId(containerPath);
            try {
                DirectDataSource datasource = repo.getRelatedDataSource(containerPath);
                OutputTransactionContext context = HadoopDataSourceUtil.createContext(executionId, datasourceId);
                datasource.commitTransactionOutput(context);
                datasource.cleanupTransactionOutput(context);
            } catch (IOException e) {
                succeed = false;
                LOG.error(MessageFormat.format(
                        "Failed to apply transaction (datastoreId={0}, executionId={1})",
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
                            "Failed to delete commit mark (executionId={0}, path={1})",
                            executionId,
                            commitMark));
                } else if (fs.delete(transactionInfo, true) == false) {
                    LOG.warn(MessageFormat.format(
                            "Failed to delete transaction info (executionId={0}, path={1})",
                            executionId,
                            transactionInfo));
                }
            } catch (FileNotFoundException e) {
                LOG.warn(MessageFormat.format(
                        "Failed to delete commit mark (executionId={0}, path={1})",
                        executionId,
                        commitMark), e);
            }
            return true;
        } else {
            throw new IOException(MessageFormat.format(
                    "Failed to apply this transaction (executionId={0});"
                    + " if you want to ignore this transaction, please abort this.",
                    executionId));
        }
    }

    private boolean doAbort(String executionId) throws IOException, InterruptedException {
        assert executionId != null;
        Path transactionInfo = HadoopDataSourceUtil.getTransactionInfoPath(getConf(), executionId);
        Path commitMark = HadoopDataSourceUtil.getCommitMarkPath(getConf(), executionId);
        FileSystem fs = commitMark.getFileSystem(getConf());
        if (fs.exists(transactionInfo) == false) {
            return false;
        }
        boolean succeed = true;
        if (fs.exists(commitMark)) {
            LOG.info(MessageFormat.format(
                    "Deleting commit mark (executionId={0}, path={1})",
                    executionId,
                    commitMark));
            if (fs.delete(commitMark, true) == false) {
                succeed = false;
                LOG.warn(MessageFormat.format(
                        "Failed to delete commit mark (executionId={0}, path={1})",
                        executionId,
                        commitMark));
            }
        }
        DirectDataSourceRepository repo = getRepository();
        for (String containerPath : repo.getContainerPaths()) {
            String datasourceId = repo.getRelatedId(containerPath);
            try {
                DirectDataSource datasource = repo.getRelatedDataSource(containerPath);
                OutputTransactionContext context = HadoopDataSourceUtil.createContext(executionId, datasourceId);
                datasource.cleanupTransactionOutput(context);
            } catch (IOException e) {
                succeed = false;
                LOG.error(MessageFormat.format(
                        "Failed to abort transaction (datastoreId={0}, executionId={1})",
                        datasourceId,
                        executionId));
            }
        }
        if (succeed) {
            LOG.info(MessageFormat.format(
                    "Deleting transaction info (executionId={0}, path={1})",
                    executionId,
                    commitMark));
            try {
                if (fs.delete(transactionInfo, true) == false) {
                    LOG.warn(MessageFormat.format(
                            "Failed to delete transaction info (executionId={0}, path={1})",
                            executionId,
                            transactionInfo));
                }
            } catch (FileNotFoundException e) {
                LOG.warn(MessageFormat.format(
                        "Failed to delete transaction info (executionId={0}, path={1})",
                        executionId,
                        commitMark), e);
            }
            return true;
        } else {
            throw new IOException(MessageFormat.format(
                    "Failed to abort this transaction (executionId={0});"
                    + " if you want to ignore this transaction, please delete {1} manually.",
                    executionId,
                    transactionInfo));
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

    /**
     * Represents a tranaction info.
     * @since 0.2.5
     */
    public static class TransactionInfo {

        private final String executionId;

        private final long timestamp;

        private final boolean committed;

        private final List<String> comment;

        /**
         * Creates a new instance.
         * @param executionId the execution ID
         * @param timestamp timestamp
         * @param committed whether this transaction was committed
         * @param comment comment for this transaction
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public TransactionInfo(String executionId, long timestamp, boolean committed, List<String> comment) {
            if (executionId == null) {
                throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
            }
            if (comment == null) {
                throw new IllegalArgumentException("comment must not be null"); //$NON-NLS-1$
            }
            this.executionId = executionId;
            this.timestamp = timestamp;
            this.committed = committed;
            this.comment = comment;
        }

        /**
         * Returns the target execution ID.
         * @return the execution ID
         */
        public String getExecutionId() {
            return executionId;
        }

        /**
         * Returns the commit start timestamp.
         * @return the timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Returns whether this transaction was committed.
         * @return the committed
         */
        public boolean isCommitted() {
            return committed;
        }

        /**
         * Returns the comment for this commit.
         * @return the comment
         */
        public List<String> getComment() {
            return comment;
        }
    }
}
