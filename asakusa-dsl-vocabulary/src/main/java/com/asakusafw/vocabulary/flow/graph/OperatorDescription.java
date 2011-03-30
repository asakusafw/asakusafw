/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * フローから利用される演算子の定義を表現する。
 */
public class OperatorDescription implements FlowElementDescription {

    private Declaration declaration;

    private List<FlowElementPortDescription> inputPorts;

    private List<FlowElementPortDescription> outputPorts;

    private List<FlowResourceDescription> resources;

    private List<Parameter> parameters;

    private Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> attributes;

    private String name;

    /**
     * インスタンスを生成する。
     * @param declaration 演算子メソッド等の宣言情報
     * @param inputPorts 入力ポートの情報
     * @param outputPorts 出力ポートの情報
     * @param resources 利用するリソースの情報
     * @param parameters 演算子に渡された入出力以外のパラメーターの情報
     * @param attributes 演算子が持つ属性の情報
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public OperatorDescription(
            Declaration declaration,
            List<FlowElementPortDescription> inputPorts,
            List<FlowElementPortDescription> outputPorts,
            List<FlowResourceDescription> resources,
            List<Parameter> parameters,
            List<FlowElementAttribute> attributes) {
        if (declaration == null) {
            throw new IllegalArgumentException("declaration must not be null"); //$NON-NLS-1$
        }
        if (inputPorts == null) {
            throw new IllegalArgumentException("inputPorts must not be null"); //$NON-NLS-1$
        }
        if (outputPorts == null) {
            throw new IllegalArgumentException("outputPorts must not be null"); //$NON-NLS-1$
        }
        if (resources == null) {
            throw new IllegalArgumentException("resources must not be null"); //$NON-NLS-1$
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        this.declaration = declaration;
        this.inputPorts = Collections.unmodifiableList(new ArrayList<FlowElementPortDescription>(inputPorts));
        this.outputPorts = Collections.unmodifiableList(new ArrayList<FlowElementPortDescription>(outputPorts));
        this.resources = Collections.unmodifiableList(new ArrayList<FlowResourceDescription>(resources));
        this.parameters = Collections.unmodifiableList(new ArrayList<Parameter>(parameters));
        this.attributes = new HashMap<Class<? extends FlowElementAttribute>, FlowElementAttribute>();
        for (FlowElementAttribute attribute : attributes) {
            this.attributes.put(attribute.getDeclaringClass(), attribute);
        }
    }

    @Override
    public FlowElementKind getKind() {
        return FlowElementKind.OPERATOR;
    }

    /**
     * この演算子の宣言に関する情報を返す。
     * @return この演算子の宣言に関する情報
     */
    public Declaration getDeclaration() {
        return declaration;
    }

    @Override
    public String getName() {
        if (name == null) {
            return MessageFormat.format(
                "{0}.{1}",
                declaration.getDeclaring().getSimpleName(),
                declaration.getName());
        }
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
        return resources;
    }

