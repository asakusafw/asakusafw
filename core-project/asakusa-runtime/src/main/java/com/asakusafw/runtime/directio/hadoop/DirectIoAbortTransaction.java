/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import java.text.MessageFormat;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.asakusafw.runtime.directio.DirectDataSourceRepository;

/**
 * CLI for {@link DirectIoTransactionEditor#abort(String)}.
 * @since 0.2.5
 */
public final class DirectIoAbortTransaction extends Configured implements Tool {

    static final Log LOG = LogFactory.getLog(DirectIoAbortTransaction.class);

    private DirectDataSourceRepository repository;

    /**
     * Creates a new instance.
     */
    public DirectIoAbortTransaction() {
        return;
    }

    /**
     * Creates a new instance for testing.
     * @param repository repository
     */
    DirectIoAbortTransaction(DirectDataSourceRepository repository) {
        this.repository = repository;
    }

    @Override
    public int run(String[] args) {
        if (args.length == 1) {
            String executionId = args[0];
            DirectIoTransactionEditor editor = new DirectIoTransactionEditor(repository);
            editor.setConf(getConf());
            try {
                editor.abort(executionId);
                return 0;
            } catch (Exception e) {
                LOG.error(MessageFormat.format(
                        "Failed to abort transaction (executionId={0})",
                        executionId), e);
                return 1;
            }
        } else {
            LOG.error(MessageFormat.format(
                    "Invalid arguments: {0}",
                    Arrays.toString(args)));
            System.err.println(MessageFormat.format(
                    "Usage: hadoop {0} -conf <datasource-conf.xml> [execution-id]",
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
        int exitCode = ToolRunner.run(new Configuration(), new DirectIoAbortTransaction(), args);
        System.exit(exitCode);
    }
}
