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
package com.asakusafw.compiler.flow.join;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowGraphRewriter;
import com.asakusafw.compiler.flow.join.operator.SideDataBranch;
import com.asakusafw.compiler.flow.join.operator.SideDataCheck;
import com.asakusafw.compiler.flow.join.operator.SideDataJoin;
import com.asakusafw.compiler.flow.join.operator.SideDataJoinUpdate;
import com.asakusafw.compiler.flow.plan.FlowGraphUtil;
import com.asakusafw.runtime.stage.input.TemporaryInputFormat;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.PortConnection;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.MasterBranch;
import com.asakusafw.vocabulary.operator.MasterCheck;
import com.asakusafw.vocabulary.operator.MasterJoin;
import com.asakusafw.vocabulary.operator.MasterJoinUpdate;

/**
 * Rewrites flow graphs for optimizing join operations.
 * @since 0.1.0
 * @version 0.7.1
 */
public class JoinRewriter extends FlowCompilingEnvironment.Initialized implements FlowGraphRewriter {

    static final Logger LOG = LoggerFactory.getLogger(JoinRewriter.class);

    @Override
    public Phase getPhase() {
        return Phase.LATER_OPTIMIZE;
    }

    @Override
    public boolean rewrite(FlowGraph graph) throws RewriteException {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        FlowCompilerOptions options = getEnvironment().getOptions();
        if (options.isHashJoinForSmall() == false && options.isHashJoinForTiny() == false) {
            LOG.debug("join optimization is disabled"); //$NON-NLS-1$
            return false;
        }
        return rewriteGraph(graph);
    }

    private boolean rewriteGraph(FlowGraph graph) {
        assert graph != null;
        boolean modified = false;
        for (FlowIn<?> input : graph.getFlowInputs()) {
            if (rewriteRequired(input) == false) {
                continue;
            }
            modified |= rewriteSuccessors(input.getDescription(), input);
        }
        return modified;
    }

    private boolean rewriteRequired(FlowIn<?> input) {
        assert input != null;
        InputDescription desc = input.getDescription();
        ImporterDescription importer = desc.getImporterDescription();
        if (importer == null) {
            return false;
        }
        if (isSupportedSize(desc) == false) {
            return false;
        }
        if (isSupportedFormat(desc) == false) {
            return false;
        }
        return true;
    }

    private boolean isSupportedSize(InputDescription desc) {
        assert desc != null;
        ImporterDescription importer = desc.getImporterDescription();
        assert importer != null;
        FlowCompilerOptions options = getEnvironment().getOptions();
        switch (importer.getDataSize()) {
            case TINY:
                return options.isHashJoinForTiny();
            case SMALL:
                // TODO implement for small
                // return options.isHashJoinForSmall();
                return false;
            default:
                return false;
        }
    }

    private boolean isSupportedFormat(InputDescription desc) {
        assert desc != null;
        assert desc.getImporterDescription() != null;
        ExternalIoDescriptionProcessor proc = getEnvironment().getExternals().findProcessor(desc);
        if (proc == null) {
            return false;
        }
        Class<?> formatType = proc.getInputInfo(desc).getFormat();
        return formatType == TemporaryInputFormat.class;
    }

    private boolean rewriteSuccessors(InputDescription source, FlowIn<?> input) {
        assert input != null;
        LinkedList<FlowElementInput> successors = new LinkedList<>();
        for (FlowElementOutput output : input.getFlowElement().getOutputPorts()) {
            successors.addAll(output.getOpposites());
        }
        Set<FlowElement> saw = new HashSet<>();
        boolean modified = false;
        while (successors.isEmpty() == false) {
            FlowElementInput next = successors.removeFirst();
            FlowElement element = next.getOwner();
            if (saw.contains(element)) {
                continue;
            }
            saw.add(element);
            if (next.getConnected().size() != 1) {
                continue;
            }
            if (element.getDescription().getKind() == FlowElementKind.PSEUD) {
                for (FlowElementOutput output : element.getOutputPorts()) {
                    successors.addAll(output.getOpposites());
                }
                continue;
            }
            if (element.getDescription().getKind() == FlowElementKind.FLOW_COMPONENT) {
                FlowPartDescription desc = (FlowPartDescription) element.getDescription();
                FlowIn<?> internal = desc.getInternalInputPort(next.getDescription());
                modified |= rewriteSuccessors(source, internal);
                continue;
            }
            if (element.getDescription().getKind() == FlowElementKind.OPERATOR) {
                modified |= rewriteOperator(source, next);
                continue;
            }
        }
        return modified;
    }

