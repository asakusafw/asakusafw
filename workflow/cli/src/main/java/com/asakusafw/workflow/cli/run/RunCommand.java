/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.workflow.cli.run;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.jcommander.CommandBuilder;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.CommandExecutionException;
import com.asakusafw.utils.jcommander.common.CommandProvider;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.asakusafw.workflow.cli.common.ExecutionContextParameter;
import com.asakusafw.workflow.cli.common.WorkflowParameter;
import com.asakusafw.workflow.executor.BatchExecutor;
import com.asakusafw.workflow.executor.ExecutionConditionException;
import com.asakusafw.workflow.executor.ExecutionContext;
import com.asakusafw.workflow.model.BatchInfo;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * Runs a batch application.
 * @since 0.10.0
 */
@Parameters(
        commandNames = { "run" },
        commandDescriptionKey = "command.run",
        resourceBundle = "com.asakusafw.workflow.cli.jcommander"
)
public class RunCommand implements Runnable, CommandProvider {

    static final Logger LOG = LoggerFactory.getLogger(RunCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final ExecutionContextParameter contextParameter = new ExecutionContextParameter();

    @ParametersDelegate
    final WorkflowParameter workflowParameter = new WorkflowParameter();

    @ParametersDelegate
    final ExecutorParameter executorParameter = new ExecutorParameter();

    @ParametersDelegate
    final ApplicationParameter applicationParameter = new ApplicationParameter();

    @Override
    public void run() {
        LOG.debug("start run command");
        ExecutionContext context = contextParameter.getExecutionContext();
        BatchInfo workflow = workflowParameter.getBatchInfo(context);
        BatchExecutor executor = executorParameter.getBatchExecutor(context);
        Map<String, String> arguments = applicationParameter.getBatchArguments();
        try {
            executor.execute(context, workflow, arguments);
        } catch (ExecutionConditionException e) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "cannot execute batch \"{0}\" ({1})",
                    workflow.getId(),
                    e.getMessage()), e);
        } catch (IOException | InterruptedException e) {
            throw new CommandExecutionException(MessageFormat.format(
                    "executing batch \"{0}\" was failed",
                    workflow.getId()), e);
        }
    }

    @Override
    public void accept(CommandBuilder<Runnable> builder) {
        builder.addCommand(new RunCommand());
    }
}
