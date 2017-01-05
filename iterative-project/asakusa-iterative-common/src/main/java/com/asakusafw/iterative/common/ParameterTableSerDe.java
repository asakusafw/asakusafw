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
package com.asakusafw.iterative.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Objects;

/**
 * Serializes and deserializes {@link ParameterTable}.
 * @since 0.8.0
 */
class ParameterTableSerDe {

    private static final int MAGIC = 0xad01_f012;

    private static final int VERSION = 0;

    private static final byte OP_ROW = 0;

    private static final byte OP_CELL = 1;

    private static final byte OP_END = (byte) 0xff;

    /**
     * Serializes the {@link ParameterTable}.
     * @param table the target object
     * @param output the target output stream
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public void serialize(ParameterTable table, OutputStream output) throws IOException {
        Objects.requireNonNull(table);
        Objects.requireNonNull(output);
        DataOutputStream data = new DataOutputStream(output);
        serialize0(table, data);
        data.flush();
    }

    public void deserialize(ParameterTable.Builder builder, InputStream input) throws IOException {
        Objects.requireNonNull(input);
        DataInputStream data = new DataInputStream(input);
        deserialize0(builder, data);
    }

    private void serialize0(ParameterTable table, DataOutputStream data) throws IOException {
        data.writeInt(MAGIC);
        data.writeInt(VERSION);
        ParameterTable.Cursor cursor = table.newCursor();
        while (cursor.next()) {
            data.writeByte(OP_ROW);
            ParameterSet row = cursor.get();
            for (String key : row.getAvailable()) {
                assert key != null;
                String value = row.get(key);
                assert value != null;
                data.writeByte(OP_CELL);
                data.writeUTF(key);
                data.writeUTF(value);
            }
        }
        data.writeByte(OP_END);
    }

    private void deserialize0(ParameterTable.Builder builder, DataInputStream data) throws IOException {
        int magic = data.readInt();
        if (magic != MAGIC) {
            throw new IOException("parameter table is broken: invalid magic number");
        }
        int version = data.readInt();
        if (version != VERSION) {
            throw new IOException(MessageFormat.format(
                    "inconsistent parameter table version: required={0}, actual={1}",
                    VERSION,
                    version));
        }

        INTERPRETER: while (true) {
            byte op = data.readByte();
            switch (op) {
            case OP_ROW:
                builder.next();
                break;
            case OP_CELL:
                String name = data.readUTF();
                String value = data.readUTF();
                builder.put(name, value);
                break;
            case OP_END:
                break INTERPRETER;
            default:
                throw new IOException(MessageFormat.format(
                        "unknown op: {0}",
                        op));
            }
        }
    }
}
