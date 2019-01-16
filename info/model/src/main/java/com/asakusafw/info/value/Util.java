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
package com.asakusafw.info.value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class Util {

    private Util() {
        return;
    }

    private static final Map<Class<?>, Function<Object, ValueInfo>> CONVERTERS;
    static {
        Map<Class<?>, Function<Object, ValueInfo>> map = new HashMap<>();
        converter(map, BooleanInfo::of, Boolean.class);
        converter(map, ByteInfo::of, Byte.class);
        converter(map, ShortInfo::of, Short.class);
        converter(map, IntInfo::of, Integer.class);
        converter(map, LongInfo::of, Long.class);
        converter(map, FloatInfo::of, Float.class);
        converter(map, DoubleInfo::of, Double.class);
        converter(map, CharInfo::of, Character.class);
        converter(map, StringInfo::of, String.class);
        converter(map, ClassInfo::of, Class.class);
        CONVERTERS = map;
    }

    private static <T> void converter(
            Map<Class<?>, Function<Object, ValueInfo>> map,
            Function<T, ValueInfo> converter,
            Class<T> target) {
        map.put(target, it -> converter.apply(target.cast(it)));
    }

    static ValueInfo convert(Object value) {
        if (value == null) {
            return NullInfo.get();
        }
        Function<Object, ValueInfo> converter = CONVERTERS.get(value.getClass());
        if (converter != null) {
            return converter.apply(value);
        } else if (value instanceof Iterable<?>) {
            List<ValueInfo> elements = new ArrayList<>();
            ((Iterable<?>) value).forEach(it -> elements.add(convert(it)));
            return ListInfo.of(elements);
        } else if (value.getClass().isArray()) {
            ValueInfo[] elements = new ValueInfo[Array.getLength(value)];
            for (int i = 0; i < elements.length; i++) {
                elements[i] = convert(Array.get(value, i));
            }
            return ListInfo.of(Arrays.asList(elements));
        } else if (value instanceof Enum<?>) {
            return EnumInfo.of((Enum<?>) value);
        } else if (value instanceof Annotation) {
            return convert0((Annotation) value);
        } else {
            return UnknownInfo.of(value);
        }
    }

    private static ValueInfo convert0(Annotation annotation) {
        try {
            Class<? extends Annotation> declaring = annotation.annotationType();
            Map<String, ValueInfo> elements = new LinkedHashMap<>();
            for (Method method : declaring.getMethods()) {
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (method.getDeclaringClass() != declaring || method.isSynthetic()) {
                    continue;
                }
                if (method.getParameterTypes().length != 0) {
                    continue;
                }
                Object v = method.invoke(annotation);
                elements.put(method.getName(), convert(v));
            }
            return AnnotationInfo.of(ClassInfo.of(declaring), elements);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "failed to analyze annotation: {0}", //$NON-NLS-1$
                    annotation), e);
        }
    }
}
