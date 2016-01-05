/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.serde;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.asakusafw.runtime.value.ValueOption;

/**
 * A {@link PropertyDescriptor} implementation using Java instance field.
 */
public class FieldPropertyDescriptor extends PropertyDescriptor {

    private final Field ref;

    /**
     * Creates a new instance.
     * @param ref field reference
     * @param serde value accessor
     */
    public FieldPropertyDescriptor(Field ref, ValueSerde serde) {
        super(ref.getName(), serde);
        this.ref = ref;
    }

    /**
     * Analyzes properties in data model as visible fields.
     * @param aClass the target data model
     * @return the {@link DataModelDescriptor} with properties represented as its visible fields
     */
    public static DataModelDescriptor extract(Class<?> aClass) {
        DataModelDescriptorBuilder builder = new DataModelDescriptorBuilder(aClass);
        for (Field field : aClass.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            ValueSerde serde = ValueSerdeFactory.fromClass(field.getType());
            if (serde != null) {
                builder.property(new FieldPropertyDescriptor(field, serde));
            }
        }
        return builder.build();
    }

    @Override
    public ValueOption<?> extract(Object dataModel) {
        try {
            return (ValueOption<?>) ref.get(dataModel);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
