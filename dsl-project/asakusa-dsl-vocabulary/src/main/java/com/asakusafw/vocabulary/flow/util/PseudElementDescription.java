/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.flow.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.PortDirection;
import com.asakusafw.vocabulary.operator.Checkpoint;
import com.asakusafw.vocabulary.operator.Confluent;
import com.asakusafw.vocabulary.operator.Empty;
import com.asakusafw.vocabulary.operator.Stop;


/**
 * 疑似要素の定義記述。
 */
public class PseudElementDescription implements FlowElementDescription {

    /**
     * 入力ポートの名前。
     */
    public static final String INPUT_PORT_NAME = "in"; //$NON-NLS-1$

    /**
     * 出力ポートの名前。
     */
    public static final String OUTPUT_PORT_NAME = "out"; //$NON-NLS-1$

    private String name;

    private final List<FlowElementPortDescription> inputPorts;

    private final List<FlowElementPortDescription> outputPorts;

    private final Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> attributes;

    /**
     * インスタンスを生成する。
     * @param name 要素の名前
     * @param type 取り扱うデータの種類
     * @param hasInput {@code true}ならば入力ポートを持つ
     * @param hasOutput {@code true}ならば出力ポートを持つ
     * @param attributes 属性の一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public PseudElementDescription(
            String name,
            Type type,
            boolean hasInput,
            boolean hasOutput,
            FlowElementAttribute... attributes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.inputPorts = create(
                hasInput,
                INPUT_PORT_NAME,
                type,
                PortDirection.INPUT);
        this.outputPorts = create(
                hasOutput,
                OUTPUT_PORT_NAME,
                type,
                PortDirection.OUTPUT);
        this.attributes = new HashMap<Class<? extends FlowElementAttribute>, FlowElementAttribute>();
        for (FlowElementAttribute attribute : attributes) {
            this.attributes.put(attribute.getDeclaringClass(), attribute);
        }
    }

    private List<FlowElementPortDescription> create(
            boolean doCreate,
            String portName,
            Type portType,
            PortDirection direction) {
        assert portName != null;
        assert direction != null;
        if (doCreate == false) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(new FlowElementPortDescription(
                    portName,
                    portType,
                    direction));
        }
    }

    @Override
    public FlowElementKind getKind() {
        return FlowElementKind.PSEUD;
    }

    @Override
    public FlowElementDescription getOrigin() {
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        this.name = name;
    }

    @Override
    public List<FlowElementPortDescription> getInputPorts() {
        return inputPorts;
    }

    @Override
    public List<FlowElementPortDescription> getOutputPorts() {
        return outputPorts;
    }

    @Override
    public List<FlowResourceDescription> getResources() {
        return Collections.emptyList();
    }

    @Override
    public <T extends FlowElementAttribute> T getAttribute(Class<T> attributeClass) {
        if (attributeClass == null) {
            throw new IllegalArgumentException("attributeClass must not be null"); //$NON-NLS-1$
        }
        Object attribute = attributes.get(attributeClass);
        return attributeClass.cast(attribute);
    }

    @Override
    public String toString() {
        Class<? extends Annotation> annotation = analyzePseud();
        return MessageFormat.format(
                "{0}#{1}(@{2})", //$NON-NLS-1$
                CoreOperatorFactory.class.getSimpleName(),
                annotation.getSimpleName().toLowerCase(Locale.ENGLISH),
                annotation.getSimpleName());
    }

    private Class<? extends Annotation> analyzePseud() {
        if (getInputPorts().isEmpty()) {
            return Empty.class;
        }
        if (getOutputPorts().isEmpty()) {
            return Stop.class;
        }
        FlowBoundary boundary = getAttribute(FlowBoundary.class);
        if (boundary == FlowBoundary.STAGE) {
            return Checkpoint.class;
        }
        return Confluent.class;
    }
}
