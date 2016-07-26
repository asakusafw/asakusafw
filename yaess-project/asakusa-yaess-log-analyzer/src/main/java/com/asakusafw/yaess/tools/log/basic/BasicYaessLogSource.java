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
package com.asakusafw.yaess.tools.log.basic;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.io.Source;
import com.asakusafw.yaess.tools.log.YaessJobId;
import com.asakusafw.yaess.tools.log.YaessLogRecord;

/**
 * Extracts {@link YaessLogRecord} from a YAESS log file with basic format.
 * @since 0.6.2
 */
public class BasicYaessLogSource implements Source<YaessLogRecord> {

    static final Logger LOG = LoggerFactory.getLogger(BasicYaessLogSource.class);

    private final Source<String> recordSource;

    private volatile YaessLogRecord nextRecord;

    /**
     * Creates a new instance.
     * @param source the input source
     */
    public BasicYaessLogSource(Source<String> source) {
        this.recordSource = source;
    }

    @Override
    public boolean next() throws IOException, InterruptedException {
        this.nextRecord = prepare();
        return nextRecord != null;
    }

    @Override
    public YaessLogRecord get() throws IOException, InterruptedException {
        YaessLogRecord result = nextRecord;
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        this.recordSource.close();
    }

    private YaessLogRecord prepare() throws IOException, InterruptedException {
        while (recordSource.next()) {
            String line = recordSource.get();
            YaessLogRecord record = parse(line);
            if (record != null) {
                return record;
            }
        }
        return null;
    }

    private YaessLogRecord parse(String line) {
        LOG.debug("Parsing record: {}", line);
        String code = extractCode(line);
        if (code == null) {
            LOG.debug("Not a YAESS job log: {}", line);
            return null;
        }
        YaessJobId id = extractId(line);
        if (id == null) {
            LOG.debug("Failed to extract job ID: {}", line);
            return null;
        }
        long time = extractTime(line);
        if (time < 0L) {
            LOG.debug("Failed to extract time: {}", line);
            // continue
        }

        YaessLogRecord record = new YaessLogRecord();
        record.setTime(time);
        record.setCode(code);
        record.setJobId(id);
        return record;
    }

    private static final Pattern CODE_PATTERN = Pattern.compile("\\[(YS-\\w+-[A-Z]\\d+)\\]");
    private String extractCode(String line) {
        Matcher matcher = CODE_PATTERN.matcher(line);
        if (matcher.find() == false) {
            return null;
        }
        return matcher.group(1);
    }

    static final String TIME_PATTERN = "yyyy/MM/dd HH:mm:ss";
    private static final ThreadLocal<DateFormat> TIME_FORMAT = ThreadLocal.withInitial(() -> {
        SimpleDateFormat format = new SimpleDateFormat(TIME_PATTERN);
        format.setLenient(true);
        return format;
    });

    private long extractTime(String line) {
        if (line.length() <= TIME_PATTERN.length()) {
            return -1L;
        }
        char success = line.charAt(TIME_PATTERN.length());
        if (success != '.' && Character.isWhitespace(success) == false) {
            return -1L;
        }
        String dateString = line.substring(0, TIME_PATTERN.length());
        try {
            Date parsed = TIME_FORMAT.get().parse(dateString);
            return parsed.getTime();
        } catch (ParseException e) {
            LOG.warn(MessageFormat.format(
                    "Invalid time \"{0}\": {1}",
                    dateString, line), e);
            return -1L;
        }
    }

    private static final String KEY_BATCH_ID = "batchId";
    private static final String KEY_FLOW_ID = "flowId";
    private static final String KEY_EXECUTION_ID = "executionId";
    private static final String KEY_PHASE = "phase";
    private static final String KEY_JOB_ID = "jobId";
    private static final String KEY_SERVICE_ID = "serviceId";
    private static final String KEY_TRACKING_ID = "trackingId";
    private YaessJobId extractId(String line) {
        Map<String, String> fields = extractFields(line);
        if (fields.isEmpty()) {
            return null;
        }
        YaessJobId id = new YaessJobId();
        id.setBatchId(fields.get(KEY_BATCH_ID));
        id.setFlowId(fields.get(KEY_FLOW_ID));
        id.setExecutionId(fields.get(KEY_EXECUTION_ID));
        id.setPhase(fields.get(KEY_PHASE));
        id.setJobId(fields.get(KEY_JOB_ID));
        id.setServiceId(fields.get(KEY_SERVICE_ID));
        id.setTrackingId(fields.get(KEY_TRACKING_ID));
        return id;
    }

    private static final String[] KEY_FIELDS = {
        KEY_BATCH_ID,
        KEY_FLOW_ID,
        KEY_EXECUTION_ID,
        KEY_PHASE,
        KEY_JOB_ID,
        KEY_SERVICE_ID,
        KEY_TRACKING_ID,
    };
    private Map<String, String> extractFields(String line) {
        Map<String, String> results = new HashMap<>();
        for (String key : KEY_FIELDS) {
            int index = line.indexOf(key + '=');
            if (index >= 0) {
                String value = extractFieldValue(line, index + key.length() + 1);
                if (value != null) {
                    results.put(key, value);
                }
            }
        }
        return results;
    }

    private String extractFieldValue(String line, int offset) {
        int end = findEndOfField(line, offset);
        if (end > offset) {
            return line.substring(offset, end);
        }
        return null;
    }

    private int findEndOfField(String line, int offset) {
        for (int i = offset, n = line.length(); i < n; i++) {
            char c = line.charAt(i);
            if (c == ' ') {
                assert i >= 1;
                if (line.charAt(i - 1) == ',') {
                    return i - 1;
                }
                return i;
            } else if (c == '=') {
                return -1;
            }
        }
        return line.length();
    }
}
