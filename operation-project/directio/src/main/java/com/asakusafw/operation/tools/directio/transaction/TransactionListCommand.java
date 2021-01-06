/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.operation.tools.directio.transaction;

import java.io.PrintWriter;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.directio.hadoop.DirectIoTransactionEditor.TransactionInfo;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing transaction list.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "list",
        commandDescriptionKey = "command.transaction-list",
        resourceBundle = "com.asakusafw.operation.tools.directio.jcommander"
)
public class TransactionListCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(TransactionListCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @ParametersDelegate
    final TransactionEditorParameter transactionEditorParameter = new TransactionEditorParameter();

    @Parameter(
            names = { "-C", "--committed" },
            descriptionKey = "parameter.committed-list")
    boolean committed;

    @Parameter(
            names = { "-U", "--uncommitted" },
            descriptionKey = "parameter.uncommitted-list")
    boolean uncommitted;

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());

        try (PrintWriter writer = outputParameter.open()) {
            transactionEditorParameter.list().stream()
                .filter(it -> committed == uncommitted
                        || it.isCommitted() && committed
                        || it.isCommitted() == false && uncommitted)
                .sorted(Comparator.comparingLong(TransactionInfo::getTimestamp).reversed())
                .forEach(tx -> {
                    writer.printf("%s%n", tx.getExecutionId());
                    verboseParameter.ifRequired(() -> TransactionShowCommand.print(writer, tx, 4));
                });
        }
    }
}
