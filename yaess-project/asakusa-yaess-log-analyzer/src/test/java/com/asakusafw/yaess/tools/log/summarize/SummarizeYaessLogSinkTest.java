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
package com.asakusafw.yaess.tools.log.summarize;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import com.asakusafw.yaess.tools.log.YaessJobId;
import com.asakusafw.yaess.tools.log.YaessLogRecord;
import com.asakusafw.yaess.tools.log.util.Filter;
import com.asakusafw.yaess.tools.log.util.LogCodeRegexFilter;

/**
 * Test for {@link SummarizeYaessLogSink}.
 */
public class SummarizeYaessLogSinkTest {

    private final List<YaessLogRecord> records = new ArrayList<YaessLogRecord>();

    private final YaessJobIdProvider provider = new YaessJobIdProvider() {
        @Override
        protected YaessJobId createId() {
            YaessJobId id = super.createId();
            id.setBatchId("b");
            id.setFlowId("f");
            id.setJobId("j");
            id.setPhase("main");
            id.setExecutionId("e");
            id.setServiceId("s");
            id.setTrackingId("t");
            return id;
        }
    };

    /**
     * Simple test case.
     */
    @Test
    public void simple() {
        YaessJobId id1 = provider.copy();
        append(0, "YS-CORE-I00000", id1);
        append(100, "YS-CORE-I00999", id1);

        Map<YaessJobId, List<String>> results = summarize(Filter.THROUGH);
        assertThat(results.size(), is(1));

        List<String> r1 = results.get(id1);
        assertThat(r1, is(notNullValue()));
        assertThat(r1.get(0), is("0"));
        assertThat(r1.get(1), is("100"));
        assertThat(r1.get(2), is("INFO"));
    }

    /**
     * Multiple records.
     */
    @Test
    public void multiple() {
        provider.id().setJobId("j1");
        YaessJobId id1 = provider.copy();
        append(10000, "YS-CORE-I00000", id1);
        append(10001, "YS-CORE-I00001", id1);
        append(10100, "YS-CORE-I00999", id1);

        provider.id().setJobId("j2");
        YaessJobId id2 = provider.copy();
        append(11000, "YS-CORE-I00000", id2);
        append(11001, "YS-CORE-W00001", id2);
        append(11200, "YS-CORE-I00999", id2);

        provider.id().setJobId("j3");
        YaessJobId id3 = provider.copy();
        append(12000, "YS-CORE-I00000", id3);
        append(12001, "YS-CORE-E00001", id3);
        append(12300, "YS-CORE-I00999", id3);

        Map<YaessJobId, List<String>> results = summarize(Filter.THROUGH);
        assertThat(results.size(), is(3));

        List<String> r1 = results.get(id1);
        assertThat(r1, is(notNullValue()));
        assertThat(r1.get(0), is("0"));
        assertThat(r1.get(1), is("100"));
        assertThat(r1.get(2), is("INFO"));

        List<String> r2 = results.get(id2);
        assertThat(r2, is(notNullValue()));
        assertThat(r2.get(0), is("1000"));
        assertThat(r2.get(1), is("200"));
        assertThat(r2.get(2), is("WARN"));

        List<String> r3 = results.get(id3);
        assertThat(r3, is(notNullValue()));
        assertThat(r3.get(0), is("2000"));
        assertThat(r3.get(1), is("300"));
        assertThat(r3.get(2), is("ERROR"));
    }

    /**
     * w/ filter.
     */
    @Test
    public void filtered() {
        YaessJobId id1 = provider.copy();
        append(0, "YS-CORE-I00000", id1);
        append(100, "YS-CORE-I00999", id1);
        append(500, "YS-UNKNOWN-I00000", id1);

        Map<YaessJobId, List<String>> results = summarize(new LogCodeRegexFilter(Pattern.compile("YS-CORE-.*")));
        assertThat(results.size(), is(1));

        List<String> r1 = results.get(id1);
        assertThat(r1, is(notNullValue()));
        assertThat(r1.get(0), is("0"));
        assertThat(r1.get(1), is("100"));
        assertThat(r1.get(2), is("INFO"));
    }

    private void append(long time, String code, YaessJobId id) {
        YaessLogRecord record = new YaessLogRecord();
        record.setTime(time);
        record.setCode(code);
        record.setJobId(id);
        records.add(record);
    }

    private Map<YaessJobId, List<String>> summarize(Filter<? super YaessLogRecord> filter) {
        ListWriter writer = new ListWriter();
        try {
            SummarizeYaessLogSink sink = new SummarizeYaessLogSink(writer, filter);
            for (YaessLogRecord record : records) {
                sink.put(record);
            }
            sink.flush();
            sink.close();
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            writer.close();
        }
        Map<YaessJobId, List<String>> results = new HashMap<YaessJobId, List<String>>();
        List<List<String>> lines = writer.getLines();
        if (lines.isEmpty() == false) {
            // ignore header
            lines = lines.subList(1, lines.size());
        }
        for (List<String> record : lines) {
            YaessJobId id = new YaessJobId();
            id.setBatchId(record.get(0));
            id.setFlowId(record.get(1));
            id.setExecutionId(record.get(2));
            id.setPhase(record.get(3));
            id.setServiceId(record.get(4));
            id.setJobId(record.get(5));
            id.setTrackingId(record.get(6));
            assertThat(results.get(id), is(nullValue()));
            results.put(id, new ArrayList<String>(record.subList(7, record.size())));
        }
        return results;
    }
}
