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
package com.asakusafw.compiler.flow.join.processor;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.LineEndProcessor;
import com.asakusafw.compiler.flow.join.JoinResourceDescription;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorHelper;

/**
 * Analyzes {@code SideData-like} operators.
 */
public class SideDataKindFlowAnalyzer {

    private final JoinResourceDescription resource;

    private Expression hasMasterExpresion;

    private Expression getMasterExpression;

    private Expression getCheckedMasterExpression;

    /**
     * Creates a new instance.
     * @param context the current context
     * @param resource information of the side data
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public SideDataKindFlowAnalyzer(LineEndProcessor.Context context, JoinResourceDescription resource) {
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

    private void processMasterFirst(LineEndProcessor.Context context) {
        assert context != null;
        ModelFactory f = context.getModelFactory();

        Expression lookup = createLookup(context, f);

        this.hasMasterExpresion = new ExpressionBuilder(f, lookup)
            .method("isEmpty") //$NON-NLS-1$
            .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
            .toExpression();
        this.getMasterExpression = new ExpressionBuilder(f, lookup)
            .method("get", Models.toLiteral(f, 0)) //$NON-NLS-1$
            .toExpression();
        this.getCheckedMasterExpression = f.newConditionalExpression(
                new ExpressionBuilder(f, lookup)
                    .method("isEmpty") //$NON-NLS-1$
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
        List<Expression> arguments = new ArrayList<>();
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
                    .method("find", context.getInput()) //$NON-NLS-1$
                    .toExpression());
        return lookup;
    }
}
