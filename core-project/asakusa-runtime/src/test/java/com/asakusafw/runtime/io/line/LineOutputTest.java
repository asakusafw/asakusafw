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
package com.asakusafw.runtime.io.line;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link LineOutput}.
 */
@RunWith(Parameterized.class)
public class LineOutputTest {

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
    public LineOutputTest(Charset charset) {
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
     * w/ null.
     * @throws Exception if failed
     */
    @Test
    public void w_null() throws Exception {
        List<String> results = restore(dump(new String[] {
                "Hello1",
                null,
                "Hello3",
                "",
        }));
        assertThat(results, equalTo(Arrays.asList("Hello1", "Hello3", "")));
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
     * w/ non-ASCII.
     * @throws Exception if failed
     */
    @Test
    public void non_ascii() throws Exception {
        check(new String[] {
                LineInputTest.HELLO_JP + "1",
                LineInputTest.HELLO_JP + "2",
                LineInputTest.HELLO_JP + "3",
        });
    }

    private void check(String[] lines) throws IOException {
        List<String> results = restore(dump(lines));
        assertThat(results, equalTo(Arrays.asList(lines)));
    }

    @SuppressWarnings("deprecation")
    private byte[] dump(String[] lines) throws IOException {
        LineConfiguration conf = new LineConfiguration().withCharset(charset);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ModelOutput<StringOption> output = LineOutput.newInstance(bytes, "testing", conf)) {
            StringOption buf = new StringOption();
            for (String line : lines) {
                if (line == null) {
                    buf.setNull();
                } else {
                    buf.modify(line);
                }
                output.write(buf);
            }
        }
        return bytes.toByteArray();
    }

    private List<String> restore(byte[] bytes) {
        try (Scanner scanner = new Scanner(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            List<String> results = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                results.add(line);
            }
            return results;
        }
    }
}
