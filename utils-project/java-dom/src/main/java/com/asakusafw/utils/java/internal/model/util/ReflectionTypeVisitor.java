/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.text.MessageFormat;

/**
 * Visitors for processing implementations of {@link java.lang.reflect.Type}.
 * Each {@code visit*} method in this implementation does nothing and always returns {@code null}.
 * @param <C> type of visitor context
 * @param <R> type of visitor result
 * @param <E> type of visitor exception
 */
public abstract class ReflectionTypeVisitor<R, C, E extends Throwable> {

    /**
     * Dispatches the suitable {@code visit*} method.
     * This can dispatch the following methods:
     * <ul>
     *   <li>
     *     {@link Class} -
     *     {@link #visitClass(Class, Object) visitClass(type, context)}
     *   </li>
     *   <li>
     *     {@link GenericArrayType} -
     *     {@link #visitGenericArrayType(GenericArrayType, Object) visitGenericArrayType(type, context)}
     *   </li>
     *   <li>
     *     {@link ParameterizedType} -
     *     {@link #visitParameterizedType(ParameterizedType, Object) visitParameterizedType(type, context)}
     *   </li>
     *   <li>
     *     {@link TypeVariable} -
     *     {@link #visitTypeVariable(TypeVariable, Object) visitTypeVariable(type, context)}
     *   </li>
     *   <li>
     *     {@link WildcardType} -
     *     {@link #visitWildcardType(WildcardType, Object) visitWildcardType(type, context)}
     *   </li>
     * </ul>
     * @param type the type to be dispatched
     * @param context the current context (nullable)
     * @return the processing result in {@code visit*}
     * @throws E if error was occurred while processing the target element {@code visit*}
     * @throws IllegalArgumentException
     *     if the {@code type} is neither
     *     {@link Class},
     *     {@link GenericArrayType},
     *     {@link ParameterizedType},
     *     {@link TypeVariable}, nor
     *     {@link WildcardType}
     */
    public final R dispatch(Type type, C context) throws E {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type instanceof Class<?>) {
            return visitClass((Class<?>) type, context);
        } else if (type instanceof GenericArrayType) {
            return visitGenericArrayType((GenericArrayType) type, context);
        } else if (type instanceof ParameterizedType) {
            return visitParameterizedType((ParameterizedType) type, context);
        } else if (type instanceof TypeVariable<?>) {
            return visitTypeVariable((TypeVariable<?>) type, context);
        } else if (type instanceof WildcardType) {
            return visitWildcardType((WildcardType) type, context);
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                "Unknown Type Kind: {0} ({1})",
                type,
                type.getClass().getSimpleName()));
        }
    }

    /**
     * Processes {@link Class} using this visitor.
     * @param type the processing target
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target type
     * @see #dispatch(Type, Object)
     */
    protected R visitClass(Class<?> type, C context) throws E {
        return null;
    }

    /**
     * Processes {@link GenericArrayType} using this visitor.
     * @param type the processing target
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target type
     * @see #dispatch(Type, Object)
     */
    protected R visitGenericArrayType(GenericArrayType type, C context) throws E {
        return null;
    }

    /**
     * Processes {@link ParameterizedType} using this visitor.
     * @param type the processing target
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target type
     * @see #dispatch(Type, Object)
     */
    protected R visitParameterizedType(ParameterizedType type, C context) throws E {
        return null;
    }

    /**
     * Processes {@link TypeVariable} using this visitor.
     * @param type the processing target
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target type
     * @see #dispatch(Type, Object)
     */
    protected R visitTypeVariable(TypeVariable<?> type, C context) throws E {
        return null;
    }

    /**
     * Processes {@link WildcardType} using this visitor.
     * @param type the processing target
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target type
     * @see #dispatch(Type, Object)
     */
    protected R visitWildcardType(WildcardType type, C context) throws E {
        return null;
    }
}
