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
package com.asakusafw.vocabulary.flow.graph;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * フロー内の任意の要素の接続構造を表現する。
 * <p>
 * DSL利用者はこのクラスのオブジェクトを直接操作すべきでない。
 * </p>
 * @since 0.1.0
 * @version 0.4.0
 */
public final class FlowElement implements FlowElementAttributeProvider {

    private final Object identity;

    private final FlowElementDescription description;

    private final List<FlowElementInput> inputPorts;

    private final List<FlowElementOutput> outputPorts;

    private final Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> attributeOverride;

    /**
     * Creates a new instance.
     * @param description definition description
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowElement(FlowElementDescription description) {
        this(new Object(), description, Collections.<FlowElementAttribute>emptyList());
    }

    /**
     * Creates a new instance.
     * @param description definition description
     * @param attributeOverride extra attributes for this element
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowElement(
            FlowElementDescription description,
            Collection<? extends FlowElementAttribute> attributeOverride) {
        this(new Object(), description, attributeOverride);
    }

    private FlowElement(
            Object identity,
            FlowElementDescription description,
            Collection<? extends FlowElementAttribute> attributeOverride) {
        assert identity != null;
        assert description != null;
        assert attributeOverride != null;
        this.identity = identity;
        this.description = description;
        this.inputPorts = new ArrayList<FlowElementInput>();
        for (FlowElementPortDescription port : description.getInputPorts()) {
            if (port.getDirection() != PortDirection.INPUT) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "{0} must be an INPUT port", //$NON-NLS-1$
                        port));
            }
            inputPorts.add(new FlowElementInput(port, this));
        }
        this.outputPorts = new ArrayList<FlowElementOutput>();
        for (FlowElementPortDescription port : description.getOutputPorts()) {
            if (port.getDirection() != PortDirection.OUTPUT) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "{0} must be an OUTPUT port", //$NON-NLS-1$
                        port));
            }
            outputPorts.add(new FlowElementOutput(port, this));
        }
        this.attributeOverride = new HashMap<Class<? extends FlowElementAttribute>, FlowElementAttribute>();
        for (FlowElementAttribute attribute : attributeOverride) {
            this.attributeOverride.put(attribute.getDeclaringClass(), attribute);
        }
    }

    /**
     * Creates a new copy without any connections.
     * This copy only has same {@link #getIdentity() identity}.
     * @return the created copy
     */
    public FlowElement copy() {
        return new FlowElement(identity, description, getAttributeOverride());
    }

    /**
     * Returns the original identity.
     * @return the identity
     * @since 0.4.0
     */
    public Object getIdentity() {
        return identity;
    }

    /**
     * この要素の定義を返す。
     * @return この要素の定義
     */
    public FlowElementDescription getDescription() {
        return description;
    }

    /**
     * この要素への入力ポートの一覧を返す。
     * @return この要素への入力ポートの一覧
     */
    public List<FlowElementInput> getInputPorts() {
        return inputPorts;
    }

    /**
     * この要素からの出力ポートの一覧を返す。
     * @return この要素空の出力ポートの一覧
     */
    public List<FlowElementOutput> getOutputPorts() {
        return outputPorts;
    }

    /**
     * この要素に指定の属性が設定されている場合のみ{@code true}を返す。
     * @param attribute 対象の属性
     * @return 指定の属性が設定されている場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public boolean hasAttribute(FlowElementAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute must not be null"); //$NON-NLS-1$
        }
        FlowElementAttribute own = getAttribute(attribute.getDeclaringClass());
        if (own == null) {
            return false;
        }
        return own.equals(attribute);
    }

    /**
     * この要素に指定された指定の属性を返す。
     * @param <T> 属性の種類
     * @param attributeClass 属性の種類
     * @return 対象の属性値、未設定の場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    @Override
    public <T extends FlowElementAttribute> T getAttribute(Class<T> attributeClass) {
        if (attributeClass == null) {
            throw new IllegalArgumentException("attributeClass must not be null"); //$NON-NLS-1$
        }
        FlowElementAttribute override = attributeOverride.get(attributeClass);
        if (override != null) {
            return attributeClass.cast(override);
        }
        return getDescription().getAttribute(attributeClass);
    }

    /**
     * 指定の属性をこの要素のインスタンスにおいてのみ上書きする。
     * <p>
     * 上書きした値は{@link #getDescription()}の属性に影響しない。
     * </p>
     * @param attribute 上書きする属性
     */
    public void override(FlowElementAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute must not be null"); //$NON-NLS-1$
        }
        attributeOverride.put(attribute.getDeclaringClass(), attribute);
    }

    /**
     * この要素で上書きされた属性の一覧を返す。
     * @return この要素で上書きされた属性の一覧
     */
    public Collection<FlowElementAttribute> getAttributeOverride() {
        return new ArrayList<FlowElementAttribute>(attributeOverride.values());
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1}#{2})", //$NON-NLS-1$
                getDescription().getName(),
                getDescription().getKind().name().toLowerCase(),
                String.valueOf(hashCode()));
    }
}
