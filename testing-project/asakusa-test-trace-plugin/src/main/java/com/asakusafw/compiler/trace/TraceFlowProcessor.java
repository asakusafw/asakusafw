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
package com.asakusafw.compiler.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.flow.LinePartProcessor;
import com.asakusafw.runtime.trace.TraceContext;
import com.asakusafw.runtime.trace.TraceContext.PortDirection;
import com.asakusafw.runtime.trace.TraceDriver;
import com.asakusafw.trace.model.Tracepoint;
import com.asakusafw.trace.model.Tracepoint.PortKind;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.operator.Trace;

/**
 * Processes {@link Trace} operators.
 * @since 0.5.1
 */
@TargetOperator(Trace.class)
public class TraceFlowProcessor extends LinePartProcessor {

    @Override
    public void emitLinePart(Context context) {
        ModelFactory f = Models.getModelFactory();
        Expression driver = context.createField(
                TraceDriver.class, "trace", //$NON-NLS-1$
                new TypeBuilder(f, context.convert(TraceDriver.class))
                    .method("get", createTraceContext(context)) //$NON-NLS-1$
                    .toExpression());

        Expression input = context.getInput();
        context.add(new ExpressionBuilder(f, driver).method("trace", input).toStatement()); //$NON-NLS-1$
        context.setOutput(input);
    }

    private Expression createTraceContext(Context context) {
        TraceSettingAttribute attribute = context.getOperatorDescription().getAttribute(TraceSettingAttribute.class);
        if (attribute == null) {
            throw new IllegalStateException();
        }
        Tracepoint tracepoint = attribute.getSetting().getTracepoint();
        ModelFactory f = Models.getModelFactory();
        List<Expression> arguments = new ArrayList<Expression>();

        // serial number
        arguments.add(Models.toLiteral(f, attribute.getSerialNumber()));

        // operator class
        arguments.add(Models.toLiteral(f, tracepoint.getOperatorClassName()));

        // operator method
        arguments.add(Models.toLiteral(f, tracepoint.getOperatorMethodName()));

        // port direction
        arguments.add(new TypeBuilder(f, context.convert(PortDirection.class))
                .field(tracepoint.getPortKind() == PortKind.INPUT
                        ? PortDirection.INPUT.name() : PortDirection.OUTPUT.name())
                .toExpression());

        // port name
        arguments.add(Models.toLiteral(f, tracepoint.getPortName()));

        // data type
        arguments.add(f.newClassLiteral(context.convert(context.getInputPort(0).getDataType())));

        // attributes
        Map<String, String> traceAttributes = new TreeMap<String, String>(attribute.getSetting().getAttributes());
        for (Map.Entry<String, String> entry : traceAttributes.entrySet()) {
            arguments.add(Models.toLiteral(f, entry.getKey()));
            arguments.add(Models.toLiteral(f, entry.getValue()));
        }

        return new TypeBuilder(f, context.convert(TraceContext.class))
            .newObject(arguments)
            .toExpression();
    }
}
