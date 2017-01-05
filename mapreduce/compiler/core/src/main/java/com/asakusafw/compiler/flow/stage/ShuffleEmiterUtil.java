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
package com.asakusafw.compiler.flow.stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Arrangement;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Term;
import com.asakusafw.utils.java.model.syntax.BasicTypeKind;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Utilities for emitters about shuffle operations.
 */
final class ShuffleEmiterUtil {

    /**
     * The method name of comparing {@code int} values.
     */
    public static final String COMPARE_INT = "compareInt"; //$NON-NLS-1$

    /**
     * The method name of extracting element ID from its port ID.
     */
    public static final String PORT_TO_ELEMENT = "portIdToElementId"; //$NON-NLS-1$

    public static List<List<Segment>> groupByElement(ShuffleModel model) {
        List<List<Segment>> results = new ArrayList<>();
        List<Segment> lastSegment = Collections.emptyList();
        int lastElementId = -1;
        for (Segment segment : model.getSegments()) {
            if (lastElementId != segment.getElementId()) {
                lastElementId = segment.getElementId();
                if (lastSegment.isEmpty() == false) {
                    results.add(lastSegment);
                }
                lastSegment = new ArrayList<>();
            }
            lastSegment.add(segment);
        }
        if (lastSegment.isEmpty() == false) {
            results.add(lastSegment);
        }
        return results;
    }

    public static String getPropertyName(Segment segment, Term term) {
        assert segment != null;
        assert term != null;
        String name;
        if (term.getArrangement() == Arrangement.GROUPING) {
            name = Naming.getShuffleKeyGroupProperty(
                    segment.getElementId(),
                    term.getTermId());
        } else {
            name = Naming.getShuffleKeySortProperty(
                    segment.getPortId(),
                    term.getTermId());
        }
        return name;
    }

    public static MethodDeclaration createCompareInts(
            ModelFactory factory) {
        SimpleName a = factory.newSimpleName("a"); //$NON-NLS-1$
        SimpleName b = factory.newSimpleName("b"); //$NON-NLS-1$
        Statement statement = factory.newIfStatement(
                new ExpressionBuilder(factory, a)
                    .apply(InfixOperator.EQUALS, b)
                    .toExpression(),
                new ExpressionBuilder(factory, Models.toLiteral(factory, 0))
                    .toReturnStatement(),
                factory.newIfStatement(
                        new ExpressionBuilder(factory, a)
                            .apply(InfixOperator.LESS, b)
                            .toExpression(),
                        new ExpressionBuilder(factory, Models.toLiteral(factory, -1))
                            .toReturnStatement(),
                        new ExpressionBuilder(factory, Models.toLiteral(factory, +1))
                            .toReturnStatement()));
        return factory.newMethodDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .toAttributes(),
                factory.newBasicType(BasicTypeKind.INT),
                factory.newSimpleName(COMPARE_INT),
                Arrays.asList(new FormalParameterDeclaration[] {
                        factory.newFormalParameterDeclaration(
                                factory.newBasicType(BasicTypeKind.INT),
                                a),
                        factory.newFormalParameterDeclaration(
                                factory.newBasicType(BasicTypeKind.INT),
                                b),
                }),
                Collections.singletonList(statement));
    }

    public static MethodDeclaration createPortToElement(
            ModelFactory factory,
            ShuffleModel model) {
        List<Statement> cases = new ArrayList<>();
        for (List<Segment> segments : groupByElement(model)) {
            for (Segment segment : segments) {
                cases.add(factory.newSwitchCaseLabel(
                        Models.toLiteral(factory, segment.getPortId())));
            }
            cases.add(factory.newReturnStatement(
                    Models.toLiteral(factory, segments.get(0).getElementId())));
        }
        cases.add(factory.newSwitchDefaultLabel());
        cases.add(factory.newReturnStatement(Models.toLiteral(factory, -1)));

        SimpleName pid = factory.newSimpleName("pid"); //$NON-NLS-1$
        Statement statement = factory.newSwitchStatement(pid, cases);
        return factory.newMethodDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .toAttributes(),
                factory.newBasicType(BasicTypeKind.INT),
                factory.newSimpleName(PORT_TO_ELEMENT),
                Collections.singletonList(factory.newFormalParameterDeclaration(
                        factory.newBasicType(BasicTypeKind.INT),
                        pid)),
                Collections.singletonList(statement));
    }

    private ShuffleEmiterUtil() {
        return;
    }
}
