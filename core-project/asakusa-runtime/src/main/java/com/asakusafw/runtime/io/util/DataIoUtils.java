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
package com.asakusafw.runtime.io.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;

/**
 * Utilities for {@link DataInput} and {@link DataOutput}.
 */
final class DataIoUtils {

    private DataIoUtils() {
        return;
    }

    /**
     * Emulates {@link DataInput#readUTF()} without using it method.
     * @param input the target {@link DataInput}
     * @return the result
     * @throws IOException if failed to read String from the {@link DataInput}
     */
    public static String readUTF(DataInput input) throws IOException {
        // TODO use modified UTF-8
        return Text.readString(input);
    }

    /**
     * Emulates {@link DataOutput#writeUTF(String)} without using it method.
     * @param output the target {@link DataOutput}
     * @param value the target value
     * @throws IOException if failed to write String into {@link DataOutput}
     */
    public static void writeUTF(DataOutput output, String value) throws IOException {
        // TODO use modified UTF-8
        Text.writeString(output, value);
    }
}
