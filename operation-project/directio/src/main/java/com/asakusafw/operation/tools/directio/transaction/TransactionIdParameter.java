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

import java.util.Optional;

import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.beust.jcommander.Parameter;

/**
 * Handles parameters about transaction ID.
 * @since 0.10.0
 */
public class TransactionIdParameter {

    @Parameter(
            description = "execution-ID",
            required = false)
    String transactionId;

    /**
     * Returns the transaction ID.
     * @return the transaction ID
     */
    public String getId() {
        return Optional.ofNullable(transactionId)
                .orElseThrow(() -> new CommandConfigurationException(
                        "transaction ID (execution ID of the target jobflow) must be specified"));
    }
}
