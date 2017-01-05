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
package com.asakusafw.compiler.flow.model;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.repository.ValueOptionProperty;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.value.ValueOption;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * An implementation of {@link DataClass} which represents a subtype of {@link DataModel}.
 */
public class DataModelClass implements DataClass {

    private final ModelFactory factory;

    private final Class<?> type;

    private final Map<String, DataClass.Property> properties;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @param type the data type
     * @return the created instance
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static DataModelClass create(FlowCompilingEnvironment environment, Class<?> type) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Map<String, Property> properties = collectProperties(environment, type);
        return new DataModelClass(environment.getModelFactory(), type, properties);
    }

    private static Map<String, DataClass.Property> collectProperties(
            FlowCompilingEnvironment environment,
            Class<?> aClass) {
        assert environment != null;
        assert aClass != null;
        Map<String, Property> results = new HashMap<>();
        for (Method method : aClass.getMethods()) {
            String propertyName = toPropertyName(method);
            Class<?> propertyType = method.getReturnType();
            if (propertyType == ValueOption.class || ValueOption.class.isAssignableFrom(propertyType) == false) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends ValueOption<?>> valueOptionType = (Class<? extends ValueOption<?>>) propertyType;
            results.put(propertyName, new ValueOptionProperty(
                    environment.getModelFactory(),
                    propertyName,
                    valueOptionType));
        }
        return results;
    }

    private static String toPropertyName(Method method) {
        assert method != null;
        JavaName name = JavaName.of(method.getName());
        List<String> segments = name.getSegments();
        if (segments.size() <= 2) {
            return null;
        }
        if (segments.get(0).equals("get") == false //$NON-NLS-1$
                || segments.get(segments.size() - 1).equals("option") == false) { //$NON-NLS-1$
            return null;
        }
        name.removeLast();
        name.removeFirst();
        return name.toMemberName();
    }

    /**
     * Creates a new instance.
     * @param factory the Java DOM factory
     * @param type the target data type
     * @param properties the properties
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    protected DataModelClass(ModelFactory factory, Class<?> type, Map<String, Property> properties) {
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
            .method("reset") //$NON-NLS-1$
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
            .method("copyFrom", source) //$NON-NLS-1$
            .toStatement();
    }

    @Override
    public Statement createWriter(Expression object, Expression dataOutput) {
        Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(dataOutput, "dataOutput"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, object)
            .method("write", dataOutput) //$NON-NLS-1$
            .toStatement();
    }

    @Override
    public Statement createReader(Expression object, Expression dataInput) {
        Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(dataInput, "dataInput"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, object)
            .method("readFields", dataInput) //$NON-NLS-1$
            .toStatement();
    }

    @Override
    public Collection<? extends Property> getProperties() {
        return Collections.unmodifiableCollection(properties.values());
    }

    @Override
    public Property findProperty(String propertyName) {
        Precondition.checkMustNotBeNull(propertyName, "propertyName"); //$NON-NLS-1$
        if (propertyName.trim().isEmpty()) {
            return null;
        }
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
        DataModelClass other = (DataModelClass) obj;
        if (type.equals(other.type) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1})", //$NON-NLS-1$
                getClass().getSimpleName(),
                type.getName());
    }
}
