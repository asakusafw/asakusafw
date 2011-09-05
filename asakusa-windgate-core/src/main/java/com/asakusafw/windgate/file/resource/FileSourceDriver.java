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
package com.asakusafw.windgate.file.resource;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;

import com.asakusafw.windgate.core.resource.SourceDriver;

/**
 * An implementation for {@link SourceDriver} using {@link ObjectInputStream}.
 * @param <T> the type of target data
 * @since 0.2.2
 */
class FileSourceDriver<T> implements SourceDriver<T> {

    private final Class<T> type;

    private final File file;

    private ObjectInputStream input;

    private boolean canGet;

    private T next;

    /**
     * Creates a new instance.
     * @param type the type of target data
     * @param file the target file
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public FileSourceDriver(Class<T> type, File file) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (file == null) {
            throw new IllegalArgumentException("file must not be null"); //$NON-NLS-1$
        }
        this.type = type;
        this.file = file;
    }

    @Override
    public void prepare() throws IOException {
        boolean green = false;
        FileInputStream in = new FileInputStream(file);
        try {
            this.input = new LoadingObjectInputStream(in, type.getClassLoader());
            green = true;
        } finally {
            if (green == false) {
                in.close();
            }
        }
        this.next = null;
        this.canGet = false;
    }

    @Override
    public boolean next() throws IOException {
        try {
            Object object = input.readObject();
            next = type.cast(object);
            canGet = true;
            return true;
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } catch (EOFException e) {
            next = null;
            canGet = true;
            return false;
        } catch (OptionalDataException e) {
            if (e.eof) {
                next = null;
                canGet = true;
                return false;
            }
            throw e;
        }
    }

    @Override
    public T get() throws IOException {
        if (canGet) {
            return next;
        }
        throw new IOException();
    }

    @Override
    public void close() throws IOException {
        if (input != null) {
            input.close();
        }
        input = null;
    }
}
