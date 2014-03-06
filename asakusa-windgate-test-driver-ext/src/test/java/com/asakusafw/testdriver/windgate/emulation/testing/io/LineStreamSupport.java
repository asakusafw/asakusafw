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
package com.asakusafw.testdriver.windgate.emulation.testing.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Scanner;

import com.asakusafw.testdriver.windgate.emulation.testing.model.Line;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;

/**
 * {@link DataModelStreamSupport} for {@link Line}.
 */
@SuppressWarnings({ "deprecation", "resource" })
public class LineStreamSupport implements DataModelStreamSupport<Line> {

    @Override
    public Class<Line> getSupportedType() {
        return Line.class;
    }

    @Override
    public DataModelReader<Line> createReader(String path, InputStream stream) throws IOException {
        final Scanner scanner = new Scanner(stream, "UTF-8");
        return new DataModelReader<Line>() {
            @Override
            public boolean readTo(Line object) throws IOException {
                if (scanner.hasNextLine()) {
                    object.getValueOption().modify(scanner.nextLine());
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public DataModelWriter<Line> createWriter(String path, OutputStream stream) throws IOException {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
        return new DataModelWriter<Line>() {
            @Override
            public void write(Line object) throws IOException {
                writer.println(object.getValueOption().getAsString());
            }
            @Override
            public void flush() throws IOException {
                writer.flush();
            }
        };
    }
}
