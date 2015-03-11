/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 型に関するユーティリティ。
 */
public final class TypeUtil {

    /**
     * 指定のクラス型またはインターフェース型から派生した型の消去型を返す。
     * @param type 対象のクラス型またはインターフェース型
     * @return 対応する消去型
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static Class<?> erase(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        GenericContext generic = toGenericContext(type);
        if (generic == null) {
            throw new IllegalArgumentException("type must be a class or interface type");
        }
        return generic.raw;
    }

    /**
     * 指定の型を起動する。
     * @param target 起動する型
     * @param context 起動に利用するコンテキスト
     * @return 起動した型
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static List<Type> invoke(Class<?> target, Type context) {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (target.isPrimitive() || target.isArray()) {
            throw new IllegalArgumentException("target must be a class or interface type");
        }
        if (target == Object.class) {
            return Collections.emptyList();
        }

        GenericContext generic = toGenericContext(context);
        if (generic == null) {
            throw new IllegalArgumentException("context must be a class or interface type");
        }
        if (target.isAssignableFrom(generic.raw) == false) {
            return null;
        }
        if (target.getTypeParameters().length == 0) {
            return Collections.emptyList();
        }

        if (target.isInterface()) {
            return invokeInterface(target, generic);
        }
        if (generic.raw.isInterface()) {
            return null;
        }
        return invokeClass(target, generic);
    }

    private static List<Type> invokeClass(Class<?> target, GenericContext context) {
        assert target != null;
        assert context != null;
        assert target.isInterface() == false;
        assert context.raw.isInterface() == false;
        for (GenericContext current = context.getSuperClass();
                current != null;
                current = current.getSuperClass()) {
            if (current.raw == target) {
                return current.getTypeArguments();
            }
        }
        return null;
    }

    private static List<Type> invokeInterface(Class<?> target, GenericContext context) {
        assert target != null;
        assert context != null;
        assert target.isInterface();
        GenericContext bottom = findBottomClass(target, context);
        if (bottom == null) {
            return null;
        }
        if (target == bottom.raw) {
            return bottom.getTypeArguments();
        }
        return findInterface(target, bottom);
    }

    private static List<Type> findInterface(
            Class<?> target,
            GenericContext context) {
        assert target != null;
        assert context != null;
        assert target.isAssignableFrom(context.raw);
        Iterator<GenericContext> iter = context.getSuperInterfaces();
        while (iter.hasNext()) {
            GenericContext intf = iter.next();
            if (target == intf.raw) {
                return intf.getTypeArguments();
            }
            if (target.isAssignableFrom(intf.raw)) {
                return findInterface(target, intf);
            }
        }
        throw new AssertionError(target);
    }

    private static GenericContext findBottomClass(
            Class<?> target,
            GenericContext context) {
        assert target != null;
        assert context != null;
        GenericContext bottom = null;
        for (GenericContext current = context;
                current != null;
                current = current.getSuperClass()) {
            if (target.isAssignableFrom(current.raw)) {
                bottom = current;
            } else {
                break;
            }
        }
        return bottom;
    }

    private static GenericContext toGenericContext(Type context) {
        assert context != null;
        if (context instanceof Class<?>) {
            return new GenericContext((Class<?>) context);
        }
        if (context instanceof ParameterizedType) {
            // 簡易版なのでQualified Typeについては考慮しない
            ParameterizedType t = (ParameterizedType) context;
            Class<?> raw = (Class<?>) t.getRawType();
            TypeVariable<?>[] params = raw.getTypeParameters();
            Type[] args = t.getActualTypeArguments();
            if (params.length != args.length) {
                return new GenericContext(raw);
            }
            LinkedHashMap<TypeVariable<?>, Type> mapping =
                new LinkedHashMap<TypeVariable<?>, Type>();
            for (int i = 0; i < params.length; i++) {
                mapping.put(params[i], args[i]);
            }
            return new GenericContext(raw, mapping);
        }
        return null;
    }

    /**
     * インスタンス生成の禁止。
     */
    private TypeUtil() {
        throw new AssertionError();
    }

    static GenericContext analyze(
            Type type,
            Map<TypeVariable<?>, Type> mapping) {
        assert type != null;
        assert mapping != null;
        Type subst = substitute(type, mapping);
        if (subst == null) {
            return null;
        }
        return toGenericContext(subst);
    }

    private static Type substitute(
            Type type,
            Map<TypeVariable<?>, Type> mapping) {
        assert type != null;
        assert mapping != null;
        if (type instanceof Class<?>) {
            return type;
        }
        if (type instanceof TypeVariable<?>) {
            // 簡易版のため無限型について考慮しない
            return mapping.get(type);
        }
        if (type instanceof ParameterizedType) {
            // 簡易版のためQualified Typeについては考慮しない
            ParameterizedType pt = (ParameterizedType) type;
            Class<?> raw = (Class<?>) pt.getRawType();
            List<Type> arguments = new ArrayList<Type>();
            for (Type t : pt.getActualTypeArguments()) {
                Type subst = substitute(t, mapping);
                if (subst == null) {
                    return raw;
                }
                arguments.add(subst);
            }
            return new SimpleParameterizedType(raw, arguments);
        }
        return null;
    }

    private static class GenericContext {

        final Class<?> raw;

        final LinkedHashMap<TypeVariable<?>, Type> mapping;

        GenericContext(Class<?> raw) {
            this.raw = raw;
            this.mapping = new LinkedHashMap<TypeVariable<?>, Type>();
        }

        GenericContext(
                Class<?> raw,
                LinkedHashMap<TypeVariable<?>, Type> mapping) {
            this.raw = raw;
            this.mapping = mapping;
        }

        public List<Type> getTypeArguments() {
            return new ArrayList<Type>(mapping.values());
        }

        public GenericContext getSuperClass() {
            if (raw.getSuperclass() == null) {
                return null;
            }
            Type parent = raw.getGenericSuperclass();
            return analyze(parent, mapping);
        }

        public Iterator<GenericContext> getSuperInterfaces() {
            return new Iterator<TypeUtil.GenericContext>() {
                Type[] interfaces = raw.getGenericInterfaces();
                int index = 0;

                @Override
                public boolean hasNext() {
                    return index < interfaces.length;
                }

                @Override
                public GenericContext next() {
                    if (hasNext() == false) {
                        throw new NoSuchElementException();
                    }
                    return analyze(interfaces[index++], mapping);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static class SimpleParameterizedType implements ParameterizedType {

        private final Class<?> rawType;

        private final List<Type> typeArguments;

        SimpleParameterizedType(
                Class<?> rawType,
                List<Type> typeArguments) {
            this.rawType = rawType;
            this.typeArguments = typeArguments;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.toArray(new Type[typeArguments.size()]);
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return rawType.getDeclaringClass();
        }
    }
}
