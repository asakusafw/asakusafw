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

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.directio.hadoop.DirectIoTransactionEditor.TransactionInfo;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing transaction information.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "show",
        commandDescription = "Displays incomplete Direct I/O transaction."
)
public class TransactionShowCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(TransactionShowCommand.class);

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

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());

        String id = transactionIdParameter.getId();
        TransactionInfo spec = transactionEditorParameter.find(id)
                .orElseThrow(() -> new CommandConfigurationException(MessageFormat.format(
                        "transaction \"{0}\" is not found (may be already completed)",
                        id)));

        try (PrintWriter writer = outputParameter.open()) {
            print(writer, spec, 0);
        }
    }

    static void print(PrintWriter writer, TransactionInfo tx, int indent) {
        writer.printf("%sID: %s%n", indent(indent), tx.getExecutionId());
        writer.printf("%sstatus: %s%n", indent(indent), tx.isCommitted() ? "Committed" : "Uncommitted");
        writer.printf("%sdate: %s%n", indent(indent), Instant.ofEpochMilli(tx.getTimestamp()));
        writer.printf("%scomment:%n", indent(indent));
        tx.getComment().forEach(s -> writer.printf("%s> %s%n", indent(indent), s));
    }

    private static CharSequence indent(int level) {
        if (level <= 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder(level);
        for (int i = 0; i < level; i++) {
            buf.append(' ');
        }
        return buf;
    }
}
