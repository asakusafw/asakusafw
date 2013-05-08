/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.testdriver.directio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Scanner;

import org.apache.hadoop.io.Text;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Mock {@link BinaryStreamFormat}.
 */
public class MockStreamFormat extends BinaryStreamFormat<Text> {

    @Override
    public Class<Text> getSupportedType() {
        return Text.class;
    }

    @Override
    public long getPreferredFragmentSize() throws IOException, InterruptedException {
        return -1;
    }

    @Override
    public long getMinimumFragmentSize() throws IOException, InterruptedException {
        return -1;
    }

    @Override
    public ModelInput<Text> createInput(Class<? extends Text> dataType, String path,
            InputStream stream, long offset, long fragmentSize) throws IOException,
            InterruptedException {
        final Scanner s = new Scanner(stream, "UTF-8");
        return new ModelInput<Text>() {
            @Override
            public boolean readTo(Text model) throws IOException {
                if (s.hasNextLine()) {
                    model.set(s.nextLine());
                    return true;
                }
                return false;
            }
            @Override
            public void close() throws IOException {
                s.close();
            }
        };
    }

    @Override
    public ModelOutput<Text> createOutput(Class<? extends Text> dataType, String path,
            OutputStream stream) throws IOException, InterruptedException {
        final PrintWriter w = new PrintWriter(new OutputStreamWriter(stream));
        return new ModelOutput<Text>() {
            @Override
            public void write(Text model) throws IOException {
                w.println(model.toString());
            }
            @Override
            public void close() throws IOException {
                w.close();
            }
        };
    }
}
