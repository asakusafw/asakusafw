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
package com.asakusafw.yaess.tools.log.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import com.asakusafw.utils.io.Source;
import com.asakusafw.yaess.tools.log.YaessLogInput;
import com.asakusafw.yaess.tools.log.YaessLogRecord;
import com.asakusafw.yaess.tools.log.util.LineSource;

/**
 * Provides YAESS log record source for basic YAESS log file.
 * @since 0.6.2
 */
public class BasicYaessLogInput implements YaessLogInput {

    private static final String KEY_FILE = "file";

    private static final String KEY_ENCODING = "encoding";

    private static final String DEFAULT_ENCODING = Charset.defaultCharset().name();

    @Override
    public Map<String, String> getOptionsInformation() {
        Map<String, String> results = new LinkedHashMap<>();
        results.put(KEY_FILE, "source log file");
        results.put(KEY_ENCODING, "source log file encoding (optional)");
        return results;
    }

    @Override
    public Source<? extends YaessLogRecord> createSource(
            Map<String, String> options) throws IOException, InterruptedException {
        String fileString = options.remove(KEY_FILE);
        if (fileString == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "\"{0}\" must be specified",
                    KEY_FILE));
        }
        String encodingString = options.remove(KEY_ENCODING);
        if (encodingString == null) {
            encodingString = DEFAULT_ENCODING;
        }
        File file = new File(fileString);
        if (file.isFile() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Missing input file \"{1}\" ({0})",
                    KEY_FILE, fileString));
        }
        Charset encoding;
        try {
            encoding = Charset.forName(encodingString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid input encoding \"{1}\" ({0})",
                    KEY_ENCODING, encodingString), e);
        }
        if (options.isEmpty() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unknown input options: {0}",
                    options.keySet()));
        }
        return new BasicYaessLogSource(new LineSource(new InputStreamReader(new FileInputStream(file), encoding)));
    }
}
