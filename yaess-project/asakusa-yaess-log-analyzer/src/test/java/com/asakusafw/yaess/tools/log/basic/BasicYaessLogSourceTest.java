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
package com.asakusafw.yaess.tools.log.basic;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.asakusafw.utils.io.Sources;
import com.asakusafw.yaess.tools.log.YaessLogRecord;

/**
 * Test for {@link BasicYaessLogSource}.
 */
public class BasicYaessLogSourceTest {

    /**
     * Simple test case.
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
     * Simple test case.
     */
    @Test
    public void multiple() {
        List<YaessLogRecord> results = parse(new String[] {
                "Hello, world!",
                "2014/01/01 00:00:00 INFO [YS-TEST-I00001] Hello1: batchId=b1",
                "Hello, world!",
                "2014/01/02 00:00:00 INFO [YS-TEST-I00002] Hello1: batchId=b2",
                "Hello, world!",
                "2014/01/03 00:00:00 INFO [YS-TEST-I00003] Hello1: batchId=b3",
                "Hello, world!",
        });
        assertThat(results, hasSize(3));

        YaessLogRecord r0 = results.get(0);
        assertThat(r0.getCode(), is("YS-TEST-I00001"));
        assertThat(r0.getTime(), is(at("2014/01/01 00:00:00")));
        assertThat(r0.getJobId().getBatchId(), is("b1"));

        YaessLogRecord r1 = results.get(1);
        assertThat(r1.getCode(), is("YS-TEST-I00002"));
        assertThat(r1.getTime(), is(at("2014/01/02 00:00:00")));
        assertThat(r1.getJobId().getBatchId(), is("b2"));

        YaessLogRecord r2 = results.get(2);
        assertThat(r2.getCode(), is("YS-TEST-I00003"));
        assertThat(r2.getTime(), is(at("2014/01/03 00:00:00")));
        assertThat(r2.getJobId().getBatchId(), is("b3"));
    }

    /**
     * W/o log code.
     */
    @Test
    public void wo_code() {
        List<YaessLogRecord> results = parse(new String[] {
                "2014/01/01 12:34:56 INFO [NOT-A-LOG-CODE] Hello, world!: batchId=com.example"
        });
        assertThat(results, hasSize(0));
    }

    /**
     * W/o job ID.
     */
    @Test
    public void wo_id() {
        List<YaessLogRecord> results = parse(new String[] {
                "2014/01/01 12:34:56 INFO [YS-TEST-I12345] Hello, world!"
        });
        assertThat(results, hasSize(0));
    }

    /**
     * W/o time.
     */
    @Test
    public void wo_time() {
        List<YaessLogRecord> results = parse(new String[] {
                "NOW() INFO [YS-TEST-I12345] Hello, world!: batchId=com.example"
        });
        assertThat(results, hasSize(1));

        YaessLogRecord r0 = results.get(0);
        assertThat(r0.getCode(), is("YS-TEST-I12345"));
        assertThat(r0.getTime(), is(-1L));
        assertThat(r0.getJobId().getBatchId(), is("com.example"));
    }

    /**
     * W/ full job id.
     */
    @Test
    public void full_id() {
        List<YaessLogRecord> results = parse(new String[] {
                new StringBuilder("2014/01/01 12:34:56 INFO [YS-TEST-I12345] Hello, world!: ")
                    .append("batchId=b, ")
                    .append("flowId=f, ")
                    .append("executionId=e, ")
                    .append("phase=main, ")
                    .append("serviceId=s, ")
                    .append("jobId=j, ")
                    .append("trackingId=t")
                    .toString()
        });
        assertThat(results, hasSize(1));

        YaessLogRecord r0 = results.get(0);
        assertThat(r0.getCode(), is("YS-TEST-I12345"));
        assertThat(r0.getTime(), is(at("2014/01/01 12:34:56")));
        assertThat(r0.getJobId().getBatchId(), is("b"));
        assertThat(r0.getJobId().getFlowId(), is("f"));
        assertThat(r0.getJobId().getExecutionId(), is("e"));
        assertThat(r0.getJobId().getPhase(), is("main"));
        assertThat(r0.getJobId().getJobId(), is("j"));
        assertThat(r0.getJobId().getServiceId(), is("s"));
        assertThat(r0.getJobId().getTrackingId(), is("t"));
    }

    static Matcher<Long> at(final String dateString) {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return new BaseMatcher<Long>() {
            @Override
            public boolean matches(Object item) {
                try {
                    long expected = format.parse(dateString).getTime();
                    long actual = (Long) item;
                    return expected == actual;
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
            @Override
            public void describeTo(Description description) {
                description.appendValue(dateString);
            }
            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText("was ").appendValue(format.format(new Date((Long) item)));
            }
        };
    }

    private List<YaessLogRecord> parse(String... lines) {
        BasicYaessLogSource source = new BasicYaessLogSource(Sources.wrap(Arrays.asList(lines).iterator()));
        try {
            try {
                List<YaessLogRecord> results = new ArrayList<YaessLogRecord>();
                while (source.next()) {
                    results.add(source.get());
                }
                return results;
            } finally {
                source.close();
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
