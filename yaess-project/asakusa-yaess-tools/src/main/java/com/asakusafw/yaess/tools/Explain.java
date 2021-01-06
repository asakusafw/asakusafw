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
package com.asakusafw.yaess.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.BatchScript;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.FlowScript;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * A YAESS Explain program main entry point.
 * @since 0.2.3
 */
public final class Explain {

    static final Logger LOG = LoggerFactory.getLogger(Explain.class);

    static final Option OPT_SCRIPT;

    private static final Options OPTIONS;
    static {
        OPT_SCRIPT = new Option("script", true, "script path");
        OPT_SCRIPT.setArgName("/path/to/script");
        OPT_SCRIPT.setRequired(true);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_SCRIPT);
    }

    private Explain() {
        return;
    }

    /**
     * Program entry.
     * @param args program arguments
     */
    public static void main(String... args) {
        int status = execute(args);
        System.exit(status);
    }

    static int execute(String[] args) {
        assert args != null;
        Configuration conf;
        try {
            conf = parseConfiguration(args);
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            Explain.class.getName()),
                    OPTIONS,
                    true);
            e.printStackTrace(System.out);
            return 1;
        }
        try {
            explainBatch(conf.script);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    private static void explainBatch(BatchScript script) throws IOException {
        assert script != null;
        JsonObject batch = analyzeBatch(script);
        Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
        Writer writer = new PrintWriter(new OutputStreamWriter(System.out, Charset.defaultCharset()));
        gson.toJson(batch, writer);
        writer.flush();
    }

    private static JsonObject analyzeBatch(BatchScript script) {
        assert script != null;
        JsonArray jobflows = new JsonArray();
        for (FlowScript flowScript : script.getAllFlows()) {
            JsonObject jobflow = analyzeJobflow(flowScript);
            jobflows.add(jobflow);
        }
        JsonObject batch = new JsonObject();
        batch.addProperty("id", script.getId());
        batch.add("jobflows", jobflows);
        return batch;
    }

    private static JsonObject analyzeJobflow(FlowScript flowScript) {
        assert flowScript != null;
        JsonArray phases = new JsonArray();
        for (Map.Entry<ExecutionPhase, Set<ExecutionScript>> entry : flowScript.getScripts().entrySet()) {
            ExecutionPhase phase = entry.getKey();
            if (entry.getValue().isEmpty() == false
                    || phase == ExecutionPhase.SETUP
                    || phase == ExecutionPhase.CLEANUP) {
                phases.add(new JsonPrimitive(phase.getSymbol()));
            }
        }
        JsonObject jobflow = new JsonObject();
        jobflow.addProperty("id", flowScript.getId());
        jobflow.add("blockers", toJsonArray(flowScript.getBlockerIds()));
        jobflow.add("phases", phases);
        return jobflow;
    }

    private static JsonArray toJsonArray(Collection<String> values) {
        assert values != null;
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(new JsonPrimitive(value));
        }
        return array;
    }

    static Configuration parseConfiguration(String[] args) throws ParseException {
        assert args != null;
        LOG.debug("Analyzing YAESS Explain arguments: {}", Arrays.toString(args));

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        String script = cmd.getOptionValue(OPT_SCRIPT.getOpt());
        LOG.debug("Script: {}", script);

        Configuration result = new Configuration();
        LOG.debug("Loading script: {}", script);
        try {
            Properties properties = CommandLineUtil.loadProperties(new File(script));
            result.script = BatchScript.load(properties);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid script \"{0}\".",
                    script), e);
        }

        LOG.debug("Analyzed YAESS Explain arguments");
        return result;
    }

    static final class Configuration {
        BatchScript script;
    }
}
