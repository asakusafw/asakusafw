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
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.hadoop.util.Tool;

import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.OutputTransactionContext;

/**
 * Edits Direct I/O transactions.
 * @since 0.2.5
 */
public final class DirectIoTransactionEditor extends Configured implements Tool {

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

    @Override
    public int run(String[] args) {
        if (args.length == 1) {
            String executionId = args[0];
            try {
                apply(executionId);
                return 0;
            } catch (Exception e) {
                LOG.error(MessageFormat.format(
                        "Failed to apply staging area (executionId={0})",
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
     * Lists Direct I/O in-doubt transactions.
     * @return in-doubt transactions, or an empty list if not exist
     * @throws IOException if failed to obtain transactions information
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<Commit> list() throws IOException {
        LOG.info("Start listing Direct I/O in-doubt transactions");
        List<FileStatus> list = new ArrayList<FileStatus>(HadoopDataSourceUtil.findAllCommitMarkFiles(getConf()));
        if (list.isEmpty()) {
            LOG.info("There are no Direct I/O in-doubt transactions");
            return Collections.emptyList();
        }
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
        LOG.info(MessageFormat.format(
                "Start extracting {0} Direct I/O commit information",
                list.size()));
        List<Commit> results = new ArrayList<Commit>();
        for (FileStatus stat : list) {
            Commit commitObject = toCommitObject(stat);
            if (commitObject != null) {
                results.add(commitObject);
            }
        }
        LOG.info("Finish listing Direct I/O in-doubt transactions");
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
        boolean applyd = doApply(executionId);
        if (applyd) {
            LOG.info(MessageFormat.format(
                    "Finish applying Direct I/O transaction (executionId={0})",
                    executionId));
        } else {
            LOG.info(MessageFormat.format(
                    "Direct I/O transaction is already completed (executionId={0})",
                    executionId));
        }
        return applyd;
    }

    private Commit toCommitObject(FileStatus stat) {
        assert stat != null;
        Path path = stat.getPath();
        String executionId = HadoopDataSourceUtil.getCommitMarkExecutionId(path);
        long timestamp = stat.getModificationTime();
        List<String> comment = new ArrayList<String>();
        try {
            FSDataInputStream input = path.getFileSystem(getConf()).open(path);
            try {
                Scanner scanner = new Scanner(new InputStreamReader(input, Charset.forName("UTF-8")));
                while (scanner.hasNextLine()) {
                    comment.add(scanner.nextLine());
                }
                scanner.close();
            } finally {
                input.close();
            }
        } catch (IOException e) {
            comment.add(e.toString());
        }
        return new Commit(executionId, timestamp, comment);
    }

    private boolean doApply(String executionId) throws IOException, InterruptedException {
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
                    "Failed to apply some transaction (executionId={0});"
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

    /**
     * Represents a commit.
     * @since 0.2.5
     */
    public static class Commit {

        private final String executionId;

        private final long timestamp;

        private final List<String> comment;

        /**
         * Creates a new instance.
         * @param executionId the execution ID
         * @param timestamp timestamp
         * @param comment comment for this commit
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public Commit(String executionId, long timestamp, List<String> comment) {
            if (executionId == null) {
                throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
            }
            if (comment == null) {
                throw new IllegalArgumentException("comment must not be null"); //$NON-NLS-1$
            }
            this.executionId = executionId;
            this.timestamp = timestamp;
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
         * Returns the comment for this commit.
         * @return the comment
         */
        public List<String> getComment() {
            return comment;
        }
    }
}
