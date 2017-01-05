/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;

import com.asakusafw.runtime.util.TypeUtil;
import com.asakusafw.vocabulary.batch.WorkDescription;

/**
 * A skeletal implementation of {@link WorkDescriptionProcessor}.
 * @param <T> the target {@link WorkDescription} class
 */
public abstract class AbstractWorkDescriptionProcessor<T extends WorkDescription>
        extends BatchCompilingEnvironment.Initialized
        implements WorkDescriptionProcessor<T> {

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getTargetType() {
        List<Type> typeArguments = TypeUtil.invoke(WorkDescriptionProcessor.class, getClass());
        if (typeArguments == null || typeArguments.size() != 1) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("AbstractWorkDescriptionProcessor.errorUnboundTypeParameter"), //$NON-NLS-1$
                    getClass().getName(),
                    WorkDescriptionProcessor.class.getName()));
        }
        Type first = typeArguments.get(0);
        if ((first instanceof Class<?>) == false) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("AbstractWorkDescriptionProcessor.errorTypeParameterNotClass"), //$NON-NLS-1$
                    getClass().getName(),
                    WorkDescriptionProcessor.class.getName(),
                    first));
        }
        return (Class<T>) first;
    }
}
