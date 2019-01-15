/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.testdriver.testing.dsl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.testing.model.Simple;

/**
 * A simple stream format.
 */
public class SimpleStreamFormat extends BinaryStreamFormat<Simple> {

    @Override
    public Class<Simple> getSupportedType() {
        return Simple.class;
    }

    @Override
    public ModelInput<Simple> createInput(
            Class<? extends Simple> dataType,
            String path,
            InputStream stream,
            long offset, long fragmentSize) throws IOException, InterruptedException {
        if (offset != 0) {
            throw new IllegalArgumentException();
        }
        Scanner scanner = new Scanner(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return new ModelInput<Simple>() {
            @Override
            public boolean readTo(Simple model) throws IOException {
                if (scanner.hasNextLine()) {
                    model.setValueAsString(scanner.nextLine());
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
    public ModelOutput<Simple> createOutput(
            Class<? extends Simple> dataType,
            String path,
            OutputStream stream) throws IOException, InterruptedException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
        return new ModelOutput<Simple>() {
            @Override
            public void write(Simple model) throws IOException {
                writer.println(model.getValueAsString());
            }
            @Override
            public void close() throws IOException {
                writer.close();
            }
        };
    }

}
