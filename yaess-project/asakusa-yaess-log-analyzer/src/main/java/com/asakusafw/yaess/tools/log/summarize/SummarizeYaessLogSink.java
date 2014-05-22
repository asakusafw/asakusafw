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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.io.RecordWriter;
import com.asakusafw.utils.io.Sink;
import com.asakusafw.yaess.tools.log.YaessJobId;
import com.asakusafw.yaess.tools.log.YaessLogRecord;
import com.asakusafw.yaess.tools.log.util.Filter;

/**
 * Summarizes {@link YaessLogRecord}s.
 * @since 0.6.2
 */
public class SummarizeYaessLogSink implements Sink<YaessLogRecord> {

    static final Logger LOG = LoggerFactory.getLogger(SummarizeYaessLogSink.class);

    private final RecordWriter writer;

    private final Map<YaessJobId, Summary> entries = new HashMap<YaessJobId, Summary>();

    private boolean closed = false;

    private final Filter<? super YaessLogRecord> filter;

    /**
     * Creates a new instance.
     * @param writer the target writer
     * @param filter the summarize target filter
     */
    public SummarizeYaessLogSink(RecordWriter writer, Filter<? super YaessLogRecord> filter) {
        this.writer = writer;
        this.filter = filter;
    }

    @Override
    public void put(YaessLogRecord object) throws IOException, InterruptedException {
        YaessJobId id = object.getJobId();
        if (id == null) {
            return;
        }
        if (filter.accepts(object) == false) {
            LOG.debug("Record \"{}\" is filtered", object.getCode());
            return;
        }
        LOG.debug("Processing: \"{}\"", object.getCode());
        Summary summary = entries.get(id);
        if (summary == null) {
            summary = new Summary(id);
            entries.put(id, summary);
        }
        summary.add(object);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            if (closed == false) {
                closed = true;
                resolve();
            }
        } catch (InterruptedException e) {
            throw (InterruptedIOException) new InterruptedIOException().initCause(e);
        } finally {
            writer.close();
        }
    }

    private void resolve() throws IOException, InterruptedException {
        LOG.debug("Summarizing {} items", entries.size());
        adjustTime();
        putHeader();
        for (Summary entry : sort(entries.values())) {
            putEntry(entry);
        }
    }

    private void adjustTime() {
        long base = 0;
        for (Summary entry : entries.values()) {
            if (entry.minTime >= 0) {
                if (base == 0) {
                    base = entry.minTime;
                } else {
                    base = Math.min(entry.minTime, base);
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Base time: {}", new Date(base));
        }
        if (base == 0L) {
            return;
        }
        for (Summary entry : entries.values()) {
            if (entry.minTime >= 0) {
                entry.minTime -= base;
            }
            if (entry.maxTime >= 0) {
                entry.maxTime -= base;
            }
        }
    }

    private Iterable<Summary> sort(Collection<Summary> values) {
        List<Summary> results = new ArrayList<Summary>(values);
        Collections.sort(results, new Comparator<Summary>() {
            @Override
            public int compare(Summary o1, Summary o2) {
                long t1 = normalize(o1.minTime);
                long t2 = normalize(o2.minTime);
                if (t1 < t2) {
                    return -1;
                } else if (t1 > t2) {
                    return +1;
                }
                long e1 = normalize(o1.getElapsed());
                long e2 = normalize(o2.getElapsed());
                if (e1 > e2) {
                    return -1;
                } else if (e1 < e2) {
                    return +1;
                }
                return 0;
            }
            private long normalize(long value) {
                return value < 0L ? Long.MAX_VALUE : value;
            }
        });
        return results;
    }

    private void putHeader() throws IOException, InterruptedException {
        putField("Batch ID");
        putField("Flow ID");
        putField("Execution ID");
        putField("Phase");
        putField("Service ID");
        putField("Job ID");
        putField("Tracking ID");
        putField("Start");
        putField("Elapsed");
        putField("Severity");
        writer.putEndOfRecord();
    }

    private void putEntry(Summary entry) throws IOException, InterruptedException {
        putField(entry.id.getBatchId());
        putField(entry.id.getFlowId());
        putField(entry.id.getExecutionId());
        putField(entry.id.getPhase());
        putField(entry.id.getServiceId());
        putField(entry.id.getJobId());
        putField(entry.id.getTrackingId());
        putField(entry.minTime);
        putField(entry.getElapsed());
        putField(entry.severity.toString());
        writer.putEndOfRecord();
    }

    private void putField(String string) throws IOException, InterruptedException {
        if (string == null) {
            writer.putField("");
        } else {
            writer.putField(string);
        }
    }

    private void putField(long number) throws IOException, InterruptedException {
        if (number < 0L) {
            putField(null);
        } else {
            putField(String.valueOf(number));
        }
    }

    private static class Summary {

        final YaessJobId id;

        long minTime = -1;

        long maxTime = -1;

        Severity severity = Severity.UNKNOWN;

        Summary(YaessJobId id) {
            this.id = id;
        }

        void add(YaessLogRecord record) {
            long time = record.getTime();
            if (time >= 0L) {
                if (minTime >= 0L) {
                    this.minTime = Math.min(minTime, time);
                } else {
                    this.minTime = time;
                }
                if (maxTime >= 0L) {
                    this.maxTime = Math.max(maxTime, time);
                } else {
                    this.maxTime = time;
                }
            }
            this.severity = severity.merge(Severity.fromCode(record.getCode()));
        }

        long getElapsed() {
            if (minTime < 0L || maxTime < 0L) {
                return -1L;
            }
            return maxTime - minTime;
        }
    }

    private enum Severity {

        UNKNOWN,

        INFO,

        WARN,

        ERROR,
        ;

        static Severity fromCode(String code) {
            if (code != null && code.length() >= 6) {
                char c = code.charAt(code.length() - 6);
                switch (c) {
                case 'I':
                    return INFO;
                case 'W':
                    return WARN;
                case 'E':
                    return ERROR;
                default:
                    return UNKNOWN;
                }
            }
            return UNKNOWN;
        }

        Severity merge(Severity other) {
            if (other.ordinal() > this.ordinal()) {
                return other;
            }
            return this;
        }
    }
}
