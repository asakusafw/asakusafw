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
package com.asakusafw.runtime.io.line;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link LineInput}.
 */
@RunWith(Parameterized.class)
public class LineInputTest {

    static final String HELLO_JP = "\u3053\u3093\u306b\u3061\u306f\u3001\u4e16\u754c\uff01";

    /**
     * Returns parameters.
     * @return parameters
     */
    @Parameters(name = "{0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            { Charset.forName("MS932") },
            { Charset.forName("ISO-2022-JP") },
            { Charset.forName("UTF-8") },
        });
    }

    private final Charset charset;

    /**
     * Constructor.
     * @param charset the charset
     */
    public LineInputTest(Charset charset) {
        this.charset = charset;
    }

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        check(new String[] {
                "Hello, world!",
        });
    }

    /**
     * multiple lines.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        check(new String[] {
                "Hello1",
                "Hello2",
                "Hello3",
        });
    }

    /**
     * w/ empty data.
     * @throws Exception if failed
     */
    @Test
    public void empty_data() throws Exception {
        check(new String[0]);
    }

    /**
     * large text.
     * @throws Exception if failed
     */
    @Test
    public void large() throws Exception {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i <= LineConfiguration.DEFAULT_BUFFER_SIZE; i++) {
            lines.add(String.valueOf(i));
        }
        check(lines.toArray(new String[lines.size()]));
    }

    /**
     * w/o line break.
     * @throws Exception if failed
     */
    @Test
    public void terminated() throws Exception {
        List<String> lines = fetch("Hello, world!");
        assertThat(lines, equalTo(Arrays.asList("Hello, world!")));
    }

    /**
     * CRLF at EOF.
     * @throws Exception if failed
     */
    @Test
    public void crlf() throws Exception {
        List<String> lines = fetch("Hello, world!\r\n");
        assertThat(lines, equalTo(Arrays.asList("Hello, world!")));
    }

    /**
     * w/ non-ASCII.
     * @throws Exception if failed
     */
    @Test
    public void non_ascii() throws Exception {
        check(new String[] {
                HELLO_JP + "1",
                HELLO_JP + "2",
                HELLO_JP + "3",
        });
    }

    private void check(String[] lines) throws IOException {
        StringBuilder buf = new StringBuilder();
        for (String line : lines) {
            buf.append(line);
            buf.append('\n');
        }
        List<String> results = fetch(buf.toString());
        assertThat(results, equalTo(Arrays.asList(lines)));
    }

    private List<String> fetch(String data) throws IOException {
        LineConfiguration conf = new LineConfiguration().withCharset(charset);
        try (ModelInput<StringOption> input = LineInput.newInstance(
                new ByteArrayInputStream(data.getBytes(charset)), "testing", conf);) {
            StringOption buf = new StringOption();
            List<String> results = new ArrayList<>();
            while (input.readTo(buf)) {
                results.add(buf.getAsString());
            }
            return results;
        }
    }
}
