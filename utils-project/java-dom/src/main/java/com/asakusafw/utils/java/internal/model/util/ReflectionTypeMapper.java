/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.utils.java.model.syntax.BasicTypeKind;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.NoThrow;

/**
 * {@link java.lang.reflect.Type}をモデルに変換する。
 */
public class ReflectionTypeMapper
        extends ReflectionTypeVisitor<Type, ModelFactory, NoThrow> {

    private static final Map<Class<?>, BasicTypeKind> BASIC_TYPES;
    static {
        Map<Class<?>, BasicTypeKind> map =
            new HashMap<Class<?>, BasicTypeKind>();
        for (BasicTypeKind kind : BasicTypeKind.values()) {
            map.put(
                kind.getJavaRepresentation(),
                kind);
        }
        BASIC_TYPES = Collections.unmodifiableMap(map);
    }

    @Override
    protected Type visitClass(Class<?> type, ModelFactory context) {
        if (BASIC_TYPES.containsKey(type)) {
            return context.newBasicType(BASIC_TYPES.get(type));
        }
        if (type.isArray()) {
            return context.newArrayType(visitClass(type.getComponentType(), context));
        }
        String name = type.getName().replace('$', '.');
        return context.newNamedType(Models.toName(context, name));
    }

    @Override
    protected Type visitGenericArrayType(
            GenericArrayType type,
            ModelFactory context) {
        Type component = dispatch(type.getGenericComponentType(), context);
        return context.newArrayType(component);
    }

    @Override
    protected Type visitParameterizedType(
            ParameterizedType type,
            ModelFactory context) {
        java.lang.reflect.Type owner = type.getOwnerType();
        java.lang.reflect.Type rawType = type.getRawType();
        Type candidate;
        if (owner == null || owner instanceof Class<?>) {
            candidate = dispatch(rawType, context);
        } else {
            Type enclosing = dispatch(owner, context);
            assert rawType instanceof Class<?> : rawType;
            candidate = context.newQualifiedType(
                enclosing,
                context.newSimpleName(((Class<?>) rawType).getSimpleName()));
        }

        List<Type> typeArguments = new ArrayList<Type>();
        for (java.lang.reflect.Type t : type.getActualTypeArguments()) {
            typeArguments.add(dispatch(t, context));
        }
        return context.newParameterizedType(candidate, typeArguments);
    }

    @Override
    protected Type visitTypeVariable(TypeVariable<?> type, ModelFactory context) {
        return context.newNamedType(context.newSimpleName(type.getName()));
    }

    @Override
    protected Type visitWildcardType(WildcardType type, ModelFactory context) {
        java.lang.reflect.Type[] lower = type.getLowerBounds();
        if (lower.length == 1) {
            return context.newWildcard(
                WildcardBoundKind.LOWER_BOUNDED,
                dispatch(lower[0], context));
        }
        java.lang.reflect.Type[] upper = type.getUpperBounds();
        if (upper.length == 1 && upper[0] != Object.class) {
            return context.newWildcard(
                WildcardBoundKind.UPPER_BOUNDED,
                dispatch(upper[0], context));
        }

        return context.newWildcard(
            WildcardBoundKind.UNBOUNDED,
            null);
    }
}
