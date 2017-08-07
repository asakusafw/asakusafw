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
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.BatchInfo;
import com.asakusafw.info.ParameterListAttribute;
import com.asakusafw.info.cli.common.BatchInfoParameter;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing list of batch parameters.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "parameter",
        commandDescription = "Displays list of batch parameters."
)
public class ListParameterCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(ListParameterCommand.class);

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
            ParameterListAttribute attr = info.findAttribute(ParameterListAttribute.class)
                    .orElseThrow(() -> new CommandConfigurationException(MessageFormat.format(
                            "there are no batch parameter information in {0} ({1})",
                            info.getId(),
                            ListUtil.normalize(info.getDescriptionClass()))));
            if (verboseParameter.isRequired()) {
                attr.getElements().forEach(it -> {
                    Map<String, Object> members = new LinkedHashMap<>();
                    members.put("comment", it.getComment());
                    members.put("pattern", it.getPattern());
                    members.put("mandatory", it.isMandatory());
                    writer.printf("%s:%n", it.getName());
                    ListUtil.printBlock(writer, 4, members);
                });
            } else {
                attr.getElements().forEach(it -> writer.println(it.getName()));
            }
        }
    }
}
