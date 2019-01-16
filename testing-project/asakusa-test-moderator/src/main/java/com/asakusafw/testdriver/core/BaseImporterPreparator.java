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
package com.asakusafw.testdriver.core;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;

import com.asakusafw.runtime.util.TypeUtil;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Abstract implementation of {@link ImporterPreparator}.
 * @param <T> type of target {@link ImporterDescription}
 * @since 0.2.2
 */
public abstract class BaseImporterPreparator<T extends ImporterDescription>
        implements ImporterPreparator<T> {

    /**
     * Returns the target {@link ImporterDescription} type from the class inheritance hierarchy.
     * @return the suitable type
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getDescriptionClass() {
        List<Type> arguments = TypeUtil.invoke(BaseImporterPreparator.class, getClass());
        if (arguments.size() != 1) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("BaseImporterPreparator.errorUnboundTypeParameter"), //$NON-NLS-1$
                    ImporterPreparator.class.getName(),
                    getClass().getName()));
        }
        Type first = arguments.get(0);
        if ((first instanceof Class<?>) == false
                || ImporterDescription.class.isAssignableFrom((Class<?>) first) == false) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("BaseImporterPreparator.errorInvalidTypeParameter"), //$NON-NLS-1$
                    ImporterDescription.class.getName(),
                    getClass().getName()));
        }
        return (Class<T>) first;
    }
}
