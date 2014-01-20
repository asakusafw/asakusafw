/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.directio.tools;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.hadoop.DirectIoTransactionEditor;
import com.asakusafw.runtime.directio.hadoop.DirectIoTransactionEditor.TransactionInfo;

/**
 * CLI for {@link DirectIoTransactionEditor#apply(String)}.
 * @since 0.4.0
 */
public final class DirectIoListTransaction extends Configured implements Tool {

    static final Log LOG = LogFactory.getLog(DirectIoListTransaction.class);

    private DirectDataSourceRepository repository;

    /**
     * Creates a new instance.
     */
    public DirectIoListTransaction() {
        return;
    }

    /**
     * Creates a new instance for testing.
     * @param repository repository
     */
    DirectIoListTransaction(DirectDataSourceRepository repository) {
        this.repository = repository;
    }

    @Override
    public int run(String[] args) {
        if (args.length == 0) {
            DirectIoTransactionEditor editor = new DirectIoTransactionEditor(repository);
            editor.setConf(getConf());
            try {
                List<TransactionInfo> list = editor.list();
                if (list.isEmpty() == false) {
                    for (TransactionInfo commit : list) {
                        System.out.println("====");
                        System.out.printf("Date:%n");
                        System.out.printf("  %s%n", new java.util.Date(commit.getTimestamp()));
                        System.out.printf("Execution ID:%n");
                        System.out.printf("  %s%n", commit.getExecutionId());
                        System.out.printf("Status:%n");
                        System.out.printf("  %s%n", commit.isCommitted() ? "Committed" : "NOT Committed");
                        System.out.printf("Comments:%n");
                        for (String line : commit.getComment()) {
                            System.out.printf("  %s%n", line);
                        }
                    }
                    System.out.println("====");
                }
                return 0;
            } catch (Exception e) {
                LOG.error("Failed to list transaction", e);
                return 1;
            }
        } else {
            LOG.error(MessageFormat.format(
                    "Invalid arguments: {0}",
                    Arrays.toString(args)));
            System.err.println(MessageFormat.format(
                    "Usage: hadoop {0} -conf <datasource-conf.xml>",
                    getClass().getName()));
            return 1;
        }
    }

    /**
     * Tool program entry.
     * @param args {@code Hadoop-generic-arguments...} {@code application specific-arguments}
     * @throws Exception if failed to execute
     * @see #run(String[])
     */
    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new Configuration(), new DirectIoListTransaction(), args);
        System.exit(exitCode);
    }
}
