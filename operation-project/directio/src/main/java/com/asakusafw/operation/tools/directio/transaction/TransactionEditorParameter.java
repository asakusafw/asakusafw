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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operation.tools.directio.common.ConfigurationParameter;
import com.asakusafw.runtime.directio.hadoop.DirectIoTransactionEditor;
import com.asakusafw.runtime.directio.hadoop.DirectIoTransactionEditor.TransactionInfo;
import com.asakusafw.utils.jcommander.CommandExecutionException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * Handles parameters about Direct I/O transaction.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.operation.tools.directio.jcommander")
public class TransactionEditorParameter {

    static final Logger LOG = LoggerFactory.getLogger(TransactionEditorParameter.class);

    @ParametersDelegate
    final ConfigurationParameter configurationParameter = new ConfigurationParameter();

    private DirectIoTransactionEditor editor;

    /**
     * Returns the Hadoop configuration.
     * @return the Hadoop configuration
     */
    public Configuration getConfiguration() {
        return configurationParameter.getConfiguration();
    }

    /**
     * Returns the editor.
     * @return the editor
     */
    public DirectIoTransactionEditor getEditor() {
        if (editor == null) {
            LOG.debug("preparing transaction editor");
            editor = ReflectionUtils.newInstance(DirectIoTransactionEditor.class, getConfiguration());
        }
        return editor;
    }

    /**
     * Returns all transaction information.
     * @return transaction information
     */
    public List<TransactionInfo> list() {
        LOG.debug("loading all transaction info");
        try {
            return getEditor().list();
        } catch (IOException e) {
            throw new CommandExecutionException("error occurred while loading transactions", e);
        }
    }

    /**
     * Returns a transaction.
     * @param id the transaction ID
     * @return the corresponded transaction information
     */
    public Optional<TransactionInfo> find(String id) {
        LOG.debug("loading transaction info: {}", id);
        try {
            return Optional.ofNullable(getEditor().get(id));
        } catch (IOException e) {
            throw new CommandExecutionException(MessageFormat.format(
                    "error occurred while loading transaction: {0}",
                    id), e);
        }
    }
}
