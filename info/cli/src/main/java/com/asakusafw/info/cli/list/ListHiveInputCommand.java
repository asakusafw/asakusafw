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
package com.asakusafw.info.cli.list;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.JobflowInfo;
import com.asakusafw.info.cli.common.JobflowInfoParameter;
import com.asakusafw.info.hive.HiveIoAttribute;
import com.asakusafw.info.hive.HivePortInfo;
import com.asakusafw.info.hive.LocationInfo;
import com.asakusafw.info.hive.TableInfo;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing list of Hive inputs.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "input",
        commandDescriptionKey = "command.generate-list-hive-input",
        resourceBundle = "com.asakusafw.info.cli.jcommander"
)
public class ListHiveInputCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(ListHiveInputCommand.class);

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
                    .filter(it -> it instanceof HiveIoAttribute)
                    .map(it -> (HiveIoAttribute) it)
                    .flatMap(it -> it.getInputs().stream())
                    .sorted(Comparator.comparing(it -> it.getSchema().getName()))
                    .distinct()
                    .forEachOrdered(info -> print(writer, info, verboseParameter.isRequired()));
        }
    }

    static void print(PrintWriter writer, HivePortInfo info, boolean verbose) {
        LocationInfo location = info.getLocation();
        TableInfo schema = info.getSchema();
        if (verbose) {
            Map<String, Object> members = new LinkedHashMap<>();
            members.put("port-name", info.getName());
            members.put("description", info.getDescriptionClass());
            members.put("base-path", location.getBasePath());
            members.put("resource-pattern", location.getResourcePattern());
            members.put("columns", schema.getColumns());
            members.put("row-format", schema.getRowFormat());
            members.put("storage-format", schema.getStorageFormat());
            members.put("properties", schema.getProperties());
            members.put("comment", schema.getComment());
            writer.printf("%s:%n", schema.getName());
            ListUtil.printBlock(writer, 4, members);
        } else {
            writer.printf("%s%n", schema.getName());
        }
    }
}
