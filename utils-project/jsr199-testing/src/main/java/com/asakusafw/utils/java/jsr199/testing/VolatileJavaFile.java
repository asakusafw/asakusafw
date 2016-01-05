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
package com.asakusafw.utils.java.jsr199.testing;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

/**
 * Represents a Java source file which is stored on the heap.
 */
public class VolatileJavaFile extends SimpleJavaFileObject {

    /**
     * The schema name of this kind of resources.
     */
    public static final String URI_SCHEME = VolatileJavaFile.class.getName();

    volatile String contents;

    /**
     * Creates a new instance with empty contents.
     * @param path the relative path from the source path (do not end with {@code .java})
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public VolatileJavaFile(String path) {
        this(path, ""); //$NON-NLS-1$
    }

    /**
     * Creates a new instance with the specified contents.
     * @param path the relative path from the source path (do not end with {@code .java})
     * @param contents the file contents
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public VolatileJavaFile(String path, String contents) {
        super(toUriFromPath(path), JavaFileObject.Kind.SOURCE);
        if (contents == null) {
            throw new IllegalArgumentException("contents must not be null"); //$NON-NLS-1$
        }
        this.contents = contents;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return contents;
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) {
        return new StringReader(contents);
    }

    @Override
    public Writer openWriter() {
        this.contents = ""; //$NON-NLS-1$
        return new StringWriter() {
            @Override
            public void close() {
                contents = toString();
            }
        };
    }

    private static URI toUriFromPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        try {
            return new URI(
                URI_SCHEME,
                null,
                "/" + path.replace('\\', '/') + JavaFileObject.Kind.SOURCE.extension, //$NON-NLS-1$
                null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
