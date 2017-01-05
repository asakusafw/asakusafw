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
package com.asakusafw.compiler.directio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Scanner;

import com.asakusafw.compiler.directio.testing.model.Line;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Format for {@link Line}.
 */
public class LineFormat extends BinaryStreamFormat<Line> {

    @Override
    public Class<Line> getSupportedType() {
        return Line.class;
    }

    @Override
    public ModelInput<Line> createInput(Class<? extends Line> dataType, String path,
            InputStream stream, long offset, long fragmentSize) throws IOException,
            InterruptedException {
        assert offset == 0;
        Scanner scanner = new Scanner(new InputStreamReader(stream, "UTF-8"));
        return new ModelInput<Line>() {
            int position = 0;
            @Override
            public boolean readTo(Line model) throws IOException {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    model.setValueAsString(line);
                    model.setFirstAsString(line.isEmpty() ? "" : line.substring(0, 1));
                    model.setLength(line.length());
                    model.setPosition(position);
                    position++;
                    return true;
                }
                return false;
            }
            @Override
            public void close() throws IOException {
                scanner.close();
            }
        };
    }

    @Override
    public ModelOutput<Line> createOutput(Class<? extends Line> dataType, String path,
            OutputStream stream) throws IOException, InterruptedException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
        return new ModelOutput<Line>() {

            @Override
            public void write(Line model) throws IOException {
                writer.println(model.getValueAsString());
            }

            @Override
            public void close() throws IOException {
                writer.close();
            }
        };
    }
}
