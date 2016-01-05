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
package com.asakusafw.windgate.stream;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;

/**
 * Supports {@link StringBuilder}.
 */
@SuppressWarnings("resource")
public class StringBuilderSupport implements DataModelStreamSupport<StringBuilder> {

    @Override
    public Class<StringBuilder> getSupportedType() {
        return StringBuilder.class;
    }

    @Override
    public DataModelReader<StringBuilder> createReader(String path, InputStream stream) throws IOException {
        final Scanner scanner = new Scanner(stream, "UTF-8");
        return new DataModelReader<StringBuilder>() {
            @Override
            public boolean readTo(StringBuilder object) throws IOException {
                if (scanner.hasNextLine()) {
                    object.setLength(0);
                    object.append(scanner.nextLine());
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public DataModelWriter<StringBuilder> createWriter(String path, OutputStream stream) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
        return new DataModelWriter<StringBuilder>() {
            @Override
            public void flush() throws IOException {
                writer.flush();
            }
            @Override
            public void write(StringBuilder object) throws IOException {
                writer.write(object.toString());
                writer.newLine();
            }
        };
    }
}
