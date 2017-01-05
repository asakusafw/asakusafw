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
package com.asakusafw.compiler.directio;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Statement;

/**
 * Mock {@link DataClass}.
 */
public class MockDataClass implements DataClass {

    private final Class<?> entity;

    private final List<Property> properties;

    /**
     * Creates a new instance.
     * @param entity the class
     */
    public MockDataClass(Class<?> entity) {
        this.entity = entity;
        this.properties = new ArrayList<>();
        for (Field field : entity.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) == false && field.isSynthetic() == false) {
                properties.add(new MockProperty(field.getName(), field.getType()));
            }
        }
    }

    @Override
    public Type getType() {
        return entity;
    }

    @Override
    public Collection<? extends Property> getProperties() {
        return properties;
    }

    @Override
    public Property findProperty(String propertyName) {
        for (Property property : properties) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        return null;
    }

    @Override
    public Expression createNewInstance(com.asakusafw.utils.java.model.syntax.Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement assign(Expression target, Expression source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement reset(Expression object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement createWriter(Expression object, Expression dataOutput) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement createReader(Expression object, Expression dataInput) {
        throw new UnsupportedOperationException();
    }

    private static class MockProperty implements Property {

        private final String name;

        private final Class<?> type;

        MockProperty(String name, Class<?> type) {
            assert name != null;
            assert type != null;
            this.name = name;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public boolean canNull() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression createNewInstance(com.asakusafw.utils.java.model.syntax.Type target) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression createIsNull(Expression object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression createGetter(Expression object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Statement assign(Expression target, Expression source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Statement createGetter(Expression object, Expression target) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Statement createSetter(Expression object, Expression value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Statement createWriter(Expression object, Expression dataOutput) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Statement createReader(Expression object, Expression dataInput) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression createHashCode(Expression object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression createBytesSize(Expression bytes, Expression start, Expression length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression createBytesDiff(Expression bytes1, Expression start1, Expression length1,
                Expression bytes2, Expression start2, Expression length2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression createValueDiff(Expression value1, Expression value2) {
            throw new UnsupportedOperationException();
        }
    }
}
