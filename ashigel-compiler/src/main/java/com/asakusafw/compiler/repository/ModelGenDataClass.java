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
package com.asakusafw.compiler.repository;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.runtime.value.ValueOption;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.TypeBuilder;

/**
 * {@code model-generator}によって生成されたクラスを対象とした{@link DataClass}の実装。
 */
public class ModelGenDataClass implements DataClass {

    private ModelFactory factory;

    private Class<?> type;

    private Map<String, DataClass.Property> properties;

    /**
     * インスタンスを生成して返す。
     * @param environment コンパイラの環境
     * @param type このデータのJava上での型
     * @return 生成したインスタンス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static ModelGenDataClass create(FlowCompilingEnvironment environment, Class<?> type) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Map<String, Property> properties = collectProperties(environment, type);
        return new ModelGenDataClass(environment.getModelFactory(), type, properties);
    }

    private static Map<String, DataClass.Property> collectProperties(
            FlowCompilingEnvironment environment,
            Class<?> aClass) {
        assert environment != null;
        assert aClass != null;
        Map<String, Property> results = new HashMap<String, DataClass.Property>();
        for (Field field : aClass.getDeclaredFields()) {
            if (field.isSynthetic() || field.getName().indexOf('$') >= 0) {
                // skip internal field
                continue;
            }
            Class<?> propertyType = field.getType();
            if (propertyType == ValueOption.class
                    || ValueOption.class.isAssignableFrom(propertyType) == false) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends ValueOption<?>> valueOptionType =
                (Class<? extends ValueOption<?>>) propertyType;
            results.put(field.getName(), new ValueOptionProperty(
                    environment.getModelFactory(),
                    JavaName.of(field.getName()).toMemberName(),
                    valueOptionType));
        }
        return results;
    }

    /**
     * インスタンスを生成する。
     * @param factory 利用するファクトリー
     * @param type このデータクラスのJava上での型
     * @param properties プロパティの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    protected ModelGenDataClass(ModelFactory factory, Class<?> type, Map<String, Property> properties) {
        Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(properties, "properties"); //$NON-NLS-1$
        this.factory = factory;
        this.type = type;
        this.properties = properties;
    }

    @Override
    public java.lang.reflect.Type getType() {
        return type;
    }

    @Override
    public Statement reset(Expression object) {
        Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, object)
            .method("reset")
            .toStatement();
    }

    @Override
    public Expression createNewInstance(Type target) {
        Precondition.checkMustNotBeNull(target, "target"); //$NON-NLS-1$
        return new TypeBuilder(factory, target)
            .newObject()
            .toExpression();
    }

    @Override
    public Statement assign(Expression target, Expression source) {
        Precondition.checkMustNotBeNull(target, "target"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, target)
            .method("copyFrom", source)
            .toStatement();
    }

    @Override
    public Statement createWriter(Expression object, Expression dataOutput) {
        Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(dataOutput, "dataOutput"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, object)
            .method("write", dataOutput)
            .toStatement();
    }

    @Override
    public Statement createReader(Expression object, Expression dataInput) {
        Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(dataInput, "dataInput"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, object)
            .method("readFields", dataInput)
            .toStatement();
    }

    @Override
    public Property findProperty(String propertyName) {
        Precondition.checkMustNotBeNull(propertyName, "propertyName"); //$NON-NLS-1$
        String normalName = JavaName.of(propertyName).toMemberName();
        return properties.get(normalName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ModelGenDataClass other = (ModelGenDataClass) obj;
        if (type.equals(other.type) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1})",
                getClass().getSimpleName(),
                type.getName());
    }
}
