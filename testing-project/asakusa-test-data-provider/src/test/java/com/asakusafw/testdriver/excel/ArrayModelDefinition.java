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
package com.asakusafw.testdriver.excel;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;

/**
 *
 */
public class ArrayModelDefinition implements DataModelDefinition<Object[]> {

    private final Map<PropertyName, PropertyType> nameAndTypes;

    /**
     * Creates a new instance.
     * @param nameAndTypes internal format
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ArrayModelDefinition(Map<PropertyName, PropertyType> nameAndTypes) {
        if (nameAndTypes == null) {
            throw new IllegalArgumentException("nameAndTypes must not be null"); //$NON-NLS-1$
        }
        this.nameAndTypes = Collections.unmodifiableMap(new LinkedHashMap<>(nameAndTypes));
    }

    @Override
    public Class<Object[]> getModelClass() {
        return Object[].class;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return null;
    }

    @Override
    public Collection<PropertyName> getProperties() {
        return nameAndTypes.keySet();
    }

    @Override
    public PropertyType getType(PropertyName name) {
        return nameAndTypes.get(name);
    }

    @Override
    public <A extends Annotation> A getAnnotation(PropertyName name, Class<A> annotationType) {
        return null;
    }

    @Override
    public DataModelDefinition.Builder<Object[]> newReflection() {
        return new Builder<>(this);
    }

    @Override
    public DataModelReflection toReflection(Object[] object) {
        int index = 0;
        DataModelDefinition.Builder<Object[]> builder = newReflection();
        for (PropertyName name : nameAndTypes.keySet()) {
            builder.add(name, object[index]);
            if (++index >= object.length) {
                break;
            }
        }
        return builder.build();
    }

    @Override
    public Object[] toObject(DataModelReflection reflection) {
        Object[] result = new Object[nameAndTypes.size()];
        int index = 0;
        for (Map.Entry<PropertyName, PropertyType> entry : nameAndTypes.entrySet()) {
            Object value = reflection.getValue(entry.getKey());
            result[index++] = value;
        }
        return result;
    }
}
