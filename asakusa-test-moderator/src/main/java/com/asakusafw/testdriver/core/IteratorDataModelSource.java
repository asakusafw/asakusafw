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
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.util.Iterator;

/**
 * Volatile {@link DataModelSource} implementation via the {@link Iterator} interface.
 * @since 0.2.0
 */
public class IteratorDataModelSource implements DataModelSource {

    private final Iterator<? extends DataModelReflection> iterator;

    /**
     * Creates a new instance.
     * @param iterator entity iterator
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public IteratorDataModelSource(Iterator<? extends DataModelReflection> iterator) {
        if (iterator == null) {
            throw new IllegalArgumentException("iterator must not be null"); //$NON-NLS-1$
        }
        this.iterator = iterator;
    }

    /**
     * Creates a new instance.
     * @param <E> type of data model
     * @param definition the data model definition
     * @param iterator data model objects
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <E> IteratorDataModelSource(DataModelDefinition<E> definition, Iterator<? extends E> iterator) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (iterator == null) {
            throw new IllegalArgumentException("iterator must not be null"); //$NON-NLS-1$
        }
        this.iterator = new IteratorDriver<E>(definition, iterator);
    }

    @Override
    public DataModelReflection next() {
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        return;
    }

    private static class IteratorDriver<E> implements Iterator<DataModelReflection> {

        private final DataModelDefinition<? super E> definition;

        private final Iterator<? extends E> iterator;

        /**
         * Creates a new instance.
         * @param definition the data model definition
         * @param iterator a data model iterator
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public IteratorDriver(DataModelDefinition<? super E> definition, Iterator<? extends E> iterator) {
            if (definition == null) {
                throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
            }
            if (iterator == null) {
                throw new IllegalArgumentException("iterator must not be null"); //$NON-NLS-1$
            }
            this.definition = definition;
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public DataModelReflection next() {
            E next = iterator.next();
            return definition.toReflection(next);
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
