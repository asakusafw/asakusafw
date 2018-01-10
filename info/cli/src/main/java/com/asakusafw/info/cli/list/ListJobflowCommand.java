/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.info.cli.list;

import java.io.PrintWriter;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.BatchInfo;
import com.asakusafw.info.JobflowInfo;
import com.asakusafw.info.cli.common.BatchInfoParameter;
import com.asakusafw.info.task.TaskListAttribute;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing list of jobflow.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "jobflow",
        commandDescriptionKey = "command.generate-list-jobflow",
        resourceBundle = "com.asakusafw.info.cli.jcommander"
)
public class ListJobflowCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(ListJobflowCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final BatchInfoParameter batchInfoParameter = new BatchInfoParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());
        try (PrintWriter writer = outputParameter.open()) {
            BatchInfo info = batchInfoParameter.load();
            if (verboseParameter.isRequired()) {
                for (JobflowInfo jobflow : info.getJobflows()) {
                    writer.printf("%s (%s):%n",
                            jobflow.getId(),
                            ListUtil.normalize(jobflow.getDescriptionClass()));
                    ListUtil.printBlock(
                            writer,
                            4,
                            "blockers",
                            jobflow.getBlockerIds().stream()
                                .sorted()
                                .collect(Collectors.toList()));
                    jobflow.findAttribute(TaskListAttribute.class)
                        .map(TaskListAttribute::getPhases)
                        .ifPresent(phases -> phases.forEach((phase, tasks) -> {
                            ListUtil.printBlock(
                                    writer,
                                    4,
                                    phase.getSymbol(),
                                    tasks.stream()
                                        .map(it -> String.format(
                                                "%s (@%s)",
                                                it.getModuleName(),
                                                ListUtil.normalize(it.getProfileName())))
                                        .collect(Collectors.toList()));
                        }));
                }
            } else {
                info.getJobflows().forEach(it -> writer.println(it.getId()));
            }
        }
    }
}