    /**
     * この演算子が利用するパラメーターの一覧を返す。
     * @return この演算子が利用するパラメーターの一覧
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public <T extends FlowElementAttribute> T getAttribute(Class<T> attributeClass) {
        if (attributeClass == null) {
            throw new IllegalArgumentException("attributeClass must not be null"); //$NON-NLS-1$
        }
        Object attribute = attributes.get(attributeClass);
        return attributeClass.cast(attribute);
    }

    /**
     * この演算子に指定された属性の一覧を返す。
     * @return この演算子に指定された属性の一覧
     */
    public Set<FlowElementAttribute> getAttributes() {
        return new HashSet<FlowElementAttribute>(attributes.values());
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}{1}",
                getDeclaration(),
                getParameters());
    }

    /**
     * 演算子メソッドの宣言。
     */
    public static class Declaration {

        private Class<? extends Annotation> annotationType;

        private Class<?> declaring;

        private Class<?> implementing;

        private String name;

        private List<Class<?>> parameterTypes;

        /**
         * インスタンスを生成する。
         * @param annotationType 演算子注釈の種類
         * @param declaring 演算子クラス
         * @param implementing 演算子実装クラス
         * @param name 演算子メソッドの名前
         * @param parameterTypes 演算子メソッドの引数型(消去型)一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Declaration(
                Class<? extends Annotation> annotationType,
                Class<?> declaring,
                Class<?> implementing,
                String name,
                List<Class<?>> parameterTypes) {
            if (annotationType == null) {
                throw new IllegalArgumentException("annotationType must not be null"); //$NON-NLS-1$
            }
            if (declaring == null) {
                throw new IllegalArgumentException("declaring must not be null"); //$NON-NLS-1$
            }
            if (implementing == null) {
                throw new IllegalArgumentException("implementing must not be null"); //$NON-NLS-1$
            }
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
            }
            this.annotationType = annotationType;
            this.declaring = declaring;
            this.implementing = implementing;
            this.name = name;
            this.parameterTypes = parameterTypes;
        }

        /**
         * 演算子注釈の種類を返す。
         * @return 演算子注釈の種類
         */
        public Class<? extends Annotation> getAnnotationType() {
            return annotationType;
        }

        /**
         * 演算子クラスを返す。
         * @return 演算子クラス
         */
        public Class<?> getDeclaring() {
            return declaring;
        }

        /**
         * 演算子実装クラスを返す。
         * @return 演算子クラス
         */
        public Class<?> getImplementing() {
            return implementing;
        }

        /**
         * 演算子メソッドの名前を返す。
         * @return 演算子メソッドの名前
         */
        public String getName() {
            return name;
        }

        /**
         * 演算子メソッドのパラメーター型一覧を返す。
         * <p>
         * これらの型は、消去型として返される。
         * </p>
         * @return 演算子メソッドのパラメーター型一覧
         */
        public List<Class<?>> getParameterTypes() {
            return parameterTypes;
        }

        /**
         * この宣言に対する実行時のメソッド表現を返す。
         * @return 実行時のメソッド表現、対応するものが存在しない場合は{@code null}
         */
        public Method toMethod() {
            Class<?>[] params = parameterTypes.toArray(new Class<?>[parameterTypes.size()]);
            try {
                return declaring.getMethod(name, params);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0}#{1}({2})",
                    declaring.getName(),
                    name,
                    parameterTypes);
        }
    }

    /**
     * 演算子ファクトリーメソッドに渡された引数。
     */
    public static class Parameter {

        private String name;

        private Type type;

        private Object value;

        /**
         * インスタンスを生成する。
         * @param name パラメーターの名前
         * @param type パラメーターの型
         * @param value パラメーターの値 ({@code nullable})
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Parameter(String name, Type type, Object value) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (type == null) {
                throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
            }
            this.name = name;
            this.type = type;
            this.value = value;
        }

        /**
         * パラメーターの名前を返す。
         * @return パラメーターの名前
         */
        public String getName() {
            return name;
        }

        /**
         * パラメーターの型を返す。
         * @return パラメーターの型
         */
        public Type getType() {
            return type;
        }

        /**
         * パラメーターの値を返す。
         * @return パラメーターの値、値が{@code null}の場合は返される値も{@code null}
         */
        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0}[{1}]={2}",
                    getName(),
                    getType(),
                    getValue());
        }
    }

    /**
     * この要素を構築するビルダー。
     */
    public static class Builder {

        private Class<? extends Annotation> annotationType;

        private Class<?> declaring;

        private Class<?> implementing;

        private String name;

        private List<Class<?>> parameterTypes;

        private List<FlowElementPortDescription> inputPorts;

        private List<FlowElementPortDescription> outputPorts;

        private List<FlowResourceDescription> resources;

        private List<Parameter> parameters;

        private List<FlowElementAttribute> attributes;

        /**
         * インスタンスを生成する。
         * @param annotationType 演算子の種類
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Builder(Class<? extends Annotation> annotationType) {
            if (annotationType == null) {
                throw new IllegalArgumentException("annotationType must not be null"); //$NON-NLS-1$
            }
            this.annotationType = annotationType;
            this.parameterTypes = new ArrayList<Class<?>>();
            this.inputPorts = new ArrayList<FlowElementPortDescription>();
            this.outputPorts = new ArrayList<FlowElementPortDescription>();
            this.resources = new ArrayList<FlowResourceDescription>();
            this.parameters = new ArrayList<OperatorDescription.Parameter>();
            this.attributes = new ArrayList<FlowElementAttribute>();
        }

        /**
         * 演算子メソッドの宣言に関する情報を追加する。
         * <p>
         * なお、演算子メソッドの引数型は{@link #declareParameter(Class)}で宣言する。
         * </p>
         * @param operatorClass 演算子クラス
         * @param implementorClass 演算子実装クラス
         * @param methodName 演算子メソッドの名前
         * @return このオブジェクト (メソッドチェイン用)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Builder declare(
                Class<?> operatorClass,
                Class<?> implementorClass,
                String methodName) {
            if (operatorClass == null) {
                throw new IllegalArgumentException("operatorClass must not be null"); //$NON-NLS-1$
            }
            if (implementorClass == null) {
                throw new IllegalArgumentException("implementorClass must not be null"); //$NON-NLS-1$
            }
            if (methodName == null) {
                throw new IllegalArgumentException("methodName must not be null"); //$NON-NLS-1$
            }
            if (this.declaring != null) {
                throw new IllegalStateException();
            }
            this.declaring = operatorClass;
            this.implementing = implementorClass;
            this.name = methodName;
            return this;
        }

        /**
         * 演算子メソッドのパラメーターを追加する。
         * @param parameterType パラメーターの型 (型消去済みのもの)
         * @return このオブジェクト (メソッドチェイン用)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Builder declareParameter(Class<?> parameterType) {
            if (parameterType == null) {
                throw new IllegalArgumentException("parameterType must not be null"); //$NON-NLS-1$
            }
            this.parameterTypes.add(parameterType);
            return this;
        }

        /**
         * 演算子の入力ポートを追加する。
         * @param portName 追加するポートの名前
         * @param dataType 追加するポートのデータ型
         * @return このオブジェクト (メソッドチェイン用)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Builder addInput(String portName, Type dataType) {
            if (portName == null) {
                throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
            }
            if (dataType == null) {
                throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
            }
            inputPorts.add(new FlowElementPortDescription(
                    portName,
                    dataType,
                    PortDirection.INPUT));
            return this;
        }

        /**
         * 演算子の入力ポートを追加する。
         * @param portName 追加するポートの名前
         * @param dataType 追加するポートのデータ型
         * @param key ポートのシャッフル条件
         * @return このオブジェクト (メソッドチェイン用)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Builder addInput(String portName, Type dataType, ShuffleKey key) {
            if (portName == null) {
                throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
            }
            if (dataType == null) {
                throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
            }
            if (key == null) {
                throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
            }
            inputPorts.add(new FlowElementPortDescription(
                    portName,
                    dataType,
                    key));
            return this;
        }

        /**
         * 演算子の出力ポートを追加する。
         * @param portName 追加するポートの名前
         * @param dataType 追加するポートのデータ型
         * @return このオブジェクト (メソッドチェイン用)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Builder addOutput(String portName, Type dataType) {
            if (portName == null) {
                throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
            }
            if (dataType == null) {
                throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
            }
            outputPorts.add(new FlowElementPortDescription(
                    portName,
                    dataType,
                    PortDirection.OUTPUT));
            return this;
        }

        /**
         * 演算子のリソースを追加する。
         * @param resource 追加するリソース
         * @return このオブジェクト (メソッドチェイン用)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Builder addResource(FlowResourceDescription resource) {
            if (resource == null) {
                throw new IllegalArgumentException("resource must not be null"); //$NON-NLS-1$
            }
            resources.add(resource);
            return this;
        }

        /**
         * 演算子が利用する入出力以外のパラメーターを追加する。
         * @param parameterName パラメーターの名前
         * @param parameterType パラメーターの型
         * @param argument 実引数の値、{@code null}を表す場合は{@code null}
         * @return このオブジェクト (メソッドチェイン用)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Builder addParameter(
                String parameterName,
                Type parameterType,
                Object argument) {
            if (parameterName == null) {
                throw new IllegalArgumentException("parameterName must not be null"); //$NON-NLS-1$
            }
            if (parameterType == null) {
                throw new IllegalArgumentException("parameterType must not be null"); //$NON-NLS-1$
            }
            parameters.add(new Parameter(parameterName, parameterType, argument));
            return this;
        }

        /**
         * 演算子の値を持たない属性情報を追加する。
         * @param attribute 追加する属性
         * @return このオブジェクト (メソッドチェイン用)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Builder addAttribute(FlowElementAttribute attribute) {
            if (attribute == null) {
                throw new IllegalArgumentException("attribute must not be null"); //$NON-NLS-1$
            }
            attributes.add(attribute);
            return this;
        }

        /**
         * ここまでの内容を元に、演算子の定義記述オブジェクトを生成して返す。
         * @return 生成したオブジェクト
         */
        public OperatorDescription toDescription() {
            return new OperatorDescription(
                    new Declaration(
                            annotationType,
                            declaring,
                            implementing,
                            name,
                            parameterTypes),
                    inputPorts,
                    outputPorts,
                    resources,
                    parameters,
                    attributes);
        }

        /**
         * ここまでの内容を元に、演算子の解決オブジェクトを生成して返す。
         * @return 生成したオブジェクト
         */
        public FlowElementResolver toResolver() {
            return new FlowElementResolver(toDescription());
        }
    }
}