    private boolean rewriteOperator(InputDescription source, FlowElementInput input) {
        assert source != null;
        assert input != null;
        FlowElement element = input.getOwner();
        assert element.getDescription().getKind() == FlowElementKind.OPERATOR;
        OperatorDescription desc = (OperatorDescription) element.getDescription();
        Class<? extends Annotation> annotationType = desc.getDeclaration().getAnnotationType();
        Class<? extends Annotation> sideDataType;
        FlowElementInput master;
        FlowElementInput tx;
        if (annotationType == MasterJoin.class) {
            sideDataType = SideDataJoin.class;
            master = getInput(element, MasterJoin.ID_INPUT_MASTER);
            tx = getInput(element, MasterJoin.ID_INPUT_TRANSACTION);
        } else if (annotationType == MasterBranch.class) {
            sideDataType = SideDataBranch.class;
            master = getInput(element, MasterBranch.ID_INPUT_MASTER);
            tx = getInput(element, MasterBranch.ID_INPUT_TRANSACTION);
        } else if (annotationType == MasterCheck.class) {
            sideDataType = SideDataCheck.class;
            master = getInput(element, MasterCheck.ID_INPUT_MASTER);
            tx = getInput(element, MasterCheck.ID_INPUT_TRANSACTION);
        } else if (annotationType == MasterJoinUpdate.class) {
            sideDataType = SideDataJoinUpdate.class;
            master = getInput(element, MasterJoinUpdate.ID_INPUT_MASTER);
            tx = getInput(element, MasterJoinUpdate.ID_INPUT_TRANSACTION);
        } else {
            return false;
        }
        if (master.equals(input) == false) {
            return false;
        }
        FlowResourceDescription resource = createResource(source, master, tx);
        if (resource == null) {
            // if the join precondition is wrong, we skip optimization and
            // succeeding operation will raise some informative diagnostics
            return false;
        }

        OperatorDescription.Builder builder = createSideDataOperator(desc, sideDataType);
        builder.addInput(
                tx.getDescription().getName(),
                tx.getDescription().getDataType());
        builder.addResource(resource);

        FlowElement rewrite = new FlowElement(builder.toDescription());
        for (FlowElementOutput upstream : tx.getOpposites()) {
            PortConnection.connect(upstream, rewrite.getInputPorts().get(0));
        }

        List<FlowElementOutput> originalOutputs = element.getOutputPorts();
        List<FlowElementOutput> rewriteOutputs = rewrite.getOutputPorts();
        assert originalOutputs.size() == rewriteOutputs.size();
        for (int i = 0, n = originalOutputs.size(); i < n; i++) {
            FlowElementOutput originalPort = originalOutputs.get(i);
            FlowElementOutput rewritePort = rewriteOutputs.get(i);
            for (FlowElementInput downstream : originalPort.getOpposites()) {
                PortConnection.connect(rewritePort, downstream);
            }
        }

        Collection<FlowElementOutput> originalUpstreams = master.getOpposites();
        FlowGraphUtil.disconnect(element);
        for (FlowElementOutput output : originalUpstreams) {
            if (output.getConnected().isEmpty()) {
                FlowGraphUtil.stop(output);
            }
        }
        return true;
    }

