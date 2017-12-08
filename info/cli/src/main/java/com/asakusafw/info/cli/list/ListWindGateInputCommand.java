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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.JobflowInfo;
import com.asakusafw.info.cli.common.JobflowInfoParameter;
import com.asakusafw.info.windgate.WindGateInputInfo;
import com.asakusafw.info.windgate.WindGateIoAttribute;
import com.asakusafw.info.windgate.WindGatePortInfo;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing list of WindGate inputs.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "input",
        commandDescriptionKey = "command.generate-list-windgate-input",
        resourceBundle = "com.asakusafw.info.cli.jcommander"
)
public class ListWindGateInputCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(ListWindGateInputCommand.class);

    private static final Map<String, List<String>> RESOURCE_KEY_MAP;
    static {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("jdbc", Arrays.asList("table"));
        map.put("local", Arrays.asList("file"));
        RESOURCE_KEY_MAP = map;
    }

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
                    .flatMap(it -> it.getInputs().stream())
                    .sorted(Comparator.comparing(WindGateInputInfo::getName))
                    .forEachOrdered(info -> print(writer, info, verboseParameter.isRequired()));
        }
    }

    static void print(PrintWriter writer, WindGatePortInfo info, boolean verbose) {
        if (verbose) {
            Map<String, Object> members = new LinkedHashMap<>();
            members.put("profile-name", info.getProfileName());
            members.put("resource-name", info.getResourceName());
            members.putAll(info.getConfiguration());
            writer.printf("%s:%n", info.getDescriptionClass());
            ListUtil.printBlock(writer, 4, members);
        } else {
            writer.printf("%s::%s::%s%n", info.getProfileName(), info.getResourceName(), getResourceIdentifier(info));
        }
    }

    private static String getResourceIdentifier(WindGatePortInfo info) {
        return RESOURCE_KEY_MAP.getOrDefault(info.getResourceName(), Collections.emptyList()).stream()
                .map(info.getConfiguration()::get)
                .filter(it -> it != null)
                .findFirst()
                .orElse("N/A");
    }
}
