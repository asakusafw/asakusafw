/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.vocabulary.external.ExporterDescription;


/**
 * フローからの出力を表す要素の定義記述。
 */
public class OutputDescription implements FlowElementDescription {

    /**
     * この要素の出力ポート名。
     */
    public static final String INPUT_PORT_NAME = "port"; //$NON-NLS-1$

    private static final Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> ATTRIBUTES;
    static {
        Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> map =
            new HashMap<Class<? extends FlowElementAttribute>, FlowElementAttribute>();
        map.put(FlowBoundary.class, FlowBoundary.STAGE);
        ATTRIBUTES = Collections.unmodifiableMap(map);
    }

    private String name;

    private FlowElementPortDescription port;

    private ExporterDescription exporterDescription;

    /**
     * インスタンスを生成する。
     * @param name この出力の名前
     * @param type この出力のデータ型
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public OutputDescription(String name, Type type) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.port = createPort(type);
        this.exporterDescription = null;
    }

    /**
     * インスタンスを生成する。
     * @param name この出力の名前
     * @param exporter エクスポーター処理の記述
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public OutputDescription(String name, ExporterDescription exporter) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (exporter == null) {
            throw new IllegalArgumentException("exporter must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.port = createPort(exporter.getModelType());
        this.exporterDescription = exporter;
    }

    private FlowElementPortDescription createPort(Type type) {
        assert type != null;
        return new FlowElementPortDescription(
                INPUT_PORT_NAME,
                type,
                PortDirection.INPUT);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        throw new UnsupportedOperationException("Renaming output does not permitted");
    }

    @Override
    public FlowElementKind getKind() {
        return FlowElementKind.OUTPUT;
    }

    /**
     * この出力が利用するデータの種類を返す。
     * @return この出力が利用するデータの種類
     */
    public Type getDataType() {
        return port.getDataType();
    }

    /**
     * エクスポーター処理の記述を返す。
     * @return エクスポーター処理の記述、利用しない場合は{@code null}
     */
    public ExporterDescription getExporterDescription() {
        return this.exporterDescription;
    }

    @Override
    public List<FlowElementPortDescription> getInputPorts() {
        return Collections.singletonList(port);
    }

    @Override
    public List<FlowElementPortDescription> getOutputPorts() {
        return Collections.emptyList();
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
        Object attribute = ATTRIBUTES.get(attributeClass);
        return attributeClass.cast(attribute);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}'{'name={1}, type={2}'}'",
                getClass().getSimpleName(),
                name,
                port.getDataType());
    }
}
