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
package com.asakusafw.compiler.flow.processor;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowElementProcessor.DataObjectMirror;
import com.asakusafw.compiler.flow.FlowElementProcessor.ListBufferMirror;
import com.asakusafw.compiler.flow.RendezvousProcessor;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorHelper;
import com.asakusafw.vocabulary.flow.processor.InputBuffer;

/**
 * Analyzes {@code Master*} operators.
 */
public class MasterKindFlowAnalyzer {

    // FIXME input ports must be ordered: master -> transaction

    private Expression hasMasterExpresion;

    private Expression getMasterExpression;

    private Expression getCheckedMasterExpression;

    /**
     * Creates a new instance.
     * @param context the current context
     * @throws IllegalArgumentException the parameter is {@code null}
     */
    public MasterKindFlowAnalyzer(RendezvousProcessor.Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        OperatorHelper selector = context.getOperatorDescription().getAttribute(OperatorHelper.class);
        if (selector == null) {
            processMasterFirst(context);
        } else {
            processMasterSelection(context, selector);
        }
    }

    /**
     * Returns a Java expression for checking whether the join target data exists or not.
     * @return a Java expression for checking whether the join target data exists or not
     */
    public Expression getHasMasterExpresion() {
        return hasMasterExpresion;
    }

    /**
     * Returns a Java expression for obtaining the join target data.
     * The expression will be resolved only if the join target data exists.
     * @return a Java expression for obtaining the join target data
     * @see #getHasMasterExpresion()
     */
    public Expression getGetRawMasterExpression() {
        return getMasterExpression;
    }

    /**
     * Returns a Java expression for obtaining the join target data.
     * This expression will become {@code null} if the join target data does not exist.
     * @return a Java expression for obtaining the join target data
     */
    public Expression getGetCheckedMasterExpression() {
        return getCheckedMasterExpression;
    }

    private void processMasterFirst(RendezvousProcessor.Context context) {
        assert context != null;
        ModelFactory f = context.getModelFactory();
        OperatorDescription desc = context.getOperatorDescription();
        FlowElementPortDescription master = desc.getInputPorts().get(0);

        Expression hasMaster = context.createField(boolean.class, "sawMaster"); //$NON-NLS-1$
        DataObjectMirror masterCache = context.createModelCache(master.getDataType());
        context.addBegin(new ExpressionBuilder(f, hasMaster)
            .assignFrom(Models.toLiteral(f, false))
            .toStatement());
        context.addProcess(master, f.newIfStatement(
                new ExpressionBuilder(f, hasMaster)
                    .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
                    .toExpression(),
                f.newBlock(new Statement[] {
                        masterCache.createSet(context.getProcessInput(master)),
                        new ExpressionBuilder(f, hasMaster)
                            .assignFrom(Models.toLiteral(f, true))
                            .toStatement()
                }),
                null));

        this.hasMasterExpresion = hasMaster;
        this.getMasterExpression = masterCache.get();
        this.getCheckedMasterExpression = f.newConditionalExpression(
                hasMasterExpresion,
                getMasterExpression,
                Models.toNullLiteral(f));
    }

    private void processMasterSelection(
            RendezvousProcessor.Context context,
            OperatorHelper selector) {
        assert context != null;
        assert selector != null;
        ModelFactory f = context.getModelFactory();
        OperatorDescription desc = context.getOperatorDescription();
        FlowElementPortDescription master = desc.getInputPorts().get(0);
        FlowElementPortDescription tx = desc.getInputPorts().get(1);

        ListBufferMirror list = context.createListBuffer(master.getDataType(), InputBuffer.EXPAND);
        context.addBegin(list.createBegin());
        Expression proc = context.getProcessInput(master);
        context.addProcess(master, list.createAdvance(proc));
        context.addEnd(list.createEnd());

        List<Expression> arguments = new ArrayList<>();
        arguments.add(list.get());
        arguments.add(context.getProcessInput(tx));
        for (OperatorDescription.Parameter param : desc.getParameters()) {
            arguments.add(Models.toLiteral(f, param.getValue()));
        }
        assert selector.getParameterTypes().size() <= arguments.size();
        if (selector.getParameterTypes().size() <= arguments.size()) {
            arguments = arguments.subList(0, selector.getParameterTypes().size());
        }

        context.addProcess(tx, list.createEnd());
        Expression impl = context.createImplementation();
        SimpleName selected = context.createName("selected"); //$NON-NLS-1$
        context.addProcess(tx, new ExpressionBuilder(f, impl)
            .method(selector.getName(), arguments)
            .toLocalVariableDeclaration(
                    context.convert(master.getDataType()),
                    selected));

        context.addEnd(list.createShrink());

        this.hasMasterExpresion = new ExpressionBuilder(f, selected)
            .apply(InfixOperator.NOT_EQUALS, Models.toNullLiteral(f))
            .toExpression();
        this.getMasterExpression = selected;
        this.getCheckedMasterExpression = selected;
    }
}
