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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.processor.JobFlowWorkDescriptionProcessor;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Export;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Import;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Stage;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Factor;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.MapUnit;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.vocabulary.batch.WorkDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;

/**
 * Describes compiled workflow.
 * @since 0.2.6
 */
public class DescribeCompiledStructureProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(DescribeCompiledStructureProcessor.class);

    static final Charset ENCODING = StandardCharsets.UTF_8;

    /**
     * Output path.
     */
    public static final String PATH = Constants.PATH_BATCH + "compiled-structure.txt"; //$NON-NLS-1$

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

        context.put("stages:"); //$NON-NLS-1$
        context.push();
        writeBody(context, model);
        context.pop();

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

    private void writeBody(Context context, JobflowModel model) {
        assert model != null;
        context.put("prologue:"); //$NON-NLS-1$
        context.push();
        writeCompiledStages(context, model.getCompiled().getPrologueStages());
        context.pop();

        context.put("main:"); //$NON-NLS-1$
        context.push();
        writeStages(context, model);
        context.pop();

        context.put("epilogue:"); //$NON-NLS-1$
        context.push();
        writeCompiledStages(context, model.getCompiled().getEpilogueStages());
        context.pop();
    }

    private void writeStages(Context context, JobflowModel model) {
        Graph<Stage> predGraph = model.getDependencyGraph();
        Graph<Stage> succGraph = Graphs.transpose(predGraph);
        for (Stage stage : model.getStages()) {
            context.put("stage: {0}", stage.getCompiled().getQualifiedName().toNameString()); //$NON-NLS-1$
            context.push();
            for (Stage pred : sort(predGraph.getConnected(stage))) {
                context.put("predecessor: {0}", pred.getCompiled().getQualifiedName().toNameString()); //$NON-NLS-1$
            }
            for (Stage succ : sort(succGraph.getConnected(stage))) {
                context.put("successor: {0}", succ.getCompiled().getQualifiedName().toNameString()); //$NON-NLS-1$
            }
            writeStageBody(context, stage);
            context.pop();
        }
    }

    private List<Stage> sort(Set<Stage> stages) {
        List<Stage> results = new ArrayList<>();
        Collections.sort(results, (o1, o2) -> o1.getCompiled().getStageId().compareTo(o2.getCompiled().getStageId()));
        return results;
    }

    private void writeStageBody(Context context, Stage stage) {
        StageModel model = stage.getModel();

        for (MapUnit unit : model.getMapUnits()) {
            context.put("mapper: {0}", name(unit.getCompiled().getQualifiedName())); //$NON-NLS-1$
            context.push();
            writeFragments(context, unit.getFragments());
            context.pop();
        }
        if (stage.getReduceOrNull() != null) {
            context.put("reducer: {0}", name(stage.getReduceOrNull().getReducerTypeName())); //$NON-NLS-1$
            context.push();
            for (ReduceUnit unit : model.getReduceUnits()) {
                writeFragments(context, unit.getFragments());
            }
            context.pop();
        }
    }

    private String name(Name name) {
        if (name == null) {
            return "N/A"; //$NON-NLS-1$
        }
        return name.toNameString();
    }

    private void writeFragments(Context context, List<Fragment> fragments) {
        for (Fragment fragment : fragments) {
            context.put("fragment: {0}", name(fragment.getCompiled().getQualifiedName())); //$NON-NLS-1$
            context.push();
            for (Factor factor : fragment.getFactors()) {
                FlowElementDescription description = factor.getElement().getDescription();
                if (description.getKind() != FlowElementKind.PSEUD) {
                    context.put("{0}: {1}", description.getKind().name().toLowerCase(), description); //$NON-NLS-1$
                    context.push();
                    for (FlowResourceDescription resource : factor.getElement().getDescription().getResources()) {
                        for (InputDescription input : resource.getSideDataInputs()) {
                            context.put("side-data: {0} ({1})", //$NON-NLS-1$
                                    input.getName(),
                                    input.getImporterDescription().getClass().getName());
                        }
                    }
                    context.pop();
                }
            }
            context.pop();
        }
    }

    private void writeCompiledStages(Context context, List<CompiledStage> stages) {
        for (CompiledStage stage : stages) {
            context.put("stage: {0}", stage.getQualifiedName().toNameString()); //$NON-NLS-1$
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
