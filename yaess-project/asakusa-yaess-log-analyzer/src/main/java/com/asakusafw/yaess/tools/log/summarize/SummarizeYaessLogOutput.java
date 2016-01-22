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
package com.asakusafw.yaess.tools.log.summarize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.asakusafw.utils.io.Sink;
import com.asakusafw.utils.io.csv.CsvWriter;
import com.asakusafw.yaess.tools.log.YaessLogOutput;
import com.asakusafw.yaess.tools.log.YaessLogRecord;
import com.asakusafw.yaess.tools.log.util.Filter;
import com.asakusafw.yaess.tools.log.util.LogCodeRegexFilter;

/**
 * Provides YAESS log summary file.
 * @since 0.6.2
 */
public class SummarizeYaessLogOutput implements YaessLogOutput {

    private static final String KEY_FILE = "file";

    private static final String KEY_ENCODING = "encoding";

    private static final String KEY_CODE = "code";

    private static final String DEFAULT_ENCODING = Charset.defaultCharset().name();

    @Override
    public Map<String, String> getOptionsInformation() {
        Map<String, String> results = new LinkedHashMap<>();
        results.put(KEY_FILE, "output file");
        results.put(KEY_CODE, "target log code pattern in regex");
        results.put(KEY_ENCODING, "output file encoding (optional)");
        return results;
    }

    @Override
    public Sink<? super YaessLogRecord> createSink(
            Map<String, String> options) throws IOException, InterruptedException {
        String fileString = options.remove(KEY_FILE);
        if (fileString == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0} must be specified",
                    KEY_FILE));
        }
        String encodingString = options.remove(KEY_ENCODING);
        if (encodingString == null) {
            encodingString = DEFAULT_ENCODING;
        }
        String codeString = options.remove(KEY_CODE);
        if (codeString == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0} must be specified",
                    KEY_CODE));
        }
        File file = new File(fileString);
        Charset encoding;
        try {
            encoding = Charset.forName(encodingString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid output encoding \"{1}\" ({0})",
                    KEY_ENCODING, encodingString), e);
        }
        Filter<YaessLogRecord> filter;
        try {
            filter = new LogCodeRegexFilter(Pattern.compile(codeString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid log code pattern \"{1}\" ({0})",
                    KEY_CODE, codeString));
        }
        if (options.isEmpty() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unknown output options: {0}",
                    options.keySet()));
        }
        File parent = file.getParentFile();
        if (parent.mkdirs() == false && parent.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to prepare output: {1} ({0})",
                    KEY_FILE, file));
        }
        return new SummarizeYaessLogSink(
                new CsvWriter(new OutputStreamWriter(new FileOutputStream(file), encoding)),
                filter);
    }
}
