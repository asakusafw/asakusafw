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
package com.asakusafw.compiler.flow.debugging;

import java.lang.reflect.Type;

import com.asakusafw.compiler.flow.LinePartProcessor;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.PortConnection;

/**
 * Compiler debugging utilities.
 * @since 0.4.0
 */
public final class DebuggingUtils {

    private static final String PORT_IN = "in"; //$NON-NLS-1$

    private static final String PORT_OUT = "out"; //$NON-NLS-1$

    private DebuggingUtils() {
        return;
    }

    /**
     * Add debug port.
     * @param port target port
     * @param delegate delegate
     * @return the debug element
     */
    public static FlowElement debug(FlowElementInput port, LinePartProcessor delegate) {
        FlowElementResolver resolver = createResolver(port.getDescription().getDataType(), delegate);
        for (FlowElementOutput opposite : port.disconnectAll()) {
            PortConnection.connect(opposite, resolver.getInput(PORT_IN));
        }
        PortConnection.connect(resolver.getOutput(PORT_OUT), port);
        return resolver.getElement();
    }

    /**
     * Add debug port.
     * @param port target port
     * @param delegate delegate
     * @return the debug element
     */
    public static FlowElement debug(FlowElementOutput port, LinePartProcessor delegate) {
        FlowElementResolver resolver = createResolver(port.getDescription().getDataType(), delegate);
        for (FlowElementInput opposite : port.disconnectAll()) {
            PortConnection.connect(resolver.getOutput(PORT_OUT), opposite);
        }
        PortConnection.connect(port, resolver.getInput(PORT_IN));
        return resolver.getElement();
    }

    private static FlowElementResolver createResolver(Type type, LinePartProcessor delegate) {
        FlowElementResolver resolver = new OperatorDescription.Builder(Debug.class)
            .addAttribute(ObservationCount.AT_LEAST_ONCE)
            .addAttribute(new DebuggingAttribute(delegate))
            .addInput(PORT_IN, type)
            .addOutput(PORT_OUT, type)
            .declare(DebuggingUtils.class, DebuggingUtils.class, "debug") //$NON-NLS-1$
            .toResolver();
        return resolver;
    }
}
