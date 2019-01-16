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
package com.asakusafw.vocabulary.flow.builder;

import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;

/**
 * Represents operator attribute information.
 * @since 0.9.0
 */
public class AttributeInfo {

    private static final Set<Class<?>> SUPPORTED;
    static {
        Set<Class<?>> set = new LinkedHashSet<>();
        set.add(FlowElementAttribute.class);
        SUPPORTED = set;
    }

    private final Object attribute;

    /**
     * Creates a new instance.
     * @param attribute the target attribute
     */
    public AttributeInfo(Object attribute) {
        Objects.requireNonNull(attribute, "attribute must not be null"); //$NON-NLS-1$
        checkSupported(attribute.getClass());
        this.attribute = attribute;
    }

    /**
     * Returns the attribute as the target adapter type.
     * @param <T> the adapter type
     * @param adapterType the adapter type
     * @return the attribute adapter
     */
    public <T> T getAdapter(Class<T> adapterType) {
        checkSupported(adapterType);
        return adapterType.cast(attribute);
    }

    private static void checkSupported(Class<?> type) {
        for (Class<?> aClass : SUPPORTED) {
            if (aClass.isAssignableFrom(type)) {
                return;
            }
        }
        throw new IllegalArgumentException(MessageFormat.format(
                "unsupported attribute type: {0}",
                type.getName()));
    }
}
