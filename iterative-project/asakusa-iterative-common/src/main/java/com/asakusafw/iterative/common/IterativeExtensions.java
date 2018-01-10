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
package com.asakusafw.iterative.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import com.asakusafw.iterative.common.basic.BasicParameterTable;

/**
 * API entry of Asakusa Iterative Extensions.
 * @since 0.8.0
 */
public final class IterativeExtensions {

    /**
     * The launching extension name.
     */
    public static final String EXTENSION_NAME = "iterative"; //$NON-NLS-1$

    private IterativeExtensions() {
        return;
    }

    /**
     * Creates a new builder for building {@link ParameterTable}.
     * @return the created builder
     */
    public static ParameterTable.Builder builder() {
        return new BasicParameterTable.BasicBuilder();
    }

    /**
     * Restores the {@link #save(OutputStream, ParameterTable) saved} object.
     * @param input the input source
     * @throws IOException if I/O error was occurred while loading the object
     * @return the restored object
     */
    public static ParameterTable load(InputStream input) throws IOException {
        Objects.requireNonNull(input);
        ParameterTableSerDe serde = new ParameterTableSerDe();
        ParameterTable.Builder builder = builder();
        serde.deserialize(builder, input);
        return builder.build();
    }

    /**
     * Stores the parameter table into the target stream.
     * @param output the target output
     * @param table the target table
     * @throws IOException if I/O error was occurred while saving the object
     * @see #load(InputStream)
     */
    public static void save(OutputStream output, ParameterTable table) throws IOException {
        Objects.requireNonNull(output);
        ParameterTableSerDe serde = new ParameterTableSerDe();
        serde.serialize(table, output);
    }
}
