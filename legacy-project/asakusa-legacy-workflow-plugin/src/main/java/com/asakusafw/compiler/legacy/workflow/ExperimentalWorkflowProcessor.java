/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.compiler.legacy.workflow;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.processor.JobFlowWorkDescriptionProcessor;
import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.Command;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Stage;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.vocabulary.batch.WorkDescription;

/**
 * ワークフローの情報を実験用のシェルスクリプトの形式で残す。
 * @deprecated Use YAESS instead
 */
@Deprecated
public class ExperimentalWorkflowProcessor extends AbstractWorkflowProcessor {

    /**
     * バッチ変数表の環境変数。
     * <p>
     * {@link VariableTable#toSerialString()}の形式で指定すること。
     * </p>
     */
    public static final String VAR_BATCH_ARGS = "ASAKUSA_BATCH_ARGS";

    static final Logger LOG = LoggerFactory.getLogger(ExperimentalWorkflowProcessor.class);

    static final Charset ENCODING = Charset.forName("UTF-8");

    // see
    // man sh > QUOTING
    // man bash > QUOTING
    // $, `, ", \, or <newline>
    private static final Pattern SH_METACHARACTERS = Pattern.compile("[\\$`\"\\\\\n]");

    private static final String CMD_HADOOP_JOB = "experimental/bin/hadoop_job_run.sh";

    private static final String CMD_CLEANER = "experimental/bin/clean_hadoop_work.sh";

    private static final String VAR_HOME = "ASAKUSA_HOME";

    private static final String VAR_BATCH_ID = "_BATCH_ID";

    private static final String VAR_FLOW_ID = "_FLOW_ID";

    private static final String VAR_EXECUTION_ID = "_EXECUTION_ID";

    private static final String EXPR_EXECUTION_ID = "$" + VAR_EXECUTION_ID;

    private static final String EXPR_BATCH_ARGS = "$" + VAR_BATCH_ARGS;

    private static final String PREFIX_APP_HOME = MessageFormat.format("${0}/", VAR_HOME);

    private static final String JOBFLOW_LIB_SOURCE = JobFlowWorkDescriptionProcessor.JOBFLOW_PACKAGE;

    private static final String JOBFLOW_LIB_DEST = MessageFormat.format(
            "${0}/batchapps/${1}/lib",
            VAR_HOME,
            VAR_BATCH_ID);

    /**
     * 出力先のパス。
     */
    public static final String PATH = "bin/experimental.sh";

    /**
     * Hadoop実行時の追加引数を指定するための環境変数名。
     */
    public static final String K_OPTS = "EXPERIMENTAL_OPTS";

