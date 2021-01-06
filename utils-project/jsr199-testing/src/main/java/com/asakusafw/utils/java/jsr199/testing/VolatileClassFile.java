/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

/**
 * Represents a Java class file which is stored on the heap.
 */
public class VolatileClassFile extends SimpleJavaFileObject {

    /**
     * The schema name of this kind of resources.
     */
    public static final String SCHEME = VolatileClassFile.class.getName();

    private final String binaryName;

    volatile byte[] contents;

    /**
     * Creates a new instance with empty contents.
     * @param binaryName the binary name of the target class
     * @throws IllegalArgumentException if the binary name is invalid
     */
    public VolatileClassFile(String binaryName) {
        this(binaryName, new byte[0]);
    }

    /**
     * Creates a new instance with the specified contents.
     * @param binaryName the binary name of the target class
     * @param contents the class file image
     * @throws IllegalArgumentException if the binary name is invalid
     */
    public VolatileClassFile(String binaryName, byte[] contents) {
        super(toUri(binaryName), JavaFileObject.Kind.CLASS);
        assert binaryName != null;
        if (contents == null) {
            throw new IllegalArgumentException("contents must not be null"); //$NON-NLS-1$
        }
        this.binaryName = binaryName;
        this.contents = contents.clone();
    }

    private static URI toUri(String binaryName) {
        if (binaryName == null) {
            throw new IllegalArgumentException("binaryName must not be null"); //$NON-NLS-1$
        }
        String path = toPath(binaryName);
        try {
            return new URI(
                SCHEME,
                null,
                "/" + path, //$NON-NLS-1$
                null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                MessageFormat.format(
                    "Invalid binary name \"{0}\"",
                    binaryName),
                e);
        }
    }

    private static String toPath(String binaryName) {
        assert binaryName != null;
        String path = binaryName.replace('.', '/');
        return path + JavaFileObject.Kind.CLASS.extension;
    }

    @Override
    public InputStream openInputStream() {
        return new ByteArrayInputStream(contents);
    }

    @Override
    public OutputStream openOutputStream() {
        contents = new byte[0];
        return new ByteArrayOutputStream() {
            @Override
            public void close() {
                contents = toByteArray();
            }
        };
    }

    /**
     * Returns the binary name of this class file.
     * @return the class binary name
     */
    public String getBinaryName() {
        return binaryName;
    }

    /**
     * Returns the contents of this class file.
     * @return the class file contents, or an empty array if the contents have never been set
     */
    public byte[] getBinaryContent() {
        return contents.clone();
    }
}
