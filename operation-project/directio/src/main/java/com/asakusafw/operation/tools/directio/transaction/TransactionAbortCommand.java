/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.directio.hadoop.DirectIoTransactionEditor;
import com.asakusafw.runtime.directio.hadoop.DirectIoTransactionEditor.TransactionInfo;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.CommandExecutionException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for aborting Direct I/O transactions.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "abort",
        commandDescriptionKey = "command.transaction-abort",
        resourceBundle = "com.asakusafw.operation.tools.directio.jcommander"
)
public class TransactionAbortCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(TransactionAbortCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @ParametersDelegate
    final TransactionEditorParameter transactionEditorParameter = new TransactionEditorParameter();

    @ParametersDelegate
    final TransactionIdParameter transactionIdParameter = new TransactionIdParameter();

    @Parameter(
            names = { "-f", "--force" },
            descriptionKey = "parameter.force-abort")
    boolean force;

    @Parameter(
            names = { "-q", "--quiet" },
            descriptionKey = "parameter.quiet-abort")
    boolean quiet;

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());

        String id = transactionIdParameter.getId();
        TransactionInfo info = transactionEditorParameter.find(id)
                .orElse(null);
        if (info == null) {
            if (quiet) {
                return;
            }
            throw new CommandConfigurationException(MessageFormat.format(
                    "transaction \"{0}\" not found (may be already completed)",
                    id));
        } else if (info.isCommitted()) {
            if (force == false) {
                if (quiet) {
                    return;
                }
                throw new CommandConfigurationException(MessageFormat.format(
                        "transaction \"{0}\" is already committed (add \"--force\" to abort this transaction)",
                        id));
            }
        }

        DirectIoTransactionEditor editor = transactionEditorParameter.getEditor();
        try (PrintWriter writer = outputParameter.open()) {
            verboseParameter.printf(writer, "abort transaction%n");
            verboseParameter.ifRequired(() -> TransactionShowCommand.print(writer, info, 0));
            if (editor.abort(id) == false) {
                throw new CommandExecutionException(MessageFormat.format(
                        "cannot abort transaction \"{0}\" (see the above log message)",
                        id));
            }
        } catch (IOException | InterruptedException e) {
            throw new CommandExecutionException(MessageFormat.format(
                    "cannot abort transaction \"{0}\" (see the causal exception message)",
                    id), e);
        }
    }
}