    /**
     * 実験用のシェルスクリプトの出力先を返す。
     * @param outputDir コンパイル結果の出力先ディレクトリ
     * @return 実験用のシェルスクリプトの出力先
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static File getScriptOutput(File outputDir) {
        Precondition.checkMustNotBeNull(outputDir, "outputDir"); //$NON-NLS-1$
        return new File(outputDir, PATH);
    }

    @Override
    public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
        List<Class<? extends WorkDescriptionProcessor<?>>> results = Lists.create();
        results.add(JobFlowWorkDescriptionProcessor.class);
        return results;
    }

    @Override
    public void process(Workflow workflow) throws IOException {
        OutputStream output = getEnvironment().openResource(PATH);
        try {
            Context context = new Context(output);
            context.put("#!/bin/bash");
            context.put("");

            context.put("### Move to the working directory");
            context.put("echo \"Moving to ''$(dirname $(dirname $0))''\"");
            context.put("pushd $(dirname $(dirname $0)) > /dev/null");
            context.put("");

            String batchId = getEnvironment().getConfiguration().getBatchId();
            context.put("### Batch - {0}", batchId);
            context.put("echo \"Processing batch {0}\"", toLiteral(batchId));
            context.put("");
            dump(context, workflow.getGraph());

            context.put("### Return to the original directory");
            context.put("echo \"Moving back to the original directory\"");
            context.put("popd > /dev/null");
            context.put("");
            context.put("echo \"Finished: SUCCESS\"");

            context.close();
        } finally {
            output.close();
        }
    }

    private void dump(Context context, Graph<Workflow.Unit> graph) {
        assert context != null;
        assert graph != null;
        for (Workflow.Unit unit : Graphs.sortPostOrder(graph)) {
            dumpUnit(context, unit);
        }
    }

    private void dumpUnit(Context context, Workflow.Unit unit) {
        assert context != null;
        assert unit != null;
        WorkDescription desc = unit.getDescription();
        if (desc instanceof JobFlowWorkDescription) {
            dumpDescription(
                    context,
                    (JobFlowWorkDescription) desc,
                    (JobflowModel) unit.getProcessed());
        } else {
            throw new AssertionError(desc);
        }
    }

    private void dumpDescription(
            Context context,
            JobFlowWorkDescription desc,
            JobflowModel model) {
        assert context != null;
        assert desc != null;
        assert model != null;

        context.put("## Jobflow - {0}", model.getFlowId());
        context.put("echo \"Processing jobflow ''{0}''\"", model.getFlowId());

        context.put("# Initialize this jobflow");
        context.put("{0}=$(uuidgen)",
                VAR_EXECUTION_ID);
        context.put("{0}={1}",
                VAR_BATCH_ID,
                toLiteral(model.getBatchId()));
        context.put("{0}={1}",
                VAR_FLOW_ID,
                toLiteral(model.getFlowId()));
        context.put("echo \"{0}=${0}\"", VAR_EXECUTION_ID);
        context.put("echo \"{0}=${0}\"", VAR_BATCH_ID);
        context.put("echo \"{0}=${0}\"", VAR_FLOW_ID);
        context.put("");

        context.put("# Deploy this jobflow");
        context.put("echo \"Deploying ''{0}/{1}'' into ''{2}''\"",
                JOBFLOW_LIB_SOURCE,
                Naming.getJobflowClassPackageName(model.getFlowId()),
                JOBFLOW_LIB_DEST);
        context.put("mkdir -p {0}",
                quote(JOBFLOW_LIB_DEST));
        context.put("cp {0}/{1} {2}",
                quote(JOBFLOW_LIB_SOURCE),
                toLiteral(Naming.getJobflowClassPackageName(model.getFlowId())),
                quote(JOBFLOW_LIB_DEST));
        context.put("");

        dumpInitializer(context, model);
        dumpImporter(context, model);
        Graph<Stage> graph = model.getDependencyGraph();
        for (CompiledStage stage : model.getCompiled().getPrologueStages()) {
            dumpStage(context, model, stage);
        }
        for (Stage stage : Graphs.sortPostOrder(graph)) {
            dumpStage(context, model, stage.getCompiled());
        }
        for (CompiledStage stage : model.getCompiled().getEpilogueStages()) {
            dumpStage(context, model, stage);
        }
        dumpExporter(context, model);
        dumpCleaner(context, model);
        dumpFinalizer(context, model, "");
        context.put("");
    }

    private void dumpImporter(Context context, JobflowModel model) {
        assert context != null;
        assert model != null;
        List<ExternalIoCommandProvider> providers = model.getCompiled().getCommandProviders();
        CommandContext cmdContext = createContext(model);
        for (ExternalIoCommandProvider provider : providers) {
            List<Command> commands = provider.getImportCommand(cmdContext);
            for (Command cmd : commands) {
                context.put("# Import by {0}", provider.getName());
                context.put("echo \"Processing importer sequence by {0}\"", provider.getName());
                dumpRun(context, model, cmd.getCommandLineString());
            }
        }
    }

    private CommandContext createContext(JobflowModel model) {
        assert model != null;
        return new CommandContext(
                quote(PREFIX_APP_HOME),
                quote(EXPR_EXECUTION_ID),
                quote(EXPR_BATCH_ARGS));
    }

    private void dumpExporter(Context context, JobflowModel model) {
        assert context != null;
        assert model != null;
        List<ExternalIoCommandProvider> providers = model.getCompiled().getCommandProviders();
        CommandContext cmdContext = createContext(model);
        for (ExternalIoCommandProvider provider : providers) {
            List<Command> commands = provider.getExportCommand(cmdContext);
            for (Command cmd : commands) {
                context.put("# Export by {0}", provider.getName());
                context.put("echo \"Processing exporter sequence by {0}\"", provider.getName());
                dumpRun(context, model, cmd.getCommandLineString());
            }
        }
    }

    private void dumpStage(Context context, JobflowModel model, CompiledStage stage) {
        assert context != null;
        assert model != null;
        if (stage.getQualifiedName() == null) {
            return;
        }
        String batchId = model.getBatchId();
        String flowId = model.getFlowId();
        String stageId = stage.getStageId();
        context.put("# Hadoop Stage - {0}",
                stage.getQualifiedName().toNameString());
        context.put("echo \"Processing hadoop job ''{0}''\"",
                StageConstants.getDefinitionId(batchId, flowId, stageId));
        dumpRun(context, model, toHadoopJob(model, stage));
    }

    private void dumpRun(
            Context context,
            JobflowModel modelOrNull,
            String pattern,
            Object... arguments) {
        assert context != null;
        assert pattern != null;
        assert arguments != null;
        String command;
        if (arguments.length == 0) {
            command = pattern;
        } else {
            command = MessageFormat.format(pattern, arguments);
        }
        context.put("pushd \"${0}\" > /dev/null", VAR_HOME);
        context.put("{0}", command);
        context.put("_RET=$?");
        context.put("popd > /dev/null");
        context.put("if [ $_RET -ne 0 ]; then");
        context.put("    echo \"Invalid return code=$_RET, from ''{0}''\"", command);
        if (modelOrNull != null) {
            dumpFinalizer(context, modelOrNull, "    ");
        }
        context.put("    echo \"Finished: FAILURE\"");
        context.put("    popd > /dev/null");
        context.put("    exit \"$_RET\"");
        context.put("fi");
        context.put("");
    }

    private void dumpInitializer(Context context, JobflowModel model) {
        assert context != null;
        assert model != null;
        List<ExternalIoCommandProvider> providers = model.getCompiled().getCommandProviders();
        CommandContext cmdContext = createContext(model);
        for (ExternalIoCommandProvider provider : providers) {
            List<Command> commands = provider.getInitializeCommand(cmdContext);
            for (Command cmd : commands) {
                context.put("# Initializer by {0}", provider.getName());
                context.put("echo \"Processing {1} initializer sequence by {0}\"",
                        provider.getName(),
                        model.getFlowId());
                dumpRun(context, model, cmd.getCommandLineString());
            }
        }
    }

    private void dumpCleaner(Context context, JobflowModel model) {
        assert context != null;
        assert model != null;
        VariableTable variables = new VariableTable();
        variables.defineVariable(StageConstants.VAR_USER, "$USER");
        variables.defineVariable(StageConstants.VAR_BATCH_ID, "$" + VAR_BATCH_ID);
        variables.defineVariable(StageConstants.VAR_FLOW_ID, "$" + VAR_FLOW_ID);
        variables.defineVariable(StageConstants.VAR_EXECUTION_ID, EXPR_EXECUTION_ID);
        String path = getEnvironment().getConfiguration().getRootLocation().toPath('/');
        try {
            String parsed = variables.parse(path, true);
            context.put("# Cleaner");
            context.put("echo \"cleaning job temporary resources\"");
            context.put("{0} {1} {2} {3} {4} {5}",
                    quote(PREFIX_APP_HOME + CMD_CLEANER),
                    quote(parsed),
                    quote(model.getBatchId()),
                    quote(model.getFlowId()),
                    quote(EXPR_EXECUTION_ID),
                    quote(EXPR_BATCH_ARGS));
            context.put("_RET=$?");
            context.put("if [ $_RET -ne 0 ]; then");
            context.put("    echo \"WARNING: Invalid return code=$_RET, from cleaner ''{0}''\"", parsed);
            context.put("fi");
        } catch (IllegalArgumentException e) {
            LOG.warn(MessageFormat.format(
                    "出力先パス{0}を解釈できませんでした",
                    path), e);
        }
    }

    private void dumpFinalizer(Context context, JobflowModel model, String indent) {
        assert context != null;
        assert model != null;
        List<ExternalIoCommandProvider> providers = model.getCompiled().getCommandProviders();
        CommandContext cmdContext = createContext(model);
        for (ExternalIoCommandProvider provider : providers) {
            List<Command> commands = provider.getFinalizeCommand(cmdContext);
            for (Command cmd : commands) {
                context.put("{0}# Finalizer by {1}",
                        indent,
                        provider.getName());
                context.put("{0}echo \"Processing {2} finalizer sequence by {1}\"",
                        indent,
                        provider.getName(),
                        model.getFlowId());
                context.put("{0}pushd \"${1}\" > /dev/null", indent, VAR_HOME);
                context.put("{0}{1}", indent, cmd.getCommandLineString());
                context.put("{0}popd > /dev/null", indent);
            }
        }
    }

    private String toHadoopJob(JobflowModel model, CompiledStage stage) {
        assert model != null;
        assert stage != null;
        return MessageFormat.format(
                "{0} {1} {2}/{3} -D {4}=\"${5}\" -D {6}=\"$USER\" {7} ${8}",
                CMD_HADOOP_JOB,
                toLiteral(stage.getQualifiedName().toNameString()),
                quote(JOBFLOW_LIB_DEST),
                toLiteral(Naming.getJobflowClassPackageName(model.getFlowId())),
                toLiteral(StageConstants.PROP_EXECUTION_ID),
                VAR_EXECUTION_ID,
                toLiteral(StageConstants.PROP_USER),
                getPluginProperties(),
                K_OPTS);
    }

    private String getPluginProperties() {
        return join(" ", new String[] {
                "-D",
                MessageFormat.format("{0}={1}",
                        toLiteral(StageConstants.PROP_ASAKUSA_BATCH_ARGS),
                        quote(EXPR_BATCH_ARGS))
        });
    }

    private String quote(String string) {
        assert string != null;
        return '"' + string + '"';
    }

    private String toLiteral(String string) {
        assert string != null;
        return quote(escape(string));
    }

    private String escape(String string) {
        assert string != null;
        String replaced = SH_METACHARACTERS.matcher(string).replaceAll("\\\\$0");
        return replaced;
    }

    private String join(String delim, String[] values) {
        if (values.length == 0) {
            return "";
        } else if (values.length == 1) {
            return values[0];
        }
        StringBuilder buf = new StringBuilder();
        buf.append(values[0]);
        for (int i = 1; i < values.length; i++) {
            buf.append(delim);
            buf.append(values[i]);
        }
        return buf.toString();
    }

    private static class Context implements Closeable {

        private final PrintWriter writer;

        public Context(OutputStream output) {
            assert output != null;
            writer = new PrintWriter(new OutputStreamWriter(output, ENCODING));
        }

        public void put(String pattern, Object... arguments) {
            assert pattern != null;
            assert arguments != null;
            String text;
            if (arguments.length == 0) {
                text = pattern;
            } else {
                text = MessageFormat.format(pattern, arguments);
            }
            writer.println(text);
            LOG.debug(text);
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
