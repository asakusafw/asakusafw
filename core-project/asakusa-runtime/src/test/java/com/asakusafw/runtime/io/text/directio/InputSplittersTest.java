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
package com.asakusafw.runtime.io.text.directio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * Test for {@link InputSplitters}.
 */
public class InputSplittersTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        InputSplitter splitter = InputSplitters.byLineFeed();
        String result = process("AAA\nBBB\nCCC|\nDDD\nEEE\n", splitter, false);
        assertThat(result, is("BBB\nCCC\n"));
    }

    /**
     * w/ whole range.
     */
    @Test
    public void without_escape_whole() {
        InputSplitter splitter = InputSplitters.byLineFeed();
        String result = process("Hello, world!", splitter, true);
        assertThat(result, is("Hello, world!"));
    }

    private String process(String text, InputSplitter splitter, boolean head) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            long length;
            int split = text.indexOf('|');
            if (split < 0) {
                output.write(text.getBytes(StandardCharsets.UTF_8));
                length = Long.MAX_VALUE;
            } else {
                byte[] first = text.substring(0, split).getBytes(StandardCharsets.UTF_8);
                byte[] last = text.substring(split + 1).getBytes(StandardCharsets.UTF_8);
                output.write(first);
                output.write(last);
                length = first.length;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] bytes = output.toByteArray();
            try (InputStream in = splitter.trim(new ByteArrayInputStream(bytes), head ? 0 : 1, length)) {
                byte[] buf = new byte[10];
                while (true) {
                    int read = in.read(buf);
                    if (read < 0) {
                        break;
                    }
                    buffer.write(buf, 0, read);
                }
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
