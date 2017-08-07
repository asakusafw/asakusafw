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
package com.asakusafw.info.cli.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.asakusafw.info.BatchInfo;
import com.asakusafw.info.JobflowInfo;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

/**
 * Provides the jobflow information.
 * @since 0.10.0
 */
public class JobflowInfoParameter {

    /**
     * The batch information.
     */
    @ParametersDelegate
    public final BatchInfoParameter batchInfoParameter = new BatchInfoParameter();

    /**
     * The flow ID.
     */
    @Parameter(
            names = { "--flow", "--jobflow" },
            description = "Target flow ID.",
            required = false
    )
    public String flowId;

    /**
     * Returns the flow ID.
     * @return the flow ID, or {@code null} if it is not specified
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * Returns the target jobflows.
     * @return the list of target jobflows
     * @throws CommandConfigurationException if there are no available jobflows
     */
    public List<JobflowInfo> getJobflows() {
        BatchInfo batch = batchInfoParameter.load();
        List<? extends JobflowInfo> candidates = batch.getJobflows();
        if (candidates.isEmpty()) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "there are no available jobflows in batch: {0}",
                    batch.getId()));
        }
        if (flowId == null) {
            return new ArrayList<>(candidates);
        }
        JobflowInfo filtered = candidates.stream()
            .filter(it -> Objects.equals(it.getId(), flowId))
            .findFirst()
            .orElseThrow(() -> new CommandConfigurationException(MessageFormat.format(
                    "there is no jobflow with flow ID \"{0}\", must be one of: '{'{1}'}'",
                    batch.getId(),
                    batch.getJobflows().stream()
                        .map(JobflowInfo::getId)
                        .sorted()
                        .collect(Collectors.joining(", ")))));
        return Collections.singletonList(filtered);
    }

    /**
     * Returns the target jobflow, and must be it is unique in the current context.
     * @return the target jobflow
     * @throws CommandConfigurationException if there are no available jobflows, or they are ambiguous
     */
    public JobflowInfo getUniqueJobflow() {
        List<JobflowInfo> candidates = getJobflows();
        if (candidates.size() != 1) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "target jobflow is ambiguous, please specify \"--jobflow <flow-ID>\": '{'{0}'}'",
                    candidates.stream()
                        .map(JobflowInfo::getId)
                        .collect(Collectors.joining(", "))));
        }
        return candidates.get(0);
    }
}
