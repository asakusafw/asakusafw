/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.util;

import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.DataInput;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

/**
 * Base test class for buffered files.
 */
public class BufferedFileTestRoot {

    /**
     * a temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * closes all resources.
     */
    @Rule
    public final ExternalResource resourceCloser = new ExternalResource() {
        @Override
        protected void after() {
            for (Closeable closeable : closeables) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    final List<Closeable> closeables = new ArrayList<Closeable>();

    /**
     * Manages the target resource.
     * @param resource resource
     * @param <T> the target type
     * @return resource
     */
    public <T> T manage(T resource) {
        if (resource instanceof Closeable) {
            closeables.add((Closeable) resource);
        }
        return resource;
    }

    /**
     * Creates and returns a new empty file.
     * @return the created file
     * @throws IOException if failed
     */
    public RandomAccessFile file() throws IOException {
        File file = folder.newFile();
        return manage(new RandomAccessFile(file, "rw"));
    }

    /**
     * Creates a new byte array.
     * @param values int array
     * @return created array
     */
    public byte[] bytes(int... values) {
        byte[] results = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            results[i] = (byte) values[i];
        }
        return results;
    }

    /**
     * Creates a new byte array.
     * @param from minimum
     * @param to maximum
     * @return created array
     */
    public byte[] range(int from, int to) {
        byte[] results = new byte[to - from + 1];
        for (int i = 0; i < results.length; i++) {
            results[i] = (byte) (from + i);
        }
        return results;
    }

    /**
     * Tests is end of file.
     * @param input input
     * @throws IOException if failed
     */
    public void eof(DataInput input) throws IOException {
        try {
            input.readByte();
            fail();
        } catch (EOFException e) {
            // ok
        }
    }
}