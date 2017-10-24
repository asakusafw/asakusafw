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
package com.asakusafw.info.cli.list;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.JobflowInfo;
import com.asakusafw.info.cli.common.JobflowInfoParameter;
import com.asakusafw.info.windgate.WindGateIoAttribute;
import com.asakusafw.info.windgate.WindGateOutputInfo;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing list of WindGate outputs.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "output",
        commandDescription = "Displays WindGate output definitions."
)
public class ListWindGateOutputCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(ListWindGateOutputCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final JobflowInfoParameter jobflowInfoParameter = new JobflowInfoParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());
        try (PrintWriter writer = outputParameter.open()) {
            List<JobflowInfo> jobflows = jobflowInfoParameter.getJobflows();
            jobflows.stream()
                    .flatMap(jobflow -> jobflow.getAttributes().stream())
                    .filter(it -> it instanceof WindGateIoAttribute)
                    .map(it -> (WindGateIoAttribute) it)
                    .flatMap(it -> it.getOutputs().stream())
                    .sorted(Comparator.comparing(WindGateOutputInfo::getName))
                    .forEachOrdered(info -> ListWindGateInputCommand.print(
                            writer, info, verboseParameter.isRequired()));
        }
    }
}
