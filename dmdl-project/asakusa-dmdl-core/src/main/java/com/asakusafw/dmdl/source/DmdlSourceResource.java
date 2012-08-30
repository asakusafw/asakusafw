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
package com.asakusafw.dmdl.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.asakusafw.utils.collections.Lists;

/**
 * DMDL source repository includes list of URLs.
 */
public class DmdlSourceResource implements DmdlSourceRepository {

    private final List<URL> resources;

    private final Charset encoding;

    /**
     * Creates and returns a new instance.
     * @param sourceFiles  the target source files
     * @param encoding the charset of each source file
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DmdlSourceResource(List<URL> sourceFiles, Charset encoding) {
        if (sourceFiles == null) {
            throw new IllegalArgumentException("sourceFiles must not be null"); //$NON-NLS-1$
        }
        if (encoding == null) {
            throw new IllegalArgumentException("encoding must not be null"); //$NON-NLS-1$
        }
        this.resources = Lists.freeze(sourceFiles);
        this.encoding = encoding;
    }

    @Override
    public Cursor createCursor() throws IOException {
        return new UrlListCursor(resources.iterator(), encoding);
    }

    static class UrlListCursor implements Cursor {

        private final Iterator<URL> rest;

        private final Charset encoding;

        private URL current;

        UrlListCursor(Iterator<URL> iterator, Charset encoding) {
            assert iterator != null;
            assert encoding != null;
            this.current = null;
            this.rest = iterator;
            this.encoding = encoding;
        }

        @Override
        public boolean next() throws IOException {
            if (rest.hasNext()) {
                current = rest.next();
                return true;
            } else {
                current = null;
                return false;
            }
        }

        @Override
        public URI getIdentifier() throws IOException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            try {
                return current.toURI();
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        @Override
        public Reader openResource() throws IOException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            InputStream in = current.openStream();
            return new InputStreamReader(in, encoding);
        }

        @Override
        public void close() {
            current = null;
            while (rest.hasNext()) {
                rest.next();
                rest.remove();
            }
        }
    }
}
