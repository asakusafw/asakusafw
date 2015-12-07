/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.yaess.tools.log.basic;

import static com.asakusafw.yaess.tools.log.basic.BasicYaessLogSourceTest.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.utils.io.Source;
import com.asakusafw.yaess.tools.log.YaessLogRecord;

/**
 * Test for {@link BasicYaessLogInput}.
 */
public class BasicYaessLogInputTest {

    /**
     * A temporary folder.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    /**
     * Test method for {@link BasicYaessLogInput#getOptionsInformation()}.
     */
    @Test
    public void getOptionsInformation() {
        Map<String, String> opts = new BasicYaessLogInput().getOptionsInformation();
        assertThat(opts.get("file"), is(notNullValue()));
        assertThat(opts.get("encoding"), is(notNullValue()));
    }

    /**
     * Simple scenario.
     */
    @Test
    public void simple() {
        List<YaessLogRecord> results = parse(new String[] {
                "2014/01/01 12:34:56 INFO [YS-TEST-I12345] Hello, world!: batchId=com.example"
        });
        assertThat(results, hasSize(1));

        YaessLogRecord r0 = results.get(0);
        assertThat(r0.getCode(), is("YS-TEST-I12345"));
        assertThat(r0.getTime(), is(at("2014/01/01 12:34:56")));
        assertThat(r0.getJobId().getBatchId(), is("com.example"));
    }

    /**
     * W/o file.
     */
    @Test
    public void wo_file() {
        Map<String, String> opts = new HashMap<>();
        opts.put("encoding", "UTF-8");
        assertInvalid(opts);
    }

    /**
     * missing file.
     */
    @Test
    public void missing_file() {
        Map<String, String> opts = new HashMap<>();
        opts.put("file", new File(temporary.getRoot(), "__MISSING__").getAbsolutePath());
        assertInvalid(opts);
    }

    /**
     * Invalid encoding.
     * @throws Exception if failed
     */
    @Test
    public void invalid_encoding() throws Exception {
        Map<String, String> opts = new HashMap<>();
        opts.put("file", temporary.newFile().getAbsolutePath());
        opts.put("encoding", "??INVALID??");
        assertInvalid(opts);
    }

    /**
     * Unknown options.
     * @throws Exception if failed
     */
    @Test
    public void unknown_opts() throws Exception {
        Map<String, String> opts = new HashMap<>();
        opts.put("file", temporary.newFile().getAbsolutePath());
        opts.put("__UNKNOWN__", "__UNKNOWN__");
        assertInvalid(opts);
    }

    private List<YaessLogRecord> parse(String[] contents) {
        try {
            File f = temporary.newFile();
            try (PrintWriter writer = new PrintWriter(f, "UTF-8")) {
                for (String line : contents) {
                    writer.printf("%s\n", line);
                }
            }
            Map<String, String> opts = new HashMap<>();
            opts.put("file", f.getAbsolutePath());
            opts.put("encoding", "UTF-8");
            try (Source<? extends YaessLogRecord> source = new BasicYaessLogInput().createSource(opts)) {
                List<YaessLogRecord> results = new ArrayList<>();
                while (source.next()) {
                    results.add(source.get());
                }
                return results;
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private void assertInvalid(Map<String, String> opts) {
        try {
            new BasicYaessLogInput().createSource(opts).close();
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
