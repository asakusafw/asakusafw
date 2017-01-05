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
package com.asakusafw.utils.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * A {@link Serialization} using the built-in Java serialization facility.
 * Clients can inherit this class.
 * @param <T> the object type
 * @see Serializable
 * @since 0.6.0
 */
public class DefaultSerialization<T> implements Serialization<T> {

    @Override
    public Source<T> createSource(InputStream stream) throws IOException, InterruptedException {
        return new ObjectSource<>(createObjectInput(stream));
    }

    @Override
    public Sink<T> createSink(OutputStream stream) throws IOException, InterruptedException {
        return new ObjectSink<>(createObjectOutput(stream));
    }

    /**
     * Creates an {@link ObjectInput} object from the {@link InputStream}.
     * This implementation returns {@link ObjectInputStream}.
     * @param stream the source {@link InputStream}
     * @return the {@link ObjectInput} which takes objects from the source
     * @throws IOException if failed to create {@link ObjectInput} by I/O error
     * @throws InterruptedException if interrupted while preparing {@link ObjectInput}
     */
    protected ObjectInput createObjectInput(InputStream stream) throws IOException, InterruptedException {
        return new ObjectInputStream(stream);
    }

    /**
     * Creates an {@link ObjectOutput} object from the {@link OutputStream}.
     * This implementation returns {@link ObjectOutputStream}.
     * @param stream the target {@link OutputStream}
     * @return the {@link ObjectOutput} which puts objects into the target
     * @throws IOException if failed to create {@link ObjectOutput} by I/O error
     * @throws InterruptedException if interrupted while preparing {@link ObjectOutput}
     */
    protected ObjectOutput createObjectOutput(OutputStream stream) throws IOException, InterruptedException {
        return new ObjectOutputStream(stream);
    }

    /**
     * An {@link ObjectInput} adapter implementation of {@link Source}.
     * @param <T> the object type
     */
    public static final class ObjectSource<T> implements Source<T> {

        private final ObjectInput input;

        private boolean prepared = false;

        private T next = null;

        /**
         * Creates a new instance.
         * @param input the source {@link ObjectInput}
         */
        public ObjectSource(ObjectInput input) {
            this.input = input;
        }

        @Override
        public boolean next() throws IOException {
            try {
                @SuppressWarnings("unchecked")
                T object = (T) input.readObject();
                prepared = true;
                next = object;
                return true;
            } catch (EOFException e) {
                prepared = false;
                next = null;
                return false;
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }

        @Override
        public T get() throws IOException {
            if (prepared == false) {
                throw new NoSuchElementException();
            }
            return next;
        }

        @Override
        public void close() throws IOException {
            prepared = false;
            next = null;
            input.close();
        }
    }

    /**
     * An {@link ObjectOutput} adapter implementation of {@link Sink}.
     * @param <T> the object type
     */
    public static final class ObjectSink<T> implements Sink<T> {

        private final ObjectOutput output;

        /**
         * Creates a new instance.
         * @param output the target {@link ObjectOutput}.
         */
        public ObjectSink(ObjectOutput output) {
            this.output = output;
        }

        @Override
        public void put(T object) throws IOException, InterruptedException {
            output.writeObject(object);
        }

        @Override
        public void flush() throws IOException {
            output.flush();
        }

        @Override
        public void close() throws IOException {
            output.close();
        }
    }
}
