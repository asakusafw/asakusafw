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
package com.asakusafw.testdriver.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;

/**
 * Simple {@link DataModelDefinition} for classes which declare public fields directly.
 * This supports only following types:
 * <ul>
 * <li> wrapper types of any primitive types, </li>
 * <li> {@link java.lang.String}, </li>
 * <li> {@link java.math.BigInteger}, </li>
 * <li> {@link java.math.BigDecimal}, </li>
 * <li> or {@link java.util.Calendar} </li>
 * </ul>
 * <p>
 * Additionaly, each field should be named in {@code camelCase}.
 * </p>
 * @param <T> type of data model
 * @since 0.2.0
 */
public class SimpleDataModelDefinition<T> implements DataModelDefinition<T> {

    private static final Map<Class<?>, PropertyType> TYPES;
    static {
        Map<Class<?>, PropertyType> map = new HashMap<Class<?>, PropertyType>();
        for (PropertyType type : PropertyType.values()) {
            map.put(type.getRepresentation(), type);
        }
        // special rule
        map.put(PropertyType.DATETIME.getRepresentation(), PropertyType.DATETIME);
        TYPES = Collections.unmodifiableMap(map);
    }

    private final Class<T> modelClass;

    private final Map<PropertyName, Field> fields;

    /**
     * Creates a new instance.
     * @param modelClass target model class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SimpleDataModelDefinition(Class<T> modelClass) {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        this.modelClass = modelClass;
        this.fields = collectProperties();
    }

    private Map<PropertyName, Field> collectProperties() {
        Map<PropertyName, Field> results = new HashMap<PropertyName, Field>();
        for (Field field : modelClass.getDeclaredFields()) {
            PropertyName name = extract(field);
            if (name != null) {
                results.put(name, field);
            }
        }
        return Collections.unmodifiableMap(results);
    }

    private static final Pattern NAME = Pattern.compile("[a-z][0-9A-Za-z]*");
    private PropertyName extract(Field field) {
        assert field != null;
        if (Modifier.isPublic(field.getModifiers()) == false) {
            return null;
        }
        if (TYPES.containsKey(field.getType()) == false) {
            return null;
        }
        String name = field.getName();
        if (NAME.matcher(name).matches() == false) {
            return null;
        }
        List<String> words = new ArrayList<String>();
        int start = 0;
        for (int i = 1, n = name.length(); i < n; i++) {
            char c = name.charAt(i);
            if ('A' <= c && c <= 'Z') {
                words.add(name.substring(start, i));
                start = i;
            }
        }
        words.add(name.substring(start));
        return PropertyName.newInstance(words);
    }

    @Override
    public Class<T> getModelClass() {
        return modelClass;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return modelClass.getAnnotation(annotationType);
    }

    @Override
    public Collection<PropertyName> getProperties() {
        return Collections.unmodifiableCollection(fields.keySet());
    }

    @Override
    public PropertyType getType(PropertyName name) {
        Field field = fields.get(name);
        return field == null ? null : getType(name, field.getType());
    }

    /**
     * Returns property type kind corresponded to the property name and type.
     * @param name property name
     * @param type property type
     * @return property type kind, or {@code null} without suitable correponded kind
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static PropertyType getType(PropertyName name, Class<?> type) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        PropertyType kind = TYPES.get(type);
        if (kind == null) {
            return null;
        }
        if (kind.getRepresentation() == Calendar.class) {
            List<String> words = name.getWords();
            if (words.contains(PropertyType.DATE.name().toLowerCase())) {
                return PropertyType.DATE;
            } else if (words.contains(PropertyType.TIME.name().toLowerCase())) {
                return PropertyType.TIME;
            } else if (words.contains(PropertyType.DATETIME.name().toLowerCase())) {
                return PropertyType.DATETIME;
            }
        }
        return kind;
    }

    @Override
    public <A extends Annotation> A getAnnotation(PropertyName name, Class<A> annotationType) {
        Field field = fields.get(name);
        return field == null ? null : field.getAnnotation(annotationType);
    }

    @Override
    public Builder<T> newReflection() {
        return new Builder<T>(this);
    }

    @Override
    public DataModelReflection toReflection(T object) {
        Builder<T> builder = newReflection();
        for (Map.Entry<PropertyName, Field> entry : fields.entrySet()) {
            PropertyName name = entry.getKey();
            Field field = entry.getValue();
            try {
                Object value = field.get(object);
                builder.add(name, value);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
        return builder.build();
    }

    @Override
    public T toObject(DataModelReflection reflection) {
        try {
            T instance = modelClass.newInstance();
            for (Map.Entry<PropertyName, Field> entry : fields.entrySet()) {
                PropertyName name = entry.getKey();
                Field field = entry.getValue();
                Object value = reflection.getValue(name);
                try {
                    field.set(instance, value);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
