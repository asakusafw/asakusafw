/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;

/**
 * Default implementation of {@link DataModelDefinition}.
 * @param <T> type of data model class
 * @since 0.2.0
 * @version 0.9.1
 * @see DefaultDataModelAdapter
 */
public class DefaultDataModelDefinition<T> implements DataModelDefinition<T> {

    static final Logger LOG = LoggerFactory.getLogger(DefaultDataModelDefinition.class);

    private static final Map<Class<?>, ValueDriver<?>> VALUE_DRIVERS;
    static {
        Map<Class<?>, ValueDriver<?>> map = new HashMap<>();
        map.put(BooleanOption.class, new ValueDriver<BooleanOption>(PropertyType.BOOLEAN) {
            @SuppressWarnings("deprecation")
            @Override
            void modify(BooleanOption holder, Object value) {
                holder.modify(cast(Boolean.class, value));
            }
            @Override
            Object extract(BooleanOption holder) {
                return holder.get();
            }
        });
        map.put(ByteOption.class, new ValueDriver<ByteOption>(PropertyType.BYTE) {
            @SuppressWarnings("deprecation")
            @Override
            void modify(ByteOption holder, Object value) {
                holder.modify(cast(Number.class, value).byteValue());
            }
            @Override
            Object extract(ByteOption holder) {
                return holder.get();
            }
        });
        map.put(ShortOption.class, new ValueDriver<ShortOption>(PropertyType.SHORT) {
            @SuppressWarnings("deprecation")
            @Override
            void modify(ShortOption holder, Object value) {
                holder.modify(cast(Number.class, value).shortValue());
            }
            @Override
            Object extract(ShortOption holder) {
                return holder.get();
            }
        });
        map.put(IntOption.class, new ValueDriver<IntOption>(PropertyType.INT) {
            @SuppressWarnings("deprecation")
            @Override
            void modify(IntOption holder, Object value) {
                holder.modify(cast(Number.class, value).intValue());
            }
            @Override
            Object extract(IntOption holder) {
                return holder.get();
            }
        });
        map.put(LongOption.class, new ValueDriver<LongOption>(PropertyType.LONG) {
            @SuppressWarnings("deprecation")
            @Override
            void modify(LongOption holder, Object value) {
                holder.modify(cast(Number.class, value).longValue());
            }
            @Override
            Object extract(LongOption holder) {
                return holder.get();
            }
        });
        map.put(FloatOption.class, new ValueDriver<FloatOption>(PropertyType.FLOAT) {
            @SuppressWarnings("deprecation")
            @Override
            void modify(FloatOption holder, Object value) {
                holder.modify(cast(Number.class, value).floatValue());
            }
            @Override
            Object extract(FloatOption holder) {
                return holder.get();
            }
        });
        map.put(DoubleOption.class, new ValueDriver<DoubleOption>(PropertyType.DOUBLE) {
            @SuppressWarnings("deprecation")
            @Override
            void modify(DoubleOption holder, Object value) {
                holder.modify(cast(Number.class, value).doubleValue());
            }
            @Override
            Object extract(DoubleOption holder) {
                return holder.get();
            }
        });
        map.put(DecimalOption.class, new ValueDriver<DecimalOption>(PropertyType.DECIMAL) {
            @SuppressWarnings("deprecation")
            @Override
            void modify(DecimalOption holder, Object value) {
                holder.modify(cast(BigDecimal.class, value));
            }
            @Override
            Object extract(DecimalOption holder) {
                return holder.get();
            }
        });
        map.put(StringOption.class, new ValueDriver<StringOption>(PropertyType.STRING) {
            @SuppressWarnings("deprecation")
            @Override
            void modify(StringOption holder, Object value) {
                holder.modify(cast(String.class, value));
            }
            @Override
            Object extract(StringOption holder) {
                return holder.getAsString();
            }
        });
        map.put(DateOption.class, new ValueDriver<DateOption>(PropertyType.DATE) {
            @SuppressWarnings("deprecation")
            @Override
            void modify(DateOption holder, Object value) {
                Calendar calendar = (Calendar) value;
                holder.modify(new Date(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DATE)));
            }
            @Override
            Object extract(DateOption holder) {
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(Calendar.YEAR, holder.get().getYear());
                calendar.set(Calendar.MONTH, holder.get().getMonth() - 1);
                calendar.set(Calendar.DATE, holder.get().getDay());
                return calendar;
            }
        });
        map.put(DateTimeOption.class, new ValueDriver<DateTimeOption>(PropertyType.DATETIME) {
            @SuppressWarnings("deprecation")
            @Override
            void modify(DateTimeOption holder, Object value) {
                Calendar calendar = (Calendar) value;
                holder.modify(new DateTime(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DATE),
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        calendar.get(Calendar.SECOND)));
            }
            @Override
            Object extract(DateTimeOption holder) {
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(Calendar.YEAR, holder.get().getYear());
                calendar.set(Calendar.MONTH, holder.get().getMonth() - 1);
                calendar.set(Calendar.DATE, holder.get().getDay());
                calendar.set(Calendar.HOUR_OF_DAY, holder.get().getHour());
                calendar.set(Calendar.MINUTE, holder.get().getMinute());
                calendar.set(Calendar.SECOND, holder.get().getSecond());
                return calendar;
            }
        });
        VALUE_DRIVERS = Collections.unmodifiableMap(map);
    }

    private final Class<T> modelClass;

    private final Map<PropertyName, Method> accessors;

    /**
     * Creates a new instance.
     * @param modelClass the original model class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DefaultDataModelDefinition(Class<T> modelClass) {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        this.modelClass = modelClass;
        this.accessors = extractAccessors();
    }

    private Map<PropertyName, Method> extractAccessors() {
        Map<PropertyName, Method> results = new TreeMap<>();
        for (Method method : modelClass.getMethods()) {
            if (VALUE_DRIVERS.containsKey(method.getReturnType()) == false) {
                continue;
            }
            PropertyName property = getPropertyNameIfAccessor(method);
            if (property == null) {
                continue;
            }
            results.put(property, method);
        }

        PropertyOrder annotation = modelClass.getAnnotation(PropertyOrder.class);
        if (annotation == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Annotation {0} is not defined in {1}", //$NON-NLS-1$
                        PropertyOrder.class.getSimpleName(),
                        modelClass.getName()));
            }
            return results;
        }
        Map<PropertyName, Method> ordered = new LinkedHashMap<>();
        for (String name : annotation.value()) {
            String[] words = name.split("(_|-)+"); //$NON-NLS-1$
            PropertyName propertyName = PropertyName.newInstance(words);
            Method method = results.remove(propertyName);
            if (method == null) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("DefaultDataModelDefinition.warnOrderContainUnknownProperty"), //$NON-NLS-1$
                        name, modelClass.getName()));
            } else {
                ordered.put(propertyName, method);
            }
        }
        if (results.isEmpty() == false) {
            LOG.warn(MessageFormat.format(
                    Messages.getString("DefaultDataModelDefinition.warnOrderNotCoverProperty"), //$NON-NLS-1$
                    results.keySet(), modelClass.getName()));
            ordered.putAll(results);
        }
        return ordered;
    }

    private static final Pattern PROPERTY_ACCESSOR = Pattern.compile("get([A-Z]\\w*)Option"); //$NON-NLS-1$

    private PropertyName getPropertyNameIfAccessor(Method method) {
        assert method != null;
        Matcher matcher = PROPERTY_ACCESSOR.matcher(method.getName());
        if (matcher.matches() == false) {
            return null;
        }
        String name = matcher.group(1);
        List<String> words = new ArrayList<>();
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
        if (annotationType == null) {
            throw new IllegalArgumentException("annotationType must not be null"); //$NON-NLS-1$
        }
        return modelClass.getAnnotation(annotationType);
    }

    @Override
    public Collection<PropertyName> getProperties() {
        return Collections.unmodifiableCollection(accessors.keySet());
    }

    @Override
    public PropertyType getType(PropertyName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        Method accessor = accessors.get(name);
        if (accessor == null) {
            return null;
        }
        ValueDriver<?> driver = VALUE_DRIVERS.get(accessor.getReturnType());
        if (driver == null) {
            throw new AssertionError(accessor.getReturnType());
        }
        return driver.valueType;
    }

    @Override
    public <A extends Annotation> A getAnnotation(PropertyName name, Class<A> annotationType) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (annotationType == null) {
            throw new IllegalArgumentException("annotationType must not be null"); //$NON-NLS-1$
        }
        Method accessor = accessors.get(name);
        if (accessor == null) {
            return null;
        }
        return accessor.getAnnotation(annotationType);
    }

    @Override
    public Builder<T> newReflection() {
        return new Builder<>(this);
    }

    @Override
    public DataModelReflection toReflection(T object) {
        Builder<T> builder = newReflection();
        try {
            for (Map.Entry<PropertyName, Method> entry : accessors.entrySet()) {
                PropertyName property = entry.getKey();
                Method accessor = entry.getValue();
                Object value = get(object, accessor);
                builder.add(property, value);
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return builder.build();
    }

    @Override
    public T toObject(DataModelReflection reflection) {
        try {
            T instance = modelClass.newInstance();
            for (Map.Entry<PropertyName, Method> entry : accessors.entrySet()) {
                PropertyName property = entry.getKey();
                Method accessor = entry.getValue();
                Object value = reflection.getValue(property);
                set(instance, accessor, value);
            }
            return instance;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private Object get(T instance, Method property) {
        assert instance != null;
        assert property != null;
        ValueOption<?> holder = getHolder(instance, property);
        if (holder.isNull()) {
            return null;
        }
        ValueDriver<?> driver = VALUE_DRIVERS.get(property.getReturnType());
        assert driver != null : property;
        return driver.extractUnsafe(holder);
    }

    @SuppressWarnings("deprecation")
    private void set(T instance, Method property, Object value) {
        assert instance != null;
        assert property != null;
        ValueOption<?> holder = getHolder(instance, property);
        if (value == null) {
            holder.setNull();
        } else {
            ValueDriver<?> driver = VALUE_DRIVERS.get(property.getReturnType());
            assert driver != null : property;
            driver.modifyUnsafe(holder, value);
        }
    }

    private ValueOption<?> getHolder(T instance, Method property) {
        assert instance != null;
        assert property != null;
        try {
            return (ValueOption<?>) property.invoke(instance);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public Object resolveRawValue(Object value) {
        if (value == null) {
            return null;
        }
        Class<?> type = value.getClass();
        ValueDriver<?> driver = VALUE_DRIVERS.get(type);
        if (driver == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported property type: {0} ({1})",
                    value, type.getSimpleName()));
        }
        if (((ValueOption<?>) value).isNull()) {
            return null;
        }
        return driver.extractUnsafe(value);
    }

    abstract static class ValueDriver<T> {

        final PropertyType valueType;

        ValueDriver(PropertyType valueType) {
            assert valueType != null;
            this.valueType = valueType;
        }

        @SuppressWarnings("unchecked")
        Object extractUnsafe(Object holder) {
            return extract((T) holder);
        }

        @SuppressWarnings("unchecked")
        void modifyUnsafe(Object holder, Object value) {
            modify((T) holder, value);
        }

        abstract Object extract(T holder);

        abstract void modify(T holder, Object value);

        protected <V> V cast(Class<V> type, Object value) {
            if (type.isInstance(value) == false) {
                throw new IllegalArgumentException();
            }
            return type.cast(value);
        }
    }
}
