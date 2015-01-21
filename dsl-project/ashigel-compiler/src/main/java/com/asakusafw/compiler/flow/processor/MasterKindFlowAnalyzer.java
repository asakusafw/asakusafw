/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowElementProcessor.DataObjectMirror;
import com.asakusafw.compiler.flow.FlowElementProcessor.ListBufferMirror;
import com.asakusafw.compiler.flow.RendezvousProcessor;
import com.asakusafw.utils.collections.Lists;
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
 * {@code Master*}系の演算子を解析する。
 */
public class MasterKindFlowAnalyzer {

    // FIXME 現在のところ、ソート順序がマスタ、トランザクション、と言うようになっていることが前提

    private Expression hasMasterExpresion;

    private Expression getMasterExpression;

    private Expression getCheckedMasterExpression;

    /**
     * インスタンスを生成する。
     * @param context 文脈オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
     * マスタデータが存在するか確認するための式を返す。
     * @return マスタデータが存在するか確認するための式
     */
    public Expression getHasMasterExpresion() {
        return hasMasterExpresion;
    }

    /**
     * マスタデータを取得するための式を返す。
     * <p>
     * ただし、{@link #getHasMasterExpresion()}が{@code false}をあらわす場合、
     * この式の内容は不定である。
     * </p>
     * @return マスタデータを取得するための式
     */
    public Expression getGetRawMasterExpression() {
        return getMasterExpression;
    }

    /**
     * マスタデータを取得するための式を返す。
     * <p>
     * ただし、{@link #getHasMasterExpresion()}が{@code false}をあらわす場合、
     * この式は常に{@code null}をあらわす。
     * </p>
     * @return マスタデータを取得するための式
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

        List<Expression> arguments = Lists.create();
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
