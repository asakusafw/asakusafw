/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.join.processor;

import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.LineEndProcessor;
import com.asakusafw.compiler.flow.join.JoinResourceDescription;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorHelper;

/**
 * {@code SideData*}系の演算子を解析する。
 */
public class SideDataKindFlowAnalyzer {

    private final JoinResourceDescription resource;

    private Expression hasMasterExpresion;

    private Expression getMasterExpression;

    private Expression getCheckedMasterExpression;

    /**
     * インスタンスを生成する。
     * @param context 文脈オブジェクト
     * @param resource 結合リソース
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public SideDataKindFlowAnalyzer(
            LineEndProcessor.Context context,
            JoinResourceDescription resource) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(resource, "resource"); //$NON-NLS-1$
        OperatorHelper selector = context.getOperatorDescription().getAttribute(OperatorHelper.class);
        this.resource = resource;
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

    private void processMasterFirst(LineEndProcessor.Context context) {
        assert context != null;
        ModelFactory f = context.getModelFactory();

        Expression lookup = createLookup(context, f);

        this.hasMasterExpresion = new ExpressionBuilder(f, lookup)
            .method("isEmpty")
            .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
            .toExpression();
        this.getMasterExpression = new ExpressionBuilder(f, lookup)
            .method("get", Models.toLiteral(f, 0))
            .toExpression();
        this.getCheckedMasterExpression = f.newConditionalExpression(
                new ExpressionBuilder(f, lookup)
                    .method("isEmpty")
                    .toExpression(),
                Models.toNullLiteral(f),
                getMasterExpression);
    }

    private void processMasterSelection(
            LineEndProcessor.Context context,
            OperatorHelper selector) {
        assert context != null;
        assert selector != null;
        ModelFactory f = context.getModelFactory();
        Expression lookup = createLookup(context, f);
        Expression selected = context.createLocalVariable(
                resource.getMasterDataClass().getType(),
                Models.toNullLiteral(f));
        List<Expression> arguments = Lists.create();
        arguments.add(lookup);
        arguments.add(context.getInput());
        for (OperatorDescription.Parameter param : context.getOperatorDescription().getParameters()) {
            arguments.add(Models.toLiteral(f, param.getValue()));
        }
        assert selector.getParameterTypes().size() <= arguments.size();
        if (selector.getParameterTypes().size() <= arguments.size()) {
            arguments = arguments.subList(0, selector.getParameterTypes().size());
        }
        Expression impl = context.createImplementation();
        context.add(f.newIfStatement(
                new ExpressionBuilder(f, lookup)
                    .apply(InfixOperator.NOT_EQUALS, Models.toNullLiteral(f))
                    .toExpression(),
                f.newBlock(new ExpressionBuilder(f, selected)
                    .assignFrom(new ExpressionBuilder(f, impl)
                        .method(selector.getName(), arguments)
                        .toExpression())
                    .toStatement())));

        this.hasMasterExpresion = new ExpressionBuilder(f, selected)
            .apply(InfixOperator.NOT_EQUALS, Models.toNullLiteral(f))
            .toExpression();
        this.getMasterExpression = selected;
        this.getCheckedMasterExpression = selected;
    }

    private Expression createLookup(LineEndProcessor.Context context, ModelFactory f) {
        assert context != null;
        assert f != null;
        Expression lookup = context.createLocalVariable(
                context.simplify(new TypeBuilder(f, Models.toType(f, List.class))
                    .parameterize(Models.toType(f, resource.getMasterDataClass().getType()))
                    .toType()),
                new ExpressionBuilder(f, context.getResource(resource))
                    .method("find", context.getInput())
                    .toExpression());
        return lookup;
    }
}
