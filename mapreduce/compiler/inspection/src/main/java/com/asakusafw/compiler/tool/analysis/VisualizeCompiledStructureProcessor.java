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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.processor.JobFlowWorkDescriptionProcessor;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Reduce;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Stage;
import com.asakusafw.compiler.flow.stage.StageModel.Factor;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.MapUnit;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.vocabulary.batch.WorkDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription.Declaration;

/**
 * Visualizes compiled workflow.
 * @since 0.2.6
 */
public class VisualizeCompiledStructureProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(VisualizeCompiledStructureProcessor.class);

    static final Charset ENCODING = StandardCharsets.UTF_8;

    /**
     * Output path.
     */
    public static final String NAIVE_PATH = Constants.PATH_BATCH + "compiled-structure.dot"; //$NON-NLS-1$

    /**
     * Output path.
     */
    public static final String MERGED_PATH = Constants.PATH_BATCH + "compiled-merged-structure.dot"; //$NON-NLS-1$

    @Override
    public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
        List<Class<? extends WorkDescriptionProcessor<?>>> results = new ArrayList<>();
        results.add(JobFlowWorkDescriptionProcessor.class);
        return results;
    }

    @Override
    public void process(Workflow workflow) throws IOException {
        process(workflow, NAIVE_PATH, false);
        process(workflow, MERGED_PATH, true);
    }

    void process(Workflow workflow, String path, boolean merged) throws IOException {
        try (OutputStream output = getEnvironment().openResource(path);
                Context context = new Context(output, merged)) {
            context.put("digraph {"); //$NON-NLS-1$
            context.push();
            context.put("rankdir = LR;"); //$NON-NLS-1$
            Class<? extends BatchDescription> desc = workflow.getDescription().getClass();
            String batchId = context.label(desc, "Batch", //$NON-NLS-1$
                    getEnvironment().getConfiguration().getBatchId());
            dump(context, batchId, workflow.getGraph());
            context.pop();
            context.put("}"); //$NON-NLS-1$
            context.close();
        }
    }

    private void dump(Context context, String batchId, Graph<Workflow.Unit> graph) {
        assert context != null;
        assert graph != null;
        for (Workflow.Unit unit : Graphs.sortPostOrder(graph)) {
            String flowId = dumpUnit(context, unit);
            context.connect(batchId, flowId);
        }
    }

    private String dumpUnit(Context context, Workflow.Unit unit) {
        assert context != null;
        assert unit != null;
        WorkDescription desc = unit.getDescription();
        if (desc instanceof JobFlowWorkDescription) {
            return dumpDescription(
                    context,
                    (JobFlowWorkDescription) desc,
                    (JobflowModel) unit.getProcessed());
        } else {
            throw new AssertionError(desc);
        }
    }

    private String dumpDescription(
            Context context,
            JobFlowWorkDescription desc,
            JobflowModel model) {
        assert context != null;
        assert desc != null;
        assert model != null;
        String id = context.label(desc.getFlowClass(), "JobFlow", model.getFlowId()); //$NON-NLS-1$
        for (Stage stage : model.getStages()) {
            String stageId = dumpStage(context, stage);
            context.connect(id, stageId);
        }
        return id;
    }

    private String dumpStage(Context context, Stage stage) {
        assert context != null;
        assert stage != null;
        String id = context.label(stage, "Stage", //$NON-NLS-1$
                stage.getCompiled().getQualifiedName().toNameString());
        for (MapUnit unit : stage.getModel().getMapUnits()) {
            String unitId = context.label(unit, "Mapper", //$NON-NLS-1$
                    unit.getCompiled().getQualifiedName().toNameString());
            context.connect(id, unitId);
            for (Fragment fragment : unit.getFragments()) {
                String fragmentId = dumpFragment(context, fragment);
                context.connect(unitId, fragmentId);
            }
        }
        Reduce reducer = stage.getReduceOrNull();
        if (reducer != null) {
            String unitId = context.label(reducer, "Reducer", //$NON-NLS-1$
                    reducer.getReducerTypeName().toNameString());
            context.connect(id, unitId);
            for (ReduceUnit unit : stage.getModel().getReduceUnits()) {
                for (Fragment fragment : unit.getFragments()) {
                    String fragmentId = dumpFragment(context, fragment);
                    context.connect(unitId, fragmentId);
                }
            }
        }
        return id;
    }

    private String dumpFragment(Context context, Fragment fragment) {
        assert context != null;
        assert fragment != null;
        String id = context.label(fragment, "Fragment", //$NON-NLS-1$
                fragment.getCompiled().getQualifiedName().toNameString());
        for (Factor factor : fragment.getFactors()) {
            String factorId = dumpFactor(context, factor);
            if (factorId != null) {
                context.connect(id, factorId);
            }
        }
        return id;
    }

    private String dumpFactor(Context context, Factor factor) {
        FlowElementDescription desc = factor.getElement().getDescription();
        switch (desc.getKind()) {
        case OPERATOR:
            Declaration decl = ((OperatorDescription) desc).getDeclaration();
            if (decl.getDeclaring().getName().startsWith("com.asakusafw.vocabulary.") == false) { //$NON-NLS-1$
                String id = context.label(
                        decl.toMethod(),
                        decl.getAnnotationType().getSimpleName(),
                        MessageFormat.format(
                                "{0}#{1}", //$NON-NLS-1$
                                decl.getDeclaring().getSimpleName(),
                                decl.toMethod().getName()));
                return id;
            }
            return null;
        case FLOW_COMPONENT:
        case INPUT:
        case OUTPUT:
        case PSEUD:
            return null;
        default:
            throw new AssertionError();
        }
    }

    private static class Context implements Closeable {

        private final PrintWriter writer;

        private final boolean merged;

        private final Map<Object, String> ids = new HashMap<>();

        private final Set<String> sawConnections = new HashSet<>();

        private int indent = 0;

        Context(OutputStream output, boolean merged) {
            assert output != null;
            this.writer = new PrintWriter(new OutputStreamWriter(output, ENCODING));
            this.merged = merged;
        }

        void push() {
            indent++;
        }

        void pop() {
            if (indent == 0) {
                throw new IllegalStateException();
            }
            indent--;
        }

        String label(Object source, String kind, String detail) {
            if (merged == false) {
                return newLabel(kind, detail);
            } else {
                String id = ids.get(source);
                if (id == null) {
                    id = newLabel(kind, detail);
                    ids.put(source, id);
                }
                return id;
            }
        }

        String newLabel(String kind, String detail) {
            String id = UUID.randomUUID().toString();
            put("\"{0}\" [shape=box, label=\"{1}\\n{2}\"];", id, kind, detail); //$NON-NLS-1$
            return id;
        }

        void connect(String src, String dst) {
            if (merged) {
                String id = src + '|' + dst;
                if (sawConnections.contains(id) == false) {
                    sawConnections.add(id);
                    put("\"{0}\" -> \"{1}\";", src, dst); //$NON-NLS-1$
                }
            } else {
                put("\"{0}\" -> \"{1}\";", src, dst); //$NON-NLS-1$
            }
        }

        void put(String pattern, Object... arguments) {
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
