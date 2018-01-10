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
package com.asakusafw.dmdl.source;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.asakusafw.utils.collections.Lists;

/**
 * Enumerated {@link DmdlSourceRepository}.
 */
public class CompositeSourceRepository implements DmdlSourceRepository {

    private final List<DmdlSourceRepository> repositories;

    /**
     * Creates and returns a new instance.
     * @param repositories to composite
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CompositeSourceRepository(List<? extends DmdlSourceRepository> repositories) {
        if (repositories == null) {
            throw new IllegalArgumentException("repositories must not be null"); //$NON-NLS-1$
        }
        this.repositories = Lists.freeze(repositories);
    }

    @Override
    public Cursor createCursor() throws IOException {
        return new CompositeCursor(repositories.iterator());
    }

    private static class CompositeCursor implements Cursor {

        private final Iterator<DmdlSourceRepository> rest;

        private Cursor current;

        CompositeCursor(Iterator<DmdlSourceRepository> iterator) {
            assert iterator != null;
            this.rest = iterator;
            this.current = null;
        }

        @Override
        public boolean next() throws IOException {
            if (current == null) {
                if (rest.hasNext()) {
                    current = rest.next().createCursor();
                } else {
                    return false;
                }
            }
            assert current != null;
            while (true) {
                if (current.next()) {
                    return true;
                }
                if (rest.hasNext()) {
                    current = rest.next().createCursor();
                } else {
                    current = null;
                    return false;
                }
            }
        }

        @Override
        public URI getIdentifier() throws IOException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            return current.getIdentifier();
        }

        @Override
        public Reader openResource() throws IOException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            return current.openResource();
        }

        @Override
        public void close() throws IOException {
            if (current == null) {
                return;
            }
            current.close();
            current = null;
            while (rest.hasNext()) {
                rest.next();
            }
        }
    }
}
