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

import org.apache.hadoop.io.Text;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * A simple stream format for {@link Text}.
 */
public class TextStreamFormat extends BinaryStreamFormat<Text> {

    @Override
    public Class<Text> getSupportedType() {
        return Text.class;
    }

    @Override
    public ModelInput<Text> createInput(
            Class<? extends Text> dataType,
            String path,
            InputStream stream,
            long offset, long fragmentSize) throws IOException, InterruptedException {
        if (offset != 0) {
            throw new IllegalArgumentException();
        }
        Scanner scanner = new Scanner(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return new ModelInput<Text>() {
            @Override
            public boolean readTo(Text model) throws IOException {
                if (scanner.hasNextLine()) {
                    model.set(scanner.nextLine());
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
    public ModelOutput<Text> createOutput(
            Class<? extends Text> dataType,
            String path,
            OutputStream stream) throws IOException, InterruptedException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
        return new ModelOutput<Text>() {
            @Override
            public void write(Text model) throws IOException {
                writer.print(model.toString());
            }
            @Override
            public void close() throws IOException {
                writer.close();
            }
        };
    }

}