    private FlowResourceDescription createResource(
            InputDescription source,
            FlowElementInput master,
            FlowElementInput tx) {
        assert source != null;
        assert master != null;
        assert tx != null;
        JoinResourceDescription resource = new JoinResourceDescription(
                source,
                toDataClass(master),
                toJoinKey(master),
                toDataClass(tx),
                toJoinKey(tx));
        List<Property> aKeys = resource.getMasterJoinKeys();
        List<Property> bKeys = resource.getTransactionJoinKeys();
        if (aKeys.size() != bKeys.size()) {
            return null;
        }
        for (int i = 0, n = aKeys.size(); i < n; i++) {
            Property a = aKeys.get(i);
            Property b = bKeys.get(i);
            if (a.getType().equals(b.getType()) == false) {
                return null;
            }
        }
        return resource;
    }

    private DataClass toDataClass(FlowElementInput input) {
        assert input != null;
        Type runtime = input.getDescription().getDataType();
        DataClass type = getEnvironment().getDataClasses().load(runtime);
        if (type == null) {
            getEnvironment().error(
                    Messages.getString("JoinRewriter.errorMissingDataClass"), //$NON-NLS-1$
                    runtime);
            return new DataClass.Unresolved(getEnvironment().getModelFactory(), runtime);
        }
        return type;
    }

    private List<Property> toJoinKey(FlowElementInput input) {
        assert input != null;
        DataClass dataClass = toDataClass(input);
        ShuffleKey key = input.getDescription().getShuffleKey();
        assert key != null;
        List<Property> results = new ArrayList<>();
        for (String name : key.getGroupProperties()) {
            Property property = dataClass.findProperty(name);
            if (property == null) {
                getEnvironment().error(
                        Messages.getString("JoinRewriter.errorMissingGroupProperty"), //$NON-NLS-1$
                        dataClass,
                        name);
            } else {
                results.add(property);
            }
        }
        return results;
    }

    private FlowElementInput getInput(FlowElement element, int id) {
        assert element != null;
        return element.getInputPorts().get(id);
    }

    private OperatorDescription.Builder createSideDataOperator(
            OperatorDescription desc,
            Class<? extends Annotation> operatorType) {
        assert desc != null;
        assert operatorType != null;
        OperatorDescription.Builder builder = new OperatorDescription.Builder(operatorType);
        builder.setOrigin(desc.getOrigin());
        builder.declare(
                desc.getDeclaration().getDeclaring(),
                desc.getDeclaration().getImplementing(),
                desc.getDeclaration().getName());
        for (Class<?> parameterType : desc.getDeclaration().getParameterTypes()) {
            builder.declareParameter(parameterType);
        }
        for (FlowElementPortDescription port : desc.getOutputPorts()) {
            builder.addOutput(port.getName(), port.getDataType());
        }
        for (OperatorDescription.Parameter parameter : desc.getParameters()) {
            builder.addParameter(parameter.getName(), parameter.getType(), parameter.getValue());
        }
        for (FlowElementAttribute attribute : desc.getAttributes()) {
            if (attribute == FlowBoundary.SHUFFLE) {
                builder.addAttribute(FlowBoundary.DEFAULT);
            } else {
                builder.addAttribute(attribute);
            }
        }
        return builder;
    }

    @Override
    public Name resolve(FlowResourceDescription resource) throws RewriteException {
        Precondition.checkMustNotBeNull(resource, "resource"); //$NON-NLS-1$
        if ((resource instanceof JoinResourceDescription) == false) {
            return null;
        }
        try {
            JoinResourceDescription joinResource = (JoinResourceDescription) resource;
            Name compiled = JoinResourceEmitter.emit(getEnvironment(), joinResource);
            return compiled;
        } catch (IOException e) {
            throw new RewriteException(
                    Messages.getString("JoinRewriter.errorFailedToResolve"), //$NON-NLS-1$
                    e);
        }
    }
}
