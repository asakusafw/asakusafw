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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.BatchInfo;
import com.asakusafw.info.cli.common.ApplicationBaseDirectoryParameter;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A command for printing list of batch.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "batch",
        commandDescriptionKey = "command.generate-list-batch",
        resourceBundle = "com.asakusafw.info.cli.jcommander"
)
public class ListBatchCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(ListBatchCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final ApplicationBaseDirectoryParameter batchappsParameter = new ApplicationBaseDirectoryParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());
        try (PrintWriter writer = outputParameter.open()) {
            List<Path> applications = batchappsParameter.getEntries();
            if (verboseParameter.isRequired()) {
                ObjectMapper mapper = new ObjectMapper();
                for (Path batchapp : applications) {
                    Path infoFile = ApplicationBaseDirectoryParameter.findInfo(batchapp).get();
                    try {
                        BatchInfo info = mapper.readValue(infoFile.toFile(), BatchInfo.class);
                        Map<String, Object> members = new LinkedHashMap<>();
                        members.put("class", info.getDescriptionClass());
                        members.put("comment", info.getComment());
                        writer.printf("%s:%n", info.getId());
                        ListUtil.printBlock(writer, 4, members);
                    } catch (IOException e) {
                        LOG.error("error occurred while loading batch information: {}", infoFile, e);
                    }
                }
            } else {
                applications.forEach(it -> writer.println(ListUtil.getName(it)));
            }
        }
    }
}
