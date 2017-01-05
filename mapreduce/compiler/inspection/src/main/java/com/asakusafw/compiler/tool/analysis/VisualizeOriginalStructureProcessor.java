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
import com.asakusafw.compiler.flow.plan.FlowGraphUtil;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.vocabulary.batch.WorkDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription.Declaration;

/**
 * Visualizes raw workflow.
 * @since 0.2.6
 */
public class VisualizeOriginalStructureProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(VisualizeOriginalStructureProcessor.class);

    static final Charset ENCODING = StandardCharsets.UTF_8;

    /**
     * Output path.
     */
    public static final String NAIVE_PATH = Constants.PATH_BATCH + "original-structure.dot"; //$NON-NLS-1$

    /**
     * Output path.
     */
    public static final String MERGED_PATH = Constants.PATH_BATCH + "original-merged-structure.dot"; //$NON-NLS-1$

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
                Context context = new Context(output, merged);) {
            context.put("digraph {"); //$NON-NLS-1$
            context.push();
            context.put("rankdir = LR;"); //$NON-NLS-1$
            Class<? extends BatchDescription> desc = workflow.getDescription().getClass();
            String batchId = context.label(desc, "Batch", desc.getSimpleName()); //$NON-NLS-1$
            dump(context, batchId, workflow.getGraph());
            context.pop();
            context.put("}"); //$NON-NLS-1$
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

        String id = context.label(desc.getFlowClass(), "JobFlow", desc.getFlowClass().getSimpleName()); //$NON-NLS-1$
        dumpFlowBody(context, id, model.getStageGraph().getInput().getSource().getOrigin());
        return id;
    }

    private void dumpFlowBody(Context context, String flowId, FlowGraph flow) {
        for (FlowElement element : FlowGraphUtil.collectElements(flow)) {
            FlowElementDescription desc = element.getDescription();
            switch (desc.getKind()) {
            case OPERATOR:
                Declaration decl = ((OperatorDescription) desc).getDeclaration();
                if (decl.getDeclaring().getName().startsWith("com.asakusafw.vocabulary.") == false) { //$NON-NLS-1$
                    String elementId = context.label(
                            decl.toMethod(),
                            decl.getAnnotationType().getSimpleName(),
                            MessageFormat.format(
                                    "{0}#{1}", //$NON-NLS-1$
                                    decl.getDeclaring().getSimpleName(),
                                    decl.toMethod().getName()));
                    context.connect(flowId, elementId);
                }
                break;
            case FLOW_COMPONENT:
                FlowPartDescription part = (FlowPartDescription) desc;
                Class<? extends FlowDescription> description = part.getFlowGraph().getDescription();
                String elementId = context.label(description, "FlowPart", description.getSimpleName()); //$NON-NLS-1$
                context.connect(flowId, elementId);
                dumpFlowBody(context, elementId, part.getFlowGraph());
                break;
            case INPUT:
            case OUTPUT:
            case PSEUD:
                break;
            default:
                throw new AssertionError();
            }
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
