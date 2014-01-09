/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.windgate.core.resource;

import java.io.IOException;
import java.util.Iterator;

/**
 * Mock {@link SourceDriver}.
 * @param <T> the type of contents
 */
public class MockSourceDriver<T> implements SourceDriver<T> {

    final String name;

    private Iterable<? extends T> source;

    private Iterator<? extends T> iterator;

    private boolean canGet;

    private T nextResult;

    /**
     * Creates a new instance.
     * @param name the name
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public MockSourceDriver(String name) {
        this.name = name;
        this.canGet = false;
        this.nextResult = null;
    }

    /**
     * Sets a source.
     * @param iterable a source
     */
    public void setIterable(Iterable<? extends T> iterable) {
        this.source = iterable;
    }

    @Override
    public void prepare() throws IOException {
        if (source == null) {
            throw new IOException("please setIterable() first");
        }
        this.iterator = source.iterator();
        this.canGet = false;
        this.nextResult = null;
    }

    @Override
    public boolean next() throws IOException {
        if (iterator == null) {
            throw new IOException();
        }
        if (iterator.hasNext()) {
            nextResult = iterator.next();
            canGet = true;
        } else {
            nextResult = null;
            canGet = false;
        }
        return canGet;
    }

    @Override
    public T get() throws IOException {
        if (canGet) {
            return nextResult;
        }
        throw new IllegalStateException();
    }

    @Override
    public void close() throws IOException {
        while (next()) {
            // do nothig
        }
        this.iterator = null;
    }
}
