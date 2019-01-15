/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.testdriver.directio.api;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

import com.asakusafw.testdriver.core.DataModelAdapter;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;

/**
 * Mock {@link DataModelAdapter}.
 */
public class MockDataModelAdapter implements DataModelAdapter {

    @SuppressWarnings("unchecked")
    @Override
    public <T> DataModelDefinition<T> get(Class<T> modelClass) {
        if (modelClass == StringBuilder.class) {
            return (DataModelDefinition<T>) new MockTextDefinition();
        }
        return null;
    }

    private static class MockTextDefinition implements DataModelDefinition<StringBuilder> {

        static final PropertyName VALUE = PropertyName.newInstance("value");

        MockTextDefinition() {
            return;
        }

        @Override
        public Class<StringBuilder> getModelClass() {
            return StringBuilder.class;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return null;
        }

        @Override
        public Collection<PropertyName> getProperties() {
            return Collections.singleton(VALUE);
        }

        @Override
        public PropertyType getType(PropertyName name) {
            if (VALUE.equals(name)) {
                return PropertyType.STRING;
            }
            return null;
        }

        @Override
        public <A extends Annotation> A getAnnotation(PropertyName name, Class<A> annotationType) {
            return null;
        }

        @Override
        public Builder<StringBuilder> newReflection() {
            return new Builder<>(this);
        }

        @Override
        public DataModelReflection toReflection(StringBuilder object) {
            return newReflection()
                .add(VALUE, object.toString())
                .build();
        }

        @Override
        public StringBuilder toObject(DataModelReflection reflection) {
            String string = (String) reflection.getValue(VALUE);
            if (string == null) {
                return new StringBuilder();
            } else {
                return new StringBuilder(string);
            }
        }
    }
}
