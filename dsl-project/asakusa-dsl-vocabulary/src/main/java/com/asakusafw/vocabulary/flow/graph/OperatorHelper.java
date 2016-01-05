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
package com.asakusafw.vocabulary.flow.graph;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

/**
 * Represents an operator helper method.
 */
public final class OperatorHelper implements FlowElementAttribute {

    private final String name;

    private final List<Class<?>> parameterTypes;

    /**
     * Creates a new instance.
     * @param name the name of the operator helper method
     * @param parameterTypes the erased parameter types of the operator helper method
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public OperatorHelper(String name, List<Class<?>> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public Class<? extends FlowElementAttribute> getDeclaringClass() {
        return OperatorHelper.class;
    }

    /**
     * Returns the name of the operator helper method.
     * @return the method name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the erased parameter types of the operator helper method.
     * @return the parameter types
     */
    public List<Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Returns a reflective object of the target operator helper method.
     * @param owner information of the owner operator method declaration
     * @return a reflective object of the target operator helper method, or {@code null} if there is no such a method
     */
    public Method toMethod(OperatorDescription.Declaration owner) {
        if (owner == null) {
            throw new IllegalArgumentException("owner must not be null"); //$NON-NLS-1$
        }
        Class<?>[] params = parameterTypes.toArray(new Class<?>[parameterTypes.size()]);
        try {
            return owner.getDeclaring().getMethod(name, params);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "#{0}({1})", //$NON-NLS-1$
                name,
                parameterTypes);
    }
}
