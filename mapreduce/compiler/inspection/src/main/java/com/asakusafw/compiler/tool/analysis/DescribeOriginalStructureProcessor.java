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
package com.asakusafw.compiler.tool.analysis;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.processor.JobFlowWorkDescriptionProcessor;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Export;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Import;
import com.asakusafw.compiler.flow.plan.FlowGraphUtil;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.vocabulary.batch.WorkDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;

/**
 * Describes raw workflow.
 * @since 0.2.6
 */
public class DescribeOriginalStructureProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(DescribeOriginalStructureProcessor.class);

    static final Charset ENCODING = StandardCharsets.UTF_8;

    /**
     * Output path.
     */
    public static final String PATH = Constants.PATH_BATCH + "original-structure.txt"; //$NON-NLS-1$

    @Override
    public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
        List<Class<? extends WorkDescriptionProcessor<?>>> results = new ArrayList<>();
        results.add(JobFlowWorkDescriptionProcessor.class);
        return results;
    }

    @Override
    public void process(Workflow workflow) throws IOException {
        try (OutputStream output = getEnvironment().openResource(PATH);
                Context context = new Context(output)) {
            context.put("batch: {0}", getEnvironment().getConfiguration().getBatchId()); //$NON-NLS-1$
            dump(context, workflow.getGraph());
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

        context.put("flow: {0}", model.getFlowId()); //$NON-NLS-1$
        context.push();

        context.put("input:"); //$NON-NLS-1$
        context.push();
        writeInput(context, model);
        context.pop();

        context.put("output:"); //$NON-NLS-1$
        context.push();
        writeOutput(context, model);
        context.pop();

        writeFlow(context, model.getStageGraph().getInput().getSource().getOrigin());

        context.pop();
    }

    private void writeFlow(Context context, FlowGraph flow) {
        context.put("flow: {0}", flow.getDescription().getName()); //$NON-NLS-1$
        context.push();
        for (FlowElement element : FlowGraphUtil.collectElements(flow)) {
            FlowElementDescription desc = element.getDescription();
            switch (desc.getKind()) {
            case INPUT:
            case OUTPUT:
            case OPERATOR:
                context.put("{0}: {1}", desc.getKind().name().toLowerCase(), desc.toString()); //$NON-NLS-1$
                break;
            case FLOW_COMPONENT:
                writeFlow(context, ((FlowPartDescription) desc).getFlowGraph());
                break;
            case PSEUD:
                break;
            default:
                throw new AssertionError();
            }
        }
        context.pop();
    }

    private void writeInput(Context context, JobflowModel model) {
        for (Import ext : model.getImports()) {
            context.put("{0} ({1})", //$NON-NLS-1$
                    ext.getDescription().getName(),
                    ext.getDescription().getImporterDescription().getClass().getName());
        }
    }

    private void writeOutput(Context context, JobflowModel model) {
        for (Export ext : model.getExports()) {
            context.put("{0} ({1})", //$NON-NLS-1$
                    ext.getDescription().getName(),
                    ext.getDescription().getExporterDescription().getClass().getName());
        }
    }

    private static class Context implements Closeable {

        private final PrintWriter writer;

        private int indent = 0;

        Context(OutputStream output) {
            assert output != null;
            writer = new PrintWriter(new OutputStreamWriter(output, ENCODING));
        }

        public void push() {
            indent++;
        }

        public void pop() {
            if (indent == 0) {
                throw new IllegalStateException();
            }
            indent--;
        }

        public void put(String pattern, Object... arguments) {
            assert pattern != null;
            assert arguments != null;
            StringBuilder buf = new StringBuilder();
            for (int i = 0, n = indent; i < n; i++) {
                buf.append("    "); //$NON-NLS-1$
            }
            if (arguments.length == 0) {
                buf.append(pattern);
            } else {
                buf.append(MessageFormat.format(pattern, arguments));
            }
            String text = buf.toString();
            writer.println(text);
            LOG.debug(text);
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
